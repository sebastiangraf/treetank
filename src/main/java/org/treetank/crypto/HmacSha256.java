/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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

  /**
   * Calculate SHA-256 for a given message.
   * 
   * @param message Message to calculate SHA-256 digest for.
   * @param offset Offset to start digest calculation with.
   * @param length Length of message to digest in bytes.
   * @param digest The resulting 32[B] digest is stored here.
   * @throws IllegalArgumentException if message is null or its length
   * is not a multiple of four or either the offset or length are negative
   * or the length exceeds the message array or digest is null or its length is
   * not 32.
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
    for (i = 0; i < 8; i++) {
      h[i] = Sha256.H[i];
    }

    // --- Main Loop -----------------------------------------------------------
    // Break message into 64[B] chunks for processing.
    for (i = 0, l = length >> 6; i < l; i++) {
      // Break 64[B] chunk into 16 4[B] words for processing.
      for (j = 0; j < 16; j++) {
        w[j] = Sha256.buildWord(message, o);
        o += 4;
      }
      // Update digest.
      Sha256.digest(h, w);
      r -= 64;
    }

    // --- Padding -------------------------------------------------------------
    // Break last 64[B] chunk into remaining 4[B] words for processing.
    for (j = 0, l = r >> 2; j < l; j++) {
      w[j] = Sha256.buildWord(message, o);
      o += 4;
    }
    // Add one bit followed by seven zero bits.
    w[j++] = 0x80000000;
    // Add a second padding block if the first is full.
    if (r >= 56) {
      for (; j < 15; j++) {
        w[j] = 0;
      }
      Sha256.digest(h, w);
      j = 0;
    }
    // Add zeroes to padding.
    for (; j < 15; j++) {
      w[j] = 0;
    }
    // Add the number of bytes contained in the message.
    w[j] = (int) (length << 3);
    // Update digest.
    Sha256.digest(h, w);

    // --- Teardown ------------------------------------------------------------
    // Build final digest.
    Sha256.buildDigest(h, digest);

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

    // --- Setup ---------------------------------------------------------------
    // Check arguments.
    checkKey(key);
    checkMessage(message, offset, length);
    checkDigest(digest);

    // Prepare round variables.
    final int[] h1 = new int[8];
    final int[] h2 = new int[8];
    final int[] w = new int[64];
    int o = offset;
    int r = length;
    int i = 0;
    int j = 0;
    int l = 0;

    // Load initial round vector.
    for (i = 0; i < 8; i++) {
      h1[i] = Sha256.H[i];
      h2[i] = Sha256.H[i];
    }

    // --- IPAD of Inner Hash --------------------------------------------------
    // Break 64[B] IPAD into 16 4[B] words for processing.
    for (j = 0, o = 0; j < 8; j++) {
      w[j] =
          ((byte) (0x36 ^ key[o++])) << 24
              | ((byte) (0x36 ^ key[o++]) & 0xFF) << 16
              | ((byte) (0x36 ^ key[o++]) & 0xFF) << 8
              | ((byte) (0x36 ^ key[o++]) & 0xFF);
      w[j + 8] = 0x36363636;
    }
    // Update digest.
    Sha256.digest(h1, w);

    // --- Main Loop of Inner Hash ---------------------------------------------
    // Break message into 64[B] chunks for processing.
    for (i = 0, o = 0, l = length >> 6; i < l; i++) {
      // Break 64[B] chunk into 16 4[B] words for processing.
      for (j = 0; j < 16; j++) {
        w[j] = Sha256.buildWord(message, o);
        o += 4;
      }
      // Update digest.
      Sha256.digest(h1, w);
      r -= 64;
    }

    // --- Padding of Inner Hash -----------------------------------------------
    // Break last 64[B] chunk into remaining 4[B] words for processing.
    for (j = 0, l = r >> 2; j < l; j++) {
      w[j] = Sha256.buildWord(message, o);
      o += 4;
    }
    // Add one bit followed by seven zero bits.
    w[j++] = 0x80000000;
    // Add a second padding block if the first is full.
    if (r >= 56) {
      for (; j < 15; j++) {
        w[j] = 0;
      }
      Sha256.digest(h1, w);
      j = 0;
    }
    // Add zeroes to padding.
    for (; j < 15; j++) {
      w[j] = 0;
    }
    // Add the number of bytes contained in the message.
    w[j] = (int) ((length + 64) << 3);
    // Update digest.
    Sha256.digest(h1, w);

    // --- OPAD of Outer Hash --------------------------------------------------
    // Break 64[B] OPAD into 16 4[B] words for processing.
    for (j = 0, o = 0; j < 8; j++) {
      w[j] =
          ((byte) (0x5c ^ key[o++])) << 24
              | ((byte) (0x5c ^ key[o++]) & 0xFF) << 16
              | ((byte) (0x5c ^ key[o++]) & 0xFF) << 8
              | ((byte) (0x5c ^ key[o++]) & 0xFF);
      w[j + 8] = 0x5c5c5c5c;
    }
    // Update digest.
    Sha256.digest(h2, w);

    // --- Padding of Outer Hash -----------------------------------------------
    // Break last 64[B] chunk into remaining 4[B] words for processing.
    for (j = 0; j < 8; j++) {
      w[j] = h1[j];
    }
    // Add one bit followed by seven zero bits.
    w[j++] = 0x80000000;
    // Add zeroes to padding.
    for (; j < 15; j++) {
      w[j] = 0;
    }
    // Add the number of bytes contained in the message.
    w[j] = (int) (96 << 3);
    // Update digest.
    Sha256.digest(h2, w);

    // --- Teardown of Inner Hash ----------------------------------------------
    // Build intermediate digest.
    Sha256.buildDigest(h2, digest);

  }

  /**
   * Stretch and salt given key to generate master key.
   * 
   * @param key
   * @param salt
   * @param r
   * @param masterKey
   */
  public final void stretch(
      final byte[] key,
      final byte[] salt,
      final int r,
      final byte[] masterKey) {

    // --- Setup ---------------------------------------------------------------
    // Check arguments.
    checkKey(key);
    checkSalt(salt);

    // Prepare round variables.
    final int[] internalKey = new int[8];
    final int[] internalSalt = new int[8];
    final int[] h = new int[8];
    final int[] w = new int[64];

    int i, j;

    for (i = 0, j = 0; i < 8; i++) {
      internalKey[i] = Sha256.buildWord(key, j);
      internalSalt[i] = Sha256.buildWord(salt, j);
      j += 4;
    }

    // Run r rounds.
    for (i = 0; i < r; i++) {

      // --- First Block -------------------------------------------------------
      for (j = 0; j < 8; j++) {
        w[j] = h[j];
        w[j + 8] = internalKey[j];
        h[j] = Sha256.H[j];
      }
      Sha256.digest(h, w);

      // --- Padding -----------------------------------------------------------
      for (j = 0; j < 8; j++) {
        w[j] = internalSalt[j];
      }
      w[8] = 0x80000000;
      w[9] = 0x00000000;
      w[10] = 0x00000000;
      w[11] = 0x00000000;
      w[12] = 0x00000000;
      w[13] = 0x00000000;
      w[14] = 0x00000000;
      w[15] = 0x00000300;
      Sha256.digest(h, w);

    }

    // --- Teardown ------------------------------------------------------------
    Sha256.buildDigest(h, masterKey);

  }

  /**
   * Make sure salt complies with specification.
   * 
   * @param salt Salt to check.
   * @throws IllegalArgumentException if salt is null or its length is not
   * 32.
   */
  private final void checkSalt(final byte[] salt) {
    if (salt == null || (salt.length != 32)) {
      throw new IllegalArgumentException("Salt="
          + salt
          + " does not comply with specification.");
    }
  }

  /**
   * Make sure key complies with specification.
   * 
   * @param key Key to check.
   * @throws IllegalArgumentException if key is null or its length is not
   * 32.
   */
  private final void checkKey(final byte[] key) {
    if (key == null || (key.length != 32)) {
      throw new IllegalArgumentException("Key="
          + key
          + " does not comply with specification.");
    }
  }

  /**
   * Make sure message complies with specification.
   * 
   * @param message Message to check.
   * @param offset Offset to check.
   * @param length Length to check.
   * @throws IllegalArgumentException if message is null or its length
   * is not a multiple of four or either the offset or length are negative
   * or the length exceeds the message array.
   */
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

  /**
   * Make sure the digest complies with the specification.
   * 
   * @param digest Digest to check.
   * @throws IllegalArgumentException if digest is null or its length is not
   * 32.
   */
  private final void checkDigest(final byte[] digest) {
    if (digest == null || (digest.length != 32)) {
      throw new IllegalArgumentException("Digest="
          + digest
          + " does not comply with specification.");
    }
  }

}
