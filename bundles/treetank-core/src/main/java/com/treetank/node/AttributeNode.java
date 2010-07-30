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
     * Creating an attributes.
     * 
     * @param mLongBuilder
     *            long array with data
     * @param mIntBuilder
     *            int array with data
     * @param mValue
     *            value for the node
     */
    AttributeNode(final long[] mLongBuilder, final int[] mIntBuilder, final byte[] mValue) {
        super(mLongBuilder, mIntBuilder);
        this.mValue = mValue;
        mIntData[VALUE_LENGTH] = mValue.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return mIntData[NAME_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int mNameKey) {
        this.mIntData[NAME_KEY] = mNameKey;
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
        return mIntData[URI_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int mUriKey) {
        mIntData[URI_KEY] = mUriKey;
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
    public void setValue(final int mValueType, final byte[] mValue) {
        mIntData[AbsNode.TYPE_KEY] = mValueType;
        mIntData[VALUE_LENGTH] = mValue.length;
        this.mValue = mValue;
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
    public void serialize(final ITTSink mOut) {
        super.serialize(mOut);
        for (final byte byteVal : mValue) {
            mOut.writeByte(byteVal);
        }
    }

    @Override
    public AbsNode clone() {
        final AbsNode toClone =
            new AttributeNode(AbsNode.cloneData(mLongData), AbsNode.cloneData(mIntData), AbsNode
                .cloneData(mValue));
        return toClone;
    }

    public static AbsNode createData(final long mNodeKey, final long parentKey, final int mNameKey,
        final int mUriKey, final int mType, final byte[] mValue) {
        final long[] longData = new long[ENodes.ATTRIBUTE_KIND.getLongSize()];
        final int[] intData = new int[ENodes.ATTRIBUTE_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = mNodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        intData[AttributeNode.NAME_KEY] = mNameKey;
        intData[AttributeNode.URI_KEY] = mUriKey;
        intData[AbsNode.TYPE_KEY] = mType;
        return ENodes.ATTRIBUTE_KIND.createNodeFromScratch(longData, intData, mValue);
    }

    public static AbsNode createData(final long mNodeKey, final AttributeNode mNode) {
        return createData(mNodeKey, mNode.getParentKey(), mNode.getNameKey(), mNode.getURIKey(), mNode
            .getTypeKey(), mNode.getRawValue());
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append("\n\tname key: ").append(getNameKey()).append(
            "\n\turi key: ").append(getURIKey()).append("\n\ttype: ").append(getTypeKey()).append(
            "\n\tvalue length: ").append(mIntData[VALUE_LENGTH]).append("\n\tvalue: ").append(
            new String(mValue)).toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(mValue);
        return result;
    }

}
