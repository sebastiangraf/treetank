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

  public CtrAes256(final byte[] key) {
    this.key = Aes256.buildKey(key);
  }

  public final void encrypt(byte[] in, int i, byte[] out, int j) {
    Aes256.encrypt(key, in, i, out, j);
  }

}
