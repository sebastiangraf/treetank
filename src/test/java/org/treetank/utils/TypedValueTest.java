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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.treetank.api.IReadTransaction;

public class TypedValueTest {

  @Test
  public void test() {
    assertEquals("a", TypedValue.parseString(TypedValue.getBytes("a")));
    assertEquals("foo", TypedValue.parseString(TypedValue.getBytes("foo")));
    assertEquals("fö§", TypedValue.parseString(TypedValue.getBytes("fö§")));
    assertEquals("", TypedValue.parseString(TypedValue.getBytes("")));
  }

  @Test
  public void testEquals() {
    assertEquals(false, TypedValue.equals(
        TypedValue.getBytes("foo"),
        TypedValue.getBytes("foobar")));
    assertEquals(false, TypedValue.equals(
        TypedValue.getBytes("foo"),
        TypedValue.getBytes("bar")));
    assertEquals(true, TypedValue.equals(TypedValue.getBytes("foo"), TypedValue
        .getBytes("foo")));

    assertEquals(true, TypedValue.equals("foo", TypedValue.getBytes("foo")));
    assertEquals(true, TypedValue.equals(TypedValue.getBytes("foo"), "foo"));
    assertEquals(true, TypedValue.equals("foo", "foo"));
  }

  @Test
  public void testInt() {
    assertEquals("0", TypedValue.atomize(IReadTransaction.INT_TYPE, TypedValue
        .getBytes(0)));
    assertEquals(0, TypedValue.parseInt(TypedValue.getBytes(0)));

    assertEquals("1234", TypedValue.atomize(
        IReadTransaction.INT_TYPE,
        TypedValue.getBytes(1234)));
    assertEquals(1234, TypedValue.parseInt(TypedValue.getBytes(1234)));

    assertEquals("4", TypedValue.atomize(IReadTransaction.INT_TYPE, TypedValue
        .getBytes(4)));
    assertEquals(4, TypedValue.parseInt(TypedValue.getBytes(4)));

    assertEquals(String.valueOf(Integer.MAX_VALUE), TypedValue.atomize(
        IReadTransaction.INT_TYPE,
        TypedValue.getBytes(Integer.MAX_VALUE)));
    assertEquals(Integer.MAX_VALUE, TypedValue.parseInt(TypedValue
        .getBytes(Integer.MAX_VALUE)));

    assertEquals("-1234", TypedValue.atomize(
        IReadTransaction.INT_TYPE,
        TypedValue.getBytes(-1234)));
    assertEquals(-1234, TypedValue.parseInt(TypedValue.getBytes(-1234)));

    assertEquals("-4", TypedValue.atomize(IReadTransaction.INT_TYPE, TypedValue
        .getBytes(-4)));
    assertEquals(-4, TypedValue.parseInt(TypedValue.getBytes(-4)));

    assertEquals(String.valueOf(Integer.MIN_VALUE + 1), TypedValue.atomize(
        IReadTransaction.INT_TYPE,
        TypedValue.getBytes(Integer.MIN_VALUE + 1)));
    assertEquals(Integer.MIN_VALUE + 1, TypedValue.parseInt(TypedValue
        .getBytes(Integer.MIN_VALUE + 1)));
  }

