/*
 * Copyright (c) 2007, Marc Kramis, University of Konstanz
 * 
 * Patent Pending.
 * 
 * Permission to use, copy, modify, and/or distribute this software for non-
 * commercial use with or without fee is hereby granted, provided that the 
 * above copyright notice, the patent notice, and this permission notice
 * appear in all copies.
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

/**
 * <h1>SHA256</h1>
 * 
 * <p>
 * Calculate 32[B] HMAC-SHA-256 message digest for messages with a size in
 * bytes being a multiple of four. The key is 32[B]. The block size is 64[B].
 * </p>
 */
public final class HmacSha256 {

  /** Block size.*/
  private static final int BLOCK_SIZE = 64;

  /** Digest size. */
  private static final int DIGEST_SIZE = 32;

  /** Initial round vector H. */
  private static final int[] H =
      {
          0x6a09e667,
          0xbb67ae85,
          0x3c6ef372,
          0xa54ff53a,
          0x510e527f,
          0x9b05688c,
          0x1f83d9ab,
          0x5be0cd19 };

  /** Table of 64 4[B] round constants. */
  private static final int[] K =
      {
          0x428a2f98,
          0x71374491,
          0xb5c0fbcf,
          0xe9b5dba5,
          0x3956c25b,
          0x59f111f1,
          0x923f82a4,
          0xab1c5ed5,
          0xd807aa98,
          0x12835b01,
          0x243185be,
          0x550c7dc3,
          0x72be5d74,
          0x80deb1fe,
          0x9bdc06a7,
          0xc19bf174,
          0xe49b69c1,
          0xefbe4786,
          0x0fc19dc6,
          0x240ca1cc,
          0x2de92c6f,
          0x4a7484aa,
          0x5cb0a9dc,
          0x76f988da,
          0x983e5152,
          0xa831c66d,
          0xb00327c8,
          0xbf597fc7,
          0xc6e00bf3,
          0xd5a79147,
          0x06ca6351,
          0x14292967,
          0x27b70a85,
          0x2e1b2138,
          0x4d2c6dfc,
          0x53380d13,
          0x650a7354,
          0x766a0abb,
          0x81c2c92e,
          0x92722c85,
          0xa2bfe8a1,
          0xa81a664b,
          0xc24b8b70,
          0xc76c51a3,
          0xd192e819,
          0xd6990624,
          0xf40e3585,
          0x106aa070,
          0x19a4c116,
          0x1e376c08,
          0x2748774c,
          0x34b0bcb5,
          0x391c0cb3,
          0x4ed8aa4a,
          0x5b9cca4f,
          0x682e6ff3,
          0x748f82ee,
          0x78a5636f,
          0x84c87814,
          0x8cc70208,
          0x90befffa,
          0xa4506ceb,
          0xbef9a3f7,
          0xc67178f2 };

  /**
   * Calculate hash for 64[B] message chunk.
   * 
   * @param hh Input and output.
   * @param ww Input.
   */
  private final void sha256(final int[] hh, final int[] ww) {

    int a = hh[0];
    int b = hh[1];
    int c = hh[2];
    int d = hh[3];
    int e = hh[4];
    int f = hh[5];
    int g = hh[6];
    int h = hh[7];

    int t1;
    int t2;

    for (int r = 16; r < 64; r++) {
      t1 = ww[r - 2];
      t2 = ww[r - 15];
      ww[r] =
          (((t1 >>> 17) | (t1 << 15)) ^ ((t1 >>> 19) | (t1 << 13)) ^ (t1 >>> 10))
              + ww[r - 7]
              + (((t2 >>> 7) | (t2 << 25)) ^ ((t2 >>> 18) | (t2 << 14)) ^ (t2 >>> 3))
              + ww[r - 16];
    }

    for (int r = 0; r < 64; r++) {
      t1 =
          h
              + (((e >>> 6) | (e << 26)) ^ ((e >>> 11) | (e << 21)) ^ ((e >>> 25) | (e << 7)))
              + ((e & f) ^ (~e & g))
              + K[r]
              + ww[r];
      t2 =
          (((a >>> 2) | (a << 30)) ^ ((a >>> 13) | (a << 19)) ^ ((a >>> 22) | (a << 10)))
              + ((a & b) ^ (a & c) ^ (b & c));
      h = g;
      g = f;
      f = e;
      e = d + t1;
      d = c;
      c = b;
      b = a;
      a = t1 + t2;
    }

    hh[0] += a;
    hh[1] += b;
    hh[2] += c;
    hh[3] += d;
    hh[4] += e;
    hh[5] += f;
    hh[6] += g;
    hh[7] += h;

  }

