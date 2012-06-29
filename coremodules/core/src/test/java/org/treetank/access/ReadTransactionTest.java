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
import static org.treetank.node.IConstants.ROOT_NODE;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.exception.AbsTTException;
import org.treetank.node.IConstants;
import org.treetank.node.interfaces.IStructNode;

public class ReadTransactionTest {

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        TestHelper.createTestDocument();
        holder = Holder.generateRtx();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testDocumentRoot() throws AbsTTException {
        assertEquals(true, holder.getNRtx().moveTo(ROOT_NODE));
        assertEquals(IConstants.ROOT, holder.getNRtx().getNode().getKind());
        assertEquals(false, holder.getNRtx().getNode().hasParent());
        assertEquals(false, ((IStructNode)holder.getNRtx().getNode()).hasLeftSibling());
        assertEquals(false, ((IStructNode)holder.getNRtx().getNode()).hasRightSibling());
        assertEquals(true, ((IStructNode)holder.getNRtx().getNode()).hasFirstChild());
        holder.getNRtx().close();
    }

    @Test
    public void testConventions() throws AbsTTException {

        // INodeReadTrx Convention 1.
        assertEquals(true, holder.getNRtx().moveTo(ROOT_NODE));
        long key = holder.getNRtx().getNode().getNodeKey();

        // INodeReadTrx Convention 2.
        assertEquals(holder.getNRtx().getNode().hasParent(), holder.getNRtx().moveTo(
            holder.getNRtx().getNode().getParentKey()));
        assertEquals(key, holder.getNRtx().getNode().getNodeKey());

        assertEquals(((IStructNode)holder.getNRtx().getNode()).hasFirstChild(), holder.getNRtx().moveTo(
            ((IStructNode)holder.getNRtx().getNode()).getFirstChildKey()));
        assertEquals(1L, holder.getNRtx().getNode().getNodeKey());

        assertEquals(false, holder.getNRtx().moveTo(Integer.MAX_VALUE));
        assertEquals(1L, holder.getNRtx().getNode().getNodeKey());

        assertEquals(((IStructNode)holder.getNRtx().getNode()).hasRightSibling(), holder.getNRtx().moveTo(
            ((IStructNode)holder.getNRtx().getNode()).getRightSiblingKey()));
        assertEquals(1L, holder.getNRtx().getNode().getNodeKey());

        assertEquals(((IStructNode)holder.getNRtx().getNode()).hasFirstChild(), holder.getNRtx().moveTo(
            ((IStructNode)holder.getNRtx().getNode()).getFirstChildKey()));
        assertEquals(4L, holder.getNRtx().getNode().getNodeKey());

        assertEquals(((IStructNode)holder.getNRtx().getNode()).hasRightSibling(), holder.getNRtx().moveTo(
            ((IStructNode)holder.getNRtx().getNode()).getRightSiblingKey()));
        assertEquals(5L, holder.getNRtx().getNode().getNodeKey());

        assertEquals(((IStructNode)holder.getNRtx().getNode()).hasLeftSibling(), holder.getNRtx().moveTo(
            ((IStructNode)holder.getNRtx().getNode()).getLeftSiblingKey()));
        assertEquals(4L, holder.getNRtx().getNode().getNodeKey());

        assertEquals(holder.getNRtx().getNode().hasParent(), holder.getNRtx().moveTo(
            holder.getNRtx().getNode().getParentKey()));
        assertEquals(1L, holder.getNRtx().getNode().getNodeKey());

        holder.getNRtx().close();
    }

}
