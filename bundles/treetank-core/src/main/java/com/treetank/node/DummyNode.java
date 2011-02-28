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

import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.settings.EFixed;

/**
 * Dummy node.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class DummyNode extends AbsStructNode {

    /**
     * Creating a dummy node.
     * 
     * @param paramLongBuilder
     *            long array with data
     * @param paramIntBuilder
     *            int array with data
     */
    public DummyNode(final long[] paramLongBuilder, final int[] paramIntBuilder) {
        super(paramLongBuilder, paramIntBuilder);
    }

    @Override
    public <T extends IItem> T accept(IReadTransaction paramTransaction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + Arrays.hashCode(mIntData);
        return result;
    }

    /** Cloning node not supported. */
    @Override
    public AbsNode clone() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long getFirstChildKey() {
        return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    @Override
    public void setFirstChildKey(final long mFirstChildKey) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void decrementChildCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void incrementChildCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChildCount(final long paramChildCount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create node data.
     * 
     * @param paramNodeKey
     *            node key
     * @param paramParentKey
     *            parent node key
     * @return {@link DummyNode} instance
     */
    public static DummyNode createData(final long paramNodeKey, final long paramParentKey) {
        final long[] longData = new long[ENodes.DUMMY_KIND.getLongSize()];
        final int[] intData = new int[ENodes.DUMMY_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = paramNodeKey;
        longData[AbsNode.PARENT_KEY] = paramParentKey;
        longData[AbsStructNode.LEFT_SIBLING_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        longData[AbsStructNode.RIGHT_SIBLING_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        longData[AbsStructNode.FIRST_CHILD_KEY] = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        return (DummyNode)ENodes.DUMMY_KIND.createNodeFromScratch(longData, intData, null);
    }

}
