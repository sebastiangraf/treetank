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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.DummyNode;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.settings.EFixed;
import org.treetank.utils.DocumentCreater;
import org.treetank.utils.IConstants;
import org.treetank.utils.TypedValue;

public class SessionTest {

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        holder = Holder.generateRtx();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testSingleton() throws AbsTTException {
        final IDatabase database = Holder.generateSession().getDatabase();
        assertEquals(database, holder.getDatabase());
        final ISession session =
            database.getSession(new SessionConfiguration.Builder(TestHelper.RESOURCE).build());
        assertEquals(session, holder.getSession());
        session.close();
        final ISession session2 =
            database.getSession(new SessionConfiguration.Builder(TestHelper.RESOURCE).build());
        assertNotSame(session2, holder.getSession());
        database.close();

    }

    @Test
    public void testClosed() throws AbsTTException {
        IReadTransaction rtx = holder.getRtx();
        rtx.close();

        try {
            final IItem node = rtx.getNode();
            node.getNodeKey();
            fail();
        } catch (Exception e) {
            // Must fail.
        } finally {
            holder.getSession().close();
        }
    }

    @Test
    public void testNonExisting() throws AbsTTException, InterruptedException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH1.getFile());
        assertTrue(database == database2);
    }

    @Test
    public void testInsertChild() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        assertNotNull(wtx.moveToDocumentRoot());
        assertEquals(ENodes.ROOT_KIND, wtx.getNode().getKind());

        assertNotNull(wtx.moveToFirstChild());
        assertEquals(ENodes.ELEMENT_KIND, wtx.getNode().getKind());
        assertEquals("p:a", wtx.nameForKey(wtx.getNode().getNameKey()));

        wtx.abort();
        wtx.close();
    }

    @Test
    public void testRevision() throws AbsTTException {
        IReadTransaction rtx = holder.getRtx();
        assertEquals(0L, rtx.getRevisionNumber());

        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        assertEquals(0L, wtx.getRevisionNumber());

        // Commit and check.
        wtx.commit();
        wtx.close();

        rtx = holder.getSession().beginReadTransaction();

        assertEquals(IConstants.UBP_ROOT_REVISION_NUMBER, rtx.getRevisionNumber());
        rtx.close();

        final IReadTransaction rtx2 = holder.getSession().beginReadTransaction();
        assertEquals(0L, rtx2.getRevisionNumber());
        rtx2.close();
    }

    @Test
    public void testShreddedRevision() throws AbsTTException {

        final IWriteTransaction wtx1 = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx1);
        assertEquals(0L, wtx1.getRevisionNumber());
        wtx1.commit();
        wtx1.close();

        final IReadTransaction rtx1 = holder.getSession().beginReadTransaction();
        assertEquals(0L, rtx1.getRevisionNumber());
        rtx1.moveTo(12L);
        assertEquals("bar", TypedValue.parseString(rtx1.getNode().getRawValue()));

        final IWriteTransaction wtx2 = holder.getSession().beginWriteTransaction();
        assertEquals(1L, wtx2.getRevisionNumber());
        wtx2.moveTo(12L);
        wtx2.setValue("bar2");

        assertEquals("bar", TypedValue.parseString(rtx1.getNode().getRawValue()));
        assertEquals("bar2", TypedValue.parseString(wtx2.getNode().getRawValue()));
        rtx1.close();
        wtx2.abort();
        wtx2.close();

        final IReadTransaction rtx2 = holder.getSession().beginReadTransaction();
        assertEquals(0L, rtx2.getRevisionNumber());
        rtx2.moveTo(12L);
        assertEquals("bar", TypedValue.parseString(rtx2.getNode().getRawValue()));
        rtx2.close();
    }

    @Test
    public void testExisting() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session1 =
            database.getSession(new SessionConfiguration.Builder(TestHelper.RESOURCE).build());

        final IWriteTransaction wtx1 = session1.beginWriteTransaction();
        DocumentCreater.create(wtx1);
        assertEquals(0L, wtx1.getRevisionNumber());
        wtx1.commit();
        wtx1.close();
        session1.close();

        final ISession session2 =
            database.getSession(new SessionConfiguration.Builder(TestHelper.RESOURCE).build());
        final IReadTransaction rtx1 = session2.beginReadTransaction();
        assertEquals(0L, rtx1.getRevisionNumber());
        rtx1.moveTo(12L);
        assertEquals("bar", TypedValue.parseString(rtx1.getNode().getRawValue()));

        final IWriteTransaction wtx2 = session2.beginWriteTransaction();
        assertEquals(1L, wtx2.getRevisionNumber());
        wtx2.moveTo(12L);
        wtx2.setValue("bar2");

        assertEquals("bar", TypedValue.parseString(rtx1.getNode().getRawValue()));
        assertEquals("bar2", TypedValue.parseString(wtx2.getNode().getRawValue()));

        rtx1.close();
        wtx2.commit();
        wtx2.close();
        session2.close();

        final IDatabase database2 = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session3 =
            database2.getSession(new SessionConfiguration.Builder(TestHelper.RESOURCE).build());
        final IReadTransaction rtx2 = session3.beginReadTransaction();
        assertEquals(1L, rtx2.getRevisionNumber());
        rtx2.moveTo(12L);
        assertEquals("bar2", TypedValue.parseString(rtx2.getNode().getRawValue()));

        rtx2.close();
        session3.close();

    }

    @Test
    public void testIdempotentClose() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();
        wtx.close();

        final IReadTransaction rtx = holder.getSession().beginReadTransaction();
        assertEquals(false, rtx.moveTo(14L));
        rtx.close();
        rtx.close();
        holder.getSession().close();

    }

    @Test
    public void testGetStructuralNode() throws AbsTTException {

        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();

        DocumentCreater.create(wtx);
        wtx.moveToDocumentRoot();
        assertEquals(DocumentRootNode.class, wtx.getStructuralNode().getClass());
        wtx.moveToFirstChild();
        assertEquals(ElementNode.class, wtx.getStructuralNode().getClass());
        wtx.moveToAttribute(0);
        assertEquals(DummyNode.class, wtx.getStructuralNode().getClass());
        assertFalse(wtx.getStructuralNode().hasLeftSibling());
        assertFalse(wtx.getStructuralNode().hasRightSibling());
        assertFalse(wtx.getStructuralNode().hasFirstChild());
        assertEquals(((Long)EFixed.NULL_NODE_KEY.getStandardProperty()).longValue(), wtx.getStructuralNode()
            .getRightSiblingKey());
        assertEquals(((Long)EFixed.NULL_NODE_KEY.getStandardProperty()).longValue(), wtx.getStructuralNode()
            .getRightSiblingKey());
        wtx.moveToParent();
        wtx.moveToNamespace(0);
        assertEquals(DummyNode.class, wtx.getStructuralNode().getClass());
        assertFalse(wtx.getStructuralNode().hasLeftSibling());
        assertFalse(wtx.getStructuralNode().hasRightSibling());
        assertFalse(wtx.getStructuralNode().hasFirstChild());
        assertEquals(((Long)EFixed.NULL_NODE_KEY.getStandardProperty()).longValue(), wtx.getStructuralNode()
            .getRightSiblingKey());
        assertEquals(((Long)EFixed.NULL_NODE_KEY.getStandardProperty()).longValue(), wtx.getStructuralNode()
            .getRightSiblingKey());
        wtx.abort();
        wtx.close();
    }

    @Test
    public void testAutoCommit() throws AbsTTException {
        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();

        DocumentCreater.create(wtx);
    }

    @Test
    public void testAutoClose() throws AbsTTException {

        final IWriteTransaction wtx = holder.getSession().beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        holder.getSession().beginReadTransaction();
    }
}
