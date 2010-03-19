/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: TypedValue.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.utils;

/**
 * <h1>UTF</h1>
 * 
 * <p>
 * Util to efficiently convert byte arrays to various Java types and vice versa.
 * It also provides efficient comparison and hash methods.
 * </p>
 */
public final class TypedValue {

    /** Empty string. */
    public static final byte[] EMPTY = new byte[0];

    /**
     * Hidden constructor.
     */
    private TypedValue() {
        // Hidden.
    }

    // /**
    // * Atomize the byte[] according to its type. This will always yield a
    // * non-null string (even if the bytes are null).
    // *
    // * @param valueType
    // * Type of value.
    // * @param bytes
    // * Byte array of value.
    // * @return Atomized value as string.
    // */
    // public static String atomize(final int valueType, final byte[] bytes) {
    // if (bytes == null) {
    // return "";
    // }
    // switch (valueType) {
    // case IReadTransaction.INT_TYPE:
    // return Integer.toString(parseInt(bytes));
    // case IReadTransaction.LONG_TYPE:
    // return Long.toString(parseLong(bytes));
    // case IReadTransaction.BOOLEAN_TYPE:
    // return Boolean.toString(parseBoolean(bytes));
    // default:
    // return parseString(bytes);
    // }
    // }

    /**
     * Parse string from given UTF-8 byte array.
     * 
     * @param bytes
     *            Byte array to parse string from.
     * @return String.
     */
    public static String parseString(final byte[] bytes) {
        try {

            final String intermediateString = new String(bytes,
                    IConstants.DEFAULT_ENCODING);
            final StringBuilder builder = new StringBuilder();
            int andCounter = 0;
            int altCounter = 0;
            for (char c : intermediateString.toCharArray()) {
                switch (c) {
                case '&':
                    andCounter++;
                    altCounter++;
                    break;
                case 'a':
                    if (andCounter == 1) {
                        andCounter++;
                    } else {
                        if (andCounter == 2) {
                            builder.append('&').append('a').append(c);
                        } else {
                            builder.append(c);
                        }
                        andCounter = 0;
                    }
                    altCounter = 0;
                    break;
                case 'm':
                    if (andCounter == 2) {
                        andCounter++;
                    } else {
                        if (andCounter == 3) {
                            builder.append('&').append('a').append('m').append(
                                    c);
                        } else if (andCounter == 1) {
                            builder.append('&').append(c);
                        } else {
                            builder.append(c);
                        }
                        andCounter = 0;
                    }
                    altCounter = 0;
                    break;
                case 'p':
                    if (andCounter == 3) {
                        andCounter++;
                    } else {
                        if (andCounter == 1) {
                            builder.append('&').append(c);
                        } else if (andCounter == 2) {
                            builder.append('&').append('a').append(c);
                        } else if (andCounter == 4) {
                            builder.append('&').append('a').append('m').append(
                                    'p').append(c);
                        } else {
                            builder.append(c);
                        }
                        andCounter = 0;
                    }
                    altCounter = 0;
                    break;
                case 'l':
                    if (altCounter == 1) {
                        altCounter++;
                    } else {
                        if (altCounter == 2) {
                            builder.append('&').append('l').append(c);
                        } else {
                            builder.append(c);
                        }
                        altCounter = 0;
                    }
                    andCounter = 0;
                    break;
                case 't':
                    if (altCounter == 2) {
                        altCounter++;
                    } else {
                        if (altCounter == 3) {
                            builder.append('&').append('l').append('t').append(
                                    c);
                        } else if (altCounter == 1) {
                            builder.append('&').append(c);
                        } else {
                            builder.append(c);
                        }
                        altCounter = 0;
                    }
                    andCounter = 0;
                    break;
                case ';':
                    if (andCounter == 4) {
                        builder.append('&');
                    } else if (andCounter > 0) {
                        if (andCounter == 3) {
                            builder.append('&').append('a').append('m').append(
                                    c);
                        } else if (andCounter == 2) {
                            builder.append('&').append('a').append(c);
                        } else {
                            builder.append('&').append(c);
                        }
                    } else if (altCounter == 3) {
                        builder.append('<');
                    } else {
                        if (altCounter == 2) {
                            builder.append('&').append('l').append(c);
                        } else if (altCounter == 1) {
                            builder.append('&').append(c);
                        } else {
                            builder.append(c);
                        }
                    }

                    altCounter = 0;
                    andCounter = 0;
                    break;
                default:
                    builder.append(c);
                }
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not convert byte[] to String: "
                    + e.getLocalizedMessage());
        }

    }

