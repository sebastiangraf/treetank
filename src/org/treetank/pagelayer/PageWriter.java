/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.Deflater;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.treetank.api.IConstants;
import org.treetank.api.IWriteTransactionState;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>PageWriter</h1>
 * 
 * <p>
 * Each commit of the ISession creates one PageWriter
 * instance to write to the TreeTank file.
 * </p>
 */
public final class PageWriter {

  /** Read-write mode of mFile to modify. */
  private static final String READ_WRITE = "rw";

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

  /** Compressor to compress the page. */
  private final Deflater mCompressor;

  /** Fast Byte array mWriter to hold temporary data. */
  private final FastByteArrayWriter mWriter;

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
  public PageWriter(final SessionConfiguration sessionConfiguration)
      throws Exception {

    mFile = new RandomAccessFile(sessionConfiguration.getPath(), READ_WRITE);

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
      mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec);
    } else {
      mIsEncrypted = false;
      mCipher = null;
      mSecretKeySpec = null;
    }

    mCompressor = new Deflater(Deflater.DEFAULT_COMPRESSION);
    mWriter = new FastByteArrayWriter();
    mOut = new ByteArrayOutputStream();
    mTmp = new byte[BUFFER_SIZE];
  }

  /**
   * {@inheritDoc}
   */
  public final void write(
      final IWriteTransactionState state,
      final PageReference pageReference) throws Exception {

    // Recursively write indirectely referenced pages.
    pageReference.getPage().commit(state);

    // Serialize page.
    mWriter.reset();
    pageReference.getPage().serialize(mWriter);

    // Prepare members.
    byte[] page = mWriter.getBytes();

    // Compress page.
    mCompressor.reset();
    mOut.reset();
    mCompressor.setInput(page, 0, mWriter.size());
    mCompressor.finish();
    int count;
    while (!mCompressor.finished()) {
      count = mCompressor.deflate(mTmp);
      mOut.write(mTmp, 0, count);
    }
    page = mOut.toByteArray();

    // Checksum page.
    if (mIsChecksummed) {
      mChecksum.reset();
      mChecksum.update(page, 0, page.length);
      pageReference.setChecksum(mChecksum.getValue());
    }

    // Encrypt page.
    if (mIsEncrypted) {
      page = mCipher.doFinal(page);
    }

    // Write page to mFile.
    final long start = mFile.length();
    mFile.seek(start);
    mFile.write(page);

    // Remember page coordinates.
    pageReference.setStart(start);
    pageReference.setLength((int) (mFile.length() - start));

  }

}
