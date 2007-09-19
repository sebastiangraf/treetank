/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this mFile except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.nodelayer;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.treetank.pagelayer.PageCache;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.PageWriter;
import org.treetank.pagelayer.UberPage;
import org.treetank.utils.IConstants;


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
  private static final Logger LOGGER = Logger.getLogger(Session.class);

  /** Pool of current sessions. */
  private static final Map<String, ISession> SESSION_MAP =
      new HashMap<String, ISession>();

  /** Shared read-only page mPageCache. */
  private final PageCache mPageCache;

  /** Write semaphore to assure only one IWriteTransaction exists. */
  private final Semaphore mWriteSemaphore;

  /** Reference to uber page as root of whole storage (primary beacon). */
  private final PageReference mPrimaryUberPageReference;

  /** Reference to uber page as root of whole storage (secondary beacon). */
  private final PageReference mSecondaryUberPageReference;

  /** Strong reference to uber page. */
  private UberPage mUberPage;

  /** AbstractPage writer for commits. */
  private final PageWriter mPageWriter;

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
   * @param path Path of TreeTank file.
   * @throws Exception of any kind.
   */
  public Session(final String path) throws Exception {

    // Make sure that the TreeTank file exists.
    new File(path).createNewFile();

    // Init session members.
    mPageCache = new PageCache(path);
    mWriteSemaphore =
        new Semaphore(IConstants.MAX_NUMBER_OF_WRITE_TRANSACTIONS);
    mPrimaryUberPageReference = new PageReference();
    mSecondaryUberPageReference = new PageReference();
    mPageWriter = new PageWriter(path);
    mFile = new RandomAccessFile(path, "rw");

    // Bootstrap uber page.
    if (mFile.length() == 0L) {
      // No revisions available, create empty uber page.
      mFile.setLength(IConstants.BEACON_LENGTH);
      mUberPage = UberPage.create(mPageCache);
      mPrimaryUberPageReference.setPage(mUberPage);
      mUberPage.prepareRevisionRootPage();
      commit();
    } else {
      // There already are revisions, read existing uber page.
      readBeacon(mFile);

      // Beacon logic case 1.
      if (mPrimaryUberPageReference.equals(mSecondaryUberPageReference)) {
        mUberPage =
            (UberPage) mPageCache.get(
                mPrimaryUberPageReference,
                IConstants.UBER_PAGE);

        // Beacon logic case 2.
      } else {

        // TODO implement cases 2i, 2ii, and 2iii to be more robust!
        LOGGER.error("Inconsistent TreeTank file encountered. Primary start="
            + mPrimaryUberPageReference.getStart()
            + " size="
            + mPrimaryUberPageReference.getSize()
            + " checksum="
            + mPrimaryUberPageReference.getChecksum()
            + " secondary start="
            + mSecondaryUberPageReference.getStart()
            + " size="
            + mSecondaryUberPageReference.getSize()
            + " checksum="
            + mSecondaryUberPageReference.getChecksum());
        throw new IllegalStateException(
            "Primary and secondary beacon not equal.");

      }

    }

    SESSION_MAP.put(path, this);
  }

  /**
   * {@inheritDoc}
   */
  public static final ISession getSession(final String path) throws Exception {
    ISession session = SESSION_MAP.get(path);

    if (session == null) {
      session = new Session(path);
      SESSION_MAP.put(path, session);
    }

    return session;
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction beginReadTransaction() throws Exception {
    return beginReadTransaction(mUberPage.getMaxRevisionKey());
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction beginReadTransaction(final long revisionKey)
      throws Exception {
    return new ReadTransaction(mUberPage.getRevisionRootPage(revisionKey));
  }

  /**
   * {@inheritDoc}
   */
  public final IWriteTransaction beginWriteTransaction() throws Exception {
    if (mWriteSemaphore.availablePermits() == 0) {
      LOGGER.error("IWriteTransaction limit reached.");
      throw new IllegalStateException(
          "Session can not start new IWriteTransaction due to "
              + (IConstants.MAX_NUMBER_OF_WRITE_TRANSACTIONS - mWriteSemaphore
                  .availablePermits())
              + " running IWriteTransaction(s).");
    }
    mWriteSemaphore.acquire();
    mUberPage = UberPage.clone(mUberPage);
    mPrimaryUberPageReference.setPage(mUberPage);
    return new WriteTransaction(mUberPage.prepareRevisionRootPage());
  }

  /**
   * {@inheritDoc}
   */
  public final void commit() throws Exception {
    mPageWriter.write(mPrimaryUberPageReference);
    mPageCache.put(mPrimaryUberPageReference);
    writeBeacon(mFile);
    mWriteSemaphore.release();
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Committed revision="
          + mUberPage.getMaxRevisionKey()
          + " start="
          + mPrimaryUberPageReference.getStart()
          + " size="
          + mPrimaryUberPageReference.getSize()
          + " checksum="
          + mPrimaryUberPageReference.getChecksum());
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void abort() throws Exception {
    mWriteSemaphore.release();
  }

  /**
   * {@inheritDoc}
   */
  public final void close() throws Exception {
    if (mWriteSemaphore.availablePermits() == 0) {
      LOGGER.warn("Attempt to close session without cleanly "
          + "committing or aborting it.");
      throw new IllegalStateException("Session can not be closed due to "
          + (IConstants.MAX_NUMBER_OF_WRITE_TRANSACTIONS - mWriteSemaphore
              .availablePermits())
          + " running IWriteTransaction(s).");
    }
    mFile.close();
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
    file.writeInt(mPrimaryUberPageReference.getSize());
    file.writeLong(mPrimaryUberPageReference.getChecksum());

    // Write primary beacon.
    file.seek(0L);
    file.writeLong(mPrimaryUberPageReference.getStart());
    file.writeInt(mPrimaryUberPageReference.getSize());
    file.writeLong(mPrimaryUberPageReference.getChecksum());

  }

  /**
   * Read the uber page with the help of the beacons.
   * 
   * @param mFile File to read from.
   * @throws Exception of any kind.
   */
  private final void readBeacon(final RandomAccessFile file) throws Exception {

    // Read primaryy beacon.
    file.seek(0L);
    mPrimaryUberPageReference.setStart(file.readLong());
    mPrimaryUberPageReference.setSize(file.readInt());
    mPrimaryUberPageReference.setChecksum(file.readLong());

    // Read secondary beacon.
    file.seek(file.length() - IConstants.BEACON_LENGTH);
    mSecondaryUberPageReference.setStart(file.readLong());
    mSecondaryUberPageReference.setSize(file.readInt());
    mSecondaryUberPageReference.setChecksum(file.readLong());
  }

}
