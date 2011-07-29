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

package org.treetank.diff;

import org.treetank.api.IStructuralItem;
import org.treetank.diff.DiffFactory.EDiff;

/**
 * Container for diffs.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class Diff {
    /** {@link EDiff} which specifies the kind of diff between two nodes. */
    private final EDiff mDiff;

    /** {@link IStructuralItem} in new revision. */
    private final IStructuralItem mNewNode;

    /** {@link IStructuralItem} in old revision. */
    private final IStructuralItem mOldNode;

    /** {@link DiffDepth} instance. */
    private final DiffDepth mDepth;

    /**
     * Constructor.
     * 
     * @param paramDiff
     *            {@link EDiff} which specifies the kind of diff between two nodes
     * @param paramNewNode
     *            {@link IStructuralItem} in new revision
     * @param paramOldNode
     *            {@link IStructuralItem} in old revision
     * @param paramDepth
     *            current {@link DiffDepth} instance
     */
    public Diff(final EDiff paramDiff, final IStructuralItem paramNewNode,
        final IStructuralItem paramOldNode, final DiffDepth paramDepth) {
        assert paramDiff != null;
        assert paramNewNode != null;
        assert paramOldNode != null;
        assert paramDepth != null;

        mDiff = paramDiff;
        mNewNode = paramNewNode;
        mOldNode = paramOldNode;
        mDepth = paramDepth;
    }

    /**
     * Get diff.
     * 
     * @return the kind of diff
     */
    public EDiff getDiff() {
        return mDiff;
    }

    /**
     * Get new node.
     * 
     * @return the new node
     */
    public IStructuralItem getNewNode() {
        return mNewNode;
    }

    /**
     * Get old node.
     * 
     * @return the old node
     */
    public IStructuralItem getOldNode() {
        return mOldNode;
    }

    /**
     * Get depth.
     * 
     * @return the depth
     */
    public DiffDepth getDepth() {
        return mDepth;
    }

    @Override
    public String toString() {
        return new StringBuilder("diff: ").append(mDiff).append(" new node: ").append(mNewNode).append(
            " old node: ").append(mOldNode).toString();
    }
}
