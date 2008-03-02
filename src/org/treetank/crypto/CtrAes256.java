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

  private static final String SS =
      "\u637C\u777B\uF26B\u6FC5\u3001\u672B\uFED7\uAB76"
          + "\uCA82\uC97D\uFA59\u47F0\uADD4\uA2AF\u9CA4\u72C0"
          + "\uB7FD\u9326\u363F\uF7CC\u34A5\uE5F1\u71D8\u3115"
          + "\u04C7\u23C3\u1896\u059A\u0712\u80E2\uEB27\uB275"
          + "\u0983\u2C1A\u1B6E\u5AA0\u523B\uD6B3\u29E3\u2F84"
          + "\u53D1\u00ED\u20FC\uB15B\u6ACB\uBE39\u4A4C\u58CF"
          + "\uD0EF\uAAFB\u434D\u3385\u45F9\u027F\u503C\u9FA8"
          + "\u51A3\u408F\u929D\u38F5\uBCB6\uDA21\u10FF\uF3D2"
          + "\uCD0C\u13EC\u5F97\u4417\uC4A7\u7E3D\u645D\u1973"
          + "\u6081\u4FDC\u222A\u9088\u46EE\uB814\uDE5E\u0BDB"
          + "\uE032\u3A0A\u4906\u245C\uC2D3\uAC62\u9195\uE479"
          + "\uE7C8\u376D\u8DD5\u4EA9\u6C56\uF4EA\u657A\uAE08"
          + "\uBA78\u252E\u1CA6\uB4C6\uE8DD\u741F\u4BBD\u8B8A"
          + "\u703E\uB566\u4803\uF60E\u6135\u57B9\u86C1\u1D9E"
          + "\uE1F8\u9811\u69D9\u8E94\u9B1E\u87E9\uCE55\u28DF"
          + "\u8CA1\u890D\uBFE6\u4268\u4199\u2D0F\uB054\uBB16";

  private static final byte[] S = new byte[256];

  private static final byte[] Si = new byte[256];

  private static final int[] T1 = new int[256];

  private static final int[] T2 = new int[256];

  private static final int[] T3 = new int[256];

  private static final int[] T4 = new int[256];

  private static final int[] T5 = new int[256];

  private static final int[] T6 = new int[256];

  private static final int[] T7 = new int[256];

  private static final int[] T8 = new int[256];

  private static final int[] U1 = new int[256];

  private static final int[] U2 = new int[256];

  private static final int[] U3 = new int[256];

  private static final int[] U4 = new int[256];

  private static final byte[] rcon = new byte[30];

  static {

    final int ROOT = 0x11B;
    int i = 0;

    // S-box, inverse S-box, T-boxes, U-boxes
    int s, s2, s3, i2, i4, i8, i9, ib, id, ie, t;
    char c;
    for (i = 0; i < 256; i++) {
      c = SS.charAt(i >>> 1);
      S[i] = (byte) (((i & 1) == 0) ? c >>> 8 : c & 0xFF);
      s = S[i] & 0xFF;
      Si[s] = (byte) i;
      s2 = s << 1;
      if (s2 >= 0x100) {
        s2 ^= ROOT;
      }
      s3 = s2 ^ s;
      i2 = i << 1;
      if (i2 >= 0x100) {
        i2 ^= ROOT;
      }
      i4 = i2 << 1;
      if (i4 >= 0x100) {
        i4 ^= ROOT;
      }
      i8 = i4 << 1;
      if (i8 >= 0x100) {
        i8 ^= ROOT;
      }
      i9 = i8 ^ i;
      ib = i9 ^ i2;
      id = i9 ^ i4;
      ie = i8 ^ i4 ^ i2;

      T1[i] = t = (s2 << 24) | (s << 16) | (s << 8) | s3;
      T2[i] = (t >>> 8) | (t << 24);
      T3[i] = (t >>> 16) | (t << 16);
      T4[i] = (t >>> 24) | (t << 8);

      T5[s] = U1[i] = t = (ie << 24) | (i9 << 16) | (id << 8) | ib;
      T6[s] = U2[i] = (t >>> 8) | (t << 24);
      T7[s] = U3[i] = (t >>> 16) | (t << 16);
      T8[s] = U4[i] = (t >>> 24) | (t << 8);
    }
    //
    // round constants
    //
    int r = 1;
    rcon[0] = 1;
    for (i = 1; i < 30; i++) {
      r <<= 1;
      if (r >= 0x100) {
        r ^= ROOT;
      }
      rcon[i] = (byte) r;
    }

  }

  private final int[][] key;

  public CtrAes256(final byte[] k) {

    final int[][] Ke = new int[15][4];
    final int[] tk = new int[8];

    int i, j;

    // copy user material bytes into temporary ints
    for (i = 0, j = 0; i < 8;) {
      tk[i++] =
          k[j++] << 24
              | (k[j++] & 0xFF) << 16
              | (k[j++] & 0xFF) << 8
              | (k[j++] & 0xFF);
    }
    // copy values into round key arrays
    int t = 0;
    for (j = 0; (j < 8) && (t < 60); j++, t++) {
      Ke[t >> 2][t % 4] = tk[j];
    }
    int tt, rconpointer = 0;
    while (t < 60) {
      // extrapolate using phi (the round key evolution function)
      tt = tk[7];
      tk[0] ^=
          (S[(tt >>> 16) & 0xFF] & 0xFF) << 24
              ^ (S[(tt >>> 8) & 0xFF] & 0xFF) << 16
              ^ (S[tt & 0xFF] & 0xFF) << 8
              ^ (S[(tt >>> 24)] & 0xFF)
              ^ rcon[rconpointer++] << 24;
      for (i = 1, j = 0; i < 4;) {
        tk[i++] ^= tk[j++];
      }
      tt = tk[3];
      tk[4] ^=
          (S[tt & 0xFF] & 0xFF)
              ^ (S[(tt >>> 8) & 0xFF] & 0xFF) << 8
              ^ (S[(tt >>> 16) & 0xFF] & 0xFF) << 16
              ^ S[(tt >>> 24) & 0xFF] << 24;
      for (j = 4, i = j + 1; i < 8;) {
        tk[i++] ^= tk[j++];
      }
      // copy values into round key arrays
      for (j = 0; (j < 8) && (t < 60); j++, t++) {
        Ke[t >> 2][t % 4] = tk[j];
      }
    }

    key = Ke;
  }

  public final void encrypt(byte[] in, int i, byte[] out, int j) {

    final int[][] Ke = key;

    int[] Ker = Ke[0];

    // plaintext to ints + key
    int t0 =
        (in[i++] << 24 | (in[i++] & 0xFF) << 16 | (in[i++] & 0xFF) << 8 | (in[i++] & 0xFF))
            ^ Ker[0];
    int t1 =
        (in[i++] << 24 | (in[i++] & 0xFF) << 16 | (in[i++] & 0xFF) << 8 | (in[i++] & 0xFF))
            ^ Ker[1];
    int t2 =
        (in[i++] << 24 | (in[i++] & 0xFF) << 16 | (in[i++] & 0xFF) << 8 | (in[i++] & 0xFF))
            ^ Ker[2];
    int t3 =
        (in[i++] << 24 | (in[i++] & 0xFF) << 16 | (in[i++] & 0xFF) << 8 | (in[i++] & 0xFF))
            ^ Ker[3];

    int a0, a1, a2, a3;

    // Round 1 to 13.
    for (int r = 1; r < 14; r++) {
      Ker = Ke[r];
      a0 =
          (T1[(t0 >>> 24)] ^ T2[(t1 >>> 16) & 0xFF] ^ T3[(t2 >>> 8) & 0xFF] ^ T4[t3 & 0xFF])
              ^ Ker[0];
      a1 =
          (T1[(t1 >>> 24)] ^ T2[(t2 >>> 16) & 0xFF] ^ T3[(t3 >>> 8) & 0xFF] ^ T4[t0 & 0xFF])
              ^ Ker[1];
      a2 =
          (T1[(t2 >>> 24)] ^ T2[(t3 >>> 16) & 0xFF] ^ T3[(t0 >>> 8) & 0xFF] ^ T4[t1 & 0xFF])
              ^ Ker[2];
      a3 =
          (T1[(t3 >>> 24)] ^ T2[(t0 >>> 16) & 0xFF] ^ T3[(t1 >>> 8) & 0xFF] ^ T4[t2 & 0xFF])
              ^ Ker[3];
      t0 = a0;
      t1 = a1;
      t2 = a2;
      t3 = a3;

    }

    // Round 14.
    Ker = Ke[14];
    int tt = Ker[0];
    out[j++] = (byte) (S[(t0 >>> 24)] ^ (tt >>> 24));
    out[j++] = (byte) (S[(t1 >>> 16) & 0xFF] ^ (tt >>> 16));
    out[j++] = (byte) (S[(t2 >>> 8) & 0xFF] ^ (tt >>> 8));
    out[j++] = (byte) (S[t3 & 0xFF] ^ tt);
    tt = Ker[1];
    out[j++] = (byte) (S[(t1 >>> 24)] ^ (tt >>> 24));
    out[j++] = (byte) (S[(t2 >>> 16) & 0xFF] ^ (tt >>> 16));
    out[j++] = (byte) (S[(t3 >>> 8) & 0xFF] ^ (tt >>> 8));
    out[j++] = (byte) (S[t0 & 0xFF] ^ tt);
    tt = Ker[2];
    out[j++] = (byte) (S[(t2 >>> 24)] ^ (tt >>> 24));
    out[j++] = (byte) (S[(t3 >>> 16) & 0xFF] ^ (tt >>> 16));
    out[j++] = (byte) (S[(t0 >>> 8) & 0xFF] ^ (tt >>> 8));
    out[j++] = (byte) (S[t1 & 0xFF] ^ tt);
    tt = Ker[3];
    out[j++] = (byte) (S[(t3 >>> 24)] ^ (tt >>> 24));
    out[j++] = (byte) (S[(t0 >>> 16) & 0xFF] ^ (tt >>> 16));
    out[j++] = (byte) (S[(t1 >>> 8) & 0xFF] ^ (tt >>> 8));
    out[j++] = (byte) (S[t2 & 0xFF] ^ tt);

  }

}
