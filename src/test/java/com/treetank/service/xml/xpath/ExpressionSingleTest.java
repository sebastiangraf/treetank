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
 * $Id: ExpressionSingleTest.java 4376 2008-08-25 07:27:39Z kramis $
 */

package com.treetank.service.xml.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.IAxis;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.ChildAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.axis.FollowingSiblingAxis;
import com.treetank.axis.NestedAxis;
import com.treetank.axis.ParentAxis;
import com.treetank.axis.SelfAxis;
import com.treetank.service.xml.xpath.expr.UnionAxis;
import com.treetank.service.xml.xpath.filter.DupFilterAxis;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

public class ExpressionSingleTest {

	ExpressionSingle builder;

	public static final String XML = "src" + File.separator + "test"
			+ File.separator + "resoruces" + File.separator + "factbook.xml";

	@Before
	public void setUp() {

		builder = new ExpressionSingle();
		Session.removeSession(ITestConstants.PATH1);
	}

	@Test
	public void testAdd() throws IOException {

		// Build simple test tree.
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		// test one axis
		IAxis self = new SelfAxis(wtx);
		builder.add(self);
		assertEquals(builder.getExpr(), self);

		// test 2 axis
		IAxis axis1 = new SelfAxis(wtx);
		IAxis axis2 = new SelfAxis(wtx);
		builder.add(axis1);
		builder.add(axis2);
		assertTrue(builder.getExpr() instanceof NestedAxis);

		wtx.abort();
		wtx.close();
		session.close();

	}

	@Test
	public void testDup() throws IOException {

		// Build simple test tree.
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		builder = new ExpressionSingle();
		builder.add(new ChildAxis(wtx));
		builder.add(new DescendantAxis(wtx));
		assertTrue(builder.getExpr() instanceof NestedAxis);

		builder = new ExpressionSingle();
		builder.add(new ChildAxis(wtx));
		builder.add(new DescendantAxis(wtx));
		assertEquals(true, builder.isOrdered());
		assertTrue(builder.getExpr() instanceof NestedAxis);

		builder = new ExpressionSingle();
		builder.add(new ChildAxis(wtx));
		builder.add(new DescendantAxis(wtx));
		builder.add(new ChildAxis(wtx));
		assertEquals(false, builder.isOrdered());

		builder = new ExpressionSingle();
		builder = new ExpressionSingle();
		builder.add(new ChildAxis(wtx));
		builder.add(new DescendantAxis(wtx));
		builder.add(new ChildAxis(wtx));
		builder.add(new ParentAxis(wtx));
		assertEquals(true, builder.isOrdered());

		builder = new ExpressionSingle();
		builder.add(new ChildAxis(wtx));
		builder.add(new DescendantAxis(wtx));
		builder.add(new FollowingSiblingAxis(wtx));
		assertEquals(false, builder.isOrdered());

		builder = new ExpressionSingle();
		builder.add(new UnionAxis(wtx, new DescendantAxis(wtx), new ParentAxis(
				wtx)));
		assertEquals(false, builder.isOrdered());
		assertTrue(builder.getExpr() instanceof DupFilterAxis);

		wtx.abort();
		wtx.close();
		session.close();

	}
}
