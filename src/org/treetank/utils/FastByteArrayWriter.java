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

import java.math.BigInteger;

public final class FastByteArrayWriter {

  private byte[] buffer;

  private int size;

  /**
   * Constructor.
   *
   */
  public FastByteArrayWriter() throws Exception {
    buffer = new byte[32];
    size = 0;
  }

  public final void writeBoolean(final boolean value) throws Exception {
    assertSize(1);
    buffer[size++] = (byte) (value ? 1 : 0);
  }

  public final void writeByte(final byte value) throws Exception {
    assertSize(1);
    buffer[size++] = value;
  }

  public final void writePseudoInt(final int value) throws Exception {
    assertSize(3);
    buffer[size++] = (byte) (value >> 16);
    buffer[size++] = (byte) (value >> 8);
    buffer[size++] = (byte) value;
  }

  public final void writeInt(final int value) throws Exception {
    assertSize(4);
    buffer[size++] = (byte) (value >> 24);
    buffer[size++] = (byte) (value >> 16);
    buffer[size++] = (byte) (value >> 8);
    buffer[size++] = (byte) value;
  }

  public final void writePseudoLong(final long value) throws Exception {
    assertSize(6);
    buffer[size++] = (byte) (value >>> 40);
    buffer[size++] = (byte) (value >>> 32);
    buffer[size++] = (byte) (value >>> 24);
    buffer[size++] = (byte) (value >>> 16);
    buffer[size++] = (byte) (value >> 8);
    buffer[size++] = (byte) value;
  }
  
  public final void writePseudoLongNew(final long value) throws Exception {
    assertSize(6);
    
    byte[] tmp = BigInteger.valueOf(value).toByteArray();
    System.arraycopy(tmp, 0, buffer, size, tmp.length);
    size += 6;
  }

  public final void writeLong(final long value) throws Exception {
    assertSize(8);
    buffer[size++] = (byte) (value >>> 56);
    buffer[size++] = (byte) (value >>> 48);
    buffer[size++] = (byte) (value >>> 40);
    buffer[size++] = (byte) (value >>> 32);
    buffer[size++] = (byte) (value >>> 24);
    buffer[size++] = (byte) (value >>> 16);
    buffer[size++] = (byte) (value >> 8);
    buffer[size++] = (byte) value;
  }

  public final void writeByteArray(final byte[] value) throws Exception {
    assertSize(value.length + 3);

    // Size of byte array.    
    buffer[size++] = (byte) (value.length >> 16);
    buffer[size++] = (byte) (value.length >> 8);
    buffer[size++] = (byte) value.length;

    // Byte array.
    System.arraycopy(value, 0, buffer, size, value.length);
    size += value.length;
  }

  public final byte[] getBytes() throws Exception {
    return buffer;
  }

  public final int size() {
    return size;
  }

  public final void reset() {
    size = 0;
  }

  private final void assertSize(final int sizeDifference) {
    final int requestedSize = size + sizeDifference;
    if (requestedSize > buffer.length) {
      final byte[] biggerBuffer = new byte[requestedSize << 1];
      System.arraycopy(buffer, 0, biggerBuffer, 0, buffer.length);
      buffer = biggerBuffer;
    }
  }

}
