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

package org.treetank.pagelayer;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.IConstants;


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
  private static final Logger LOGGER = Logger.getLogger(PageReader.class);

  /** Read-only mode for random access mFile. */
  private static final String READ_ONLY = "r";

  /** Size of temporary buffer. */
  private static final int BUFFER_SIZE = 8192;

  /** Random access mFile to work on. */
  private final RandomAccessFile mFile;

  /** Adler32 Checksum to assert integrity. */
  private final Checksum mChecksum;

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
   * @param path Path of mFile to read from.
   * @throws Exception of any kind.
   */
  public PageReader(final String path) throws Exception {
    mFile = new RandomAccessFile(path, READ_ONLY);
    mChecksum =
        IConstants.CHECKSUM_ALGORITHM == "CRC" ? new CRC32() : new Adler32();
    mCipher = Cipher.getInstance(IConstants.ENCRYPTION_ALGORITHM);
    mSecretKeySpec =
        new SecretKeySpec(
            IConstants.ENCRYPTION_KEY,
            IConstants.ENCRYPTION_ALGORITHM);
    mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec);
    mDecompressor = new Inflater();
    mOut = new ByteArrayOutputStream();
    mTmp = new byte[BUFFER_SIZE];
  }

  /**
   * {@inheritDoc}
   */
  public final FastByteArrayReader read(final PageReference pageReference)
      throws Exception {

    // Prepare members.
    byte[] page;

    // Read encrypted page from mFile.
    mFile.seek(pageReference.getStart());
    page = new byte[pageReference.getSize()];
    mFile.read(page);

    // Decrypt page.
    if (IConstants.ENCRYPT) {
      page = mCipher.doFinal(page);
    }

    // Verify checksummed page.
    if (IConstants.CHECKSUM) {
      mChecksum.reset();
      mChecksum.update(page, 0, page.length);
      if (mChecksum.getValue() != pageReference.getChecksum()) {
        LOGGER.error("Page checksum is not valid for start="
            + pageReference.getStart()
            + "; size="
            + pageReference.getSize()
            + "; checksum="
            + pageReference.getChecksum());
        throw new IllegalStateException("Page checksum is not valid.");
      }
    }

    // Decompress page.
    if (IConstants.COMPRESS) {
      mDecompressor.reset();
      mOut.reset();
      mDecompressor.setInput(page);
      int count;
      while (!mDecompressor.finished()) {
        count = mDecompressor.inflate(mTmp);
        mOut.write(mTmp, 0, count);
      }
      page = mOut.toByteArray();
    }

    // Logging.
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Read start="
          + pageReference.getStart()
          + "; size="
          + pageReference.getSize()
          + "; checksum="
          + pageReference.getChecksum());
    }

    // Return reader required to instantiate and deserialize page.
    return new FastByteArrayReader(page);

  }

}
