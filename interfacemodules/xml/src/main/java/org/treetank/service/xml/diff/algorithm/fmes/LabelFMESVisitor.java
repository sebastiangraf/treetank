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
package org.treetank.service.xml.diff.algorithm.fmes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.treetank.access.NodeReadTrx;
import org.treetank.api.INodeReadTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.ElementNode;
import org.treetank.node.TextNode;
import org.treetank.node.interfaces.INode;

/**
 * Label visitor.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class LabelFMESVisitor {

    /** {@link IReadTransaction} implementation. */
    private final INodeReadTrx mRtx;

    /** For each node type: list of inner nodes. */
    private final Map<Integer, List<INode>> mLabels;

    /** For each node type: list of leaf nodes. */
    private final Map<Integer, List<INode>> mLeafLabels;

    /**
     * Constructor.
     * 
     * @param paramSession
     *            {@link ISession} implementation
     * @throws TTException
     *             if setting up treetank fails
     */
    public LabelFMESVisitor(final ISession paramSession) throws TTException {
        mRtx = new NodeReadTrx(paramSession.beginPageReadTransaction(paramSession.getMostRecentVersion()));
        mLabels = new HashMap<Integer, List<INode>>();
        mLeafLabels = new HashMap<Integer, List<INode>>();
    }

    /**
     * Visiting an {@link ElementNode}.
     * 
     * @param pNode
     *            to be visited
     * @throws TTIOException
     */
    public void visit(final ElementNode pNode) throws TTIOException {
        final long nodeKey = pNode.getNodeKey();
        mRtx.moveTo(nodeKey);
        for (int i = 0; i < pNode.getAttributeCount(); i++) {
            mRtx.moveToAttribute(i);
            addLeafLabel();
            mRtx.moveTo(nodeKey);
        }
        for (int i = 0; i < pNode.getNamespaceCount(); i++) {
            mRtx.moveToNamespace(i);
            addLeafLabel();
            mRtx.moveTo(nodeKey);
        }
        if (pNode.hasFirstChild() || pNode.getAttributeCount() > 0
            || pNode.getNamespaceCount() > 0) {
            if (!mLabels.containsKey(pNode.getKind())) {
                mLabels.put(pNode.getKind(), new ArrayList<INode>());
            }
            mLabels.get(pNode.getKind()).add(pNode);
        }
    }

    /**
     * Visiting a {@link TextNode}
     * 
     * @param pNode
     *            to be visited
     * @throws TTIOException
     */
    public void visit(final TextNode pNode) throws TTIOException {
        mRtx.moveTo(pNode.getNodeKey());
        addLeafLabel();
    }

    /**
     * Add leaf node label.
     */
    private void addLeafLabel() {
        final int nodeKind = mRtx.getNode().getKind();
        if (!mLeafLabels.containsKey(nodeKind)) {
            mLeafLabels.put(nodeKind, new ArrayList<INode>());
        }
        mLeafLabels.get(nodeKind).add(mRtx.getNode());
    }

    /**
     * Get labels.
     * 
     * @return the Labels
     */
    public Map<Integer, List<INode>> getLabels() {
        return mLabels;
    }

    /**
     * Get leaf labels.
     * 
     * @return the leaf labels
     */
    public Map<Integer, List<INode>> getLeafLabels() {
        return mLeafLabels;
    }
}
