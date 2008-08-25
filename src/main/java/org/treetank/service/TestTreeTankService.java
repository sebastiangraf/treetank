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
 * $Id$
 */

package org.treetank.service;

import java.nio.ByteBuffer;
import java.util.Random;

import org.treetank.pagelayer.CryptoNativeImpl;
import org.treetank.pagelayer.ICrypto;

public class TestTreeTankService {

  /**
   * @param args
   */
  public static void main(String[] args) {

    System.loadLibrary("TreeTank");

    final ByteBuffer buffer = ByteBuffer.allocateDirect(32767);
    final ICrypto crypto = new CryptoNativeImpl();

    final Random r = new Random();
    final byte[] b = new byte[32767];
    r.nextBytes(b);

    final short length = 777;
    buffer.put(b);

    final short cryptLength = crypto.crypt(length, buffer);
    System.out.print("cryptLength=" + cryptLength + "...");
    
    final short decryptLength = crypto.decrypt(cryptLength, buffer);
    System.out.println(" decryptLength=" + decryptLength + ".");

    if (decryptLength != length) {
      System.out.println("Error: Length after decryption is wrong: " + decryptLength);
    }

    buffer.rewind();
    for (int i = 0; i < 32767; i++) {
      if (buffer.get() != b[i]) {
        System.out.println("Error: Byte does not match at " + i);
        break;
      }
    }

  }

}
