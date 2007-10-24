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
    assertEquals("a", UTF.convert(UTF.convert("a")));
    assertEquals("foo", UTF.convert(UTF.convert("foo")));
    assertEquals("fö§", UTF.convert(UTF.convert("fö§")));
    assertEquals("", UTF.convert(UTF.convert("")));
    
    //Tests for fastConvert()
    assertEquals("a", UTF.fastConvert(UTF.fastConvert("a")));
    assertEquals("foo", UTF.fastConvert(UTF.fastConvert("foo")));
    assertEquals("fö§", UTF.fastConvert(UTF.fastConvert("fö§")));
    assertEquals("", UTF.fastConvert(UTF.fastConvert("")));
  }

  @Test
  public void testEquals() {
    assertEquals(false, UTF.equals(UTF.convert("foo"), UTF.convert("foobar")));
    assertEquals(false, UTF.equals(UTF.convert("foo"), UTF.convert("bar")));
    assertEquals(true, UTF.equals(UTF.convert("foo"), UTF.convert("foo")));

    assertEquals(true, UTF.equals("foo", UTF.convert("foo")));
    assertEquals(true, UTF.equals(UTF.convert("foo"), "foo"));
    assertEquals(true, UTF.equals("foo", "foo"));
    
    //Tests for fastConvert()
    assertEquals(false, UTF.equals(UTF.fastConvert("foo"), UTF.fastConvert("foobar")));
    assertEquals(false, UTF.equals(UTF.fastConvert("foo"), UTF.fastConvert("bar")));
    assertEquals(true, UTF.equals(UTF.fastConvert("foo"), UTF.fastConvert("foo")));

    assertEquals(true, UTF.fastEquals("foo", UTF.fastConvert("foo")));
    assertEquals(true, UTF.fastEquals(UTF.fastConvert("foo"), "foo"));
  }

}
