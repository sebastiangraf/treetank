/*
 * Copyright (c) 2007, Marc Kramis
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.utils;


/**
 * <h1>UTF</h1>
 * 
 * <p>
 * Util to efficiently convert byte arrays to various Java types and vice versa.
 * It also provides efficient comparison and hash methods.
 * </p>
 */
public final class UTF {

  /** Empty string. */
  public static final byte[] EMPTY = new byte[0];

  /** Offset that must be added to digit to make it ASCII. */
  private static final int ASCII_OFFSET = 48;

  /** ASCII character minus ('-'). */
  private static final int ASCII_MINUS = 45;

  /** Precalculated powers of each available int digit. */
  private static final int[] INT_POWERS =
      {
          1,
          10,
          100,
          1000,
          10000,
          100000,
          1000000,
          10000000,
          100000000,
          1000000000 };

  /** Precalculated powers of each available long digit. */
  private static final long[] LONG_POWERS =
      {
          1L,
          10L,
          100L,
          1000L,
          10000L,
          100000L,
          1000000L,
          10000000L,
          100000000L,
          1000000000L,
          10000000000L,
          100000000000L,
          1000000000000L,
          10000000000000L,
          100000000000000L,
          1000000000000000L,
          10000000000000000L,
          100000000000000000L,
          1000000000000000000L };

  /**
   * Hidden constructor.
   */
  private UTF() {
    // Hidden.
  }

  /**
   * Parse string from given UTF-8 byte array.
   * 
   * @param bytes Byte array to parse string from.
   * @return String.
   */
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

  /**
   * Parse int from given UTF-8 byte array.
   * 
   * @param bytes Byte array to parse int from.
   * @return Int.
   */
  public static final int parseInt(final byte[] bytes) {
    final int length = bytes.length - 1;
    int value = 0;
    //int power = 1;
    if (bytes[0] == (byte) ASCII_MINUS) {
      for (int i = length; i > 0; i--) {
        value += (bytes[i] - ASCII_OFFSET) * INT_POWERS[length - i];
        //power *= 10;
      }
      return value *= -1;
    } else {
      for (int i = length; i >= 0; i--) {
        value += (bytes[i] - ASCII_OFFSET) * INT_POWERS[length - i];
      }
      return value;
    }
  }

  /**
   * Parse long from given UTF-8 byte array.
   * 
   * @param bytes Byte array to parse long from.
   * @return Long.
   */
  public static final long parseLong(final byte[] bytes) {
    final int length = bytes.length - 1;
    long value = 0L;
    if (bytes[0] == (byte) ASCII_MINUS) {
      for (int i = length; i > 0; i--) {
        value += (bytes[i] - ASCII_OFFSET) * LONG_POWERS[length - i];
      }
      return value *= -1;
    } else {
      for (int i = length; i >= 0; i--) {
        value += (bytes[i] - ASCII_OFFSET) * LONG_POWERS[length - i];
      }
      return value;
    }
  }

  /**
   * Get UTF-8 byte array from int. The given byte array yields a string
   * representation if read with parseString().
   * 
   * @param value Int to encode as UTF-8 byte array.
   * @return UTF-8-encoded byte array of int.
   */
  public static final byte[] getBytes(final int value) {
    byte[] bytes = null;
    if (value > 0) {
      int remainder = value;
      final int length = (int) Math.log10((double) remainder);
      bytes = new byte[length + 1];
      int digit = 0;
      for (int i = length; i >= 0; i--) {
        digit = (byte) (remainder / INT_POWERS[i]);
        bytes[length - i] = (byte) (digit + ASCII_OFFSET);
        remainder -= digit * INT_POWERS[i];
      }
      return bytes;
    } else if (value < 0) {
      if (value == Integer.MIN_VALUE) {
        throw new IllegalArgumentException("Integer.MIN_VALUE not supported.");
      }
      int remainder = Math.abs(value);
      final int length = (int) Math.log10((double) remainder);
      bytes = new byte[length + 2];
      int digit = 0;
      for (int i = length; i >= 0; i--) {
        digit = (byte) (remainder / INT_POWERS[i]);
        bytes[length + 1 - i] = (byte) (digit + ASCII_OFFSET);
        remainder -= digit * INT_POWERS[i];
      }
      bytes[0] = (byte) ASCII_MINUS;
      return bytes;
    } else {
      bytes = new byte[1];
      bytes[0] = (byte) ASCII_OFFSET;
      return bytes;
    }
  }

  /**
   * Get UTF-8 byte array from long. The given byte array yields a string
   * representation if read with parseString().
   * 
   * @param value Long to encode as UTF-8 byte array.
   * @return UTF-8-encoded byte array of long.
   */
  public static final byte[] getBytes(final long value) {
    byte[] bytes = null;
    if (value > 0) {
      long remainder = value;
      final int length = (int) Math.log10((double) remainder);
      bytes = new byte[length + 1];
      int digit = 0;
      for (int i = length; i >= 0; i--) {
        digit = (byte) (remainder / LONG_POWERS[i]);
        bytes[length - i] = (byte) (digit + ASCII_OFFSET);
        remainder -= digit * LONG_POWERS[i];
      }
      return bytes;
    } else if (value < 0) {
      if (value == Long.MIN_VALUE) {
        throw new IllegalArgumentException("Long.MIN_VALUE not supported.");
      }
      long remainder = Math.abs(value);
      final int length = (int) Math.log10((double) remainder);
      bytes = new byte[length + 2];
      int digit = 0;
      for (int i = length; i >= 0; i--) {
        digit = (byte) (remainder / LONG_POWERS[i]);
        bytes[length + 1 - i] = (byte) (digit + ASCII_OFFSET);
        remainder -= digit * LONG_POWERS[i];
      }
      bytes[0] = (byte) ASCII_MINUS;
      return bytes;
    } else {
      bytes = new byte[1];
      bytes[0] = (byte) ASCII_OFFSET;
      return bytes;
    }
  }

  /**
   * Get UTF-8 byte array from string. The given byte array yields a int
   * if read with parseInt().
   * 
   * @param value String to encode as UTF-8 byte array.
   * @return UTF-8-encoded byte array of string.
   */
  public static final byte[] getBytes(final String value) {
    byte[] bytes = null;
    try {
      if (value == null || value.length() == 0) {
        bytes = EMPTY;
      } else {
        bytes = value.getBytes(IConstants.DEFAULT_ENCODING);
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
