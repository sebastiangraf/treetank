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

import org.treetank.api.IItem;
import org.treetank.io.ITTSink;
import org.treetank.settings.EFixed;

/**
 * <h1>AbstractNode</h1>
 * 
 * <p>
 * Abstract node class to implement all methods required with INode. To reduce implementation overhead in
 * subclasses it implements all methods but does silently not do anything there. A subclass must only
 * implement those methods that are required to provide proper subclass functionality.
 * </p>
 */
@Deprecated
public abstract class AbsNode implements IItem, Comparable<AbsNode> {

    /** standard NODE_KEY. */
    protected static final int NODE_KEY = 0;

    /** standard PARENT_KEY. */
    protected static final int PARENT_KEY = 8;

    /** Hashcode for subtree integrity. */
    protected static final int HASHCODE = 16;

    /** standard TYPE_KEY. */
    protected static final int TYPE_KEY = 0;

    protected final byte[] mByteData;

    protected final byte[] mPointerData;

    /**
     * Constructor for inserting node.
     * 
     * @param mLongBuilder
     *            longData to build
     * @param mIntBuilder
     *            intData to build
     */
    AbsNode(final byte[] mByteBuilder, final byte[] mPointerBuilder) {
        this.mByteData = mByteBuilder;
        this.mPointerData = mPointerBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNodeKey() {
        return readLongPointer(NODE_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParent() {
        final long mParentKey = readLongPointer(PARENT_KEY);
        return mParentKey != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return (Integer)EFixed.NULL_INT_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getParentKey() {
        return readLongPointer(PARENT_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.UNKOWN_KIND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return (Integer)EFixed.NULL_INT_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeKey() {
        return readIntBytes(TYPE_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getHash() {
        return readLongPointer(HASHCODE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawValue() {
        return null;
    }

    /**
     * Serializing the data.
     * 
     * @param mNodeOut
     *            target to serialize.
     */
    public void serialize(final ITTSink mNodeOut) {
        for (int i = 0; i < mPointerData.length; i++) {
            mNodeOut.writeByte(mPointerData[i]);
        }

        for (int i = 0; i < mByteData.length; i++) {
            mNodeOut.writeByte(mByteData[i]);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeKey(final long mNodeKey) {
        writeLongPointer(NODE_KEY, mNodeKey);
    }

    /**
     * Setting the parent key.
     * 
     * @param parentKey
     *            the key for the parent.
     */
    public void setParentKey(final long mParentKey) {
        writeLongPointer(PARENT_KEY, mParentKey);
    }

    /**
     * setting hash to current node.
     * 
     * @param paramHash
     *            to be set
     */
    @Override
    public final void setHash(final long mHashcode) {
        writeLongPointer(HASHCODE, mHashcode);
    }

    /**
     * Setting the name key for this node.
     * 
     * @param mNameKey
     *            to be set.
     */
    public void setNameKey(final int mNameKey) {
    }

    /**
     * Setting the uri for this node.
     * 
     * @param mUriKey
     *            to be set.
     */
    public void setURIKey(final int mUriKey) {
    }

    /**
     * Setting the value for this node.
     * 
     * @param mValueType
     *            type of value to be set.
     * @param mValue
     *            the value to be set.
     */
    public void setValue(final int mValueType, final byte[] mValue) {
    }

    @Override
    public abstract int hashCode();

    @Override
    public final boolean equals(final Object mObj) {
        return this.hashCode() == mObj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int compareTo(final AbsNode mNode) {
        final long nodeKey = (mNode).getNodeKey();
        final long mNodeKey = readLongPointer(NODE_KEY);

        if (mNodeKey < nodeKey) {
            return -1;
        } else if (mNodeKey == nodeKey) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("\n").append(this.getClass().getName()).append("\n\tnode key: ").append(
            getNodeKey()).append("\n\tparentKey: ").append(getParentKey()).append("\n\ttypeKey: ").append(
            getTypeKey()).append("\n\thash: ").append(getHash()).toString();
    }

    @Override
    public abstract AbsNode clone();

    /**
     * Converting an integer value to byte array.
     * 
     * @param mValue
     *            Integer value to convert.
     * @return Byte array of integer value.
     */
    protected static byte[] intToByteArray(final int mIntVal) {
        final byte[] mBuffer = new byte[4];

        mBuffer[0] = (byte)(0xff & (mIntVal >>> 24));
        mBuffer[1] = (byte)(0xff & (mIntVal >>> 16));
        mBuffer[2] = (byte)(0xff & (mIntVal >>> 8));
        mBuffer[3] = (byte)(0xff & mIntVal);

        return mBuffer;
    }

    /**
     * Converting a Long value to byte array.
     * 
     * @param mValue
     *            Long value to convert.
     * @return Byte array of long value.
     */
    protected static byte[] longToByteArray(final long mLongVal) {
        final byte[] mBuffer = new byte[8];

        mBuffer[0] = (byte)(0xff & (mLongVal >> 56));
        mBuffer[1] = (byte)(0xff & (mLongVal >> 48));
        mBuffer[2] = (byte)(0xff & (mLongVal >> 40));
        mBuffer[3] = (byte)(0xff & (mLongVal >> 32));
        mBuffer[4] = (byte)(0xff & (mLongVal >> 24));
        mBuffer[5] = (byte)(0xff & (mLongVal >> 16));
        mBuffer[6] = (byte)(0xff & (mLongVal >> 8));
        mBuffer[7] = (byte)(0xff & mLongVal);

        return mBuffer;
    }

    /**
     * Converting a byte array to integer.
     * 
     * @param mByteArray
     *            Byte array to convert.
     * @return converted integer value.
     */
    protected static int byteArrayToInt(final byte[] mByteArray) {
        final int mConvInt =
            ((mByteArray[0] & 0xff) << 24) | ((mByteArray[1] & 0xff) << 16) | ((mByteArray[2] & 0xff) << 8)
                | (mByteArray[3] & 0xff);

        return mConvInt;
    }

    /**
     * Converting a byte array to long.
     * 
     * @param mByteArray
     *            Byte array to convert.
     * @return converted long value.
     */
    protected static long byteArrayToLong(final byte[] mByteArray) {
        final long mConvLong =
            ((long)(mByteArray[0] & 0xff) << 56) | ((long)(mByteArray[1] & 0xff) << 48)
                | ((long)(mByteArray[2] & 0xff) << 40) | ((long)(mByteArray[3] & 0xff) << 32)
                | ((long)(mByteArray[4] & 0xff) << 24) | ((long)(mByteArray[5] & 0xff) << 16)
                | ((long)(mByteArray[6] & 0xff) << 8) | ((long)(mByteArray[7] & 0xff));

        return mConvLong;
    }

    public long readLongBytes(final int mOffset) {
        final byte[] mBuffer = new byte[8];
        for (int i = 0; i < mBuffer.length; i++) {
            mBuffer[i] = mByteData[mOffset + i];
        }
        return byteArrayToLong(mBuffer);
    }

    public int readIntBytes(final int mOffset) {
        final byte[] mBuffer = new byte[4];
        for (int i = 0; i < mBuffer.length; i++) {
            mBuffer[i] = mByteData[mOffset + i];
        }
        return byteArrayToInt(mBuffer);
    }

    public void writeLongBytes(final int mOffset, final long mLongVal) {
        final byte[] mBuffer = longToByteArray(mLongVal);
        int i = mOffset;
        for (byte aByte : mBuffer) {
            mByteData[i++] = aByte;
        }
    }

    public void writeIntBytes(final int mOffset, final int mIntVal) {
        final byte[] mBuffer = intToByteArray(mIntVal);
        int i = mOffset;
        for (byte aByte : mBuffer) {
            mByteData[i++] = aByte;
        }
    }

    public long readLongPointer(final int mOffset) {
        final byte[] mBuffer = new byte[8];
        for (int i = 0; i < mBuffer.length; i++) {
            mBuffer[i] = mPointerData[mOffset + i];
        }
        return byteArrayToLong(mBuffer);
    }

    public int readIntPointer(final int mOffset) {
        final byte[] mBuffer = new byte[4];
        for (int i = 0; i < mBuffer.length; i++) {
            mBuffer[i] = mPointerData[mOffset + i];
        }
        return byteArrayToInt(mBuffer);
    }

    public void writeLongPointer(final int mOffset, final long mLongVal) {
        final byte[] mBuffer = longToByteArray(mLongVal);
        int i = mOffset;
        for (byte aByte : mBuffer) {
            mPointerData[i++] = aByte;
        }
    }

    public void writeIntPointer(final int mOffset, final int mIntVal) {
        final byte[] mBuffer = intToByteArray(mIntVal);
        int i = mOffset;
        for (byte aByte : mBuffer) {
            mPointerData[i++] = aByte;
        }
    }

}
