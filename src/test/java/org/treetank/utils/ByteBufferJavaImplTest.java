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

package org.treetank.utils;

import org.junit.Assert;
import org.junit.Test;

public class ByteBufferJavaImplTest {

  @Test
  public void testBasics() {
    final IByteBuffer buffer = new ByteBufferJavaImpl(100);
    Assert.assertEquals(0, buffer.position());
    try {
      buffer.position(100);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }
    try {
      buffer.position(-100);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      // Must throw IllegalArgumentException.
    }
  }

  @Test
  public void testPutGet() {
    final IByteBuffer buffer = new ByteBufferJavaImpl(100);
    // Byte.
    buffer.position(0);
    buffer.put((byte) 13);
    buffer.put(Byte.MAX_VALUE);
    Assert.assertEquals(2, buffer.position());
    buffer.position(0);
    Assert.assertEquals(13, buffer.get());
    Assert.assertEquals(Byte.MAX_VALUE, buffer.get());
    // Int.
    buffer.position(0);
    buffer.put(8192);
    buffer.put(Integer.MAX_VALUE);
    Assert.assertEquals(7, buffer.position());
    buffer.position(0);
    Assert.assertEquals(8192, buffer.get());
    Assert.assertEquals(Integer.MAX_VALUE, buffer.get());
    // Long.
    buffer.position(0);
    buffer.put(819281928192L);
    buffer.put(Long.MAX_VALUE);
    Assert.assertEquals(15, buffer.position());
    buffer.position(0);
    Assert.assertEquals(819281928192L, buffer.get());
    Assert.assertEquals(Long.MAX_VALUE, buffer.get());
  }

  @Test
  public void testPutGetArray() {
    final IByteBuffer buffer = new ByteBufferJavaImpl(100);

    final byte[] reference = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
    buffer.position(0);
    buffer.putArray(reference);
    Assert.assertEquals(8, buffer.position());

    buffer.position(0);
    final byte[] result = buffer.getArray(8);
    Assert.assertEquals(8, buffer.position());
    Assert.assertArrayEquals(reference, result);
  }

}
