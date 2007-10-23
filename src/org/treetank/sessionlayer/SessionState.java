/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id:Session.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.treetank.api.IConstants;
import org.treetank.api.IPage;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IReadTransactionState;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.api.IWriteTransactionState;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.PageWriter;
import org.treetank.pagelayer.UberPage;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastWeakHashMap;

/**
 * <h1>SessionState</h1>
 * 
 * <p>
 * 
 * </p>
 */
public final class SessionState implements ISession {

  /** Logger. */
  private static final Logger LOGGER =
      Logger.getLogger(SessionState.class.getName());

  /** Session configuration. */
  private final SessionConfiguration mSessionConfiguration;

  /** Shared read-only page mPageCache. */
  private final Map<Long, IPage> mPageCache;

  /** Write semaphore to assure only one exclusive write transaction exists. */
  private final Semaphore mWriteSemaphore;

  /** Read semaphore to control running read transactions. */
  private final Semaphore mReadSemaphore;

  /** Reference to uber page as root of whole storage (primary beacon). */
  private final PageReference mPrimaryUberPageReference;

  /** Reference to uber page as root of whole storage (secondary beacon). */
  private final PageReference mSecondaryUberPageReference;

  /** Strong reference to uber page. */
  private UberPage mUberPage;

  /** Strong reference to uber page before the begin of a write transaction. */
  private UberPage mLastCommittedUberPage;

  /** AbstractPage writer for commits. */
  private IWriteTransactionState mWriteTransactionState;

  /** Random access mFile for beacons. */
  private final RandomAccessFile mFile;

  /**
   * Constructor to bind to a TreeTank file.
   * 
   * <p>
   * The beacon logic works as follows:
   * 
   * <ol>
   * <li><code>Primary beacon == secondary beacon</code>: OK.</li>
   * <li><code>Primary beacon != secondary beacon</code>: try to recover...
   *    <ol type="i">
   *    <li><code>Checksum(uberpage) == primary beacon</code>:
   *        truncate file and write secondary beacon - OK.</li>
   *    <li><code>Checksum(uberpage) == secondary beacon</code>:
   *        write primary beacon - OK.</li>
   *    <li><code>Checksum(uberpage) != secondary beacon 
   *        != primary beacon</code>: NOK.</li>
   *    </ol>
   * </li>
   * </ol>
   * </p>
   * 
   * @param sessionConfiguration Session configuration for the TreeTank.
   * @throws IOException if there is a problem with opening the file.
   */
  public SessionState(final SessionConfiguration sessionConfiguration)
      throws IOException {

    mSessionConfiguration = sessionConfiguration;

    // Make sure that the TreeTank file exists.
    new File(mSessionConfiguration.getPath()).createNewFile();

    // Init session members.
    mPageCache = new FastWeakHashMap<Long, IPage>();
    mWriteSemaphore = new Semaphore(IConstants.MAX_WRITE_TRANSACTIONS);
    mReadSemaphore = new Semaphore(IConstants.MAX_READ_TRANSACTIONS);
    mPrimaryUberPageReference = new PageReference();
    mSecondaryUberPageReference = new PageReference();
    mFile = new RandomAccessFile(mSessionConfiguration.getPath(), "rw");

    if (mFile.length() == 0L) {
      // Bootstrap uber page and make sure there already is a root node.
      mUberPage = UberPage.create();
      mPrimaryUberPageReference.setPage(mUberPage);
      mLastCommittedUberPage = mUberPage;
    } else {
      // Read existing uber page.
      readBeacon(mFile);

      // Beacon logic case 1.
      if (mPrimaryUberPageReference.equals(mSecondaryUberPageReference)) {

        final FastByteArrayReader in =
            new PageReader(mSessionConfiguration)
                .read(mPrimaryUberPageReference);
        mUberPage = UberPage.read(in);
        mLastCommittedUberPage = mUberPage;

        // Beacon logic case 2.
      } else {

        // TODO implement cases 2i, 2ii, and 2iii to be more robust!
        throw new IllegalStateException(
            "Inconsistent TreeTank file encountered. Primary start="
                + mPrimaryUberPageReference.getStart()
                + " size="
                + mPrimaryUberPageReference.getLength()
                + " checksum="
                + mPrimaryUberPageReference.getChecksum()
                + " secondary start="
                + mSecondaryUberPageReference.getStart()
                + " size="
                + mSecondaryUberPageReference.getLength()
                + " checksum="
                + mSecondaryUberPageReference.getChecksum());

      }

    }
  }

  public final IReadTransaction beginReadTransaction() {
    return beginReadTransaction(mLastCommittedUberPage.getRevisionKey());
  }

