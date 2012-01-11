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

import java.util.IdentityHashMap;
import java.util.Map;

import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.node.interfaces.INode;

/**
 * Keeps track of nodes in a matching.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class Matching {

    /** Forward matching. */
    private final Map<INode, INode> mMapping;

    /** Backward machting. */
    private final Map<INode, INode> mReverseMapping;

    /**
     * Tracks the (grand-)parent-child relation of nodes. We use this to speed
     * up the calculation of the number of nodes in the subtree of two nodes
     * that are in the matching.
     */
    private final ConnectionMap<INode> mIsInSubtree;

    /** {@link IReadTransaction} reference on old revision. */
    private final IReadTransaction mRtxOld;

    /** {@link IReadTransaction} reference on new revision. */
    private final IReadTransaction mRtxNew;

    /**
     * Creates a new matching.
     * 
     * @param paramRtxOld
     *            {@link IReadTransaction} reference on old revision
     * @param paramRtxNew
     *            {@link IReadTransaction} reference on new revision.
     */
    public Matching(final IReadTransaction paramRtxOld, final IReadTransaction paramRtxNew) {
        mMapping = new IdentityHashMap<INode, INode>();
        mReverseMapping = new IdentityHashMap<INode, INode>();
        mIsInSubtree = new ConnectionMap<INode>();
        mRtxOld = paramRtxOld;
        mRtxNew = paramRtxNew;
    }

    /**
     * Copy constructor. Creates a new matching with the same state as the
     * matching paramMatch.
     * 
     * @param paramMatch
     *            the original
     */
    public Matching(final Matching paramMatch) {
        mMapping = new IdentityHashMap<INode, INode>(paramMatch.mMapping);
        mReverseMapping = new IdentityHashMap<INode, INode>(paramMatch.mReverseMapping);
        mIsInSubtree = new ConnectionMap<INode>(paramMatch.mIsInSubtree);
        mRtxOld = paramMatch.mRtxOld;
        mRtxNew = paramMatch.mRtxNew;
    }

    /**
     * Adds the matching x -> y.
     * 
     * @param paramNodeX
     *            source node
     * @param paramNodeY
     *            partner of paramNodeX
     */
    public void add(final INode paramNodeX, final INode paramNodeY) {
        mMapping.put(paramNodeX, paramNodeY);
        mReverseMapping.put(paramNodeY, paramNodeX);
        updateSubtreeMap(paramNodeX, mRtxNew);
        updateSubtreeMap(paramNodeY, mRtxOld);
    }

    /**
     * For each anchestor of n: n is in it's subtree.
     * 
     * @param paramNode
     *            node in subtree
     * @param paramRtx
     *            {@link IReadTransaction} reference
     */
    private void updateSubtreeMap(final INode paramNode, final IReadTransaction paramRtx) {
        assert paramNode != null;
        assert paramRtx != null;

        mIsInSubtree.set(paramNode, paramNode, true);
        if (paramNode.hasParent()) {
            paramRtx.moveTo(paramNode.getNodeKey());
            while (paramRtx.getStructuralNode().hasParent()) {
                paramRtx.moveToParent();
                mIsInSubtree.set(paramRtx.getNode(), paramNode, true);
            }
        }
    }

    /**
     * Checks if the matching contains the pair (x, y).
     * 
     * @param paramNodeX
     *            source node
     * @param paramNodeY
     *            partner of x
     * @return true iff add(x, y) was invoked first
     */
    public boolean contains(final INode paramNodeX, final INode paramNodeY) {
        return mMapping.get(paramNodeX) == paramNodeY;
    }

    /**
     * Counts the number of child nodes in the subtrees of x and y that are also
     * in the matching.
     * 
     * @param paramNodeX
     *            first subtree root node
     * @param paramNodeY
     *            second subtree root node
     * @return number of children which have been matched
     */
    public long containedChildren(final INode paramNodeX, final INode paramNodeY) {
        assert paramNodeX != null;
        assert paramNodeY != null;
        long retVal = 0;

        mRtxOld.moveTo(paramNodeX.getNodeKey());
        for (final AbsAxis axis = new DescendantAxis(mRtxOld, true); axis.hasNext(); axis.next()) {
            retVal += mIsInSubtree.get(paramNodeY, partner(mRtxOld.getNode())) ? 1 : 0;
        }

        return retVal;
    }

    /**
     * Returns the partner node of node according to mapping.
     * 
     * @param paramNode
     *            node for which a partner has to be found
     * @return the other node or null
     */
    public INode partner(final INode paramNode) {
        return mMapping.get(paramNode);
    }

    /**
     * Returns the node for which "node" is the partner.
     * 
     * @param paramNode
     *            node for which a reverse partner has to be found
     * @return x iff add(x, node) was called before
     */
    public INode reversePartner(final INode paramNode) {
        return mReverseMapping.get(paramNode);
    }
}
