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

    protected static final int TYPE = 6;

    protected static final int VALUE_LENGTH = 7;

    /** Typed value of node. */
    private byte[] mValue;

    TextNode(final long[] builder, final byte[] value) {
        super(builder);
        // mData[TYPE] = builder.getTypeKey();
        mValue = value;
        mData[VALUE_LENGTH] = value.length;
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
    @Override
    public int getTypeKey() {
        return (int) mData[TYPE];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawValue() {
        return mValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final int valueType, final byte[] value) {
        mData[TYPE] = valueType;
        mData[VALUE_LENGTH] = value.length;
        mValue = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setType(final int valueType) {
        mData[TYPE] = valueType;
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
    public AbsNode clone() {
        final AbsNode toClone = new TextNode(AbsNode.cloneData(mData),
                AbsNode.cloneValue(mValue));
        return toClone;
    }

    public final static long[] createData(final long nodeKey,
            final long parentKey, final long leftSibKey,
            final long rightSibKey, final int type) {
        final long[] data = new long[ENodes.TEXT_KIND.getSize()];
        data[AbsNode.NODE_KEY] = nodeKey;
        data[AbsNode.PARENT_KEY] = parentKey;
        data[AbsStructNode.LEFT_SIBLING_KEY] = leftSibKey;
        data[AbsStructNode.RIGHT_SIBLING_KEY] = rightSibKey;
        data[AbsStructNode.FIRST_CHILD_KEY] = (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
        data[TextNode.TYPE] = type;
        return data;
    }

    public final static long[] createData(final long nodeKey,
            final TextNode node) {
        return createData(nodeKey, node.getParentKey(),
                node.getLeftSiblingKey(), node.getRightSiblingKey(),
                node.getTypeKey());
    }

    @Override
    public String toString() {
        final StringBuilder returnVal = new StringBuilder(super.toString());
        returnVal.append("\n\ttype key: ").append(getTypeKey())
                .append("\n\tvalueLength: ").append(mData[VALUE_LENGTH])
                .append("\n\tvalue:").append(new String(mValue)).toString();
        return returnVal.toString();
    }

}
