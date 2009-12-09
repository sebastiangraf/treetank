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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import com.treetank.api.IItemList;
import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.constants.EFixed;
import com.treetank.constants.ESettable;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.io.AbstractIOFactory;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.io.StorageProperties;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;

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

    /** Major version, reading from file */
    private final long mVersionMajor;

    /** Minor version, reading from file */
    private final long mVersionMinor;

    /** abstract factory for all interaction to the storage */
    private final AbstractIOFactory fac;

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
    protected SessionState(final SessionConfiguration sessionConfiguration)
            throws TreetankException {

        mSessionConfiguration = sessionConfiguration;
        mTransactionMap = new ConcurrentHashMap<Long, IReadTransaction>();
        mRandom = new Random();

        // Init session members.
        mWriteSemaphore = new Semaphore(
                (Integer) ESettable.MAX_WRITE_TRANSACTIONS
                        .getStandardProperty());
        mReadSemaphore = new Semaphore(
                (Integer) ESettable.MAX_READ_TRANSACTIONS
                        .getStandardProperty());
        final PageReference uberPageReference = new PageReference();

        fac = AbstractIOFactory.getInstance(mSessionConfiguration);
        StorageProperties props;
        if (!fac.exists()) {
            // Bootstrap uber page and make sure there already is a root
            // node.
            mLastCommittedUberPage = new UberPage();
            uberPageReference.setPage(mLastCommittedUberPage);

            props = new StorageProperties(
                    (Integer) EFixed.VERSION_MAJOR
                            .getStandardProperty(),
                    (Integer) EFixed.VERSION_MINOR
                            .getStandardProperty());
        } else {
            final IReader reader = fac.getReader();
            final PageReference firstRef = reader.readFirstReference();
            mLastCommittedUberPage = (UberPage) firstRef.getPage();
            props = reader.getProps();
            reader.close();
        }

        mVersionMajor = props.getVersionMajor();
        mVersionMinor = props.getVersionMinor();

        checkValidStorage(props);

    }

    private void checkValidStorage(final StorageProperties props)
            throws TreetankUsageException {

        // Fail if an old TreeTank file is encountered.
        if (mVersionMajor < (Integer) EFixed.VERSION_MAJOR
                .getStandardProperty()
                || mVersionMinor < (Integer) EFixed.VERSION_MINOR
                        .getStandardProperty()) {
            throw new TreetankUsageException(new StringBuilder("'").append(
                    mSessionConfiguration.getFile().getAbsolutePath()).append(
                    "' was created with TreeTank release ").append(
                    mVersionMajor).append(".").append(mVersionMinor).append(
                    " and is incompatible with release ").append(
                    EFixed.VERSION_MAJOR.getStandardProperty())
                    .append(".").append(
                            (Integer) EFixed.VERSION_MINOR
                                    .getStandardProperty()).append(".")
                    .toString());
        }

    }

    protected int getReadTransactionCount() {
        return ((Integer) ESettable.MAX_READ_TRANSACTIONS
                .getStandardProperty() - (int) mReadSemaphore
                .availablePermits());
    }

    protected int getWriteTransactionCount() {
        return ((Integer) ESettable.MAX_WRITE_TRANSACTIONS
                .getStandardProperty() - (int) mWriteSemaphore
                .availablePermits());
    }

    protected IReadTransaction beginReadTransaction() throws TreetankException {
        return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(),
                null);
    }

    protected IReadTransaction beginReadTransaction(final IItemList itemList)
            throws TreetankException {
        return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(),
                itemList);
    }

    protected IReadTransaction beginReadTransaction(final long revisionNumber,
            final IItemList itemList) throws TreetankException {

        // Make sure not to exceed available number of read transactions.
        try {
            mReadSemaphore.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        IReadTransaction rtx = null;
        // Create new read transaction.
        rtx = new ReadTransaction(generateTransactionID(), this,
                new ReadTransactionState(mSessionConfiguration,
                        mLastCommittedUberPage, revisionNumber, itemList, fac
                                .getReader()));

        // Remember transaction for debugging and safe close.
        if (mTransactionMap.put(rtx.getTransactionID(), rtx) != null) {
            throw new TreetankUsageException(
                    "ID generation is bogus because of duplicate ID.");
        }
        return rtx;
    }

    protected IWriteTransaction beginWriteTransaction(final int maxNodeCount,
            final int maxTime) throws TreetankIOException {

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

    protected WriteTransactionState createWriteTransactionState()
            throws TreetankIOException {
        IWriter writer;
        try {
            writer = fac.getWriter();
        } catch (final TreetankIOException exc) {
            throw new RuntimeException(exc);
        }

        return new WriteTransactionState(mSessionConfiguration, new UberPage(
                mLastCommittedUberPage), writer);
    }

    protected UberPage getLastCommittedUberPage() {
        return mLastCommittedUberPage;
    }

    protected void setLastCommittedUberPage(final UberPage lastCommittedUberPage) {
        mLastCommittedUberPage = lastCommittedUberPage;
    }

    protected void closeWriteTransaction(final long transactionID) {
        // Purge transaction from internal state.
        mTransactionMap.remove(transactionID);
        // Make new transactions available.
        mWriteSemaphore.release();
    }

    protected void closeReadTransaction(final long transactionID) {
        // Purge transaction from internal state.
        mTransactionMap.remove(transactionID);
        // Make new transactions available.
        mReadSemaphore.release();
    }

    protected void close() throws TreetankException {
        // Forcibly close all open transactions.
        for (final IReadTransaction rtx : mTransactionMap.values()) {
            if (rtx instanceof IWriteTransaction) {
                ((IWriteTransaction) rtx).abort();
            }
            rtx.close();
        }

        // Immediately release all ressources.
        mSessionConfiguration = null;
        mWriteSemaphore = null;
        mReadSemaphore = null;
        mLastCommittedUberPage = null;
        mTransactionMap = null;

        fac.closeStorage();
    }

    protected SessionConfiguration getSessionConfiguration() {
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
    private long generateTransactionID() {
        long id = mRandom.nextLong();
        synchronized (mTransactionMap) {
            while (mTransactionMap.containsKey(id)) {
                id = mRandom.nextLong();
            }
        }
        return id;
    }

    /**
     * @return the versionMajor
     */
    protected long getVersionMajor() {
        return mVersionMajor;
    }

    /**
     * @return the versionMinor
     */
    protected long getVersionMinor() {
        return mVersionMinor;
    }

}
