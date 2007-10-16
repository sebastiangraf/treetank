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

package org.treetank.utils;

import java.math.BigInteger;

public final class FastByteArrayWriter {

  private byte[] mBuffer;

  private int mSize;

  /**
   * Constructor.
   *
   */
  public FastByteArrayWriter() throws Exception {
    mBuffer = new byte[32];
    mSize = 0;
  }

  public final void writeBoolean(final boolean value) throws Exception {
    assertSize(1);
    mBuffer[mSize++] = (byte) (value ? 1 : 0);
  }

  public final void writeByte(final byte value) throws Exception {
    assertSize(1);
    mBuffer[mSize++] = value;
  }

  public final void writePseudoInt(final int value) throws Exception {
    assertSize(3);
    mBuffer[mSize++] = (byte) (value >> 16);
    mBuffer[mSize++] = (byte) (value >> 8);
    mBuffer[mSize++] = (byte) value;
  }

  public final void writeInt(final int value) throws Exception {
    assertSize(4);
    mBuffer[mSize++] = (byte) (value >> 24);
    mBuffer[mSize++] = (byte) (value >> 16);
    mBuffer[mSize++] = (byte) (value >> 8);
    mBuffer[mSize++] = (byte) value;
  }
  
  public final void writePseudoVarSizeInt(final int value) throws Exception {
    assertSize(4);
    mBuffer[mSize++] = (byte) (value);
    if (value > 63 || value < -64) {
      mBuffer[mSize-1] |= 128;
      mBuffer[mSize++] = (byte) (value >> 7);
      if (value > 8191 || value < -8192) {
        mBuffer[mSize-1] |= 128;
        mBuffer[mSize++] = (byte) (value >> 14);
        if (value > 1048575 || value < -1048576) {
          mBuffer[mSize-1] |= 128;
          mBuffer[mSize++] = (byte) (value >> 21);
        } else mBuffer[mSize-1] &= 127;
      } else mBuffer[mSize-1] &= 127;
    } else mBuffer[mSize-1] &= 127;
  }

  public final void writePseudoLong(final long value) throws Exception {
    assertSize(6);
    mBuffer[mSize++] = (byte) (value >>> 40);
    mBuffer[mSize++] = (byte) (value >>> 32);
    mBuffer[mSize++] = (byte) (value >>> 24);
    mBuffer[mSize++] = (byte) (value >>> 16);
    mBuffer[mSize++] = (byte) (value >> 8);
    mBuffer[mSize++] = (byte) value;
  }
  
  public final void writeVarSizeLong(final long value) throws Exception {
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
            if (value > (2^39)-1 || value < -(2^39)) {
              mBuffer[mSize++] = (byte) (value >>> 40);
              if (value > (2^47)-1 || value < -(2^47)) {
                mBuffer[mSize++] = (byte) (value >>> 48);
                if (value > (2^55)-1 || value < -(2^55)) {
                  mBuffer[mSize++] = (byte) (value >>> 56);
                  mBuffer[mSize-9] = (byte) 8;
                } else mBuffer[mSize-8] = (byte) 7;
              } else mBuffer[mSize-7] = (byte) 6;
            } else mBuffer[mSize-6] = (byte) 5;
          } else mBuffer[mSize-5] = (byte) 4;
        } else mBuffer[mSize-4] = (byte) 3;
      } else mBuffer[mSize-3] = (byte) 2;
    } else mBuffer[mSize-2] = (byte) 1;
  }

  public final void writeLong(final long value) throws Exception {
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

  public final void writeByteArray(final byte[] value) throws Exception {
    assertSize(value.length + 3);

    // Size of byte array.    
    mBuffer[mSize++] = (byte) (value.length >> 16);
    mBuffer[mSize++] = (byte) (value.length >> 8);
    mBuffer[mSize++] = (byte) value.length;

    // Byte array.
    System.arraycopy(value, 0, mBuffer, mSize, value.length);
    mSize += value.length;
  }

  public final byte[] getBytes() throws Exception {
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
