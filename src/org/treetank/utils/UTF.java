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

import org.treetank.api.IConstants;

public final class UTF {

  public static final byte[] EMPTY = new byte[0];

  private static final int MAX_HASH_LENGTH = 32;

  private static final int ASCII_OFFSET = 48;

  private static final int ASCII_MINUS = 45;

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

  public static final String parseString(final byte[] bytes) {
    String string = null;
    try {
      string = new String(bytes, IConstants.DEFAULT_ENCODING);
    } catch (Exception e) {
      throw new RuntimeException("Could not convert byte[] to String: "
          + e.getLocalizedMessage());
    }
    return string;
  }

  public static final int parseInt(final byte[] bytes) {
    int value = 0;
    int power = 1;
    if (bytes[0] == (byte) ASCII_MINUS) {
      for (int i = bytes.length - 1; i > 0; i--) {
        value += (bytes[i] - ASCII_OFFSET) * power;
        power *= 10;
      }
      return value *= -1;
    } else {
      for (int i = bytes.length - 1; i >= 0; i--) {
        value += (bytes[i] - ASCII_OFFSET) * power;
        power *= 10;
      }
      return value;
    }
  }

  public static final byte[] getBytes(final int value) {
    byte[] bytes = null;
    if (value > 0) {
      int remainder = value;
      final int length = (int) Math.log10((double) remainder);
      bytes = new byte[length + 1];
      int dividend = (int) Math.pow((double) 10, (double) length);
      int digit = 0;
      for (int i = length; i >= 0; i--) {
        digit = (byte) (remainder / dividend);
        bytes[length - i] = (byte) (digit + ASCII_OFFSET);
        remainder -= digit * dividend;
        dividend /= 10;
      }
      return bytes;
    } else if (value < 0) {
      long remainder = Math.abs((long) value);
      final int length = (int) Math.log10((double) remainder);
      bytes = new byte[length + 2];
      long dividend = (int) Math.pow((double) 10, (double) length);
      int digit = 0;
      for (int i = length; i >= 0; i--) {
        digit = (byte) (remainder / dividend);
        bytes[length + 1 - i] = (byte) (digit + ASCII_OFFSET);
        remainder -= digit * dividend;
        dividend /= 10;
      }
      bytes[0] = (byte) ASCII_MINUS;
      return bytes;
    } else {
      bytes = new byte[1];
      bytes[0] = (byte) ASCII_OFFSET;
      return bytes;
    }
  }

  public static final byte[] getBytes(final String string) {
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

  public static final boolean equals(final byte[] value1, final byte[] value2) {
    // Fail if one is null.
    if ((value1 == null) || (value2 == null)) {
      return false;
    }
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
    return equals(value1, UTF.getBytes(value2));
  }

  public static final boolean equals(final String value1, final byte[] value2) {
    return equals(UTF.getBytes(value1), value2);
  }

  public static final boolean equals(final String value1, final String value2) {
    return equals(UTF.getBytes(value1), UTF.getBytes(value2));
  }

}
