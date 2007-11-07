/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
