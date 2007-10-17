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

public final class FastByteArrayReader {

  private final byte[] mBuffer;

  private int mPosition;

  /**F
   * Constructor.
   *
   */
  public FastByteArrayReader(final byte[] buffer) throws Exception {
    mBuffer = buffer;
    mPosition = 0;
  }

  public final boolean readBoolean() throws Exception {
    return (mBuffer[mPosition++] == 1 ? true : false);
  }

  public final byte readByte() throws Exception {
    return mBuffer[mPosition++];
  }

  public final int readInt() throws Exception {
    return ((mBuffer[mPosition++] & 0xFF) << 24)
        | ((mBuffer[mPosition++] & 0xFF) << 16)
        | ((mBuffer[mPosition++] & 0xFF) << 8)
        | (mBuffer[mPosition++] & 0xFF);
  }

  public final int readVarInt() throws Exception {
    int value = ((mBuffer[mPosition++] & 127));
    if ((mBuffer[mPosition - 1] & 128) != 0) {
      value |= ((mBuffer[mPosition++] & 127)) << 7;
      if ((mBuffer[mPosition - 1] & 128) != 0) {
        value |= ((mBuffer[mPosition++] & 127)) << 14;
        if ((mBuffer[mPosition - 1] & 128) != 0) {
          value |= ((mBuffer[mPosition++] & 127)) << 21;
          if ((mBuffer[mPosition - 1] & 128) != 0) {
            value |= ((mBuffer[mPosition++] & 255)) << 28;
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

  public final long readVarLong() throws Exception {
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

  public final long readLong() throws Exception {
    return (((long) mBuffer[mPosition++] << 56)
        + ((long) (mBuffer[mPosition++] & 255) << 48)
        + ((long) (mBuffer[mPosition++] & 255) << 40)
        + ((long) (mBuffer[mPosition++] & 255) << 32)
        + ((long) (mBuffer[mPosition++] & 255) << 24)
        + ((mBuffer[mPosition++] & 255) << 16)
        + ((mBuffer[mPosition++] & 255) << 8) + (mBuffer[mPosition++] & 255));
  }

  public final byte[] readByteArray() throws Exception {
    final int size =
        ((mBuffer[mPosition++] & 0xFF) << 16)
            | ((mBuffer[mPosition++] & 0xFF) << 8)
            | (mBuffer[mPosition++] & 0xFF);

    final byte[] byteArray = new byte[size];
    System.arraycopy(mBuffer, mPosition, byteArray, 0, size);
    mPosition += size;
    return byteArray;
  }

}
