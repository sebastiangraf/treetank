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

public final class FastByteArrayWriter {

  private byte[] mBuffer;

  private int mSize;

  /**
   * Constructor.
   *
   */
  public FastByteArrayWriter() {
    mBuffer = new byte[32768];
    mSize = 0;
  }

  public final void writeBoolean(final boolean value) {
    assertSize(1);
    mBuffer[mSize++] = (byte) (value ? 1 : 0);
  }

  public final void writeByte(final byte value) {
    assertSize(1);
    mBuffer[mSize++] = value;
  }

  public final void writeInt(final int value) {
    assertSize(4);
    mBuffer[mSize++] = (byte) (value >> 24);
    mBuffer[mSize++] = (byte) (value >> 16);
    mBuffer[mSize++] = (byte) (value >> 8);
    mBuffer[mSize++] = (byte) value;
  }

  public final void writeVarInt(final int value) {
    assertSize(5);
    mBuffer[mSize++] = (byte) (value);
    if (value > 63 || value < -64) {
      mBuffer[mSize - 1] |= 128;
      mBuffer[mSize++] = (byte) (value >> 7);
      if (value > 8191 || value < -8192) {
        mBuffer[mSize - 1] |= 128;
        mBuffer[mSize++] = (byte) (value >> 14);
        if (value > 1048575 || value < -1048576) {
          mBuffer[mSize - 1] |= 128;
          mBuffer[mSize++] = (byte) (value >> 21);
          if (value > 134217727 || value < -134217728) {
            mBuffer[mSize - 1] |= 128;
            mBuffer[mSize++] = (byte) (value >> 28);
          } else
            mBuffer[mSize - 1] &= 127;
        } else
          mBuffer[mSize - 1] &= 127;
      } else
        mBuffer[mSize - 1] &= 127;
    } else
      mBuffer[mSize - 1] &= 127;
  }

  public final void writeVarLong(final long value) {
    assertSize(9);
    mSize++;
    mBuffer[mSize++] = (byte) value;
    if (value > 127 || value < -128) {
      mBuffer[mSize++] = (byte) (value >> 8);
      if (value > 32767 || value < -32768) {
        mBuffer[mSize++] = (byte) (value >>> 16);
        if (value > 8388607 || value < -8388608) {
          mBuffer[mSize++] = (byte) (value >>> 24);
          if (value > 2147483647 || value < -2147483648) {
            mBuffer[mSize++] = (byte) (value >>> 32);
            if (value > (2 ^ 39) - 1 || value < -(2 ^ 39)) {
              mBuffer[mSize++] = (byte) (value >>> 40);
              if (value > (2 ^ 47) - 1 || value < -(2 ^ 47)) {
                mBuffer[mSize++] = (byte) (value >>> 48);
                if (value > (2 ^ 55) - 1 || value < -(2 ^ 55)) {
                  mBuffer[mSize++] = (byte) (value >>> 56);
                  mBuffer[mSize - 9] = (byte) 8;
                } else
                  mBuffer[mSize - 8] = (byte) 7;
              } else
                mBuffer[mSize - 7] = (byte) 6;
            } else
              mBuffer[mSize - 6] = (byte) 5;
          } else
            mBuffer[mSize - 5] = (byte) 4;
        } else
          mBuffer[mSize - 4] = (byte) 3;
      } else
        mBuffer[mSize - 3] = (byte) 2;
    } else
      mBuffer[mSize - 2] = (byte) 1;
  }

  public final void writeLong(final long value) {
    assertSize(8);
    mBuffer[mSize++] = (byte) (value >>> 56);
    mBuffer[mSize++] = (byte) (value >>> 48);
    mBuffer[mSize++] = (byte) (value >>> 40);
    mBuffer[mSize++] = (byte) (value >>> 32);
    mBuffer[mSize++] = (byte) (value >>> 24);
    mBuffer[mSize++] = (byte) (value >>> 16);
    mBuffer[mSize++] = (byte) (value >> 8);
    mBuffer[mSize++] = (byte) value;
  }

  public final void writeByteArray(final byte[] value) {
    assertSize(value.length + 3);

    // Size of byte array.    
    mBuffer[mSize++] = (byte) (value.length >> 16);
    mBuffer[mSize++] = (byte) (value.length >> 8);
    mBuffer[mSize++] = (byte) value.length;

    // Byte array.
    System.arraycopy(value, 0, mBuffer, mSize, value.length);
    mSize += value.length;
  }

  public final byte[] getBytes() {
    return mBuffer;
  }

  public final int size() {
    return mSize;
  }

  public final void reset() {
    mSize = 0;
  }

  private final void assertSize(final int sizeDifference) {
    final int requestedSize = mSize + sizeDifference;
    if (requestedSize > mBuffer.length) {
      final byte[] biggerBuffer = new byte[requestedSize << 1];
      System.arraycopy(mBuffer, 0, biggerBuffer, 0, mBuffer.length);
      mBuffer = biggerBuffer;
    }
  }

}
