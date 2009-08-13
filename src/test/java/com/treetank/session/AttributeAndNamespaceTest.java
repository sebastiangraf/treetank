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
 * $Id: AttributeAndNamespaceTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.session;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.utils.DocumentCreater;

public class AttributeAndNamespaceTest {

	@Before
	public void setUp() {
		Session.removeSession(ITestConstants.PATH1);
	}

	@Test
	public void testAttribute() throws IOException {

		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		wtx.moveTo(1L);
		TestCase.assertEquals(1, wtx.getNode().getAttributeCount());
		wtx.moveToAttribute(0);
		TestCase.assertEquals("i", wtx.nameForKey(wtx.getNode().getNameKey()));

		wtx.moveTo(9L);
		TestCase.assertEquals(1, wtx.getNode().getAttributeCount());
		wtx.moveToAttribute(0);
		TestCase
				.assertEquals("p:x", wtx.nameForKey(wtx.getNode().getNameKey()));
		TestCase.assertEquals("ns", wtx.nameForKey(wtx.getNode().getURIKey()));

		wtx.abort();
		wtx.close();
		session.close();

	}

	@Test
	public void testNamespace() throws IOException {

		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		wtx.moveTo(1L);
		TestCase.assertEquals(1, wtx.getNode().getNamespaceCount());
		wtx.moveToNamespace(0);
		TestCase.assertEquals("p", wtx.nameForKey(wtx.getNode().getNameKey()));
		TestCase.assertEquals("ns", wtx.nameForKey(wtx.getNode().getURIKey()));

		wtx.abort();
		wtx.close();
		session.close();

	}

}