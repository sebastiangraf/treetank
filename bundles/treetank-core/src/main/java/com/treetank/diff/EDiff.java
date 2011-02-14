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

    /** Node has been inserted. */
    INSERTED,

    /** Node has been deleted. */
    DELETED,

    /** Node has been renamed. */
    RENAMED,

    /** Diff computation done. */
    DONE;

    /** Item node. */
    private transient IItem mNode;

    /**
     * Set node.
     * 
     * @param paramNode
     *            {@link IItem} to set
     */
    void setNode(final IItem paramNode) {
        assert paramNode != null;
        mNode = paramNode;
    }
    
    /**
     * Get node.
     * 
     * @return the node
     */
    public IItem getNode() {
        return mNode;
    }
}
