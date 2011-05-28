/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import javax.xml.namespace.QName;

import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTUsageException;
import org.treetank.node.AbsStructNode;
import org.treetank.utils.DocumentCreater;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UpdateTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testInsertChild() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder().build());

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

    }

    @Test
    public void testInsertPath() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder().build());

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

    }

    @Test
    public void testPageBoundary() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder().build());
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
    }

    @Test(expected = TTUsageException.class)
    public void testRemoveDocument() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder().build());

        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveToDocumentRoot();

        try {
            wtx.remove();
        } finally {
            wtx.abort();
            wtx.close();
            session.close();
        }

    }

    @Test
    public void testRemoveDescendant() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder().build());
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
    }

}
