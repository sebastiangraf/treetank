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

public final class CtrAes256 {

  private final int[][] key;

  private final int[] nonce;

  private final byte[] streamKey;

  private long counter;

  public CtrAes256(final byte[] key, final byte[] nonce) {

    checkKey(key);
    checkNonce(nonce);

    this.key = Aes256.buildKey(key);
    this.nonce = Aes256.buildNonce(nonce);

    streamKey = new byte[16];
    counter = 0L;
  }

  public final void encrypt(
      final byte[] message,
      final int offset,
      final int length) {

    checkMessage(message, offset, length);

    for (int i = 0, l = length >> 4, o = offset, j; i < l; i++, o += 16) {
      counter += 1;
      Aes256.encrypt(key, nonce, counter, streamKey);
      for (j = 0; j < 16; j++) {
        message[j + o] = (byte) (message[j + o] ^ streamKey[j]);
      }
    }
  }

  /**
   * Make sure the key complies with the specification.
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
   * Make sure the nonce complies with the specification.
   * 
   * @param nonce Nonce to check.
   * @throws IllegalArgumentException if nonce is null or its length is not
   * 32.
   */
  private final void checkNonce(final byte[] nonce) {
    if (nonce == null || (nonce.length != 8)) {
      throw new IllegalArgumentException("Nonce="
          + nonce
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
   * is not a multiple of sixteen or either the offset or length are negative
   * or the length exceeds the message array.
   */
  private final void checkMessage(
      final byte[] message,
      final int offset,
      final int length) {
    if ((message == null)
        || (offset < 0)
        || (length < 0)
        || ((length % 16) != 0)
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

}
