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
 * $Id$
 */

package org.treetank.pagelayer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.treetank.sessionlayer.SessionConfiguration;
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

  /** Random access mFile to work on. */
  private final RandomAccessFile mFile;

  /** File channel to support directly allocated ByteBuffer. */
  private final FileChannel mChannel;

  /** Inflater to decompress. */
  private ICompression mDecompressor;

  /** Temporary data buffer. */
  private final ByteBuffer mBuffer;

  /** Temporary reference buffer. */
  private final ByteBuffer mReference;

  /**
   * Constructor.
   * 
   * @param sessionConfiguration Configuration of session we are bound to.
   * @throws RuntimeException if the class could not be instantiated.
   */
  public PageReader(final SessionConfiguration sessionConfiguration) {

    try {

      mFile =
          new RandomAccessFile(
              sessionConfiguration.getAbsolutePath(),
              IConstants.READ_ONLY);
      mChannel = mFile.getChannel();

      try {
        System.loadLibrary("TreeTank");
        mDecompressor = new NativeTreeTank();
      } catch (UnsatisfiedLinkError e) {
        mDecompressor = new JavaCompression();
      }

      mBuffer = ByteBuffer.allocateDirect(IConstants.BUFFER_SIZE);
      mReference = ByteBuffer.allocateDirect(IConstants.REFERENCE_SIZE);

    } catch (Exception e) {
      throw new RuntimeException("Could not create page reader: "
          + e.getLocalizedMessage());
    }
  }

  /**
   * Read page from storage.
   * 
   * @param pageReference to read.
   * @return Byte array reader to read bytes from.o
   * @throws RuntimeException if there was an error during reading.
   */
  public final ByteBuffer read(
      final PageReference<? extends AbstractPage> pageReference) {

    if (!pageReference.isCommitted()) {
      throw new IllegalArgumentException("Page reference is invalid.");
    }

    try {

      // Prepare environment for read.
      mBuffer.clear();
      mReference.clear();
      mBuffer.limit(pageReference.getLength());
      mReference.putInt(8, pageReference.getLength());

      // Read page from file.
      mChannel.position(pageReference.getStart());
      mChannel.read(mBuffer);
      mBuffer.flip();

      // Perform crypto operations.
      if (mDecompressor.decrypt(mReference, mBuffer) != 0) {
        throw new Exception("Page crypto error.");
      }

    } catch (Exception e) {
      throw new RuntimeException("Could not read page "
          + pageReference
          + " due to: "
          + e.getLocalizedMessage());
    }

    // Return reader required to instantiate and deserialize page.
    mBuffer.clear();
    return mBuffer;

  }

  /**
   * Properly close file handle.
   */
  public final void close() {
    try {
      if (mChannel != null) {
        mChannel.close();
      }
      if (mFile != null) {
        mFile.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
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
      if (mChannel != null) {
        mChannel.close();
      }
      if (mFile != null) {
        mFile.close();
      }
    } finally {
      super.finalize();
    }
  }

}
