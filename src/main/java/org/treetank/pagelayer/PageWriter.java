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
 * <h1>PageWriter</h1>
 * 
 * <p>
 * Each commit of the ISession creates one PageWriter
 * instance to write to the TreeTank file.
 * </p>
 */
public final class PageWriter {

  /** Random access mFile to work on. */
  private final RandomAccessFile mFile;

  /** File channel to support directly allocated ByteBuffer. */
  private final FileChannel mChannel;

  /** Compressor to compress the page. */
  private ICompression mCompressor;

  /** Fast Byte array mWriter to hold temporary data. */
  private final ByteBuffer mBuffer;

  private final ByteBuffer mReference;

  /**
   * Constructor.
   * 
   * @param sessionConfiguration Configuration of session we are bound to.
   * @throws RuntimeException if the PageWriter could not be instantiated.
   */
  public PageWriter(final SessionConfiguration sessionConfiguration) {

    try {

      mFile =
          new RandomAccessFile(
              sessionConfiguration.getAbsolutePath(),
              IConstants.READ_WRITE);
      mChannel = mFile.getChannel();

      try {
        System.loadLibrary("TreeTank");
        mCompressor = new NativeTreeTank();
      } catch (UnsatisfiedLinkError e) {
        mCompressor = new JavaCompression();
      }

      mBuffer = ByteBuffer.allocateDirect(32768);
      mReference = ByteBuffer.allocateDirect(24);

    } catch (Exception e) {
      throw new RuntimeException("Could not create page writer: "
          + e.getLocalizedMessage());
    }
  }

  /**
   * Write page contained in page reference to storage.
   * 
   * @param pageReference Page reference to write.
   * @throws RuntimeException due to errors during writing.
   */
  public final void write(
      final PageReference<? extends AbstractPage> pageReference) {

    try {

      // Prepare environment for write.
      mBuffer.clear();
      mReference.clear();

      // Serialise page.
      pageReference.getPage().serialize(mBuffer);
      int bufferLength = mBuffer.position();
      mReference.putInt(8, bufferLength);

      // Perform crypto operations.
      if (mCompressor.crypt(mReference, mBuffer) != 0) {
        throw new Exception("Page crypto error.");
      }
      bufferLength = mReference.getInt(8);

      // Write page to mFile.
      final long fileSize = mChannel.size();
      mBuffer.flip();
      mChannel.position(fileSize);
      int bytesWritten = 0;
      while (bytesWritten < bufferLength) {
        bytesWritten += mChannel.write(mBuffer);
      }
      
      // Remember page coordinates.
      pageReference.setStart(fileSize);
      pageReference.setLength(bufferLength);

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Could not write page "
          + pageReference
          + " due to: "
          + e.getLocalizedMessage());
    }

  }

  /**
   * Properly close file handle.
   */
  public final void close() {
    try {
      mChannel.close();
      mFile.close();
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
      mChannel.close();
      mFile.close();
    } finally {
      super.finalize();
    }
  }

}
