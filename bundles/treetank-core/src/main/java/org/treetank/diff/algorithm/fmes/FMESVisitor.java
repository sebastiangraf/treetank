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
package org.treetank.diff.algorithm.fmes;

import java.util.Map;

import org.w3c.dom.NodeList;

import org.treetank.access.AbsVisitorSupport;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IVisitor;
import org.treetank.exception.AbsTTException;
import org.treetank.node.*;

/**
 * Initialize data structures.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class FMESVisitor extends AbsVisitorSupport {

    /** {@link IReadTransaction} reference. */
    private final IReadTransaction mRtx;

    /** Determines if nodes are in order. */
    private Map<IItem, Boolean> mInOrder;

    /** Descendant count per node. */
    private Map<IItem, Long> mDescendants;

    /**
     * Constructor.
     * 
     * @param paramSession
     *            {@link ISession} implementation
     * @param paramInOrder
     *            {@link Map} reference to track ordered nodes
     * @param paramDescendants
     *            {@link Map} reference to track descendants per node
     * @throws AbsTTException
     *             if setting up treetank fails
     */
    public FMESVisitor(final ISession paramSession, final Map<IItem, Boolean> paramInOrder,
        final Map<IItem, Long> paramDescendants) throws AbsTTException {
        assert paramSession != null;
        assert paramInOrder != null;
        assert paramDescendants != null;
        mRtx = paramSession.beginReadTransaction();
        mInOrder = paramInOrder;
        mDescendants = paramDescendants;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final ElementNode paramNode) {
        final long nodeKey = paramNode.getNodeKey();
        mRtx.moveTo(nodeKey);
        for (int i = 0; i < paramNode.getAttributeCount(); i++) {
            mRtx.moveToAttribute(i);
            fillDataStructures();
            mRtx.moveTo(nodeKey);
        }
        for (int i = 0; i < paramNode.getNamespaceCount(); i++) {
            mRtx.moveToNamespace(i);
            fillDataStructures();
            mRtx.moveTo(nodeKey);
        }
        countDescendants();
    }

    /**
     * Fill data structures.
     */
    private void fillDataStructures() {
        final IItem node = mRtx.getNode();
        mInOrder.put(node, true);
        mDescendants.put(node, 1L);
    }

    /**
     * Count descendants of node (including self).
     */
    private void countDescendants() {
        long descendants = 1;
        final long nodeKey = mRtx.getNode().getNodeKey();
        if (mRtx.getStructuralNode().hasFirstChild()) {
            mRtx.moveToFirstChild();
            do {
                descendants += mDescendants.get(mRtx.getNode());
            } while (mRtx.getStructuralNode().hasRightSibling() && mRtx.moveToRightSibling());
        }
        mRtx.moveTo(nodeKey);
        mDescendants.put(mRtx.getNode(), descendants);
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final TextNode paramNode) {
        final long nodeKey = paramNode.getNodeKey();
        mRtx.moveTo(nodeKey);
        mInOrder.put(mRtx.getNode(), false);
        mDescendants.put(mRtx.getNode(), 1L);
    }
}
