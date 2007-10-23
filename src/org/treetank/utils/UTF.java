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

import java.io.ByteArrayOutputStream;

import org.treetank.api.IConstants;

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
      string = new String(bytes, IConstants.DEFAULT_ENCODING);
    } catch (Exception e) {
      throw new RuntimeException("Could not convert byte[] to String: "
          + e.getLocalizedMessage());
    }
    return string;
  }

  public static final byte[] convert(final String string) {
    byte[] bytes = null;
    try {
      if (string == null || string.length() == 0) {
        bytes = EMPTY;
      } else {
        bytes = string.getBytes(IConstants.DEFAULT_ENCODING);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not convert String to byte[]: "
          + e.getLocalizedMessage());
    }
    return bytes;
  }

  public static final byte[] fastConvert(final String string) {
    final int l = string.length();
    byte[] bytes = new byte[l];
    for (int i = 0; i < l; i++) {
      bytes[i] = (byte) string.charAt(i);
      // Check if char is ASCII
      if (bytes[i] < 0)
        return convert(string);
    }
    return bytes;
  }

  public static final byte[] nextConvert(final String string) {
    final int l = string.length();
    ByteArrayOutputStream bytes = new ByteArrayOutputStream(l);
    try {
      for (int i = 0; i < l; i++) {
        byte b = (byte) string.charAt(i);
        if (b < 0)
          bytes.write(b);
        else
          bytes.write(string.substring(i, i + 1).getBytes(
              IConstants.DEFAULT_ENCODING));
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not convert String to byte[]: "
          + e.getLocalizedMessage());
    }
    return bytes.toByteArray();
  }

  public static boolean ascii(final byte[] bytes) {
    final int l = bytes.length;
    for (int i = 0; i < l; i++)
      if (bytes[i] < 0)
        return false;
    return true;
  }

  public static final boolean equals(final byte[] value1, final byte[] value2) {
    // Fail if the values are not of equal length.
    if (value1.length != value2.length) {
      return false;
    }
    // Fail if a single byte does not match.
    for (int i = 0, l = value1.length; i < l; i++) {
      if (value1[i] != value2[i]) {
        return false;
      }
    }
    // Values must be equal if we reach here.
    return true;
  }

  public static final boolean equals(final byte[] value1, final String value2) {
    return equals(value1, UTF.convert(value2));
  }

  public static final boolean equals(final String value1, final byte[] value2) {
    return equals(UTF.convert(value1), value2);
  }

  public static final boolean equals(final String value1, final String value2) {
    return equals(UTF.convert(value1), UTF.convert(value2));
  }

}
