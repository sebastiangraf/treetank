/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: SessionState.java 4470 2008-09-06 15:24:52Z kramis $
 */

package com.treetank.session;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import com.treetank.api.IItemList;
import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.page.PageReader;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;
import com.treetank.utils.IConstants;

/**
 * <h1>SessionState</h1>
 * 
 * <p>
 * State of each session.
 * </p>
 */
public final class SessionState {

	/** Session configuration. */
	private SessionConfiguration mSessionConfiguration;

	/** Write semaphore to assure only one exclusive write transaction exists. */
	private Semaphore mWriteSemaphore;

	/** Read semaphore to control running read transactions. */
	private Semaphore mReadSemaphore;

	/** Strong reference to uber page before the begin of a write transaction. */
	private UberPage mLastCommittedUberPage;

	/** Remember all running transactions (both read and write). */
	private Map<Long, IReadTransaction> mTransactionMap;

	/** Random generator for transaction IDs. */
	private Random mRandom;

	/**
	 * Constructor to bind to a TreeTank file.
	 * 
	 * <p>
	 * The beacon logic works as follows:
	 * 
	 * <ol>
	 * <li><code>Primary beacon == secondary beacon</code>: OK.</li>
	 * <li><code>Primary beacon != secondary beacon</code>: try to recover...
	 * <ol type="i">
	 * <li><code>Checksum(uberpage) == primary beacon</code>: truncate file and
	 * write secondary beacon - OK.</li>
	 * <li><code>Checksum(uberpage) == secondary beacon</code>: write primary
	 * beacon - OK.</li>
	 * <li><code>Checksum(uberpage) != secondary beacon 
	 *        != primary beacon</code>: NOK.</li>
	 * </ol>
	 * </li>
	 * </ol>
	 * </p>
	 * 
	 * @param sessionConfiguration
	 *            Session configuration for the TreeTank.
	 */
	protected SessionState(final SessionConfiguration sessionConfiguration) {

		mSessionConfiguration = sessionConfiguration;
		RandomAccessFile file = null;
		mTransactionMap = new ConcurrentHashMap();
		mRandom = new Random();

		try {

			// Make sure that the TreeTank file exists.
			new File(mSessionConfiguration.getFileName()).createNewFile();

			// Init session members.
			mWriteSemaphore = new Semaphore(IConstants.MAX_WRITE_TRANSACTIONS);
			mReadSemaphore = new Semaphore(IConstants.MAX_READ_TRANSACTIONS);
			final PageReference<UberPage> uberPageReference = new PageReference<UberPage>();
			final PageReference<UberPage> secondaryUberPageReference = new PageReference<UberPage>();

			file = new RandomAccessFile(
					mSessionConfiguration.getFileName(),
					IConstants.READ_WRITE);

			if (file.length() == 0L) {
				// Bootstrap uber page and make sure there already is a root
				// node.
				mLastCommittedUberPage = new UberPage();
				uberPageReference.setPage(mLastCommittedUberPage);
			} else {

				byte[] tmp = new byte[IConstants.CHECKSUM_SIZE];

				// Read primary beacon.
				file.seek(IConstants.BEACON_START);
				uberPageReference.setStart(file.readLong());
				uberPageReference.setLength(file.readInt());
				file.read(tmp);
				uberPageReference.setChecksum(tmp);

				// Read secondary beacon.
				// file.seek(file.length() - IConstants.BEACON_LENGTH);
				// secondaryUberPageReference.setStart(file.readLong());
				// secondaryUberPageReference.setLength(file.readInt());
				// file.read(tmp);
				// secondaryUberPageReference.setChecksum(tmp);

				// Beacon logic case 1.
				// if (uberPageReference.equals(secondaryUberPageReference)) {

				final ByteBuffer in = new PageReader(mSessionConfiguration)
						.read(uberPageReference);
				mLastCommittedUberPage = new UberPage(in);

				// Beacon logic case 2.
				// } else {
				// // TODO implement cases 2i, 2ii, and 2iii to be more robust!
				// throw new IllegalStateException(
				// "Inconsistent TreeTank file encountered. Primary start="
				// + uberPageReference.getStart()
				// + " size="
				// + uberPageReference.getLength()
				// + " checksum="
				// + "TODO"
				// + " secondary start="
				// + secondaryUberPageReference.getStart()
				// + " size="
				// + secondaryUberPageReference.getLength()
				// + " checksum="
				// + "TODO");
				//
				// }

			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	protected final int getReadTransactionCount() {
		return (IConstants.MAX_READ_TRANSACTIONS - (int) mReadSemaphore
				.availablePermits());
	}

	protected final int getWriteTransactionCount() {
		return (IConstants.MAX_WRITE_TRANSACTIONS - (int) mWriteSemaphore
				.availablePermits());
	}

	protected final IReadTransaction beginReadTransaction() {
		return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(),
				null);
	}

	protected final IReadTransaction beginReadTransaction(
			final IItemList itemList) {
		return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(),
				itemList);
	}

	protected final IReadTransaction beginReadTransaction(
			final long revisionNumber, final IItemList itemList) {

		// Make sure not to exceed available number of read transactions.
		try {
			mReadSemaphore.acquire();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		IReadTransaction rtx = null;
		try {
			// Create new read transaction.
			rtx = new ReadTransaction(generateTransactionID(), this,
					new ReadTransactionState(mSessionConfiguration,
							mLastCommittedUberPage, revisionNumber, itemList));

			// Remember transaction for debugging and safe close.
			if (mTransactionMap.put(rtx.getTransactionID(), rtx) != null) {
				throw new IllegalStateException(
						"ID generation is bogus because of duplicate ID.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Revision " + revisionNumber
					+ " can not be found.");
		}

		return rtx;
	}

	protected final IWriteTransaction beginWriteTransaction(
			final int maxNodeCount, final int maxTime) {

		// Make sure not to exceed available number of write transactions.
		if (mWriteSemaphore.availablePermits() == 0) {
			throw new IllegalStateException(
					"There already is a running exclusive write transaction.");
		}
		try {
			mWriteSemaphore.acquire();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// Create new write transaction.
		final IWriteTransaction wtx = new WriteTransaction(
				generateTransactionID(), this, createWriteTransactionState(),
				maxNodeCount, maxTime);

		// Remember transaction for debugging and safe close.
		if (mTransactionMap.put(wtx.getTransactionID(), wtx) != null) {
			throw new IllegalStateException(
					"ID generation is bogus because of duplicate ID.");
		}

		return wtx;
	}

	protected final WriteTransactionState createWriteTransactionState() {
		return new WriteTransactionState(mSessionConfiguration, new UberPage(
				mLastCommittedUberPage));
	}

	protected final UberPage getLastCommittedUberPage() {
		return mLastCommittedUberPage;
	}

	protected final void setLastCommittedUberPage(
			final UberPage lastCommittedUberPage) {
		mLastCommittedUberPage = lastCommittedUberPage;
	}

	protected final void closeWriteTransaction(final long transactionID) {
		// Purge transaction from internal state.
		mTransactionMap.remove(transactionID);
		// Make new transactions available.
		mWriteSemaphore.release();
	}

	protected final void closeReadTransaction(final long transactionID) {
		// Purge transaction from internal state.
		mTransactionMap.remove(transactionID);
		// Make new transactions available.
		mReadSemaphore.release();
	}

	protected final void close() {
		// Forcibly close all open transactions.
		for (final IReadTransaction rtx : mTransactionMap.values()) {
			rtx.close();
		}

		// Immediately release all ressources.
		mSessionConfiguration = null;
		mWriteSemaphore = null;
		mReadSemaphore = null;
		mLastCommittedUberPage = null;
		mTransactionMap = null;
	}

	protected final SessionConfiguration getSessionConfiguration() {
		return mSessionConfiguration;
	}

	/**
	 * Required to close file handle.
	 * 
	 * @throws Throwable
	 *             if the finalization of the superclass does not work.
	 */
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	/**
	 * Generate new unique ID for the transaction.
	 * 
	 * @return Generated unique ID.
	 */
	private final long generateTransactionID() {
		long id = mRandom.nextLong();
		synchronized (mTransactionMap) {
			while (mTransactionMap.containsKey(id)) {
				id = mRandom.nextLong();
			}
		}
		return id;
	}

}