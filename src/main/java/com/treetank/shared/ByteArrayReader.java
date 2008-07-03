/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * NO permission to use, copy, modify, and/or distribute this software.
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

package com.treetank.shared;

public final class ByteArrayReader {

  private final byte[] mBuffer;

  private int mPosition;

  /**F
   * Constructor.
   *
   */
  public ByteArrayReader(final byte[] buffer) {
    mBuffer = buffer;
    mPosition = 0;
  }

  public final boolean setOffset(int offset) {
    if (offset < mBuffer.length) {
      mPosition = offset;
      return true;
    }
    return false;
  }

  public final boolean readBoolean() {
    return (mBuffer[mPosition++] == 1 ? true : false);
  }

  public final byte readByte() {
    return mBuffer[mPosition++];
  }

  public final int readInt() {
    return ((mBuffer[mPosition++] & 0xFF) << 24)
        | ((mBuffer[mPosition++] & 0xFF) << 16)
        | ((mBuffer[mPosition++] & 0xFF) << 8)
        | (mBuffer[mPosition++] & 0xFF);
  }

  public final int readVarInt() {
    int value = ((mBuffer[mPosition++] & 127));
    if ((mBuffer[mPosition - 1] & 128) != 0) {
      value |= ((mBuffer[mPosition++] & 127)) << 7;
      if ((mBuffer[mPosition - 1] & 128) != 0) {
        value |= ((mBuffer[mPosition++] & 127)) << 14;
        if ((mBuffer[mPosition - 1] & 128) != 0) {
          value |= ((mBuffer[mPosition++] & 127)) << 21;
          if ((mBuffer[mPosition - 1] & 128) != 0) {
            value |= ((mBuffer[mPosition++] & 127)) << 28;
          } else if ((mBuffer[mPosition - 1] & 64) != 0)
            value |= 0xF0000000;
        } else if ((mBuffer[mPosition - 1] & 64) != 0)
          value |= 0xFFF00000;
      } else if ((mBuffer[mPosition - 1] & 64) != 0)
        value |= 0xFFFFE000;
    } else if ((mBuffer[mPosition - 1] & 64) != 0)
      value |= 0xFFFFFFC0;
    return value;
  }

  public final long readVarLong() {
    mPosition++;
    long value = (long) (mBuffer[mPosition++] & 255);
    if (mBuffer[mPosition - 2] > 1) {
      value += ((long) (mBuffer[mPosition++] & 255) << 8);
      if (mBuffer[mPosition - 3] > 2) {
        value += ((long) (mBuffer[mPosition++] & 255) << 16);
        if (mBuffer[mPosition - 4] > 3) {
          value += ((long) (mBuffer[mPosition++] & 255) << 24);
          if (mBuffer[mPosition - 5] > 4) {
            value += ((long) (mBuffer[mPosition++] & 255) << 32);
            if (mBuffer[mPosition - 6] > 5) {
              value += ((long) (mBuffer[mPosition++] & 255) << 40);
              if (mBuffer[mPosition - 7] > 6) {
                value += ((long) (mBuffer[mPosition++] & 255) << 48);
                if (mBuffer[mPosition - 8] > 7) {
                  value += ((long) mBuffer[mPosition++] << 56);
                } else if ((mBuffer[mPosition - 1] & 128) != 0)
                  value |= 0xFF000000000000L;
              } else if ((mBuffer[mPosition - 1] & 128) != 0)
                value |= 0xFFFF000000000000L;
            } else if ((mBuffer[mPosition - 1] & 128) != 0)
              value |= 0xFFFFFF0000000000L;
          } else if ((mBuffer[mPosition - 1] & 128) != 0)
            value |= 0xFFFFFFFF00000000L;
        } else if ((mBuffer[mPosition - 1] & 128) != 0)
          value |= 0xFFFFFFFFFF000000L;
      } else if ((mBuffer[mPosition - 1] & 128) != 0)
        value |= 0xFFFFFFFFFFFF0000L;
    } else if ((mBuffer[mPosition - 1] & 128) != 0)
      value |= 0xFFFFFFFFFFFFFF00L;
    return value;
  }

  public final long readLong() {
    return (((long) mBuffer[mPosition++] << 56)
        + ((long) (mBuffer[mPosition++] & 255) << 48)
        + ((long) (mBuffer[mPosition++] & 255) << 40)
        + ((long) (mBuffer[mPosition++] & 255) << 32)
        + ((long) (mBuffer[mPosition++] & 255) << 24)
        + ((mBuffer[mPosition++] & 255) << 16)
        + ((mBuffer[mPosition++] & 255) << 8) + (mBuffer[mPosition++] & 255));
  }

  public final byte[] readVarByteArray() {
    final int size =
        ((mBuffer[mPosition++] & 0xFF) << 16)
            | ((mBuffer[mPosition++] & 0xFF) << 8)
            | (mBuffer[mPosition++] & 0xFF);

    final byte[] byteArray = new byte[size];
    System.arraycopy(mBuffer, mPosition, byteArray, 0, size);
    mPosition += size;
    return byteArray;
  }

  public final byte[] readByteArray(final int size) {
    final byte[] byteArray = new byte[size];
    System.arraycopy(mBuffer, mPosition, byteArray, 0, size);
    mPosition += size;
    return byteArray;
  }

}
