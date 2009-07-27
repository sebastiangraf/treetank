/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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
 * $Id: SomeExprTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

/**
 * JUnit-test class to test the functionality of the UnionAxis.
 * 
 * @author Tina Scherer
 * 
 */
public class SomeExprTest {

	public static final String PATH = "target" + File.separator + "tnk"
			+ File.separator + "SomeExprTest.tnk";

	@Before
	public void setUp() {

		Session.removeSession(PATH);
	}

	@Test
	public void testEveryExpr() throws IOException {

		// Build simple test tree.
		final ISession session = Session.beginSession(PATH);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);
		IReadTransaction rtx = session.beginReadTransaction();

		final IAxis axis1 = new XPathAxis(rtx,
				"some $child in child::node() satisfies $child/@i");
		assertEquals(true, axis1.hasNext());
		assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
				.getNode().getRawValue()))));
		assertEquals(false, axis1.hasNext());

		final IAxis axis2 = new XPathAxis(rtx,
				"some $child in child::node() satisfies $child/@abc");
		assertEquals(true, axis2.hasNext());
		assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx
				.getNode().getRawValue()))));
		assertEquals(false, axis2.hasNext());

		rtx.moveTo(1L);
		final IAxis axis3 = new XPathAxis(rtx,
				"some $child in child::node() satisfies $child/attribute::attribute()");
		assertEquals(true, axis3.hasNext());
		assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
				.getNode().getRawValue()))));
		assertEquals(false, axis3.hasNext());

		rtx.close();
		wtx.abort();
		wtx.close();
		session.close();

	}

}
