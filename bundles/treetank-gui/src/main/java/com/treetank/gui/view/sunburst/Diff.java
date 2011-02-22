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

import com.treetank.api.IItem;
import com.treetank.diff.DiffFactory.EDiff;

/**
 * Container for diffs.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
class Diff {
    /** {@link EDiff} which specifies the kind of diff between two nodes. */
    private transient EDiff mDiff;

    /** {@link IItem} in new revision. */
    private transient IItem mNewNode;

    /** {@link IItem} in old revision. */
    private transient IItem mOldNode;
    
    /**
     * Constructor.
     * 
     * @param paramDiff
     *            {@link EDiff} which specifies the kind of diff between two nodes
     * @param paramNewNode
     *            {@link IItem} in new revision
     * @param paramOldNode
     *            {@link IItem} in old revision
     */
    public Diff(final EDiff paramDiff, final IItem paramNewNode, final IItem paramOldNode) {
        assert paramDiff != null;
        assert paramNewNode != null;
        assert paramOldNode != null;

        mDiff = paramDiff;
        mNewNode = paramNewNode;
        mOldNode = paramOldNode;
    }
    
    /**
     * Get diff.
     * 
     * @return the kind of diff
     */
    EDiff getDiff() {
        return mDiff;
    }
    
    /**
     * Get new node.
     * 
     * @return the new node
     */
    IItem getNewNode() {
        return mNewNode;
    }
    
    /**
     * Get old node.
     * 
     * @return the old node
     */
    public IItem getOldNode() {
        return mOldNode;
    }
}