    /**
     * Parse boolean from given UTF-8 byte array.
     * 
     * @param bytes
     *            Byte array to parse int from.
     * @return Boolean.
     */
    public static boolean parseBoolean(final byte[] bytes) {
        return (bytes[0] == 1);
    }

    /**
     * Parse int from given UTF-8 byte array.
     * 
     * @param bytes
     *            Byte array to parse int from.
     * @return Int.
     */
    public static int parseInt(final byte[] bytes) {
        int position = 0;
        int value = ((bytes[position++] & 127));
        if ((bytes[position - 1] & 128) != 0) {
            value |= ((bytes[position++] & 127)) << 7;
            if ((bytes[position - 1] & 128) != 0) {
                value |= ((bytes[position++] & 127)) << 14;
                if ((bytes[position - 1] & 128) != 0) {
                    value |= ((bytes[position++] & 127)) << 21;
                    if ((bytes[position - 1] & 128) != 0) {
                        value |= ((bytes[position++] & 255)) << 28;
                    } else if ((bytes[position - 1] & 64) != 0)
                        value |= 0xF0000000;
                } else if ((bytes[position - 1] & 64) != 0)
                    value |= 0xFFF00000;
            } else if ((bytes[position - 1] & 64) != 0)
                value |= 0xFFFFE000;
        } else if ((bytes[position - 1] & 64) != 0)
            value |= 0xFFFFFFC0;
        return value;
    }

    /**
     * Parse long from given UTF-8 byte array.
     * 
     * @param bytes
     *            Byte array to parse long from.
     * @return Long.
     */
    public static long parseLong(final byte[] bytes) {
        int position = 1;
        long value = (long) (bytes[position++] & 255);
        if (bytes[position - 2] > 1) {
            value += ((long) (bytes[position++] & 255) << 8);
            if (bytes[position - 3] > 2) {
                value += ((long) (bytes[position++] & 255) << 16);
                if (bytes[position - 4] > 3) {
                    value += ((long) (bytes[position++] & 255) << 24);
                    if (bytes[position - 5] > 4) {
                        value += ((long) (bytes[position++] & 255) << 32);
                        if (bytes[position - 6] > 5) {
                            value += ((long) (bytes[position++] & 255) << 40);
                            if (bytes[position - 7] > 6) {
                                value += ((long) (bytes[position++] & 255) << 48);
                                if (bytes[position - 8] > 7) {
                                    value += ((long) bytes[position++] << 56);
                                } else if ((bytes[position - 1] & 128) != 0)
                                    value |= 0xFF000000000000L;
                            } else if ((bytes[position - 1] & 128) != 0)
                                value |= 0xFFFF000000000000L;
                        } else if ((bytes[position - 1] & 128) != 0)
                            value |= 0xFFFFFF0000000000L;
                    } else if ((bytes[position - 1] & 128) != 0)
                        value |= 0xFFFFFFFF00000000L;
                } else if ((bytes[position - 1] & 128) != 0)
                    value |= 0xFFFFFFFFFF000000L;
            } else if ((bytes[position - 1] & 128) != 0)
                value |= 0xFFFFFFFFFFFF0000L;
        } else if ((bytes[position - 1] & 128) != 0)
            value |= 0xFFFFFFFFFFFFFF00L;
        return value;
    }

    /**
     * Get UTF-8 byte array from int. The given byte array yields a string
     * representation if read with parseString().
     * 
     * @param value
     *            Int to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of int.
     */
    public static byte[] getBytes(final boolean value) {
        final byte[] bytes = new byte[1];
        if (value) {
            bytes[0] = 1;
        } else {
            bytes[0] = 0;
        }
        return bytes;
    }

    /**
     * Get UTF-8 byte array from int. The given byte array yields a string
     * representation if read with parseString().
     * 
     * @param value
     *            Int to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of int.
     */
    public static byte[] getBytes(final int value) {
        final byte[] tmpBytes = new byte[5];
        int position = 0;
        tmpBytes[position++] = (byte) (value);
        if (value > 63 || value < -64) {
            tmpBytes[position - 1] |= 128;
            tmpBytes[position++] = (byte) (value >> 7);
            if (value > 8191 || value < -8192) {
                tmpBytes[position - 1] |= 128;
                tmpBytes[position++] = (byte) (value >> 14);
                if (value > 1048575 || value < -1048576) {
                    tmpBytes[position - 1] |= 128;
                    tmpBytes[position++] = (byte) (value >> 21);
                    if (value > 268435455 || value < -268435456) {
                        tmpBytes[position - 1] |= 128;
                        tmpBytes[position++] = (byte) (value >> 28);
                    } else
                        tmpBytes[position - 1] &= 127;
                } else
                    tmpBytes[position - 1] &= 127;
            } else
                tmpBytes[position - 1] &= 127;
        } else
            tmpBytes[position - 1] &= 127;
        final byte[] bytes = new byte[position];
        System.arraycopy(tmpBytes, 0, bytes, 0, position);
        return bytes;
    }

