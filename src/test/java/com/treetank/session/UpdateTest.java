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
 * $Id: UpdateTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankFrameworkException;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

public class UpdateTest {

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public void testInsertChild() {
        try {
            ISession session = Session.beginSession(ITestConstants.PATH1);

            // Document root.
            IWriteTransaction wtx = session.beginWriteTransaction();
            wtx.commit();
            wtx.close();

            IReadTransaction rtx = session.beginReadTransaction();
            assertEquals(1L, rtx.getNodeCount());
            assertEquals(0L, rtx.getRevisionNumber());
            rtx.close();

            // Insert 100 children.
            for (int i = 1; i <= 10; i++) {
                wtx = session.beginWriteTransaction();
                wtx.moveToDocumentRoot();
                wtx.insertTextAsFirstChild(Integer.toString(i));
                wtx.commit();
                wtx.close();

                rtx = session.beginReadTransaction();
                rtx.moveToDocumentRoot();
                rtx.moveToFirstChild();
                assertEquals(Integer.toString(i), TypedValue.parseString(rtx
                        .getNode().getRawValue()));
                assertEquals(i + 1L, rtx.getNodeCount());
                assertEquals(i, rtx.getRevisionNumber());
                rtx.close();
            }

            rtx = session.beginReadTransaction();
            rtx.moveToDocumentRoot();
            rtx.moveToFirstChild();
            assertEquals("10", TypedValue.parseString(rtx.getNode()
                    .getRawValue()));
            assertEquals(11L, rtx.getNodeCount());
            assertEquals(10L, rtx.getRevisionNumber());
            rtx.close();

            session.close();
        } catch (final Exception exc) {
            exc.printStackTrace();
        }

    }

    @Test
    public void testInsertPath() {
        try {
            final ISession session = Session.beginSession(ITestConstants.PATH1);

            IWriteTransaction wtx = session.beginWriteTransaction();
            wtx.commit();
            wtx.close();

            wtx = session.beginWriteTransaction();
            TestCase.assertNotNull(wtx.moveToDocumentRoot());
            assertEquals(1L, wtx.insertElementAsFirstChild("", ""));

            assertEquals(2L, wtx.insertElementAsFirstChild("", ""));
            assertEquals(3L, wtx.insertElementAsFirstChild("", ""));

            TestCase.assertNotNull(wtx.moveToParent());
            assertEquals(4L, wtx.insertElementAsRightSibling("", ""));

            wtx.commit();
            wtx.close();

            final IWriteTransaction wtx2 = session.beginWriteTransaction();

            TestCase.assertNotNull(wtx2.moveToDocumentRoot());
            assertEquals(5L, wtx2.insertElementAsFirstChild("", ""));

            wtx2.commit();
            wtx2.close();

            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void testPageBoundary() {
        try {
            ISession session = Session.beginSession(ITestConstants.PATH1);

            // Document root.
            IWriteTransaction wtx = session.beginWriteTransaction();

            for (int i = 0; i < 256 * 256 + 1; i++) {
                wtx.insertTextAsFirstChild("");
            }

            TestCase.assertNotNull(wtx.moveTo(2L));
            assertEquals(2L, wtx.getNode().getNodeKey());

            wtx.abort();
            wtx.close();
            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void testRemoveDocument() {
        try {
            final ISession session = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);

            try {
                wtx.moveToDocumentRoot();
                wtx.remove();
                TestCase.fail();
            } catch (Exception e) {
                // Must fail.
            }

            wtx.abort();
            wtx.close();
            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void testRemoveDescendant() {
        try {
            final ISession session = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);

            wtx.moveTo(2L);
            wtx.remove();

            wtx.abort();
            wtx.close();
            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

}
