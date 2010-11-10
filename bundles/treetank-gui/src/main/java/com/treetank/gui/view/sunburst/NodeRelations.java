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

import com.treetank.gui.view.sunburst.SunburstItem.StructKind;

/**
 * <h1>NodeRelations</h1>
 * 
 * <p>
 * Relations between a node and it's children. Container class used to simplify the
 * {@link SunburstView.Builder} constructor.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class NodeRelations {
    /** Depth in the tree. */
    transient int mDepth;

    /** Determines the structural kind of the node. */
    transient StructKind mStructKind;

    /** The child count of the node. */
    transient long mChildCount;

    /** Minimum child count of it's children. */
    transient long mMinChildCount;

    /** Maximum child count of it's childre. */
    transient long mMaxChildCount;

    /** Index to the parent item. */
    transient int mIndexToParent;

    /**
     * Set all fields.
     * 
     * @param paramDepth
     *            Depth in the tree.
     * @param paramStructKind
     *            Determines the structural kind of the node.
     * @param paramChildCount
     *            The child count of the node.
     * @param paramMinChildCount
     *            Minimum child count of it's children.
     * @param paramMaxChildCount
     *            Maximum child count of it's childre.
     * @param paramIndexToParent
     *            Index to the parent item.
     * @return NodeRelations instance.
     */
    NodeRelations setAll(final int paramDepth, final StructKind paramStructKind, final long paramChildCount,
        final long paramMinChildCount, final long paramMaxChildCount, final int paramIndexToParent) {
        assert paramStructKind != null;
        mDepth = paramDepth;
        mStructKind = paramStructKind;
        mChildCount = paramChildCount;
        mMinChildCount = paramMinChildCount;
        mMaxChildCount = paramMaxChildCount;
        mIndexToParent = paramIndexToParent;
        return this;
    }
}