  public final IReadTransaction beginReadTransaction(final long revisionKey) {

    try {
      mReadSemaphore.acquire();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    final PageReader pageReader = new PageReader(mSessionConfiguration);
    final IReadTransactionState state =
        new ReadTransactionState(
            mPageCache,
            pageReader,
            mLastCommittedUberPage,
            revisionKey);

    return new ReadTransaction(this, state);
  }

  public final IWriteTransaction beginWriteTransaction() {

    // Make sure that only one write transaction exists per session.
    if (mWriteSemaphore.availablePermits() == 0) {
      throw new IllegalStateException(
          "There already is a running exclusive write transaction.");
    }

    try {
      mWriteSemaphore.acquire();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // Make uber page only ready for new commit if it is not the first WTX.
    mUberPage = UberPage.clone(mUberPage);
    mPrimaryUberPageReference.setPage(mUberPage);

    // Make write transaction state ready.
    final PageWriter pageWriter = new PageWriter(mSessionConfiguration);
    final PageReader pageReader = new PageReader(mSessionConfiguration);
    mWriteTransactionState =
        new WriteTransactionState(mPageCache, pageReader, pageWriter, mUberPage);

    // Return fresh write transaction.
    return new WriteTransaction(this, mWriteTransactionState);
  }

  public final void commitWriteTransaction() throws Exception {

    if (mUberPage.isBootstrap()) {
      mFile.setLength(IConstants.BEACON_LENGTH);
    }

    // Recursively write indirectely referenced pages.
    mUberPage.commit(mWriteTransactionState);

    mWriteTransactionState.getPageWriter().write(mPrimaryUberPageReference);
    mPageCache.put(
        mPrimaryUberPageReference.getStart(),
        mPrimaryUberPageReference.getPage());
    mPrimaryUberPageReference.setPage(null);
    writeBeacon(mFile);
    mWriteTransactionState = null;
    closeWriteTransaction();
    mLastCommittedUberPage = mUberPage;
  }

  public final void abortWriteTransaction() {
    closeWriteTransaction();
  }

  public final void closeWriteTransaction() {
    mWriteSemaphore.release();
  }

  public final void closeReadTransaction() {
    mReadSemaphore.release();
  }

  public final void close() {
    if (mWriteSemaphore.drainPermits() != IConstants.MAX_WRITE_TRANSACTIONS) {
      throw new IllegalStateException("Session can not be closed due to a"
          + "running exclusive write transaction.");
    }
    if (mReadSemaphore.drainPermits() != IConstants.MAX_READ_TRANSACTIONS) {
      throw new IllegalStateException("Session can not be closed due to one "
          + "or more running share read transactions.");
    }
    try {
      mFile.close();
    } catch (Exception e) {
      LOGGER.warning("Could not close file: " + e.getLocalizedMessage());
    }
  }

  /**
   * Required to close file handle.
   * 
   * @throws Throwable if the finalization of the superclass does not work.
   */
  protected void finalize() throws Throwable {
    try {
      close();
    } finally {
      super.finalize();
    }
  }

  public SessionConfiguration getSessionConfiguration() {
    return mSessionConfiguration;
  }

  /**
   * Write a primary and secondary beacon to safely, quickly and conveniently
   * access the uber page later on.
   * 
   * The committing process first writes the uber page, then the secondary
   * beacon to the end of the file and eventually the primary beacon to the
   * start of the file. If the writing process crashes during writing the
   * pages or beacons, the storage can still be reset to the last successfully
   * committed revision.
   * 
   * The secondary beacon is written before the primary beacon because it can
   * be written sequentially just after the uber page.
   * 
   * @param mFile File to write to.
   * @throws Exception of any kind.
   */
  private final void writeBeacon(final RandomAccessFile file)
      throws IOException {

    // Write secondary beacon.
    file.seek(file.length());
    file.writeLong(mPrimaryUberPageReference.getStart());
    file.writeInt(mPrimaryUberPageReference.getLength());
    file.writeLong(mPrimaryUberPageReference.getChecksum());

    // Write primary beacon.
    file.seek(0L);
    file.writeLong(mPrimaryUberPageReference.getStart());
    file.writeInt(mPrimaryUberPageReference.getLength());
    file.writeLong(mPrimaryUberPageReference.getChecksum());

  }

  /**
   * Read the uber page with the help of the beacons.
   * 
   * @param mFile File to read from.
   * @throws Exception of any kind.
   */
  private final void readBeacon(final RandomAccessFile file) throws IOException {

    // Read primary beacon.
    file.seek(0L);
    mPrimaryUberPageReference.setStart(file.readLong());
    mPrimaryUberPageReference.setLength(file.readInt());
    mPrimaryUberPageReference.setChecksum(file.readLong());

    // Read secondary beacon.
    file.seek(file.length() - IConstants.BEACON_LENGTH);
    mSecondaryUberPageReference.setStart(file.readLong());
    mSecondaryUberPageReference.setLength(file.readInt());
    mSecondaryUberPageReference.setChecksum(file.readLong());
  }

}
