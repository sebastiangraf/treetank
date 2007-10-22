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
 * $Id$
 */

package org.treetank.pagelayer;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.treetank.api.IConstants;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.FastByteArrayReader;

/**
 * <h1>PageReader</h1>
 * 
 * <p>
 * Each thread working on a I[Read|Write]Transaction has one PageReader
 * instance to independently read from an TreeTank file.
 * </p>
 */
public final class PageReader {

  /** Logger. */
  private static final Logger LOGGER =
      Logger.getLogger(PageReader.class.getName());

  /** Read-only mode for random access mFile. */
  private static final String READ_ONLY = "r";

  /** Size of temporary buffer. */
  private static final int BUFFER_SIZE = 8192;

  /** Random access mFile to work on. */
  private final RandomAccessFile mFile;

  /** Adler32 Checksum to assert integrity. */
  private final Checksum mChecksum;

  /** Do we use encryption? */
  private final boolean mIsEncrypted;

  /** Do we use checksumming? */
  private final boolean mIsChecksummed;

  /** Cipher to encrypt and decrypt blocks. */
  private final Cipher mCipher;

  /** Secret nodeKey to use for cryptographic operations. */
  private final SecretKeySpec mSecretKeySpec;

  /** Inflater to decompress. */
  private final Inflater mDecompressor;

  /** Byte array output stream to hold temporary data. */
  private final ByteArrayOutputStream mOut;

  /** Temporary (de)compression array. */
  private final byte[] mTmp;

  /**
   * Constructor.
   * 
   * @param sessionConfiguration Configuration of session we are bound to.
   * @throws Exception of any kind.
   */
  public PageReader(final SessionConfiguration sessionConfiguration)
      throws Exception {

    mFile = new RandomAccessFile(sessionConfiguration.getPath(), READ_ONLY);

    if (sessionConfiguration.isChecksummed()) {
      mIsChecksummed = true;
      mChecksum = new CRC32();
    } else {
      mIsChecksummed = false;
      mChecksum = null;
    }

    if (sessionConfiguration.isEncrypted()) {
      mIsEncrypted = true;
      mCipher = Cipher.getInstance(IConstants.DEFAULT_ENCRYPTION_ALGORITHM);
      mSecretKeySpec =
          new SecretKeySpec(
              sessionConfiguration.getEncryptionKey(),
              IConstants.DEFAULT_ENCRYPTION_ALGORITHM);
      mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec);
    } else {
      mIsEncrypted = false;
      mCipher = null;
      mSecretKeySpec = null;
    }

    mDecompressor = new Inflater();
    mOut = new ByteArrayOutputStream();
    mTmp = new byte[BUFFER_SIZE];
  }

  /**
   * {@inheritDoc}
   */
  public final FastByteArrayReader read(final PageReference pageReference)
      throws Exception {

    if (!pageReference.isCommitted()) {
      throw new Exception("Empty page reference.");
    }

    // Prepare members.
    byte[] page;

    // Read encrypted page from mFile.
    mFile.seek(pageReference.getStart());
    page = new byte[pageReference.getLength()];
    mFile.read(page);

    // Decrypt page.
    if (mIsEncrypted) {
      page = mCipher.doFinal(page);
    }

    // Verify checksummed page.
    if (mIsChecksummed) {
      mChecksum.reset();
      mChecksum.update(page, 0, page.length);
      if (mChecksum.getValue() != pageReference.getChecksum()) {
        throw new Exception("Page checksum is not valid for start="
            + pageReference.getStart()
            + "; size="
            + pageReference.getLength()
            + "; checksum="
            + pageReference.getChecksum());
      }
    }

    // Decompress page.
    mDecompressor.reset();
    mOut.reset();
    mDecompressor.setInput(page);
    int count;
    while (!mDecompressor.finished()) {
      count = mDecompressor.inflate(mTmp);
      mOut.write(mTmp, 0, count);
    }
    page = mOut.toByteArray();

    // Return reader required to instantiate and deserialize page.
    return new FastByteArrayReader(page);

  }

  /**
   * Properly close file handle.
   */
  public final void close() {
    try {
      mFile.close();
    } catch (Exception e) {
      LOGGER.warning("Could not close file: " + e.getLocalizedMessage());
    }
  }

  /**
   * Close file handle in case it is not properly closed by the application.
   * 
   * @throws Throwable if the finalization of the superclass does not work.
   */
  @Override
  protected void finalize() throws Throwable {
    try {
      mFile.close();
    } finally {
      super.finalize();
    }
  }

}