    /**
     * Get UTF-8 byte array from long. The given byte array yields a string
     * representation if read with parseString().
     * 
     * @param value
     *            Long to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of long.
     */
    public static byte[] getBytes(final long value) {
        final byte[] tmpBytes = new byte[9];
        int position = 1;
        tmpBytes[position++] = (byte) value;
        if (value > 127 || value < -128) {
            tmpBytes[position++] = (byte) (value >> 8);
            if (value > 32767 || value < -32768) {
                tmpBytes[position++] = (byte) (value >>> 16);
                if (value > 8388607 || value < -8388608) {
                    tmpBytes[position++] = (byte) (value >>> 24);
                    if (value > 2147483647 || value < -2147483648) {
                        tmpBytes[position++] = (byte) (value >>> 32);
                        if (value > (2 ^ 39) - 1 || value < -(2 ^ 39)) {
                            tmpBytes[position++] = (byte) (value >>> 40);
                            if (value > (2 ^ 47) - 1 || value < -(2 ^ 47)) {
                                tmpBytes[position++] = (byte) (value >>> 48);
                                if (value > (2 ^ 55) - 1 || value < -(2 ^ 55)) {
                                    tmpBytes[position++] = (byte) (value >>> 56);
                                    tmpBytes[position - 9] = (byte) 8;
                                } else
                                    tmpBytes[position - 8] = (byte) 7;
                            } else
                                tmpBytes[position - 7] = (byte) 6;
                        } else
                            tmpBytes[position - 6] = (byte) 5;
                    } else
                        tmpBytes[position - 5] = (byte) 4;
                } else
                    tmpBytes[position - 4] = (byte) 3;
            } else
                tmpBytes[position - 3] = (byte) 2;
        } else
            tmpBytes[position - 2] = (byte) 1;
        final byte[] bytes = new byte[position];
        System.arraycopy(tmpBytes, 0, bytes, 0, position);
        return bytes;
    }

    /**
     * Get UTF-8 byte array from string. The given byte array yields a int if
     * read with parseInt().
     * 
     * @param value
     *            String to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of string.
     */
    public static byte[] getBytes(final String value) {
        byte[] bytes = null;
        try {
            if (value == null || value.length() == 0) {
                bytes = EMPTY;
            } else {
                final StringBuilder builder = new StringBuilder();
                for (int i = 0; i < value.length(); i++) {
                    switch (value.charAt(i)) {
                    case '&':
                        builder.append("&amp;");
                        break;
                    case '<':
                        builder.append("&lt;");
                        break;
                    default:
                        builder.append(value.charAt(i));
                    }
                }
                bytes = builder.toString()
                        .getBytes(IConstants.DEFAULT_ENCODING);

                // bytes = value.replace("&", "&amp;").replace("<", "&lt;")
                // .getBytes(IConstants.DEFAULT_ENCODING);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not convert String to byte[]: "
                    + e.getLocalizedMessage());
        }
        return bytes;
    }

    public static boolean equals(final byte[] value1, final byte[] value2) {
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

    public static boolean equals(final byte[] value1, final String value2) {
        return equals(value1, TypedValue.getBytes(value2));
    }

    public static boolean equals(final String value1, final byte[] value2) {
        return equals(TypedValue.getBytes(value1), value2);
    }

    public static boolean equals(final String value1, final String value2) {
        return equals(TypedValue.getBytes(value1), TypedValue.getBytes(value2));
    }

    /**
     * Get UTF-8 byte array from double. The given byte array yields a double if
     * read with parseDouble().
     * 
     * @param value
     *            double value to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of double.
     */
    public static byte[] getBytes(final Double value) {
        return value.toString().getBytes();
    }

    /**
     * Get UTF-8 byte array from float. The given byte array yields a float if
     * read with parseFloat().
     * 
     * @param value
     *            float to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of float.
     */
    public static byte[] getBytes(final Float value) {
        return value.toString().getBytes();
    }

    /**
     * Parse double from given UTF-8 byte array.
     * 
     * @param value
     *            Byte array to parse double from.
     * @return double.
     */
    public static double parseDouble(final byte[] value) {
        return Double.parseDouble(parseString(value));
    }

    /**
     * Parse float from given UTF-8 byte array.
     * 
     * @param value
     *            Byte array to parse float from.
     * @return float.
     */
    public static float parseFloat(final byte[] value) {
        return Float.parseFloat(parseString(value));
    }

}
