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
import org.treetank.exception.AbsTTException;
import org.treetank.node.ENode;
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
    private final Map<ENode, List<INode>> mLabels;

    /** For each node type: list of leaf nodes. */
    private final Map<ENode, List<INode>> mLeafLabels;

    /**
     * Constructor.
     * 
     * @param paramSession
     *            {@link ISession} implementation
     * @throws AbsTTException
     *             if setting up treetank fails
     */
    public LabelFMESVisitor(final ISession paramSession) throws AbsTTException {
        mRtx = new NodeReadTrx(paramSession.beginPageReadTransaction(paramSession.getMostRecentVersion()));
        mLabels = new HashMap<ENode, List<INode>>();
        mLeafLabels = new HashMap<ENode, List<INode>>();
    }

    /** {@inheritDoc} */
    public void visit(final ElementNode paramNode) {
        final long nodeKey = paramNode.getNodeKey();
        mRtx.moveTo(nodeKey);
        for (int i = 0; i < paramNode.getAttributeCount(); i++) {
            mRtx.moveToAttribute(i);
            addLeafLabel();
            mRtx.moveTo(nodeKey);
        }
        for (int i = 0; i < paramNode.getNamespaceCount(); i++) {
            mRtx.moveToNamespace(i);
            addLeafLabel();
            mRtx.moveTo(nodeKey);
        }
        if (paramNode.hasFirstChild() || paramNode.getAttributeCount() > 0
            || paramNode.getNamespaceCount() > 0) {
            if (!mLabels.containsKey(paramNode.getKind())) {
                mLabels.put(paramNode.getKind(), new ArrayList<INode>());
            }
            mLabels.get(paramNode.getKind()).add(paramNode);
        }
    }

    /** {@inheritDoc} */
    public void visit(final TextNode paramNode) {
        mRtx.moveTo(paramNode.getNodeKey());
        addLeafLabel();
    }

    /**
     * Add leaf node label.
     */
    private void addLeafLabel() {
        final ENode nodeKind = mRtx.getNode().getKind();
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
    public Map<ENode, List<INode>> getLabels() {
        return mLabels;
    }

    /**
     * Get leaf labels.
     * 
     * @return the leaf labels
     */
    public Map<ENode, List<INode>> getLeafLabels() {
        return mLeafLabels;
    }
}
