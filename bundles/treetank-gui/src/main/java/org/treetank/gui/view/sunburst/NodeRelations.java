/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.gui.view.sunburst;

import org.treetank.gui.view.sunburst.SunburstItem.EStructType;

/**
 * <h1>NodeRelations</h1>
 * 
 * <p>
 * Relations between a node and it's children. Container class used to simplify the
 * {@link SunburstItem.Builder} constructor.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class NodeRelations {
    /** Depth in the tree. */
    transient int mDepth;

    /** Determines the structural kind of the node. */
    transient EStructType mStructKind;

    /** Descendant count of the node. */
    transient long mDescendantCount;

    /** Global minimum descendant count. */
    transient long mMinDescendantCount;

    /** Global maximum descendant count. */
    transient long mMaxDescendantCount;

    /** Index to the parent item. */
    transient int mIndexToParent;
    
    /** Determines if one must be subtracted. */
    transient boolean mSubtract;

    /**
     * Set all fields.
     * 
     * @param paramDepth
     *            depth in the tree
     * @param paramStructKind
     *            determines the structural kind of the node
     * @param paramDescendantCount
     *            the descendant count of the node
     * @param paramMinDescendantCount
     *            global minimum descendant count
     * @param paramMaxDescendantCount
     *            global maximum descendant count
     * @param paramIndexToParent
     *            index to the parent item
     * @return NodeRelations instance.
     */
    NodeRelations setAll(final int paramDepth, final EStructType paramStructKind,
        final long paramDescendantCount, final long paramMinDescendantCount,
        final long paramMaxDescendantCount, final int paramIndexToParent) {
        assert paramDepth >= 0;
        assert paramStructKind != null;
        assert paramDescendantCount >= 0;
        assert paramMinDescendantCount >= 0;
        assert paramMaxDescendantCount >= 0;
        assert paramIndexToParent >= -1;
        mDepth = paramDepth;
        mStructKind = paramStructKind;
        mDescendantCount = paramDescendantCount;
        mMinDescendantCount = paramMinDescendantCount;
        mMaxDescendantCount = paramMaxDescendantCount;
        mIndexToParent = paramIndexToParent;
        return this;
    }

    /**
     * Set subtract.
     * 
     * @param paramSubtract
     *            determines if one must be subtracted
     * @return this relation
     */
    NodeRelations setSubtract(final boolean paramSubtract) {
        mSubtract = paramSubtract;
        return this;
    }
}
