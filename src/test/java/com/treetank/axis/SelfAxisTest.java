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
 * $Id: SelfAxisTest.java 4376 2008-08-25 07:27:39Z kramis $
 */

package com.treetank.axis;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

public class SelfAxisTest {

	public static final String PATH = "target" + File.separator + "tnk"
			+ File.separator + "SelfAxisTest.tnk";

	@Before
	public void setUp() {
		Session.removeSession(PATH);
	}

	@Test
	public void testIterate() {

		final ISession session = Session.beginSession(PATH);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);

		wtx.moveTo(4L);
		IAxisTest.testIAxisConventions(new SelfAxis(wtx), new long[] { 4L });

		wtx.moveTo(8L);
		IAxisTest.testIAxisConventions(new SelfAxis(wtx), new long[] { 8L });

		wtx.abort();
		wtx.close();
		session.close();

	}

}
