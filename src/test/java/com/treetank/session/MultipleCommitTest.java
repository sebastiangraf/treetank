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
 * $Id: MultipleCommitTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.session;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.axis.PostOrderAxis;
import com.treetank.utils.DocumentCreater;

public class MultipleCommitTest {


	@Before
	public void setUp() {
		Session.removeSession(ITestConstants.PATH1);
	}

	@Test
	public void test() {
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		TestCase.assertEquals(0L, wtx.getRevisionNumber());
		TestCase.assertEquals(1L, wtx.getNodeCount());
		wtx.commit();

		wtx.insertElementAsFirstChild("foo", "");
		TestCase.assertEquals(1L, wtx.getRevisionNumber());
		TestCase.assertEquals(2L, wtx.getNodeCount());
		wtx.abort();

		TestCase.assertEquals(1L, wtx.getRevisionNumber());
		TestCase.assertEquals(1L, wtx.getNodeCount());
		wtx.close();

		session.close();
	}

	@Test
	public void testAutoCommit() {
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction(100, 1);
		DocumentCreater.create(wtx);
		wtx.close();

		final IReadTransaction rtx = session.beginReadTransaction();
		Assert.assertEquals(14, rtx.getNodeCount());
		rtx.close();
		session.close();
	}

	@Test
	public void testAttributeRemove() {
		final ISession session = Session.beginSession(ITestConstants.PATH1);
		final IWriteTransaction wtx = session.beginWriteTransaction();
		DocumentCreater.create(wtx);
		wtx.commit();
		wtx.moveToDocumentRoot();

		final IAxis postorderAxis = new PostOrderAxis(wtx);
		while (postorderAxis.hasNext()) {
			postorderAxis.next();
			if (wtx.getNode().getAttributeCount() > 0) {
				for (int i = 0, attrCount = wtx.getNode().getAttributeCount(); i < attrCount; i++) {
					wtx.moveToAttribute(i);
					wtx.remove();
				}
			}
		}
		wtx.commit();
		wtx.moveToDocumentRoot();

		int attrTouch = 0;
		final IAxis descAxis = new DescendantAxis(wtx);
		while (descAxis.hasNext()) {
			descAxis.next();
			for (int i = 0, attrCount = wtx.getNode().getAttributeCount(); i < attrCount; i++) {
				if (wtx.moveToAttribute(i)) {
					attrTouch++;
				} else {
					throw new IllegalStateException("Should never occur!");
				}
			}
		}
		wtx.close();
		session.close();
		Assert.assertEquals(0, attrTouch);

	}

}
