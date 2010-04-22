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
 * $Id: FilterAxisTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.axis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.utils.DocumentCreater;

public class FilterAxisTest {
	@Before
	public void setUp() throws TreetankException {
		TestHelper.deleteEverything();
	}

	@Test
	public void testNameAxisTest() throws TreetankException {
		// Build simple test tree.
		final IDatabase database = TestHelper
				.getDatabase(PATHS.PATH1.getFile());
		final ISession session = database.getSession();
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		wtx.moveToDocumentRoot();
		IAxisTest.testIAxisConventions(new FilterAxis(new DescendantAxis(wtx),
				new NameFilter(wtx, "b")), new long[] { 5L, 9L });

		wtx.abort();
		wtx.close();
		session.close();
		database.close();
	}

	@Test
	public void testValueAxisTest() throws TreetankException {
		// Build simple test tree.
		final IDatabase database = TestHelper
				.getDatabase(PATHS.PATH1.getFile());
		final ISession session = database.getSession();
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		wtx.moveToDocumentRoot();
		IAxisTest.testIAxisConventions(new FilterAxis(new DescendantAxis(wtx),
				new ValueFilter(wtx, "foo")), new long[] { 6L });

		wtx.abort();
		wtx.close();
		session.close();
		database.close();
	}

	@Test
	public void testValueAndNameAxisTest() throws TreetankException {
		// Build simple test tree.
		final IDatabase database = TestHelper
				.getDatabase(PATHS.PATH1.getFile());
		final ISession session = database.getSession();
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		wtx.moveTo(1L);
		IAxisTest.testIAxisConventions(new FilterAxis(new AttributeAxis(wtx),
				new NameFilter(wtx, "i"), new ValueFilter(wtx, "j")),
				new long[] { 2L });

		wtx.moveTo(9L);
		IAxisTest.testIAxisConventions(new FilterAxis(new AttributeAxis(wtx),
				new NameFilter(wtx, "y"), new ValueFilter(wtx, "y")),
				new long[] {});

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
