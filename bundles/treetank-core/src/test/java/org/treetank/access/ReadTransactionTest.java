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

import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ReadTransactionTest {

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        TestHelper.createTestDocument();
        holder = Holder.generate();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testEmptyRtx() throws AbsTTException {
        assertFalse(PATHS.PATH2.getFile().exists());
        Database.createDatabase(PATHS.PATH2.getFile(), new DatabaseConfiguration.Builder());
        final IDatabase db = Database.openDatabase(PATHS.PATH2.getFile());
        final ISession session = db.getSession(new SessionConfiguration.Builder());
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.getRevisionNumber();
        rtx.close();
        session.close();
        Database.closeDatabase(PATHS.PATH1.getFile());
    }

    @Test
    public void testDocumentRoot() throws AbsTTException {
        final IReadTransaction rtx = holder.rtx;
        assertEquals(true, rtx.moveToDocumentRoot());
        assertEquals(ENodes.ROOT_KIND, rtx.getNode().getKind());
        assertEquals(false, rtx.getNode().hasParent());
        assertEquals(false, ((AbsStructNode)rtx.getNode()).hasLeftSibling());
        assertEquals(false, ((AbsStructNode)rtx.getNode()).hasRightSibling());
        assertEquals(true, ((AbsStructNode)rtx.getNode()).hasFirstChild());
        rtx.close();
    }

    @Test
    public void testConventions() throws AbsTTException {
        final IReadTransaction rtx = holder.rtx;

        // IReadTransaction Convention 1.
        assertEquals(true, rtx.moveToDocumentRoot());
        long key = rtx.getNode().getNodeKey();

        // IReadTransaction Convention 2.
        assertEquals(rtx.getNode().hasParent(), rtx.moveToParent());
        assertEquals(key, rtx.getNode().getNodeKey());

        assertEquals(((AbsStructNode)rtx.getNode()).hasFirstChild(), rtx.moveToFirstChild());
        assertEquals(1L, rtx.getNode().getNodeKey());

        assertEquals(false, rtx.moveTo(Integer.MAX_VALUE));
        assertEquals(false, rtx.moveTo(Integer.MIN_VALUE));
        assertEquals(1L, rtx.getNode().getNodeKey());

        assertEquals(((AbsStructNode)rtx.getNode()).hasRightSibling(), rtx.moveToRightSibling());
        assertEquals(1L, rtx.getNode().getNodeKey());

        assertEquals(((AbsStructNode)rtx.getNode()).hasFirstChild(), rtx.moveToFirstChild());
        assertEquals(4L, rtx.getNode().getNodeKey());

        assertEquals(((AbsStructNode)rtx.getNode()).hasRightSibling(), rtx.moveToRightSibling());
        assertEquals(5L, rtx.getNode().getNodeKey());

        assertEquals(((AbsStructNode)rtx.getNode()).hasLeftSibling(), rtx.moveToLeftSibling());
        assertEquals(4L, rtx.getNode().getNodeKey());

        assertEquals(rtx.getNode().hasParent(), rtx.moveToParent());
        assertEquals(1L, rtx.getNode().getNodeKey());

        rtx.close();
    }

}
