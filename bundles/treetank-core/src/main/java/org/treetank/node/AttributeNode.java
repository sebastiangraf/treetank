/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.node;

import java.util.Arrays;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;

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
     * Creating an attribute.
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
            new AttributeNode(ENodes.cloneData(mLongData), ENodes.cloneData(mIntData), ENodes
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
        return new AttributeNode(longData, intData, mValue);
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
        final int prime = 44819;
        int result = 1;
        result = prime * result + Arrays.hashCode(mIntData);
        result = prime * result + Arrays.hashCode(mValue);
        return result;
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IItem> T accept(final IReadTransaction paramTransaction) {
        return (T)paramTransaction.getNode(this);
    }

    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);
    }
}
