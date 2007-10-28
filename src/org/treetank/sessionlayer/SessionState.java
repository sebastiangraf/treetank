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

import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.pagelayer.AbstractPage;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.UberPage;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastWeakHashMap;

/**
 * <h1>SessionState</h1>
 * 
 * <p>
 * State of each session.
 * </p>
 */
public final class SessionState {

  /** Session configuration. */
  private final SessionConfiguration mSessionConfiguration;

  /** Shared read-only page mPageCache. */
  private final Map<Long, AbstractPage> mPageCache;

  /** Write semaphore to assure only one exclusive write transaction exists. */
  private final Semaphore mWriteSemaphore;

  /** Read semaphore to control running read transactions. */
  private final Semaphore mReadSemaphore;

  /** Strong reference to uber page before the begin of a write transaction. */
  private UberPage mLastCommittedUberPage;

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
    new File(mSessionConfiguration.getAbsolutePath()).createNewFile();

    // Init session members.
    mPageCache = new FastWeakHashMap<Long, AbstractPage>();
    mWriteSemaphore = new Semaphore(IConstants.MAX_WRITE_TRANSACTIONS);
    mReadSemaphore = new Semaphore(IConstants.MAX_READ_TRANSACTIONS);
    final PageReference uberPageReference = new PageReference();
    PageReference secondaryUberPageReference = new PageReference();
    final RandomAccessFile file =
        new RandomAccessFile(mSessionConfiguration.getAbsolutePath(), "rw");

    if (file.length() == 0L) {
      // Bootstrap uber page and make sure there already is a root node.
      mLastCommittedUberPage = new UberPage();
      uberPageReference.setPage(mLastCommittedUberPage);
    } else {
      // Read existing uber page.
      // Read primary beacon.
      file.seek(IConstants.BEACON_START);
      uberPageReference.setStart(file.readLong());
      uberPageReference.setLength(file.readInt());
      uberPageReference.setChecksum(file.readLong());

      // Read secondary beacon.
      file.seek(file.length() - IConstants.BEACON_LENGTH);
      secondaryUberPageReference.setStart(file.readLong());
      secondaryUberPageReference.setLength(file.readInt());
      secondaryUberPageReference.setChecksum(file.readLong());

      // Beacon logic case 1.
      if (uberPageReference.equals(secondaryUberPageReference)) {

        final FastByteArrayReader in =
            new PageReader(mSessionConfiguration).read(uberPageReference);
        mLastCommittedUberPage = new UberPage(in);

        // Beacon logic case 2.
      } else {

        // TODO implement cases 2i, 2ii, and 2iii to be more robust!
        throw new IllegalStateException(
            "Inconsistent TreeTank file encountered. Primary start="
                + uberPageReference.getStart()
                + " size="
                + uberPageReference.getLength()
                + " checksum="
                + uberPageReference.getChecksum()
                + " secondary start="
                + secondaryUberPageReference.getStart()
                + " size="
                + secondaryUberPageReference.getLength()
                + " checksum="
                + secondaryUberPageReference.getChecksum());

      }

    }

    file.close();
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

    return new ReadTransaction(this, new ReadTransactionState(
        mSessionConfiguration,
        mPageCache,
        mLastCommittedUberPage,
        revisionKey));
  }

  public final IWriteTransaction beginWriteTransaction() {

    if (mWriteSemaphore.availablePermits() == 0) {
      throw new IllegalStateException(
          "There already is a running exclusive write transaction.");
    }

    try {
      mWriteSemaphore.acquire();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new WriteTransaction(this, new WriteTransactionState(
        mSessionConfiguration,
        mPageCache,
        new UberPage(mLastCommittedUberPage)));
  }

  public final void commitWriteTransaction(final UberPage lastCommittedUberPage)
      throws Exception {
    mLastCommittedUberPage = lastCommittedUberPage;
  }

  public final void abortWriteTransaction() {
    // Nothing to do here
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

  protected final SessionConfiguration getSessionConfiguration() {
    return mSessionConfiguration;
  }

}
