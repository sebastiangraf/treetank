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
      h[i] = H[i];
    }

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
      for (; j < 15; j++) {
        w[j] = 0;
      }
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
      h1[i] = H[i];
      h2[i] = H[i];
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
    sha256(h1, w);

    // --- Main Loop of Inner Hash ---------------------------------------------
    // Break message into 64[B] chunks for processing.
    for (i = 0, o = 0, l = length >> 6; i < l; i++) {
      // Break 64[B] chunk into 16 4[B] words for processing.
      for (j = 0; j < 16; j++) {
        w[j] = loadWord(message, o);
        o += 4;
      }
      // Update digest.
      sha256(h1, w);
      r -= BLOCK_SIZE;
    }

    // --- Padding of Inner Hash -----------------------------------------------
    // Break last 64[B] chunk into remaining 4[B] words for processing.
    for (j = 0, l = r >> 2; j < l; j++) {
      w[j] = loadWord(message, o);
      o += 4;
    }
    // Add one bit followed by seven zero bits.
    w[j++] = 0x80000000;
    // Add a second padding block if the first is full.
    if (r >= 56) {
      for (; j < 15; j++) {
        w[j] = 0;
      }
      sha256(h1, w);
      j = 0;
    }
    // Add zeroes to padding.
    for (; j < 15; j++) {
      w[j] = 0;
    }
    // Add the number of bytes contained in the message.
    w[j] = (int) ((length + 64) << 3);
    // Update digest.
    sha256(h1, w);

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
    sha256(h2, w);

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
    sha256(h2, w);

    // --- Teardown of Inner Hash ----------------------------------------------
    // Build intermediate digest.
    buildDigest(h2, digest);

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
      internalKey[i] = loadWord(key, j);
      internalSalt[i] = loadWord(salt, j);
      j += 4;
    }

    // Run r rounds.
    for (i = 0; i < r; i++) {

      // --- First Block -------------------------------------------------------
      for (j = 0; j < 8; j++) {
        w[j] = h[j];
        w[j + 8] = internalKey[j];
        h[j] = H[j];
      }
      sha256(h, w);

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
      sha256(h, w);

    }

    // --- Teardown ------------------------------------------------------------
    buildDigest(h, masterKey);

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
    if (digest == null || (digest.length != DIGEST_SIZE)) {
      throw new IllegalArgumentException("Digest="
          + digest
          + " does not comply with specification.");
    }
  }

  /**
   * Store h in digest.
   * 
   * @param h Round vector after last round.
   * @param digest Final digest.
   */
  private final void buildDigest(final int[] h, final byte[] digest) {
    for (int i = 0, o = 0; i < 8; i++) {
      digest[o++] = (byte) (h[i] >>> 24);
      digest[o++] = (byte) (h[i] >>> 16);
      digest[o++] = (byte) (h[i] >>> 8);
      digest[o++] = (byte) h[i];
    }
  }

  /**
   * Load four bytes of message into one word.
   * 
   * @param message Message to read bytes from.
   * @param offset Offset to start reading.
   * @return Word containing four bytes of the message.
   */
  private final int loadWord(final byte[] message, final int offset) {
    return message[offset] << 24
        | (message[offset + 1] & 0xFF) << 16
        | (message[offset + 2] & 0xFF) << 8
        | (message[offset + 3] & 0xFF);
  }

  /**
   * Calculate hash for 64[B] message chunk.
   * 
   * @param hh Round vector before and after the round.
   * @param ww Words to calculate round for.
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

}
