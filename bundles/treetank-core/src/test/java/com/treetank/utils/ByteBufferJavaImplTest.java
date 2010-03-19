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
 * $Id: ByteBufferJavaImplTest.java 4442 2008-08-30 16:17:17Z kramis $
 */

package com.treetank.utils;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class ByteBufferJavaImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void testBasics() throws IllegalArgumentException {
        final ByteBuffer buffer = ByteBuffer.allocate(100);
        Assert.assertEquals(0, buffer.position());
        final IllegalArgumentException[] excsToFire = new IllegalArgumentException[2];
        try {
            buffer.position(101);
        } catch (final IllegalArgumentException exc) {
            excsToFire[0] = exc;
        }
        try {
            buffer.position(-100);
        } catch (final IllegalArgumentException exc) {
            excsToFire[1] = exc;
        }
        if (excsToFire[0] != null && excsToFire[1] != null) {
            throw excsToFire[0];
        }

    }

    @Test
    public void testPutGet() {
        final ByteBuffer buffer = ByteBuffer.allocate(100);
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
        buffer.putInt(8192);
        buffer.putInt(Integer.MAX_VALUE);
        Assert.assertEquals(8, buffer.position());
        buffer.position(0);
        Assert.assertEquals(8192, buffer.getInt());
        Assert.assertEquals(Integer.MAX_VALUE, buffer.getInt());
        // Long.
        buffer.position(0);
        buffer.putLong(819281928192L);
        buffer.putLong(Long.MAX_VALUE);
        Assert.assertEquals(16, buffer.position());
        buffer.position(0);
        Assert.assertEquals(819281928192L, buffer.getLong());
        Assert.assertEquals(Long.MAX_VALUE, buffer.getLong());
    }

    @Test
    public void testPutGetArray() {
        final ByteBuffer buffer = ByteBuffer.allocate(100);

        final byte[] reference = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        buffer.position(0);
        for (final byte byteVal : reference) {
            buffer.put(byteVal);
        }
        Assert.assertEquals(8, buffer.position());

        buffer.position(0);
        final byte[] result = new byte[8];
        for (int i = 0; i < result.length; i++) {
            result[i] = buffer.get();
        }
        Assert.assertEquals(8, buffer.position());
        Assert.assertArrayEquals(reference, result);
    }
}
