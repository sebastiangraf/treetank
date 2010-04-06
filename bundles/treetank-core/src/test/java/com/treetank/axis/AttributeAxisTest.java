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
 * $Id: AttributeAxisTest.java 4427 2008-08-28 10:12:33Z scherer $
 */

package com.treetank.axis;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

public class AttributeAxisTest {

	@Before
	public void setUp() throws TreetankException {
		TestHelper.deleteEverything();
	}

	@Test
	public void testIterate() throws TreetankException {
		final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
		final ISession session = database.getSession();
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		wtx.moveToDocumentRoot();
		IAxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {});

		wtx.moveTo(1L);
		IAxisTest.testIAxisConventions(new AttributeAxis(wtx),
				new long[] { 2L });

		wtx.moveTo(9L);
		IAxisTest.testIAxisConventions(new AttributeAxis(wtx),
				new long[] { 10L });

		wtx.moveTo(12L);
		IAxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {});

		wtx.moveTo(2L);
		IAxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {});

		wtx.abort();
		wtx.close();
		session.close();
		database.close();
	}

	@Test
	public void testMultipleAttributes() throws TreetankException {
		final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
		final ISession session = database.getSession();
		final IWriteTransaction wtx = session.beginWriteTransaction();
		final long nodeKey = wtx.insertElementAsFirstChild(new QName("foo"));
		wtx.insertAttribute(new QName("foo0"), "0");
		wtx.moveTo(nodeKey);
		wtx.insertAttribute(new QName("foo1"), "1");
		wtx.moveTo(nodeKey);
		wtx.insertAttribute(new QName("foo2"), "2");

		Assert.assertEquals(true, wtx.moveTo(nodeKey));

		Assert.assertEquals(true, wtx.moveToAttribute(0));
		Assert.assertEquals("0", TypedValue.parseString(wtx.getNode()
				.getRawValue()));
		Assert.assertEquals("foo0", wtx.nameForKey(wtx.getNode().getNameKey()));

		Assert.assertEquals(true, wtx.moveToParent());
		Assert.assertEquals(true, wtx.moveToAttribute(1));
		Assert.assertEquals("1", TypedValue.parseString(wtx.getNode()
				.getRawValue()));
		Assert.assertEquals("foo1", wtx.nameForKey(wtx.getNode().getNameKey()));

		Assert.assertEquals(true, wtx.moveToParent());
		Assert.assertEquals(true, wtx.moveToAttribute(2));
		Assert.assertEquals("2", TypedValue.parseString(wtx.getNode()
				.getRawValue()));
		Assert.assertEquals("foo2", wtx.nameForKey(wtx.getNode().getNameKey()));

		Assert.assertEquals(true, wtx.moveTo(nodeKey));
		final IAxis axis = new AttributeAxis(wtx);

		Assert.assertEquals(true, axis.hasNext());
		axis.next();
		Assert.assertEquals(nodeKey + 1, wtx.getNode().getNodeKey());
		Assert.assertEquals("foo0", wtx.nameForKey(wtx.getNode().getNameKey()));
		Assert.assertEquals("0", TypedValue.parseString(wtx.getNode()
				.getRawValue()));

		Assert.assertEquals(true, axis.hasNext());
		axis.next();
		Assert.assertEquals(nodeKey + 2, wtx.getNode().getNodeKey());
		Assert.assertEquals("foo1", wtx.nameForKey(wtx.getNode().getNameKey()));
		Assert.assertEquals("1", TypedValue.parseString(wtx.getNode()
				.getRawValue()));

		Assert.assertEquals(true, axis.hasNext());
		axis.next();
		Assert.assertEquals(nodeKey + 3, wtx.getNode().getNodeKey());
		Assert.assertEquals("foo2", wtx.nameForKey(wtx.getNode().getNameKey()));
		Assert.assertEquals("2", TypedValue.parseString(wtx.getNode()
				.getRawValue()));

		wtx.abort();
		wtx.close();
		session.close();
		database.close();
	}

	@After
	public void tearDown() throws TreetankException {
		TestHelper.closeEverything();
	}
}
