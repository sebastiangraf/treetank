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
 * $Id: SessionTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankFrameworkException;
import com.treetank.exception.TreetankIOException;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.IConstants;
import com.treetank.utils.TypedValue;

public class SessionTest {

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public void testClosed() throws IOException {

        ISession session = Session
                .beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);
        session.close();

        try {
            session.getAbsolutePath();
            TestCase.fail();
        } catch (Exception e) {
            // Must fail.
        }

        session = Session.beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);
        IReadTransaction rtx = session.beginReadTransaction();
        rtx.close();

        try {
            rtx.getNode().getAttributeCount();
            TestCase.fail();
        } catch (Exception e) {
            // Must fail.
        }

        session.close();

        try {
            session.getAbsolutePath();
            TestCase.fail();
        } catch (Exception e) {
            // Must fail.
        }
    }

    @Test
    @Ignore
    public void testNoWritesBeforeFirstCommit() {
        try {
            ISession session = Session
                    .beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);
            assertEquals(0L, new File(ITestConstants.TEST_INSERT_CHILD_PATH
                    + File.separator + "tt.tnk").length());
            session.close();
            assertEquals(0L, new File(ITestConstants.TEST_INSERT_CHILD_PATH
                    + File.separator + "tt.tnk").length());

            session = Session
                    .beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);
            assertEquals(0L, new File(ITestConstants.TEST_INSERT_CHILD_PATH
                    + File.separator + "tt.tnk").length());

            final IWriteTransaction wtx = session.beginWriteTransaction();
            wtx.commit();
            wtx.close();
            session.close();

            session = Session
                    .beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);
            final IReadTransaction rtx = session.beginReadTransaction();
            rtx.close();
            session.close();

            TestCase.assertNotSame(0L, new File(
                    ITestConstants.TEST_INSERT_CHILD_PATH + File.separator
                            + "tt.tnk").length());
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void testNonExisting() {
        try {
            final ISession session = Session
                    .beginSession(ITestConstants.NON_EXISTING_PATH);
            final Thread secondAccess = new Thread() {
                @Override
                public void run() {
                    try {
                        Session.beginSession(ITestConstants.NON_EXISTING_PATH);
                        fail();
                    } catch (final Exception e) {
                        // Must catch to pass test.
                    }
                }
            };
            secondAccess.start();
            Thread.sleep(100);
            if (secondAccess.isAlive()) {
                fail("Second access should have died!");
            }
            session.close();
        } catch (final InterruptedException exc2) {
            fail(exc2.toString());
        }
    }

    @Test
    public void testInsertChild() {
        try {
            final ISession session = Session
                    .beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);

            final IWriteTransaction wtx = session.beginWriteTransaction();

            DocumentCreater.create(wtx);

            TestCase.assertNotNull(wtx.moveToDocumentRoot());
            assertEquals(IReadTransaction.ROOT_KIND, wtx.getNode().getKind());

            TestCase.assertNotNull(wtx.moveToFirstChild());
            assertEquals(IReadTransaction.ELEMENT_KIND, wtx.getNode().getKind());
            assertEquals("p:a", wtx.nameForKey(wtx.getNode().getNameKey()));

            wtx.abort();
            wtx.close();
            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void testRevision() {
        try {
            final ISession session = Session
                    .beginSession(ITestConstants.TEST_REVISION_PATH);

            IReadTransaction rtx = session.beginReadTransaction();
            assertEquals(0L, rtx.getRevisionNumber());
            assertEquals(1L, rtx.getNodeCount());

            final IWriteTransaction wtx = session.beginWriteTransaction();
            assertEquals(0L, wtx.getRevisionNumber());
            assertEquals(1L, wtx.getNodeCount());

            // Commit and check.
            wtx.commit();
            wtx.close();

            rtx = session.beginReadTransaction();

            assertEquals(IConstants.UBP_ROOT_REVISION_NUMBER, rtx
                    .getRevisionNumber());
            assertEquals(1L, rtx.getNodeCount());
            rtx.close();

            final IReadTransaction rtx2 = session.beginReadTransaction();
            assertEquals(0L, rtx2.getRevisionNumber());
            assertEquals(1L, rtx2.getNodeCount());
            rtx2.close();

            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void testShreddedRevision() {
        try {
            final ISession session = Session
                    .beginSession(ITestConstants.TEST_SHREDDED_REVISION_PATH);

            final IWriteTransaction wtx1 = session.beginWriteTransaction();
            DocumentCreater.create(wtx1);
            assertEquals(0L, wtx1.getRevisionNumber());
            assertEquals(14L, wtx1.getNodeCount());
            wtx1.commit();
            wtx1.close();

            final IReadTransaction rtx1 = session.beginReadTransaction();
            assertEquals(0L, rtx1.getRevisionNumber());
            rtx1.moveTo(12L);
            assertEquals("bar", TypedValue.parseString(rtx1.getNode()
                    .getRawValue()));

            final IWriteTransaction wtx2 = session.beginWriteTransaction();
            assertEquals(1L, wtx2.getRevisionNumber());
            wtx2.moveTo(12L);
            wtx2.setValue("bar2");

            assertEquals("bar", TypedValue.parseString(rtx1.getNode()
                    .getRawValue()));
            assertEquals("bar2", TypedValue.parseString(wtx2.getNode()
                    .getRawValue()));
            rtx1.close();
            wtx2.abort();
            wtx2.close();

            final IReadTransaction rtx2 = session.beginReadTransaction();
            assertEquals(0L, rtx2.getRevisionNumber());
            rtx2.moveTo(12L);
            assertEquals("bar", TypedValue.parseString(rtx2.getNode()
                    .getRawValue()));
            rtx2.close();

            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void testExisting() {
        try {
            final ISession session1 = Session
                    .beginSession(ITestConstants.TEST_EXISTING_PATH);

            final IWriteTransaction wtx1 = session1.beginWriteTransaction();
            DocumentCreater.create(wtx1);
            assertEquals(0L, wtx1.getRevisionNumber());
            wtx1.commit();
            wtx1.close();
            session1.close();

            final ISession session2 = Session
                    .beginSession(ITestConstants.TEST_EXISTING_PATH);
            final IReadTransaction rtx1 = session2.beginReadTransaction();
            assertEquals(0L, rtx1.getRevisionNumber());
            rtx1.moveTo(12L);
            assertEquals("bar", TypedValue.parseString(rtx1.getNode()
                    .getRawValue()));

            final IWriteTransaction wtx2 = session2.beginWriteTransaction();
            assertEquals(1L, wtx2.getRevisionNumber());
            wtx2.moveTo(12L);
            wtx2.setValue("bar2");

            assertEquals("bar", TypedValue.parseString(rtx1.getNode()
                    .getRawValue()));
            assertEquals("bar2", TypedValue.parseString(wtx2.getNode()
                    .getRawValue()));

            rtx1.close();
            wtx2.commit();
            wtx2.close();
            session2.close();

            final ISession session3 = Session
                    .beginSession(ITestConstants.TEST_EXISTING_PATH);
            final IReadTransaction rtx2 = session3.beginReadTransaction();
            assertEquals(1L, rtx2.getRevisionNumber());
            rtx2.moveTo(12L);
            assertEquals("bar2", TypedValue.parseString(rtx2.getNode()
                    .getRawValue()));

            rtx2.close();
            session3.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }

    }

    @Test
    public void testIdempotentClose() {
        try {
            final ISession session = Session
                    .beginSession(ITestConstants.TEST_EXISTING_PATH);

            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);
            wtx.commit();
            wtx.close();
            wtx.close();

            final IReadTransaction rtx = session.beginReadTransaction();
            assertEquals(14L, rtx.getNodeCount());
            assertEquals(false, rtx.moveTo(14L));
            rtx.close();
            rtx.close();

            session.close();
            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testAutoCommit() throws IllegalStateException {

        final ISession session = Session
                .beginSession(ITestConstants.TEST_EXISTING_PATH);

        IWriteTransaction wtx = null;
        try {
            wtx = session.beginWriteTransaction();

            DocumentCreater.create(wtx);

            session.close();
        } catch (final TreetankIOException exc) {
            fail();
        } catch (final IllegalStateException exc2) {
            throw exc2;
        } finally {
            try {
                wtx.abort();
            } catch (final TreetankIOException exc3) {
                throw new RuntimeException(exc3);
            }
            session.close();
        }
    }

    @Test
    public void testAutoClose() {
        try {
            final ISession session = Session
                    .beginSession(ITestConstants.TEST_EXISTING_PATH);

            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);
            wtx.commit();
            session.beginReadTransaction();

            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void testTransactionCount() {
        try {
            final ISession session = Session
                    .beginSession(ITestConstants.TEST_EXISTING_PATH);

            final IWriteTransaction wtx = session.beginWriteTransaction();
            Assert.assertEquals(1, session.getWriteTransactionCount());
            Assert.assertEquals(0, session.getReadTransactionCount());
            wtx.close();

            final IReadTransaction rtx = session.beginReadTransaction();
            Assert.assertEquals(0, session.getWriteTransactionCount());
            Assert.assertEquals(1, session.getReadTransactionCount());

            final IReadTransaction rtx1 = session.beginReadTransaction();
            Assert.assertEquals(0, session.getWriteTransactionCount());
            Assert.assertEquals(2, session.getReadTransactionCount());

            rtx.close();
            rtx1.close();

            Assert.assertEquals(0, session.getWriteTransactionCount());
            Assert.assertEquals(0, session.getReadTransactionCount());

            session.close();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

}