  /**
   * Calculate SHA-256 for a given message.
   * 
   * @param message Message to calculate SHA-256 digest for. The message must
   * not be null.
   * @param offset Offset to start digest calculation with.
   * @param length Length of message to digest in bytes. 
   * Must be a multiple of four.
   * @param digest The resulting 32[B] digest is stored here. Must not be null
   * and 32[B].
   */
  public final void digest(
      final byte[] message,
      final int offset,
      final int length,
      final byte[] digest) {

    // --- Setup ---------------------------------------------------------------
    // Check arguments.
    checkMessage(message, offset, length);
    checkDigest(digest);

    // Prepare round variables.
    final int[] h = new int[8];
    final int[] w = new int[64];
    int o = offset;
    int r = length;
    int i = 0;
    int j = 0;
    int l = 0;

    // Load initial round vector.
    System.arraycopy(H, 0, h, 0, 8);

    // --- Main Loop -----------------------------------------------------------
    // Break message into 64[B] chunks for processing.
    for (i = 0, l = length >> 6; i < l; i++) {
      // Break 64[B] chunk into 16 4[B] words for processing.
      for (j = 0; j < 16; j++) {
        w[j] = loadWord(message, o);
        o += 4;
      }
      // Update digest.
      sha256(h, w);
      r -= BLOCK_SIZE;
    }

    // --- Padding -------------------------------------------------------------
    // Break last 64[B] chunk into remaining 4[B] words for processing.
    for (j = 0, l = r >> 2; j < l; j++) {
      w[j] = loadWord(message, o);
      o += 4;
    }
    // Add one bit followed by seven zero bits.
    w[j++] = 0x80000000;
    // Add a second padding block if the first is full.
    if (r >= 56) {
      sha256(h, w);
      j = 0;
    }
    // Add zeroes to padding.
    for (; j < 15; j++) {
      w[j] = 0;
    }
    // Add the number of bytes contained in the message.
    w[j] = (int) (length << 3);
    // Update digest.
    sha256(h, w);

    // --- Teardown ------------------------------------------------------------
    // Build final digest.
    buildDigest(h, digest);

  }

  /**
   * Calculate HMAC-SHA-256 for a given key and message.
   * 
   * @param key Key to calculate HMAC-SHA-256 digest for. The key must not be
   * null and of length 32[B].
   * @param message Message to calculate HMAC-SHA-256 digest for. The message
   * must not be null.
   * @param offset Offset to start digest calculation with.
   * @param length Length of message to digest in bytes. 
   * Must be a multiple of four.
   * @param digest The resulting 32[B] digest is stored here. Must not be null
   * and 32[B].
   */
  public final void digest(
      final byte[] key,
      final byte[] message,
      final int offset,
      final int length,
      final byte[] digest) {

    // Check arguments.
    checkKey(key);
    checkMessage(message, offset, length);
    checkDigest(digest);

    final byte[] ipad = new byte[64];
    final byte[] opad = new byte[64];

    // First half of 64[B] chunk.
    for (int i = 0; i < 32; i++) {
      ipad[i] = (byte) (0x36 ^ key[i]);
      opad[i] = (byte) (0x5c ^ key[i]);
    }

    // Second half of 64[B] chunk.
    for (int i = 32; i < 64; i++) {
      ipad[i] = (byte) (0x36);
      opad[i] = (byte) (0x5c);
    }

    //return hash(opad || hash(ipad || message))

    // Inner hash

    // Outer hash
  }

  private final void checkKey(final byte[] key) {
    if (key == null || (key.length != 32)) {
      throw new IllegalArgumentException("Key="
          + key
          + " does not comply with specification.");
    }
  }

  private final void checkMessage(
      final byte[] message,
      final int offset,
      final int length) {
    if ((message == null)
        || (offset < 0)
        || (length < 0)
        || ((length % 4) != 0)
        || (message.length < (offset + length))) {
      throw new IllegalArgumentException("Message="
          + message
          + ", offset="
          + offset
          + ", and length="
          + length
          + " does not comply with specification.");
    }
  }

  private final void checkDigest(final byte[] digest) {
    if (digest == null || (digest.length != DIGEST_SIZE)) {
      throw new IllegalArgumentException("Digest="
          + digest
          + " does not comply with specification.");
    }
  }

  private final void buildDigest(final int[] h, final byte[] digest) {
    for (int i = 0, o = 0; i < 8; i++) {
      digest[o++] = (byte) (h[i] >>> 24);
      digest[o++] = (byte) (h[i] >>> 16);
      digest[o++] = (byte) (h[i] >>> 8);
      digest[o++] = (byte) h[i];
    }
  }

  private final int loadWord(final byte[] message, final int offset) {
    return message[offset] << 24
        | (message[offset + 1] & 0xFF) << 16
        | (message[offset + 2] & 0xFF) << 8
        | (message[offset + 3] & 0xFF);
  }

}
