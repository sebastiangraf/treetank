/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.utils;

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

  public final void writeInt(final int value) throws Exception {
    assertSize(4);
    buffer[size++] = (byte) (value >> 24);
    buffer[size++] = (byte) (value >> 16);
    buffer[size++] = (byte) (value >> 8);
    buffer[size++] = (byte) value;
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

  public final void writeUTF(final String value) throws Exception {
    final byte[] utf = value.getBytes("UTF-8");
    assertSize(utf.length + 4);

    // Size of UTF-8-encoded string.    
    buffer[size++] = (byte) (utf.length >> 24);
    buffer[size++] = (byte) (utf.length >> 16);
    buffer[size++] = (byte) (utf.length >> 8);
    buffer[size++] = (byte) utf.length;

    // UTF-8-encoded string.
    System.arraycopy(utf, 0, buffer, size, utf.length);
    size += utf.length;
  }

  public final void writeByteArray(final byte[] value) throws Exception {
    assertSize(value.length + 4);

    // Size of byte array.    
    buffer[size++] = (byte) (value.length >> 24);
    buffer[size++] = (byte) (value.length >> 16);
    buffer[size++] = (byte) (value.length >> 8);
    buffer[size++] = (byte) value.length;

    // Byte array.
    System.arraycopy(value, 0, buffer, size, value.length);
    size += value.length;
  }

  public final void writeCharArray(final char[] value) throws Exception {
    assertSize((value.length << 1) + 4);

    // Size of byte array.    
    buffer[size++] = (byte) (value.length >> 24);
    buffer[size++] = (byte) (value.length >> 16);
    buffer[size++] = (byte) (value.length >> 8);
    buffer[size++] = (byte) value.length;

    // Byte array.
    for (int i = 0, l = value.length; i < l; i++) {
      buffer[size++] = (byte) (value[i] >> 8);
      buffer[size++] = (byte) value[i];
    }
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
