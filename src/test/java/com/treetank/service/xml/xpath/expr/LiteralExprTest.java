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
 * $Id: LiteralExprTest.java 4487 2008-10-02 09:12:29Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.treetank.api.IAxis;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

/**
 * JUnit-test class to test the functionality of the LiteralExpr.
 * 
 * @author Tina Scherer
 */
public class LiteralExprTest {

	public static final String PATH = "target" + File.separator + "tnk"
			+ File.separator + "LiteralExprTest.tnk";

	IItem item1;

	IItem item2;

	int key1;

	int key2;

	@Before
	public void setUp() {

		Session.removeSession(PATH);
		item1 = new AtomicValue(false);
		item2 = new AtomicValue(14, Type.INTEGER);

	}

	@Test
	public void testLiteralExpr() throws IOException {

		// Build simple test tree.
		final ISession session = Session.beginSession(PATH);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);
		IReadTransaction rtx = session.beginReadTransaction();

		key1 = rtx.getItemList().addItem(item1);
		key2 = rtx.getItemList().addItem(item2);

		final IAxis axis1 = new LiteralExpr(rtx, key1);
		assertEquals(true, axis1.hasNext());
		assertEquals(key1, rtx.getNode().getNodeKey());
		assertEquals(rtx.keyForName("xs:boolean"), rtx.getNode().getTypeKey());
		assertEquals(false, TypedValue.parseBoolean((rtx.getNode()
				.getRawValue())));
		assertEquals(false, axis1.hasNext());

		final IAxis axis2 = new LiteralExpr(rtx, key2);
		assertEquals(true, axis2.hasNext());
		assertEquals(key2, rtx.getNode().getNodeKey());
		assertEquals(rtx.keyForName("xs:integer"), rtx.getNode().getTypeKey());
		assertEquals(14, (int) TypedValue.parseDouble(rtx.getNode()
				.getRawValue()));
		assertEquals(false, axis2.hasNext());

		rtx.close();
		wtx.abort();
		wtx.close();
		session.close();

	}

}
