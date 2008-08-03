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

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JavaCompression implements ICompression {

  private final Deflater mCompressor;

  private final Inflater mDecompressor;

  private final byte[] mTmp;

  private final ByteArrayOutputStream mOut;

  /**
   * Initialize compressor.
   */
  public JavaCompression() {
    mCompressor = new Deflater();
    mDecompressor = new Inflater();
    mTmp = new byte[BUFFER_SIZE];
    mOut = new ByteArrayOutputStream();
  }

  /**
   * Compress data.
   * 
   * @param data data that should be compressed
   * @return compressed data, null if failed
   */
  public byte[] compress(final byte[] data) {
    try {
      mCompressor.reset();
      mOut.reset();
      mCompressor.setInput(data);
      mCompressor.finish();
      int count;
      while (!mCompressor.finished()) {
        count = mCompressor.deflate(mTmp);
        mOut.write(mTmp, 0, count);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return mOut.toByteArray();
  }

  /**
   * Decompress data.
   * 
   * @param data data that should be decompressed
   * @return Decompressed data, null if failed
   */
  public byte[] decompress(final byte[] data) {
    try {
      mDecompressor.reset();
      mOut.reset();
      mDecompressor.setInput(data);
      int count;
      while (!mDecompressor.finished()) {
        count = mDecompressor.inflate(mTmp);
        mOut.write(mTmp, 0, count);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return mOut.toByteArray();
  }

}
