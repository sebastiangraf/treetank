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

import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.FastByteArrayWriter;
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

  /** Compressor to compress the page. */
  private ICompression mCompressor;

  /** Fast Byte array mWriter to hold temporary data. */
  private final FastByteArrayWriter mWriter;

  private final byte[] mReference;

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

      try {
        System.loadLibrary("TreeTank");
        mCompressor = new NativeTreeTank();
      } catch (UnsatisfiedLinkError e) {
        mCompressor = new JavaCompression();
      }

      mWriter = new FastByteArrayWriter();
      mReference = new byte[24];

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

      // Serialise page.
      mWriter.reset();
      pageReference.getPage().serialize(mWriter);

      // Initialise call to crypt.
      final int size = mWriter.size();
      mReference[8] = (byte) (size >> 24);
      mReference[9] = (byte) (size >> 16);
      mReference[10] = (byte) (size >> 8);
      mReference[11] = (byte) size;
      final byte[] buffer = mWriter.getBytes();

      // Compress page.
      if (mCompressor.crypt(mReference, buffer) != 0) {
        throw new Exception("Page crypto error.");
      }

      // Write page to mFile.
      final long start = mFile.length();
      mFile.seek(start);
      mFile.write(buffer, 0, ((mReference[8] & 0xFF) << 24)
          | ((mReference[9] & 0xFF) << 16)
          | ((mReference[10] & 0xFF) << 8)
          | (mReference[11] & 0xFF));

      // Remember page coordinates.
      pageReference.setStart(start);
      pageReference.setLength((int) (mFile.length() - start));

    } catch (Exception e) {
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
      mFile.close();
    } finally {
      super.finalize();
    }
  }

}