  @Test
  public void testLong() {
    assertEquals("0", TypedValue.atomize(IReadTransaction.LONG_TYPE, TypedValue
        .getBytes(0L)));
    assertEquals(0L, TypedValue.parseLong(TypedValue.getBytes(0L)));

    assertEquals("1234", TypedValue.atomize(
        IReadTransaction.LONG_TYPE,
        TypedValue.getBytes(1234L)));
    assertEquals(1234L, TypedValue.parseLong(TypedValue.getBytes(1234L)));

    assertEquals("4", TypedValue.atomize(IReadTransaction.LONG_TYPE, TypedValue
        .getBytes(4L)));
    assertEquals(4L, TypedValue.parseLong(TypedValue.getBytes(4L)));

    assertEquals(String.valueOf(Long.MAX_VALUE), TypedValue.atomize(
        IReadTransaction.LONG_TYPE,
        TypedValue.getBytes(Long.MAX_VALUE)));
    assertEquals(Long.MAX_VALUE, TypedValue.parseLong(TypedValue
        .getBytes(Long.MAX_VALUE)));

    assertEquals("-1234", TypedValue.atomize(
        IReadTransaction.LONG_TYPE,
        TypedValue.getBytes(-1234L)));
    assertEquals(-1234L, TypedValue.parseLong(TypedValue.getBytes(-1234L)));

    assertEquals("-4", TypedValue.atomize(
        IReadTransaction.LONG_TYPE,
        TypedValue.getBytes(-4L)));
    assertEquals(-4L, TypedValue.parseLong(TypedValue.getBytes(-4L)));

    assertEquals(String.valueOf(Long.MIN_VALUE + 1), TypedValue.atomize(
        IReadTransaction.LONG_TYPE,
        TypedValue.getBytes(Long.MIN_VALUE + 1)));
    assertEquals(Long.MIN_VALUE + 1, TypedValue.parseLong(TypedValue
        .getBytes(Long.MIN_VALUE + 1)));
  }

  @Test
  public void testDouble() {

    assertThat(0.0, is(TypedValue.parseDouble(TypedValue.getBytes(0.0))));

    assertThat(1234.123, is(TypedValue.parseDouble(TypedValue
        .getBytes(1234.123))));

    assertThat(
        4.00001,
        is(TypedValue.parseDouble(TypedValue.getBytes(4.00001))));

    assertThat(Double.MAX_VALUE, is(TypedValue.parseDouble(TypedValue
        .getBytes(Double.MAX_VALUE))));

    assertThat(-1234.5432, is(TypedValue.parseDouble(TypedValue
        .getBytes(-1234.5432))));

    assertThat(-4E-13, is(TypedValue.parseDouble(TypedValue.getBytes(-4E-13))));

    assertThat(Double.MIN_VALUE + 1, is(TypedValue.parseDouble(TypedValue
        .getBytes(Double.MIN_VALUE + 1))));
  }

  @Test
  public void testFloat() {

    assertThat(0.0f, is(TypedValue.parseFloat(TypedValue.getBytes(0.0f))));

    assertThat(1234.123f, is(TypedValue.parseFloat(TypedValue
        .getBytes(1234.123f))));

    assertThat(4.00001f, is(TypedValue
        .parseFloat(TypedValue.getBytes(4.00001f))));

    assertThat(Float.MAX_VALUE, is(TypedValue.parseFloat(TypedValue
        .getBytes(Float.MAX_VALUE))));

    assertThat(-1234.5432f, is(TypedValue.parseFloat(TypedValue
        .getBytes(-1234.5432f))));

    assertThat(-4E-13f, 
        is(TypedValue.parseFloat(TypedValue.getBytes(-4E-13f))));

    assertThat(Float.MIN_VALUE + 1, is(TypedValue.parseFloat(TypedValue
        .getBytes(Float.MIN_VALUE + 1))));
  }

  @Test
  public void testBoolean() {
    assertEquals("true", TypedValue.atomize(
        IReadTransaction.BOOLEAN_TYPE,
        TypedValue.getBytes(true)));
    assertEquals("false", TypedValue.atomize(
        IReadTransaction.BOOLEAN_TYPE,
        TypedValue.getBytes(false)));
  }

  @Test
  public void testNullBytesToAtom() {
    assertEquals("", TypedValue.atomize(IReadTransaction.STRING_TYPE, null));
  }

  @Test
  public void testEntityInString() {
    assertEquals("<&", TypedValue.parseString(TypedValue.getBytes("<&")));
    assertEquals(9, TypedValue.getBytes("<&").length);
  }

}
