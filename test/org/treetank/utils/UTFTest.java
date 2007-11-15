/*
 * Copyright (c) 2007, Marc Kramis
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UTFTest {

  @Test
  public void test() {
    assertEquals("a", UTF.parseString(UTF.getBytes("a")));
    assertEquals("foo", UTF.parseString(UTF.getBytes("foo")));
    assertEquals("fö§", UTF.parseString(UTF.getBytes("fö§")));
    assertEquals("", UTF.parseString(UTF.getBytes("")));
  }

  @Test
  public void testEquals() {
    assertEquals(false, UTF.equals(UTF.getBytes("foo"), UTF.getBytes("foobar")));
    assertEquals(false, UTF.equals(UTF.getBytes("foo"), UTF.getBytes("bar")));
    assertEquals(true, UTF.equals(UTF.getBytes("foo"), UTF.getBytes("foo")));

    assertEquals(true, UTF.equals("foo", UTF.getBytes("foo")));
    assertEquals(true, UTF.equals(UTF.getBytes("foo"), "foo"));
    assertEquals(true, UTF.equals("foo", "foo"));
  }

  @Test
  public void testInt() {
    assertEquals("0", UTF.parseString(UTF.getBytes(0)));
    assertEquals(0, UTF.parseInt(UTF.getBytes(0)));

    assertEquals("1234", UTF.parseString(UTF.getBytes(1234)));
    assertEquals(1234, UTF.parseInt(UTF.getBytes(1234)));

    assertEquals("4", UTF.parseString(UTF.getBytes(4)));
    assertEquals(4, UTF.parseInt(UTF.getBytes(4)));

    assertEquals(String.valueOf(Integer.MAX_VALUE), UTF.parseString(UTF
        .getBytes(Integer.MAX_VALUE)));
    assertEquals(Integer.MAX_VALUE, UTF.parseInt(UTF
        .getBytes(Integer.MAX_VALUE)));

    assertEquals("-1234", UTF.parseString(UTF.getBytes(-1234)));
    assertEquals(-1234, UTF.parseInt(UTF.getBytes(-1234)));

    assertEquals("-4", UTF.parseString(UTF.getBytes(-4)));
    assertEquals(-4, UTF.parseInt(UTF.getBytes(-4)));

    assertEquals(String.valueOf(Integer.MIN_VALUE + 1), UTF.parseString(UTF
        .getBytes(Integer.MIN_VALUE + 1)));
    assertEquals(Integer.MIN_VALUE + 1, UTF.parseInt(UTF
        .getBytes(Integer.MIN_VALUE + 1)));
  }

  @Test
  public void testLong() {
    assertEquals("0", UTF.parseString(UTF.getBytes(0L)));
    assertEquals(0L, UTF.parseLong(UTF.getBytes(0L)));

    assertEquals("1234", UTF.parseString(UTF.getBytes(1234L)));
    assertEquals(1234L, UTF.parseLong(UTF.getBytes(1234L)));

    assertEquals("4", UTF.parseString(UTF.getBytes(4L)));
    assertEquals(4L, UTF.parseLong(UTF.getBytes(4L)));

    assertEquals(String.valueOf(Long.MAX_VALUE), UTF.parseString(UTF
        .getBytes(Long.MAX_VALUE)));
    assertEquals(Long.MAX_VALUE, UTF.parseLong(UTF.getBytes(Long.MAX_VALUE)));

    assertEquals("-1234", UTF.parseString(UTF.getBytes(-1234L)));
    assertEquals(-1234L, UTF.parseLong(UTF.getBytes(-1234L)));

    assertEquals("-4", UTF.parseString(UTF.getBytes(-4L)));
    assertEquals(-4L, UTF.parseLong(UTF.getBytes(-4L)));

    assertEquals(String.valueOf(Long.MIN_VALUE + 1), UTF.parseString(UTF
        .getBytes(Long.MIN_VALUE + 1)));
    assertEquals(Long.MIN_VALUE + 1, UTF.parseLong(UTF
        .getBytes(Long.MIN_VALUE + 1)));
  }

}
