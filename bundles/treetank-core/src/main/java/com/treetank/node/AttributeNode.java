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
 * $Id: AttributeNode.java 4550 2009-02-05 09:25:46Z graf $
 */

package com.treetank.node;

import com.treetank.io.ITTSink;

/**
 * <h1>AttributeNode</h1>
 * 
 * <p>
 * Node representing an attribute.
 * </p>
 */
public final class AttributeNode extends AbsNode {

    protected static final int NAME_KEY = 2;

    protected static final int URI_KEY = 3;

    protected static final int TYPE = 4;

    protected static final int VALUE_LENGTH = 5;

    /** Value of attribute. */
    private byte[] mValue;

    /**
     * Creating an attributes
     * 
     * @param builder
     *            long array with data
     * @param value
     *            value for the node
     */
    AttributeNode(final long[] builder, final byte[] value) {
        super(builder);
        mValue = value;
        mData[VALUE_LENGTH] = value.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return (int) mData[NAME_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int nameKey) {
        this.mData[NAME_KEY] = nameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParent() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return (int) mData[URI_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int uriKey) {
        mData[URI_KEY] = uriKey;
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
    public ENodes getKind() {
        return ENodes.ATTRIBUTE_KIND;
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

    public AbsNode clone() {
        final AbsNode toClone = new AttributeNode(AbsNode.cloneData(mData),
                AbsNode.cloneValue(mValue));
        return toClone;
    }

    public final static long[] createData(final long nodeKey,
            final long parentKey, final int nameKey, final int uriKey,
            final int type) {
        final long[] data = new long[ENodes.ATTRIBUTE_KIND.getSize()];
        data[AbsNode.NODE_KEY] = nodeKey;
        data[AbsNode.PARENT_KEY] = parentKey;
        data[AttributeNode.NAME_KEY] = nameKey;
        data[AttributeNode.URI_KEY] = uriKey;
        data[AttributeNode.TYPE] = type;
        return data;
    }

    public final static long[] createData(final long nodeKey,
            final AttributeNode node) {
        return createData(nodeKey, node.getParentKey(), node.getNameKey(),
                node.getURIKey(), node.getTypeKey());
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append("\n\tname key: ")
                .append(getNameKey()).append("\n\turi key: ")
                .append(getURIKey()).append("\n\ttype: ").append(getTypeKey())
                .append("\n\tvalue length: ").append(mData[VALUE_LENGTH])
                .append("\n\tvalue: ").append(new String(mValue)).toString();
    }

}
