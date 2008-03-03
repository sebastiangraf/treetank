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

  /**
   * Test parameter checks with SHA-256.
   */
  @Test
  public void testSha256Parameter() {
    final HmacSha256 hmac = new HmacSha256();
    final byte[] message = new byte[64];
    final byte[] digest = new byte[32];

    try {
      hmac.digest(null, 0, 64, digest);
      Assert.fail("Must not accept null message.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      hmac.digest(message, -33, 64, digest);
      Assert.fail("Must not accept negative offsets.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      hmac.digest(message, 0, -33, digest);
      Assert.fail("Must not accept negative lengths.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      hmac.digest(message, 0, 33, digest);
      Assert.fail("Must not accept lengths not being a multiple of 4.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      hmac.digest(message, 0, 333, digest);
      Assert.fail("Must not accept lengths exceeding the message size.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      hmac.digest(message, 0, 64, null);
      Assert.fail("Must not accept null digest.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

    try {
      hmac.digest(message, 0, 64, new byte[33]);
      Assert.fail("Must not accept digest with a size not equal to 32.");
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }

  }

  /**
   * Test SHA-256 implementation. Test vectors applied to an empty array with
   * length being a multiple of 4. All test vectors verified with
   * https://www.cosic.esat.kuleuven.be/nessie/.
   */
  @Test
  public void testSha256() {
    final HmacSha256 hmac = new HmacSha256();
    final byte[] message = new byte[64];
    final byte[] digest = new byte[32];

    hmac.digest(message, 0, 0, digest);
    Assert.assertEquals(
        "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855",
        toHexString(digest));

    hmac.digest(message, 0, 4, digest);
    Assert.assertEquals(
        "DF3F619804A92FDB4057192DC43DD748EA778ADC52BC498CE80524C014B81119",
        toHexString(digest));

    hmac.digest(message, 0, 8, digest);
    Assert.assertEquals(
        "AF5570F5A1810B7AF78CAF4BC70A660F0DF51E42BAF91D4DE5B2328DE0E83DFC",
        toHexString(digest));

    hmac.digest(message, 0, 12, digest);
    Assert.assertEquals(
        "15EC7BF0B50732B49F8228E07D24365338F9E3AB994B00AF08E5A3BFFE55FD8B",
        toHexString(digest));

    hmac.digest(message, 0, 16, digest);
    Assert.assertEquals(
        "374708FFF7719DD5979EC875D56CD2286F6D3CF7EC317A3B25632AAB28EC37BB",
        toHexString(digest));

    hmac.digest(message, 0, 20, digest);
    Assert.assertEquals(
        "DE47C9B27EB8D300DBB5F2C353E632C393262CF06340C4FA7F1B40C4CBD36F90",
        toHexString(digest));

    hmac.digest(message, 0, 24, digest);
    Assert.assertEquals(
        "9D908ECFB6B256DEF8B49A7C504E6C889C4B0E41FE6CE3E01863DD7B61A20AA0",
        toHexString(digest));

    hmac.digest(message, 0, 28, digest);
    Assert.assertEquals(
        "3ADDFB141CD7C9C4C6543A82191A3707AC29C7A041217782E61D4D91C691AEE8",
        toHexString(digest));

    hmac.digest(message, 0, 32, digest);
    Assert.assertEquals(
        "66687AADF862BD776C8FC18B8E9F8E20089714856EE233B3902A591D0D5F2925",
        toHexString(digest));

    hmac.digest(message, 0, 36, digest);
    Assert.assertEquals(
        "6DB65FD59FD356F6729140571B5BCD6BB3B83492A16E1BF0A3884442FC3C8A0E",
        toHexString(digest));

    hmac.digest(message, 0, 40, digest);
    Assert.assertEquals(
        "2C34CE1DF23B838C5ABF2A7F6437CCA3D3067ED509FF25F11DF6B11B582B51EB",
        toHexString(digest));

    hmac.digest(message, 0, 44, digest);
    Assert.assertEquals(
        "85759B3811FF7DC47B03792AC85317BE51431A3F9E01DCAFCE317ED736A391B0",
        toHexString(digest));

    hmac.digest(message, 0, 48, digest);
    Assert.assertEquals(
        "17B0761F87B081D5CF10757CCC89F12BE355C70E2E29DF288B65B30710DCBCD1",
        toHexString(digest));

    hmac.digest(message, 0, 52, digest);
    Assert.assertEquals(
        "7955CB2DE90DD9EFC6DF9FDBF5F5D10C114F4135A9A6B52DB1003BE749E32F7A",
        toHexString(digest));

    hmac.digest(message, 0, 56, digest);
    Assert.assertEquals(
        "D4817AA5497628E7C77E6B606107042BBBA3130888C5F47A375E6179BE789FBB",
        toHexString(digest));

    hmac.digest(message, 0, 60, digest);
    Assert.assertEquals(
        "5DCC1B5872DD9FF1C234501F1FEFDA01F664164E1583C3E1BB3DBEA47588AB31",
        toHexString(digest));

    hmac.digest(message, 0, 64, digest);
    Assert.assertEquals(
        "F5A5FD42D16A20302798EF6ED309979B43003D2320D9F0E8EA9831A92759FB4B",
        toHexString(digest));
  }

  //  @Test
  //  public void testHmacSha256() {
  //    final HmacSha256 hmac = new HmacSha256();
  //    final byte[] key = new byte[32];
  //    final byte[] message = new byte[64];
  //    final byte[] digest = new byte[32];
  //
  //    hmac.digest(key, message, 0, 64, digest);
  //    Assert.assertEquals(
  //        "F5A5FD42D16A20302798EF6ED309979B43003D2320D9F0E8EA9831A92759FB4B",
  //        toHexString(digest));
  //  }

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
