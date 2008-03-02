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

package org.treetank.crypto;

import org.junit.Assert;
import org.junit.Test;

/**
 * <h1>HmacSha256Test</h1>
 * 
 * <p>
 * RFC 4231 - Identifiers and Test Vectors for 
 * HMAC-SHA-224, HMAC-SHA-256, HMAC-SHA-384, and HMAC-SHA-512.
 * </p>
 */
public class HmacSha256Test {

  /** Hexadecimal digits. */
  private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

  @Test
  public void testSha256() {
    final HmacSha256 hmac = new HmacSha256();
    final byte[] message = new byte[64];
    final byte[] digest = new byte[32];

    hmac.digest(message, 0, 64, digest);
    Assert.assertEquals(
        "F5A5FD42D16A20302798EF6ED309979B43003D2320D9F0E8EA9831A92759FB4B",
        toHexString(digest));
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

}
