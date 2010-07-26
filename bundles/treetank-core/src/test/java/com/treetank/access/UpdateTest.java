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

package com.treetank.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.node.AbsStructNode;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

public class UpdateTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testInsertChild() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();

        IWriteTransaction wtx = session.beginWriteTransaction();
        wtx.commit();
        wtx.close();

        IReadTransaction rtx = session.beginReadTransaction();
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
            assertEquals(Integer.toString(i), TypedValue.parseString(rtx.getNode().getRawValue()));
            assertEquals(i, rtx.getRevisionNumber());
            rtx.close();
        }

        rtx = session.beginReadTransaction();
        rtx.moveToDocumentRoot();
        rtx.moveToFirstChild();
        assertEquals("10", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(10L, rtx.getRevisionNumber());
        rtx.close();

        session.close();
        database.close();

    }

    @Test
    public void testInsertPath() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();

        IWriteTransaction wtx = session.beginWriteTransaction();

        wtx.commit();
        wtx.close();

        wtx = session.beginWriteTransaction();
        assertNotNull(wtx.moveToDocumentRoot());
        assertEquals(1L, wtx.insertElementAsFirstChild(new QName("")));

        assertEquals(2L, wtx.insertElementAsFirstChild(new QName("")));
        assertEquals(3L, wtx.insertElementAsFirstChild(new QName("")));

        assertNotNull(wtx.moveToParent());
        assertEquals(4L, wtx.insertElementAsRightSibling(new QName("")));

        wtx.commit();
        wtx.close();

        final IWriteTransaction wtx2 = session.beginWriteTransaction();

        assertNotNull(wtx2.moveToDocumentRoot());
        assertEquals(5L, wtx2.insertElementAsFirstChild(new QName("")));

        wtx2.commit();
        wtx2.close();

        session.close();
        database.close();

    }

    @Test
    public void testPageBoundary() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        // Document root.
        wtx.insertElementAsFirstChild(new QName(""));
        for (int i = 0; i < 256 * 256 + 1; i++) {
            wtx.insertTextAsRightSibling("");
        }

        assertTrue(wtx.moveTo(2L));
        assertEquals(2L, wtx.getNode().getNodeKey());

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test(expected = TreetankUsageException.class)
    public void testRemoveDocument() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();

        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveToDocumentRoot();

        try {
            wtx.remove();

            session.close();
            database.close();
        } finally {
            wtx.abort();
            wtx.close();
            session.close();
            database.close();
        }

    }

    @Test
    public void testRemoveDescendant() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.moveTo(5L);
        wtx.remove();
        wtx.commit();
        wtx.close();
        final IReadTransaction rtx = session.beginReadTransaction();
        assertEquals(0, rtx.getNode().getNodeKey());
        assertTrue(rtx.moveToFirstChild());
        assertEquals(1, rtx.getNode().getNodeKey());
        assertEquals(4, ((AbsStructNode)rtx.getNode()).getChildCount());
        assertTrue(rtx.moveToFirstChild());
        assertEquals(4, rtx.getNode().getNodeKey());
        assertTrue(rtx.moveToRightSibling());
        assertEquals(8, rtx.getNode().getNodeKey());
        assertTrue(rtx.moveToRightSibling());
        assertEquals(9, rtx.getNode().getNodeKey());
        assertTrue(rtx.moveToRightSibling());
        assertEquals(13, rtx.getNode().getNodeKey());
        rtx.close();
        session.close();
        database.close();
    }

}
