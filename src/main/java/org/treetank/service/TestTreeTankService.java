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

import java.util.Random;

import org.treetank.openbsd.ByteBufferNativeImpl;
import org.treetank.openbsd.CryptoNativeImpl;
import org.treetank.utils.ByteBufferJavaImpl;
import org.treetank.utils.CryptoJavaImpl;
import org.treetank.utils.IByteBuffer;
import org.treetank.utils.IConstants;
import org.treetank.utils.ICrypto;

public class TestTreeTankService {

  /**
   * @param args
   */
  public static void main(String[] args) {

    try {

      System.out
          .println("--- Test Java Crypto: ----------------------------------");
      final ICrypto javaCrypto = new CryptoJavaImpl();
      final IByteBuffer javaBuffer =
          new ByteBufferJavaImpl(IConstants.BUFFER_SIZE);
      testCrypto(javaCrypto, javaBuffer);

      System.out
          .println("--- Test Native Crypto: --------------------------------");
      System.loadLibrary("TreeTank");

      final ICrypto nativeCrypto = new CryptoNativeImpl();
      final IByteBuffer nativeBuffer =
          new ByteBufferNativeImpl(IConstants.BUFFER_SIZE);
      testCrypto(nativeCrypto, nativeBuffer);

    } catch (Exception e) {
      System.out.println(": FAILURE: " + e.getMessage());
    }

  }

  private static final void testCrypto(
      final ICrypto crypto,
      final IByteBuffer buffer) throws Exception {

    final Random r = new Random();
    final byte[] referenceBuffer = new byte[IConstants.BUFFER_SIZE];
    r.nextBytes(referenceBuffer);
    buffer.position(0);
    buffer.putArray(referenceBuffer);

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
      final IByteBuffer buffer,
      final byte[] referenceBuffer) throws Exception {

    System.out.print("Test page length: " + length);

    final long start = System.currentTimeMillis();

    buffer.position(0);
    final short cryptLength = crypto.crypt(length, buffer);
    buffer.position(0);
    final short decryptLength = crypto.decrypt(cryptLength, buffer);

    final long stop = System.currentTimeMillis();

    System.out.print("..." + cryptLength + "..." + decryptLength);

    if (decryptLength != length) {
      throw new Exception("Bad result length: " + decryptLength);
    }

    buffer.position(0);
    final byte[] tmp = buffer.getArray(referenceBuffer.length);
    buffer.position(0);
    for (int i = 24; i < decryptLength; i++) {
      if (tmp[i] != referenceBuffer[i]) {
        throw new Exception("Error: Byte does not match at " + i);
      }
    }

    buffer.position();
    buffer.putArray(referenceBuffer);

    System.out.println(": " + (stop - start) + "[ms]: SUCCESS.");

  }

}
