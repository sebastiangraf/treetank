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
 * $Id: TypedValueTest.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
		assertEquals(false, TypedValue.equals(TypedValue.getBytes("foo"),
				TypedValue.getBytes("foobar")));
		assertEquals(false, TypedValue.equals(TypedValue.getBytes("foo"),
				TypedValue.getBytes("bar")));
		assertEquals(true, TypedValue.equals(TypedValue.getBytes("foo"),
				TypedValue.getBytes("foo")));

		assertEquals(true, TypedValue.equals("foo", TypedValue.getBytes("foo")));
		assertEquals(true, TypedValue.equals(TypedValue.getBytes("foo"), "foo"));
		assertEquals(true, TypedValue.equals("foo", "foo"));
	}

	@Test
	public void testEntityInString() {
		final byte[] bytes = TypedValue.getBytes("<&");
		assertEquals("<&", TypedValue.parseString(bytes));
		assertEquals(9, TypedValue.getBytes("<&").length);
	}

}
