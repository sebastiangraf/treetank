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

import java.util.Arrays;

import com.treetank.io.ITTSink;

/**
 * <h1>AttributeNode</h1>
 * 
 * <p>
 * Node representing an attribute.
 * </p>
 */
public final class AttributeNode extends AbsNode {

    protected static final int NAME_KEY = 1;

    protected static final int URI_KEY = 2;

    protected static final int VALUE_LENGTH = 3;

    /** Value of attribute. */
    private byte[] mValue;

    /**
     * Creating an attributes
     * 
     * @param longBuilder
     *            long array with data
     * @param intBuilder
     *            int array with data
     * @param value
     *            value for the node
     */
    AttributeNode(final long[] longBuilder, final int[] intBuilder,
            final byte[] value) {
        super(longBuilder, intBuilder);
        mValue = value;
        mIntData[VALUE_LENGTH] = value.length;
    }

    /**
     * {@inheritDoc}
     */
    public int getNameKey() {
        return mIntData[NAME_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public void setNameKey(final int nameKey) {
        this.mIntData[NAME_KEY] = nameKey;
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
    public int getURIKey() {
        return mIntData[URI_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public void setURIKey(final int uriKey) {
        mIntData[URI_KEY] = uriKey;
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
        final AbsNode toClone = new AttributeNode(AbsNode.cloneData(mLongData),
                AbsNode.cloneData(mIntData), AbsNode.cloneData(mValue));
        return toClone;
    }

    public final static AbsNode createData(final long nodeKey,
            final long parentKey, final int nameKey, final int uriKey,
            final int type, final byte[] value) {
        final long[] longData = new long[ENodes.ATTRIBUTE_KIND.getLongSize()];
        final int[] intData = new int[ENodes.ATTRIBUTE_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = nodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        intData[AttributeNode.NAME_KEY] = nameKey;
        intData[AttributeNode.URI_KEY] = uriKey;
        intData[AbsNode.TYPE_KEY] = type;
        return ENodes.ATTRIBUTE_KIND.createNodeFromScratch(longData, intData,
                value);
    }

    public final static AbsNode createData(final long nodeKey,
            final AttributeNode node) {
        return createData(nodeKey, node.getParentKey(), node.getNameKey(),
                node.getURIKey(), node.getTypeKey(), node.getRawValue());
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append("\n\tname key: ")
                .append(getNameKey()).append("\n\turi key: ")
                .append(getURIKey()).append("\n\ttype: ").append(getTypeKey())
                .append("\n\tvalue length: ").append(mIntData[VALUE_LENGTH])
                .append("\n\tvalue: ").append(new String(mValue)).toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(mValue);
        return result;
    }

}
