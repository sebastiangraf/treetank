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
 * $Id: TypeFilterTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.service.xml.xpath.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.IFilterTest;
import com.treetank.service.xml.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;

public class TypeFilterTest {

	public static final String XML = "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator + "test.xml";

	public static final String PATH = "target" + File.separator + "tnk"
			+ File.separator + "TypeFilterTest.tnk";

	@Before
	public void setUp() {

		Session.removeSession(PATH);
	}

	@Test
	public void testIFilterConvetions() {

		// Build simple test tree.
		// final ISession session = Session.beginSession(PATH);
		// final IWriteTransaction wtx = session.beginWriteTransaction();
		// TestDocument.create(wtx);
		//    

		XMLShredder.shred(XML, new SessionConfiguration(PATH));

		// Verify.
		final ISession session = Session.beginSession(PATH);
		final IReadTransaction rtx = session.beginReadTransaction();
		final IAxis axis = new XPathAxis(rtx, "a");
		final IReadTransaction xtx = axis.getTransaction();

		xtx.moveTo(9L);
		IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untyped"),
				true);
		IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:long"),
				false);

		xtx.moveTo(4L);
		IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untyped"),
				true);
		IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:double"),
				false);

		xtx.moveTo(1L);
		xtx.moveToAttribute(0);
		IFilterTest.testIFilterConventions(new TypeFilter(xtx,
				"xs:untypedAtomic"), true);

		IFilterTest.testIFilterConventions(new TypeFilter(xtx,
				"xs:anyAtomicType"), false);
		try {
			IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:bla"),
					false);
			fail("Expected a Type not found error.");
		} catch (XPathError e) {
			assertThat(e.getMessage(), is("err:XPST0051 "
					+ "Type is not defined in the in-scope schema types as an "
					+ "atomic type."));
		}

		xtx.close();
		rtx.close();
		session.close();

	}
}
