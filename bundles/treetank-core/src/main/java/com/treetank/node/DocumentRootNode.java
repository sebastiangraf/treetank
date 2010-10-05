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

package com.treetank.node;

import java.util.Arrays;

import com.treetank.settings.EFixed;

/**
 * <h1>DocumentNode</h1>
 * 
 * <p>
 * Node representing the root of a document. This node is guaranteed to exist in revision 0 and can not be
 * removed.
 * </p>
 */
public final class DocumentRootNode extends AbsStructNode {

    /**
     * Constructor.
     * 
     * @param mLongBuilder
     *            long array to set
     * @param mIntBuilder
     *            int array to set
     */
    DocumentRootNode(final long[] mLongBuilder, final int[] mIntBuilder) {
        super(mLongBuilder, mIntBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.ROOT_KIND;
    }

    @Override
    public void setLeftSiblingKey(final long mLeftSiblingKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRightSiblingKey(final long rightSiblingKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbsNode clone() {
        return new DocumentRootNode(AbsNode.cloneData(mLongData), AbsNode.cloneData(mIntData));
    }

    public static DocumentRootNode createData() {
        final long[] longData = new long[ENodes.ROOT_KIND.getLongSize()];
        final int[] intData = new int[ENodes.ROOT_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = (Long)EFixed.ROOT_NODE_KEY.getStandardProperty();
        longData[AbsNode.PARENT_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        longData[AbsStructNode.CHILD_COUNT] = 0;
        longData[AbsStructNode.FIRST_CHILD_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        longData[AbsStructNode.LEFT_SIBLING_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        longData[AbsStructNode.RIGHT_SIBLING_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        return new DocumentRootNode(longData, intData);
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = prime * result + Arrays.hashCode(mLongData);
        result = prime * result + Arrays.hashCode(mIntData);
        return result;
    }

}
