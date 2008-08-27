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

import org.treetank.pagelayer.CryptoJavaImpl;
import org.treetank.pagelayer.CryptoNativeImpl;
import org.treetank.pagelayer.ICrypto;

public class TestTreeTankService {

  /**
   * @param args
   */
  public static void main(String[] args) {

    try {

      System.out.println("--- Test Java Crypto: ----------------------------------");
      final ICrypto javaCrypto = new CryptoJavaImpl();
      testCrypto(javaCrypto);

      System.out.println("--- Test Native Crypto: --------------------------------");
      System.loadLibrary("TreeTank");
      final ICrypto nativeCrypto = new CryptoNativeImpl();
      testCrypto(nativeCrypto);

    } catch (Exception e) {
      System.out.println(": FAILURE: " + e.getMessage());
    }

  }

  private static final void testCrypto(final ICrypto crypto) throws Exception {
    final ByteBuffer buffer = ByteBuffer.allocateDirect(32767);

    final Random r = new Random();
    final byte[] referenceBuffer = new byte[32767];
    r.nextBytes(referenceBuffer);
    buffer.put(referenceBuffer);

    testCryptDecrypt(crypto, (short) 30, buffer, referenceBuffer);
    testCryptDecrypt(crypto, (short) 32, buffer, referenceBuffer);
    testCryptDecrypt(crypto, (short) 188, buffer, referenceBuffer);
    testCryptDecrypt(crypto, (short) 1200, buffer, referenceBuffer);
    testCryptDecrypt(crypto, (short) 4932, buffer, referenceBuffer);
    testCryptDecrypt(crypto, (short) 8452, buffer, referenceBuffer);
    testCryptDecrypt(crypto, (short) 9000, buffer, referenceBuffer);
  }

  private static final void testCryptDecrypt(
      final ICrypto crypto,
      final short length,
      final ByteBuffer buffer,
      final byte[] referenceBuffer) throws Exception {

    System.out.print("Test page length: " + length);

    final long start = System.currentTimeMillis();

    final short cryptLength = crypto.crypt(length, buffer);
    final short decryptLength = crypto.decrypt(cryptLength, buffer);

    final long stop = System.currentTimeMillis();

    System.out.print("..." + cryptLength + "..." + decryptLength);

    if (decryptLength != length) {
      throw new Exception("Bad result length: " + decryptLength);
    }

    buffer.rewind();
    buffer.position(24);
    for (int i = 24; i < decryptLength; i++) {
      if (buffer.get() != referenceBuffer[i]) {
        throw new Exception("Error: Byte does not match at " + i);
      }
    }

    buffer.rewind();
    buffer.put(referenceBuffer);

    System.out.println(": " + (stop - start) + "[ms]: SUCCESS.");

  }

}
