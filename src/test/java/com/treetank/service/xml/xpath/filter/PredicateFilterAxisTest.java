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
 * $Id: PredicateFilterAxisTest.java 4433 2008-08-28 14:26:02Z scherer $
 */

package com.treetank.service.xml.xpath.filter;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.IAxisTest;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

/**
 * JUnit-test class to test the functionality of the PredicateAxis.
 * 
 * @author Tina Scherer
 */
public class PredicateFilterAxisTest {


	@Before
	public void setUp() {

		Session.removeSession(ITestConstants.PATH1);
	}

	@Test
	public void testPredicates() throws IOException {

		// Build simple test tree.
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);
		wtx.commit();
		IReadTransaction rtx = session.beginReadTransaction();

		// Find descendants starting from nodeKey 0L (root).
		rtx.moveToDocumentRoot();

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a[@i]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/b[@p:x]"),
				new long[] { 9L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[text()]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[element()]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(
				new XPathAxis(rtx, "p:a[node()/text()]"), new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[./node()]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"p:a[./node()/node()/node()]"), new long[] {});

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[//element()]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[/text()]"),
				new long[] {});

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3<4]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[13>=4]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[13.0>=4]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[4 = 4]"),
				new long[] { 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3=4]"),
				new long[] {});

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3.2 = 3.22]"),
				new long[] {});

		rtx.moveTo(1L);

		IAxisTest
				.testIAxisConventions(new XPathAxis(rtx, "child::b[child::c]"),
						new long[] { 5L, 9L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"child::*[text() or c]"), new long[] { 5l, 9L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"child::*[text() or c], /node(), //c"), new long[] { 5l, 9L,
				1L, 7L, 11L });

		rtx.close();
		wtx.abort();
		wtx.close();
		session.close();

	}

}