/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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

  private final byte[] buffer;

  private int position;

  /**
   * Constructor.
   *
   */
  public FastByteArrayReader(final byte[] initBuffer) throws Exception {
    buffer = initBuffer;
    position = 0;
  }

  public final boolean readBoolean() throws Exception {
    return (buffer[position++] == 1 ? true : false);
  }

  public final byte readByte() throws Exception {
    return buffer[position++];
  }

  public final int readPseudoInt() throws Exception {
    int value =
        ((buffer[position++] & 0xFF) << 16)
            | ((buffer[position++] & 0xFF) << 8)
            | (buffer[position++] & 0xFF);
    if (value >> 23 == 1) {
      value = value | 0xFF000000;
    }
    return value;
  }

  public final int readInt() throws Exception {
    return ((buffer[position++] & 0xFF) << 24)
        | ((buffer[position++] & 0xFF) << 16)
        | ((buffer[position++] & 0xFF) << 8)
        | (buffer[position++] & 0xFF);
  }

  public final long readPseudoLong() throws Exception {
    long value =
        (((long) (buffer[position++] & 255) << 40)
            + ((long) (buffer[position++] & 255) << 32)
            + ((long) (buffer[position++] & 255) << 24)
            + ((buffer[position++] & 255) << 16)
            + ((buffer[position++] & 255) << 8) + (buffer[position++] & 255));
    if (value >> 47 == 1) {
      value = value | 0xFFFF000000000000L;
    }
    return value;
  }
  
  public final long readPseudoLongNew() throws Exception {
    return 0;
    
  }

  public final long readLong() throws Exception {
    return (((long) buffer[position++] << 56)
        + ((long) (buffer[position++] & 255) << 48)
        + ((long) (buffer[position++] & 255) << 40)
        + ((long) (buffer[position++] & 255) << 32)
        + ((long) (buffer[position++] & 255) << 24)
        + ((buffer[position++] & 255) << 16)
        + ((buffer[position++] & 255) << 8) + (buffer[position++] & 255));
  }

  public final byte[] readByteArray() throws Exception {
    final int size =
        ((buffer[position++] & 0xFF) << 16)
            | ((buffer[position++] & 0xFF) << 8)
            | (buffer[position++] & 0xFF);

    final byte[] byteArray = new byte[size];
    System.arraycopy(buffer, position, byteArray, 0, size);
    position += size;
    return byteArray;
  }

}
