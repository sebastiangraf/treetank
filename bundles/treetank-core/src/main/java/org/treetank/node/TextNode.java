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

import org.treetank.api.IStructuralItem;
import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.settings.EFixed;

/**
 * <h1>TextNode</h1>
 * 
 * <p>
 * Node representing a text node.
 * </p>
 */
public final class TextNode extends AbsStructNode implements IStructuralItem {

    protected static final int VALUE_LENGTH = 20;

    /** Typed value of node. */
    private byte[] mValue;

    /**
     * Constructor for TextNode.
     * 
     * @param paramByteBuilder
     *            vals of bytes to set
     * @param paramPointerBuilder
     *            vals of bytes to set
     * @param paramValue
     *            val to set
     */
    TextNode(final byte[] paramByteBuilder, final byte[] paramPointerBuilder, final byte[] paramValue) {
        super(paramByteBuilder, paramPointerBuilder);
        mValue = paramValue;
        writeIntBytes(VALUE_LENGTH, paramValue.length);
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
    public byte[] getRawValue() {
        return mValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final int paramValueType, final byte[] paramValue) {
        writeIntBytes(AbsNode.TYPE_KEY, paramValueType);
        writeIntBytes(VALUE_LENGTH, paramValue.length);
        mValue = paramValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink paramNodeOut) {
        super.serialize(paramNodeOut);
        for (final byte byteVal : mValue) {
            paramNodeOut.writeByte(byteVal);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getFirstChildKey() {
        return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /** {@inheritDoc} */
    @Override
    public void setFirstChildKey(final long mFirstChildKey) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void decrementChildCount() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void incrementChildCount() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setChildCount(final long paramChildCount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + Arrays.hashCode(mByteData);
        result = prime * result + Arrays.hashCode(mValue);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public AbsNode clone() {
        final AbsNode toClone =
            new TextNode(ENodes.cloneData(mByteData), ENodes.cloneData(mPointerData), ENodes
                .cloneData(mValue));
        return toClone;
    }

    public static AbsNode createData(final long mNodeKey, final long mParentKey, final long mLeftSibKey,
        final long mRightSibKey, final int mType, final byte[] mValue) {

        final byte[] byteData = new byte[ENodes.TEXT_KIND.getByteSize()];

        final byte[] pointerData = new byte[ENodes.TEXT_KIND.getPointerSize()];

        int mCount = AbsNode.NODE_KEY;
        for (byte aByte : longToByteArray(mNodeKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsNode.PARENT_KEY;
        for (byte aByte : longToByteArray(mParentKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.LEFT_SIBLING_KEY;
        for (byte aByte : longToByteArray(mLeftSibKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.RIGHT_SIBLING_KEY;
        for (byte aByte : longToByteArray(mRightSibKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.FIRST_CHILD_KEY;
        for (byte aByte : longToByteArray((Long)EFixed.NULL_NODE_KEY.getStandardProperty())) {
            pointerData[mCount++] = aByte;
        }

        mCount = TextNode.TYPE_KEY;
        for (byte aByte : intToByteArray(mType)) {
            byteData[mCount++] = aByte;
        }

        return new TextNode(byteData, pointerData, mValue);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final int valLength = readIntBytes(VALUE_LENGTH);
        final StringBuilder returnVal = new StringBuilder(super.toString());
        returnVal.append("\n\ttype key: ").append(getTypeKey()).append("\n\tvalueLength: ").append(valLength)
            .append("\n\tvalue:").append(new String(mValue)).toString();
        return returnVal.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);
    }

}
