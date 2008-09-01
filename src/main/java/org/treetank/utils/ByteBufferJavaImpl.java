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

package org.treetank.utils;

public final class ByteBufferJavaImpl implements IByteBuffer {

  private final byte[] mBuffer;

  private int mPosition;

  public ByteBufferJavaImpl(final int capacity) {
    mBuffer = new byte[capacity];
    mPosition = 0;
  }

  public final int position() {
    return mPosition;
  }

  public final void position(final int position) {
    if ((position >= mBuffer.length) || (position < 0)) {
      throw new IllegalArgumentException("Position out of bounds.");
    }
    mPosition = position;
  }

  public final void put(long value) {
    while ((value & ~0x7F) != 0) {
      mBuffer[mPosition++] = ((byte) ((value & 0x7f) | 0x80));
      value >>>= 7;
    }
    mBuffer[mPosition++] = (byte) value;
  }

  public final void putArray(byte[] value) {
    System.arraycopy(value, 0, mBuffer, mPosition, value.length);
    mPosition += value.length;
  }

  public final void putAll(long[] values) {
    for (int i = 0; i < values.length; i++) {
      put(values[i]);
    }
  }

  public final long get() {
    byte singleByte = mBuffer[mPosition++];
    long value = singleByte & 0x7F;
    for (int shift = 7; (singleByte & 0x80) != 0; shift += 7) {
      singleByte = mBuffer[mPosition++];
      value |= (singleByte & 0x7FL) << shift;
    }
    return value;
  }

  public final void get(long[] values) {
    for (int i = 0; i < values.length; i++) {
      values[i] = get();
    }
  }

  public final byte[] getArray(int length) {
    byte[] buffer = new byte[length];
    System.arraycopy(mBuffer, mPosition, buffer, 0, length);
    mPosition += length;
    return buffer;
  }

  public final long[] getAll(int count) {
    long[] values = new long[count];
    for (int i = 0; i < count; i++) {
      values[i] = get();
    }
    return values;
  }

  public final void close() {
    // Nothing to do here.
  }

}
