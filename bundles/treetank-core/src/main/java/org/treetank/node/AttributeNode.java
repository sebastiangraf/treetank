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

import java.util.Arrays;

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
     * @param paramByteBuilder
     *            byte array with data
     * @param paramPointerBuilder
     *            byte array with data
     * @param paramValue
     *            value for the node
     */
    AttributeNode(final byte[] paramByteBuilder, final byte[] paramPointerBuilder, final byte[] paramValue) {
        super(paramByteBuilder, paramPointerBuilder);
        mValue = paramValue;
        writeIntBytes(VALUE_LENGTH, mValue.length);
    }

    /** {@inheritDoc} */
    @Override
    public int getNameKey() {
        return readIntBytes(NAME_KEY);
    }

    /** {@inheritDoc} */
    @Override
    public void setNameKey(final int paramNameKey) {
        writeIntBytes(NAME_KEY, paramNameKey);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasParent() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int getURIKey() {
        return readIntBytes(URI_KEY);
    }

    /** {@inheritDoc} */
    @Override
    public void setURIKey(final int paramUriKey) {
        writeIntBytes(URI_KEY, paramUriKey);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getRawValue() {
        return mValue;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(final int paramValueType, final byte[] paramValue) {
        writeIntBytes(TYPE_KEY, paramValueType);
        writeIntBytes(VALUE_LENGTH, paramValue.length);

        mValue = paramValue;
    }

    /** {@inheritDoc} */
    @Override
    public ENodes getKind() {
        return ENodes.ATTRIBUTE_KIND;
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(final ITTSink paramNodeOut) {
        super.serialize(paramNodeOut);
        for (final byte byteVal : mValue) {
            paramNodeOut.writeByte(byteVal);
        }
    }

    /** {@inheritDoc} */
    @Override
    public AbsNode clone() {
        final AbsNode toClone =
            new AttributeNode(ENodes.cloneData(mByteData), ENodes.cloneData(mPointerData), ENodes
                .cloneData(mValue));
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final int valLength = readIntBytes(VALUE_LENGTH);
        return new StringBuilder(super.toString()).append("\n\tname key: ").append(getNameKey()).append(
            "\n\turi key: ").append(getURIKey()).append("\n\ttype: ").append(getTypeKey()).append(
            "\n\tvalue length: ").append(valLength).append("\n\tvalue: ").append(new String(mValue))
            .toString();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 44819;
        int result = 1;
        result = prime * result + Arrays.hashCode(mByteData);
        result = prime * result + Arrays.hashCode(mValue);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);
    }
}
