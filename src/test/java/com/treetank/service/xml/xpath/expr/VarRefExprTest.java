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
 * $Id: VarRefExprTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

/**
 * JUnit-test class to test the functionality of the VarRefExpr.
 * 
 * @author Tina Scherer
 */
public class VarRefExprTest {


	@Before
	public void setUp() {

		Session.removeSession(ITestConstants.PATH1);
	}

	@Test
	public void testEveryExpr() throws IOException {

		// Build simple test tree.
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);
		wtx.commit();
		IReadTransaction rtx = session.beginReadTransaction();

		final IAxis axis = new XPathAxis(rtx, "for $a in b return $a");

		final VariableAxis variable = new VariableAxis(rtx, axis);

		final VarRefExpr axis1 = new VarRefExpr(rtx, variable);
		// assertEquals(false, axis1.hasNext());
		axis1.update(5L);
		assertEquals(true, axis1.hasNext());
		assertEquals(5L, rtx.getNode().getNodeKey());
		axis1.update(13L);
		assertEquals(true, axis1.hasNext());
		assertEquals(13L, rtx.getNode().getNodeKey());
		axis1.update(1L);
		assertEquals(true, axis1.hasNext());
		assertEquals(1L, rtx.getNode().getNodeKey());
		assertEquals(false, axis1.hasNext());

		final VarRefExpr axis2 = new VarRefExpr(rtx, variable);
		// assertEquals(false, axis2.hasNext());
		axis2.update(13L);
		assertEquals(true, axis2.hasNext());
		assertEquals(13L, rtx.getNode().getNodeKey());
		assertEquals(false, axis2.hasNext());
		axis2.update(12L);
		assertEquals(true, axis2.hasNext());
		assertEquals(12L, rtx.getNode().getNodeKey());
		assertEquals(false, axis2.hasNext());

		rtx.close();
		wtx.abort();
		wtx.close();
		session.close();

	}

}
