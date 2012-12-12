/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.utils;

/**
 * <h1>UTF</h1>
 * 
 * <p>
 * Util to efficiently convert byte arrays to various Java types and vice versa. It also provides efficient
 * comparison and hash methods.
 * </p>
 */
public final class TypedValue {

    /**
     * Hidden constructor.
     */
    private TypedValue() {
        // Hidden.
    }

    /**
     * Get UTF-8 byte array from int. The given byte array yields a string
     * representation if read with parseString().
     * 
     * @param mValue
     *            Int to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of int.
     */
    public static byte[] getBytes(final int mValue) {
        final byte[] tmpBytes = new byte[5];
        int position = 0;
        tmpBytes[position++] = (byte)(mValue);
        if (mValue > 63 || mValue < -64) {
            tmpBytes[position - 1] |= 128;
            tmpBytes[position++] = (byte)(mValue >> 7);
            if (mValue > 8191 || mValue < -8192) {
                tmpBytes[position - 1] |= 128;
                tmpBytes[position++] = (byte)(mValue >> 14);
                if (mValue > 1048575 || mValue < -1048576) {
                    tmpBytes[position - 1] |= 128;
                    tmpBytes[position++] = (byte)(mValue >> 21);
                    if (mValue > 268435455 || mValue < -268435456) {
                        tmpBytes[position - 1] |= 128;
                        tmpBytes[position++] = (byte)(mValue >> 28);
                    } else {
                        tmpBytes[position - 1] &= 127;
                    }
                } else {
                    tmpBytes[position - 1] &= 127;
                }
            } else {
                tmpBytes[position - 1] &= 127;
            }
        } else {
            tmpBytes[position - 1] &= 127;
        }

        final byte[] bytes = new byte[position];
        System.arraycopy(tmpBytes, 0, bytes, 0, position);
        return bytes;
    }

    /**
     * Get UTF-8 byte array from string. The given byte array yields a int if
     * read with parseInt().
     * 
     * @param mValue
     *            String to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of string.
     */
    public static byte[] getBytes(final String mValue) {
        byte[] bytes = null;
        try {
            if (mValue == null || mValue.length() == 0) {
                bytes = new byte[0];
            } else {
                final StringBuilder builder = new StringBuilder();
                for (int i = 0; i < mValue.length(); i++) {
                    switch (mValue.charAt(i)) {
                    case '&':
                        builder.append("&amp;");
                        break;
                    case '<':
                        builder.append("&lt;");
                        break;
                    default:
                        builder.append(mValue.charAt(i));
                    }
                }
                bytes = builder.toString().getBytes("UTF-8");

                // bytes = value.replace("&", "&amp;").replace("<", "&lt;")
                // .getBytes(ContructorProps.DEFAULT_ENCODING);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Could not convert String to byte[]: " + e.getLocalizedMessage());
        }
        return bytes;
    }

    /**
     * Get UTF-8 byte array from double. The given byte array yields a double if
     * read with parseDouble().
     * 
     * @param mValue
     *            double value to encode as UTF-8 byte array.
     * @return UTF-8-encoded byte array of double.
     */
    public static byte[] getBytes(final Double mValue) {
        return mValue.toString().getBytes();
    }

}
