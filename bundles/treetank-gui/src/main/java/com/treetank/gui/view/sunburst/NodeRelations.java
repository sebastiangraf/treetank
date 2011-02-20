/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.gui.view.sunburst;

import com.treetank.gui.view.sunburst.SunburstItem.EStructType;

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
