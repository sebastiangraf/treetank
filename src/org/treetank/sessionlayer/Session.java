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
 * <h1>Session</h1>
 * 
 * <p>
 * Singelton per TreeTank file. Manages transaction handling and links
 * to page layer. Multiple threads can concurrently create
 * transactions on this object but only one IWriteTransaction can exist at
 * any time.
 * </p>
 */
public final class Session implements ISession {

  /** Logger. */
  private static final Logger LOGGER =
      Logger.getLogger(Session.class.getName());

  /** Session configuration. */
  private final SessionConfiguration mSessionConfiguration;

  /** Shared read-only page mPageCache. */
  private final Map<Long, IPage> mPageCache;

  /** Write semaphore to assure only one IWriteTransaction exists. */
  private final Semaphore mWriteSemaphore;

  /** Reference to uber page as root of whole storage (primary beacon). */
  private final PageReference mPrimaryUberPageReference;

  /** Reference to uber page as root of whole storage (secondary beacon). */
  private final PageReference mSecondaryUberPageReference;

  /** Strong reference to uber page. */
  private UberPage mUberPage;

  /** AbstractPage writer for commits. */
  private IWriteTransactionState mWriteTransactionState;

  /** Random access mFile for beacons. */
  private final RandomAccessFile mFile;

  /**
   * Convenient constructor.
   * 
   * @param path Path to TreeTank file.
   * @throws Exception of any kind.
   */
  public Session(final String path) throws Exception {
    this(new SessionConfiguration(path));
  }

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
   * @throws Exception of any kind.
   */
  public Session(final SessionConfiguration sessionConfiguration)
      throws Exception {

    mSessionConfiguration = sessionConfiguration;

    // Make sure that the TreeTank file exists.
    new File(mSessionConfiguration.getPath()).createNewFile();

    // Init session members.
    mPageCache = new FastWeakHashMap<Long, IPage>();
    mWriteSemaphore =
        new Semaphore(IConstants.MAX_NUMBER_OF_WRITE_TRANSACTIONS);
    mPrimaryUberPageReference = new PageReference();
    mSecondaryUberPageReference = new PageReference();
    mFile = new RandomAccessFile(mSessionConfiguration.getPath(), "rw");

    // Bootstrap uber page.
    if (mFile.length() == 0L) {
      // No revisions available, create empty uber page.
      mFile.setLength(IConstants.BEACON_LENGTH);
      mUberPage = UberPage.create();
      mPrimaryUberPageReference.setPage(mUberPage);
    } else {
      // There already are revisions, read existing uber page.
      readBeacon(mFile);

      // Beacon logic case 1.
      if (mPrimaryUberPageReference.equals(mSecondaryUberPageReference)) {

        final FastByteArrayReader in =
            new PageReader(mSessionConfiguration)
                .read(mPrimaryUberPageReference);
        mUberPage = UberPage.read(in);

        // Beacon logic case 2.
      } else {

        // TODO implement cases 2i, 2ii, and 2iii to be more robust!
        LOGGER.severe("Inconsistent TreeTank file encountered. Primary start="
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
        throw new IllegalStateException(
            "Primary and secondary beacon not equal.");

      }

    }
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction beginReadTransaction() throws Exception {
    return beginReadTransaction(mUberPage.getRevisionCount());
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction beginReadTransaction(final long revisionKey)
      throws Exception {

    final PageReader pageReader = new PageReader(mSessionConfiguration);
    final IReadTransactionState state =
        new ReadTransactionState(mPageCache, pageReader, mUberPage, revisionKey);

    return new ReadTransaction(state);
  }

  /**
   * {@inheritDoc}
   */
  public final IWriteTransaction beginWriteTransaction() throws Exception {
    if (mWriteSemaphore.availablePermits() == 0) {
      LOGGER.severe("IWriteTransaction limit reached.");
      throw new IllegalStateException(
          "Session can not start new IWriteTransaction due to "
              + (IConstants.MAX_NUMBER_OF_WRITE_TRANSACTIONS - mWriteSemaphore
                  .availablePermits())
              + " running IWriteTransaction(s).");
    }
    mWriteSemaphore.acquire();
    mUberPage = UberPage.clone(mUberPage);
    mPrimaryUberPageReference.setPage(mUberPage);

    final PageWriter pageWriter = new PageWriter(mSessionConfiguration);
    final PageReader pageReader = new PageReader(mSessionConfiguration);

    mWriteTransactionState =
        new WriteTransactionState(mPageCache, pageReader, pageWriter, mUberPage);
    return new WriteTransaction(mWriteTransactionState);
  }

  /**
   * {@inheritDoc}
   */
  public final void commit() throws Exception {

    // Recursively write indirectely referenced pages.
    mPrimaryUberPageReference.getPage().commit(mWriteTransactionState);

    mWriteTransactionState.getPageWriter().write(mPrimaryUberPageReference);
    mPageCache.put(
        mPrimaryUberPageReference.getStart(),
        mPrimaryUberPageReference.getPage());
    mPrimaryUberPageReference.setPage(null);
    writeBeacon(mFile);
    mWriteTransactionState = null;
    mWriteSemaphore.release();
  }

  /**
   * {@inheritDoc}
   */
  public final void abort() throws Exception {
    mUberPage.abort();
    mWriteSemaphore.release();
  }

  /**
   * {@inheritDoc}
   */
  public final void close() throws Exception {
    if (mWriteSemaphore.availablePermits() == 0) {
      LOGGER.warning("Attempt to close session without cleanly "
          + "committing or aborting it.");
      throw new IllegalStateException("Session can not be closed due to "
          + (IConstants.MAX_NUMBER_OF_WRITE_TRANSACTIONS - mWriteSemaphore
              .availablePermits())
          + " running IWriteTransaction(s).");
    }
    mFile.close();
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
  private final void writeBeacon(final RandomAccessFile file) throws Exception {

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
  private final void readBeacon(final RandomAccessFile file) throws Exception {

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
