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
 * $Id: TextNode.java 4448 2008-08-31 07:41:34Z kramis $
 */

package com.treetank.node;

import java.util.Arrays;

import com.treetank.io.ITTSink;
import com.treetank.settings.EFixed;

/**
 * <h1>TextNode</h1>
 * 
 * <p>
 * Node representing a text node.
 * </p>
 */
public final class TextNode extends AbsStructNode {

    protected static final int VALUE_LENGTH = 1;

    /** Typed value of node. */
    private byte[] mValue;

    /**
     * Constructor for TextNode
     * 
     * @param longBuilder
     *            vals of longs to set
     * @param intBuilder
     *            vals of ints to set
     * @param value
     *            val to set
     */
    TextNode(final long[] longBuilder, final int[] intBuilder,
            final byte[] value) {
        super(longBuilder, intBuilder);
        mValue = value;
        mIntData[VALUE_LENGTH] = value.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.TEXT_KIND;
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getRawValue() {
        return mValue;
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(final int valueType, final byte[] value) {
        mIntData[AbsNode.TYPE_KEY] = valueType;
        mIntData[VALUE_LENGTH] = value.length;
        mValue = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setType(final int valueType) {
        mIntData[AbsNode.TYPE_KEY] = valueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink out) {
        super.serialize(out);
        for (final byte byteVal : mValue) {
            out.writeByte(byteVal);
        }
    }

    @Override
    public long getFirstChildKey() {
        return (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    @Override
    public void setFirstChildKey(final long firstChildKey) {
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
    public void setChildCount(long childCount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(mValue);
        return result;
    }

    @Override
    public AbsNode clone() {
        final AbsNode toClone = new TextNode(AbsNode.cloneData(mLongData),
                AbsNode.cloneData(mIntData), AbsNode.cloneData(mValue));
        return toClone;
    }

    public final static AbsNode createData(final long nodeKey,
            final long parentKey, final long leftSibKey,
            final long rightSibKey, final int type, final byte[] value) {
        final long[] longData = new long[ENodes.TEXT_KIND.getLongSize()];
        final int[] intData = new int[ENodes.TEXT_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = nodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        longData[AbsStructNode.LEFT_SIBLING_KEY] = leftSibKey;
        longData[AbsStructNode.RIGHT_SIBLING_KEY] = rightSibKey;
        longData[AbsStructNode.FIRST_CHILD_KEY] = (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
        intData[AbsNode.TYPE_KEY] = type;
        return new TextNode(longData, intData, value);
    }

    public final static AbsNode createData(final long nodeKey,
            final TextNode node) {
        return createData(nodeKey, node.getParentKey(),
                node.getLeftSiblingKey(), node.getRightSiblingKey(),
                node.getTypeKey(), node.getRawValue());
    }

    @Override
    public String toString() {
        final StringBuilder returnVal = new StringBuilder(super.toString());
        returnVal.append("\n\ttype key: ").append(getTypeKey())
                .append("\n\tvalueLength: ").append(mIntData[VALUE_LENGTH])
                .append("\n\tvalue:").append(new String(mValue)).toString();
        return returnVal.toString();
    }

}
