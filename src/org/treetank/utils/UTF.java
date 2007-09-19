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

public final class UTF {

  public static final int MAX_HASH_LENGTH = 32;

  public static final byte[] EMPTY = new byte[0];

  /**
   * Default constructor is hidden.
   * 
   */
  private UTF() {
    // hidden
  }

  public static final int hash(final byte[] bytes) {
    int h = 0;
    int l = Math.min(bytes.length, MAX_HASH_LENGTH);
    for (int i = 0; i < l; i++)
      h = (h << 5) - h + bytes[i];
    return h;
  }

  public static final String convert(final byte[] bytes) {
    String string = null;
    try {
      string = new String(bytes, IConstants.ENCODING);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return string;
  }

  public static final byte[] convert(final String string) {
    byte[] bytes = null;
    try {
      if (string == null || string.length() == 0) {
        bytes = EMPTY;
      } else {
        bytes = string.getBytes(IConstants.ENCODING);
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return bytes;
  }

}
