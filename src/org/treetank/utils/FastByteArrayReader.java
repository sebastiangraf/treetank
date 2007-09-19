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

  public final int readInt() throws Exception {
    return ((buffer[position++] & 0xFF) << 24)
        | ((buffer[position++] & 0xFF) << 16)
        | ((buffer[position++] & 0xFF) << 8)
        | (buffer[position++] & 0xFF);
  }

  public final long readLong() throws Exception {
    return (((long) buffer[position++] << 56)
        + ((long) (buffer[position++] & 255) << 48)
        + ((long) (buffer[position++] & 255) << 40)
        + ((long) (buffer[position++] & 255) << 32)
        + ((long) (buffer[position++] & 255) << 24)
        + ((buffer[position++] & 255) << 16)
        + ((buffer[position++] & 255) << 8) + ((buffer[position++] & 255) << 0));
  }

  public final String readUTF() throws Exception {
    final int size =
        ((buffer[position++] & 0xFF) << 24)
            | ((buffer[position++] & 0xFF) << 16)
            | ((buffer[position++] & 0xFF) << 8)
            | (buffer[position++] & 0xFF);

    final String string = new String(buffer, position, size, "UTF-8");
    position += size;
    return string;
  }

  public final byte[] readByteArray() throws Exception {
    final int size =
        ((buffer[position++] & 0xFF) << 24)
            | ((buffer[position++] & 0xFF) << 16)
            | ((buffer[position++] & 0xFF) << 8)
            | (buffer[position++] & 0xFF);

    final byte[] byteArray = new byte[size];
    System.arraycopy(buffer, position, byteArray, 0, size);
    position += size;
    return byteArray;
  }

  public final char[] readCharArray() throws Exception {
    final int size =
        ((buffer[position++]) << 24)
            | ((buffer[position++] & 0xFF) << 16)
            | ((buffer[position++] & 0xFF) << 8)
            | (buffer[position++] & 0xFF);

    final char[] chars = new char[size];
    for (int i = 0; i < size; i++) {
      chars[i] =
          (char) (short) (((buffer[position++] & 0xFF) << 8) | (buffer[position++] & 0xFF));
    }
    return chars;
  }

}
