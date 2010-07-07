/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: DocumentRootNode.java 4448 2008-08-31 07:41:34Z kramis $
 */

package com.treetank.node;

import com.treetank.settings.EFixed;

/**
 * <h1>DocumentNode</h1>
 * 
 * <p>
 * Node representing the root of a document. This node is guaranteed to exist in
 * revision 0 and can not be removed.
 * </p>
 */
public final class DocumentRootNode extends AbsStructNode {

    DocumentRootNode(final long[] builder) {
        super(builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.ROOT_KIND;
    }

    @Override
    public void setLeftSiblingKey(final long leftSiblingKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRightSiblingKey(final long rightSiblingKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbsNode clone() {
        return new DocumentRootNode(AbsNode.cloneData(mData));
    }

    public final static long[] createData() {
        final long[] data = new long[ENodes.ROOT_KIND.getSize()];
        data[AbsNode.NODE_KEY] = (Long) EFixed.ROOT_NODE_KEY
                .getStandardProperty();
        data[AbsNode.PARENT_KEY] = (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
        data[AbsStructNode.CHILD_COUNT] = 0;
        data[AbsStructNode.FIRST_CHILD_KEY] = (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
        data[AbsStructNode.LEFT_SIBLING_KEY] = (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
        data[AbsStructNode.RIGHT_SIBLING_KEY] = (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
        return data;
    }

}
