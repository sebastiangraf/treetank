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
package com.treetank.diff;

import java.util.ArrayList;
import java.util.List;

import com.treetank.api.IItem;

/**
 * Possible kinds of differences between two nodes.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public enum EDiff {
    /** Nodes are the same. */
    SAME,

    /** Nodes are the same (including subtrees). */
    SAMEHASH,

    /** Node has been inserted. */
    INSERTED,

    /** Node has been deleted. */
    DELETED,

    /** Node has been renamed. */
    RENAMED,

    /** Diff computation done. */
    DONE;

    /** Item node. */
    private transient List<IItem> mContainer;
    
    /** Constructor. */
    EDiff() {
        mContainer = new ArrayList<IItem>();
    }

    /**
     * Set node.
     * 
     * @param paramNode
     *            {@link IItem} to set
     */
    void setNode(final IItem paramNode) {
        assert paramNode != null;
        mContainer.add(paramNode);
    }

    /**
     * Get node.
     * 
     * @param paramIndex
     *            index of item to chose
     * 
     * @return the node
     */
    public IItem getNode(final int paramIndex) {
        return mContainer.get(paramIndex);
    }
}
