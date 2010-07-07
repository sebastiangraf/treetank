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

package com.treetank.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.node.ENodes;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.IConstants;
import com.treetank.utils.TypedValue;

public class SessionTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testClosed() throws TreetankException {

        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        database.close();

        assertEquals(null, database.getFile());
        database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        IReadTransaction rtx = session.beginReadTransaction();
        rtx.close();

        try {
            final IItem node = rtx.getNode();
            node.getNodeKey();
            TestCase.fail();
        } catch (Exception e) {
            // Must fail.
        }

        session.close();
        database.close();

        assertEquals(null, database.getFile());
    }

    @Test
    @Ignore
    public void testNoWritesBeforeFirstCommit() throws TreetankException {
        // ISession session = Session
        // .beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);
        // assertEquals(0L, new File(ITestConstants.TEST_INSERT_CHILD_PATH
        // + File.separator + "tt.tnk").length());
        // session.close();
        // assertEquals(0L, new File(ITestConstants.TEST_INSERT_CHILD_PATH
        // + File.separator + "tt.tnk").length());
        //
        // session =
        // Session.beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);
        // assertEquals(0L, new File(ITestConstants.TEST_INSERT_CHILD_PATH
        // + File.separator + "tt.tnk").length());
        //
        // final IWriteTransaction wtx = session.beginWriteTransaction();
        // wtx.commit();
        // wtx.close();
        // session.close();
        //
        // session =
        // Session.beginSession(ITestConstants.TEST_INSERT_CHILD_PATH);
        // final IReadTransaction rtx = session.beginReadTransaction();
        // rtx.close();
        // session.close();
        //
        // TestCase.assertNotSame(0L, new File(
        // ITestConstants.TEST_INSERT_CHILD_PATH + File.separator
        // + "tt.tnk").length());
    }

    @Test
    public void testNonExisting() throws TreetankException,
            InterruptedException {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH1
                .getFile());
        assertTrue(database == database2);
        database.close();
    }

    @Test
    public void testInsertChild() throws TreetankException {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());

        final ISession session = database.getSession();

        final IWriteTransaction wtx = session.beginWriteTransaction();

        DocumentCreater.create(wtx);

        TestCase.assertNotNull(wtx.moveToDocumentRoot());
        assertEquals(ENodes.ROOT_KIND, wtx.getNode().getKind());

        TestCase.assertNotNull(wtx.moveToFirstChild());
        assertEquals(ENodes.ELEMENT_KIND, wtx.getNode().getKind());
        assertEquals("p:a", wtx.nameForKey(wtx.getNode().getNameKey()));

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testRevision() throws TreetankException {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();

        IReadTransaction rtx = session.beginReadTransaction();
        assertEquals(0L, rtx.getRevisionNumber());

        final IWriteTransaction wtx = session.beginWriteTransaction();
        assertEquals(0L, wtx.getRevisionNumber());

        // Commit and check.
        wtx.commit();
        wtx.close();

        rtx = session.beginReadTransaction();

        assertEquals(IConstants.UBP_ROOT_REVISION_NUMBER,
                rtx.getRevisionNumber());
        rtx.close();

        final IReadTransaction rtx2 = session.beginReadTransaction();
        assertEquals(0L, rtx2.getRevisionNumber());
        rtx2.close();

        session.close();
    }

    @Test
    public void testShreddedRevision() throws TreetankException {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();

        final IWriteTransaction wtx1 = session.beginWriteTransaction();
        DocumentCreater.create(wtx1);
        assertEquals(0L, wtx1.getRevisionNumber());
        wtx1.commit();
        wtx1.close();

        final IReadTransaction rtx1 = session.beginReadTransaction();
        assertEquals(0L, rtx1.getRevisionNumber());
        rtx1.moveTo(12L);
        assertEquals("bar",
                TypedValue.parseString(rtx1.getNode().getRawValue()));

        final IWriteTransaction wtx2 = session.beginWriteTransaction();
        assertEquals(1L, wtx2.getRevisionNumber());
        wtx2.moveTo(12L);
        wtx2.setValue("bar2");

        assertEquals("bar",
                TypedValue.parseString(rtx1.getNode().getRawValue()));
        assertEquals("bar2",
                TypedValue.parseString(wtx2.getNode().getRawValue()));
        rtx1.close();
        wtx2.abort();
        wtx2.close();

        final IReadTransaction rtx2 = session.beginReadTransaction();
        assertEquals(0L, rtx2.getRevisionNumber());
        rtx2.moveTo(12L);
        assertEquals("bar",
                TypedValue.parseString(rtx2.getNode().getRawValue()));
        rtx2.close();

        session.close();
        database.close();
    }

    @Test
    public void testExisting() throws TreetankException {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session1 = database.getSession();

        final IWriteTransaction wtx1 = session1.beginWriteTransaction();
        DocumentCreater.create(wtx1);
        assertEquals(0L, wtx1.getRevisionNumber());
        wtx1.commit();
        wtx1.close();
        session1.close();

        final ISession session2 = database.getSession();
        final IReadTransaction rtx1 = session2.beginReadTransaction();
        assertEquals(0L, rtx1.getRevisionNumber());
        rtx1.moveTo(12L);
        assertEquals("bar",
                TypedValue.parseString(rtx1.getNode().getRawValue()));

        final IWriteTransaction wtx2 = session2.beginWriteTransaction();
        assertEquals(1L, wtx2.getRevisionNumber());
        wtx2.moveTo(12L);
        wtx2.setValue("bar2");

        assertEquals("bar",
                TypedValue.parseString(rtx1.getNode().getRawValue()));
        assertEquals("bar2",
                TypedValue.parseString(wtx2.getNode().getRawValue()));

        rtx1.close();
        wtx2.commit();
        wtx2.close();
        session2.close();
        database.close();

        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH1
                .getFile());
        final ISession session3 = database2.getSession();
        final IReadTransaction rtx2 = session3.beginReadTransaction();
        assertEquals(1L, rtx2.getRevisionNumber());
        rtx2.moveTo(14L);
        assertEquals("bar2",
                TypedValue.parseString(rtx2.getNode().getRawValue()));

        rtx2.close();
        session3.close();
        database2.close();

    }

    @Test
    public void testIdempotentClose() throws TreetankException {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();
        wtx.close();

        final IReadTransaction rtx = session.beginReadTransaction();
        assertEquals(false, rtx.moveTo(14L));
        rtx.close();
        rtx.close();

        session.close();
        session.close();

        database.close();
        database.close();
    }

    @Test
    public void testAutoCommit() throws TreetankException {

        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();

        final IWriteTransaction wtx = session.beginWriteTransaction();

        DocumentCreater.create(wtx);

        database.close();
    }

    @Test
    public void testAutoClose() throws TreetankException {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();

        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        session.beginReadTransaction();

        database.close();
    }

    @Test
    public void testTransactionCount() throws TreetankException {
        final IDatabase database = TestHelper
                .getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();

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
        database.close();
    }

}
