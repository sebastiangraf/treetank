/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

import java.io.IOException;
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

    protected static final int NAME_KEY = 4;

    protected static final int URI_KEY = 8;

    protected static final int VALUE_LENGTH = 20;

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
    AttributeNode(final byte[] mByteBuilder, final byte[] mPointerBuilder, final byte[] mValue) {
        super(mByteBuilder, mPointerBuilder);
        this.mValue = mValue;
        writeIntBytes(VALUE_LENGTH, mValue.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return readIntBytes(NAME_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int mNameKey) {
        writeIntBytes(NAME_KEY, mNameKey);
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
        return readIntBytes(URI_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int mUriKey) {
        writeIntBytes(URI_KEY, mUriKey);
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
        writeIntBytes(TYPE_KEY, mValueType);
        writeIntBytes(VALUE_LENGTH, mValue.length);

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
     * 
     * @throws IOException
     */
    @Override
    public void serialize(final ITTSink mNodeOut) {
        super.serialize(mNodeOut);
        for (final byte byteVal : mValue) {
            mNodeOut.writeByte(byteVal);
        }
    }

    @Override
    public AbsNode clone() {
        final AbsNode toClone = new AttributeNode(ENodes.cloneData(mByteData), ENodes.cloneData(mPointerData), ENodes.cloneData(mValue));
        return toClone;
    }

    public static AbsNode createData(final long mNodeKey, final long mParentKey, final int mNameKey,
        final int mUriKey, final int mType, final byte[] mValue) {

        final byte[] byteData = new byte[ENodes.ATTRIBUTE_KIND.getByteSize()];
        
        final byte[] pointerData = new byte[ENodes.ATTRIBUTE_KIND.getPointerSize()];

        int mCount = AbsNode.NODE_KEY;
        for (byte aByte : longToByteArray(mNodeKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsNode.PARENT_KEY;
        for (byte aByte : longToByteArray(mParentKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AttributeNode.NAME_KEY;
        for (byte aByte : intToByteArray(mNameKey)) {
            byteData[mCount++] = aByte;
        }

        mCount = AttributeNode.URI_KEY;
        for (byte aByte : intToByteArray(mUriKey)) {
            byteData[mCount++] = aByte;
        }

        mCount = AbsNode.TYPE_KEY;
        for (byte aByte : intToByteArray(mType)) {
            byteData[mCount++] = aByte;
        }

        return new AttributeNode(byteData, pointerData, mValue);
    }

    @Override
    public String toString() {
        final int valLength = readIntBytes(VALUE_LENGTH);
        return new StringBuilder(super.toString()).append("\n\tname key: ").append(getNameKey()).append(
            "\n\turi key: ").append(getURIKey()).append("\n\ttype: ").append(getTypeKey()).append(
            "\n\tvalue length: ").append(valLength).append("\n\tvalue: ").append(new String(mValue))
            .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 44819;
        int result = 1;
        result = prime * result + Arrays.hashCode(mByteData);
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

    public long getNodeKey() {
        return super.getNodeKey();
    }
}
