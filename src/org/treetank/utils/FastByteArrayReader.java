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

import java.nio.ByteBuffer;

public final class FastByteArrayReader {

  private final byte[] mBuffer;

  private int mPosition;

  /**
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

  public final int readPseudoInt() throws Exception {
    int value =
        ((mBuffer[mPosition++] & 0xFF) << 16)
            | ((mBuffer[mPosition++] & 0xFF) << 8)
            | (mBuffer[mPosition++] & 0xFF);
    if (value >> 23 == 1) {
      value = value | 0xFF000000;
    }
    return value;
  }

  public final int readInt() throws Exception {
    return ((mBuffer[mPosition++] & 0xFF) << 24)
        | ((mBuffer[mPosition++] & 0xFF) << 16)
        | ((mBuffer[mPosition++] & 0xFF) << 8)
        | (mBuffer[mPosition++] & 0xFF);
  }

  public final long readPseudoLong() throws Exception {
    long value =
        (((long) (mBuffer[mPosition++] & 255) << 40)
            + ((long) (mBuffer[mPosition++] & 255) << 32)
            + ((long) (mBuffer[mPosition++] & 255) << 24)
            + ((mBuffer[mPosition++] & 255) << 16)
            + ((mBuffer[mPosition++] & 255) << 8) + (mBuffer[mPosition++] & 255));
    if (value >> 47 == 1) {
      value = value | 0xFFFF000000000000L;
    }
    return value;
  }
  
  public final long readPseudoLongNew() throws Exception {
    ByteBuffer bb = ByteBuffer.allocate(8);
    bb.put(mBuffer, mPosition, 6);
    bb.position(0);
    mPosition += 6;
    
    return bb.getLong();   
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
