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
 * $Id: AncestorAxisTest.java 3604 2007-11-27 09:13:42Z kramis $
 */

package org.treetank.crypto;

import org.junit.Assert;
import org.junit.Test;

/**
 * <h1>CtrAes256Test</h1>
 */
public class CtrAes256Test {

  /** Hexadecimal digits. */
  private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

  /**
   * Test parameter checks with HMAC-SHA-256.
   */
  @Test
  public void testCtrAes256Parameter() {

    final byte[] key = new byte[32];
    final byte[] message = new byte[64];
    final byte[] nonce = new byte[8];

    final CtrAes256 cipher = new CtrAes256(key, nonce);

    try {
      new CtrAes256(null, nonce);
      Assert.fail("Must not accept null key.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      new CtrAes256(key, null);
      Assert.fail("Must not accept null nonce.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      new CtrAes256(new byte[33], nonce);
      Assert.fail("Must not accept key not equal to 32[B] in length.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      new CtrAes256(key, new byte[33]);
      Assert.fail("Must not accept nonce not equal to 8[B] in length.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      cipher.encrypt(null, 0, 64);
      Assert.fail("Must not accept null message.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      cipher.encrypt(message, -33, 64);
      Assert.fail("Must not accept negative offsets.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      cipher.encrypt(message, 0, -33);
      Assert.fail("Must not accept negative lengths.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      cipher.encrypt(message, 0, 33);
      Assert.fail("Must not accept lengths not being a multiple of 16.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      cipher.encrypt(message, 0, 333);
      Assert.fail("Must not accept lengths exceeding the message size.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

  }

  @Test
  public void testCtrAes256SingleBlock() {
    final CtrAes256 cipher = new CtrAes256(new byte[32], new byte[8]);
    final byte[] message = new byte[16];
    cipher.encrypt(message, 0, 16);
    Assert.assertArrayEquals(
        toByteArray("530F8AFBC74536B9A963B4F1C4CB738B"),
        message);
  }

  @Test
  public void testCtrAes256DoubleBlock() {
    final CtrAes256 cipher = new CtrAes256(new byte[32], new byte[8]);
    final byte[] message = new byte[32];
    cipher.encrypt(message, 0, 32);
    Assert
        .assertArrayEquals(
            toByteArray("530F8AFBC74536B9A963B4F1C4CB738BCEA7403D4D606B6E074EC5D3BAF39D18"),
            message);
  }

  /**
   * Convert byte array to hexadecimal digits.
   * 
   * @param input Byte array.
   * @return String of hexadecimal digits.
   */
  private static final String toHexString(final byte[] input) {
    final char[] buffer = new char[input.length << 1];
    for (int i = 0, j = 0, k = 0, o = 0, l = input.length; i < l;) {
      k = input[o + i++];
      buffer[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
      buffer[j++] = HEX_DIGITS[k & 0x0F];
    }
    return new String(buffer);
  }

  /**
   * Convert hexadecimal digits to byte array.
   * 
   * @param input Hexadecimal string.
   * @return Byte array.
   */
  private static final byte[] toByteArray(final String input) {
    final String upper = input.toUpperCase();
    final byte[] buffer = new byte[input.length() >> 1];
    for (int i = 0; i < buffer.length; i++) {
      int d1 = Character.digit(upper.charAt(2 * i), 16);
      int d2 = Character.digit(upper.charAt(2 * i + 1), 16);
      buffer[i] = (byte) (d1 * 16 + d2);
    }
    return buffer;
  }

}
