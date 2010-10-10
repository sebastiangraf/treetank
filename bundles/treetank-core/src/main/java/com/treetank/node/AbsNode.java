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

import com.treetank.api.IItem;
import com.treetank.io.ITTSink;
import com.treetank.settings.EFixed;

/**
 * <h1>AbstractNode</h1>
 * 
 * <p>
 * Abstract node class to implement all methods required with INode. To reduce implementation overhead in
 * subclasses it implements all methods but does silently not do anything there. A subclass must only
 * implement those methods that are required to provide proper subclass functionality.
 * </p>
 */
public abstract class AbsNode implements IItem, Comparable<AbsNode> {

    /** standard NODE_KEY. */
    protected static final int NODE_KEY = 0;

    /** standard PARENT_KEY. */
    protected static final int PARENT_KEY = 1;

    /** Hashcode for subtree integrity. */
    protected static final int HASHCODE = 2;

    /** standard TYPE_KEY. */
    protected static final int TYPE_KEY = 0;

    /** Node key is common to all node kinds. */
    protected final long[] mLongData;

    /** Node key is common to all node data. */
    protected final int[] mIntData;

    /**
     * Constructor for inserting node.
     * 
     * @param mLongBuilder
     *            longData to build
     * @param mIntBuilder
     *            intData to build
     */
    AbsNode(final long[] mLongBuilder, final int[] mIntBuilder) {
        mLongData = mLongBuilder;
        mIntData = mIntBuilder;
    }

    /**
     * {@inheritDoc}
     */
    public final long getNodeKey() {
        return mLongData[NODE_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasParent() {
        return mLongData[PARENT_KEY] != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public int getNameKey() {
        return (Integer)EFixed.NULL_INT_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public long getParentKey() {
        return mLongData[PARENT_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public ENodes getKind() {
        return ENodes.UNKOWN_KIND;
    }

    /**
     * {@inheritDoc}
     */
    public int getURIKey() {
        return (Integer)EFixed.NULL_INT_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public int getTypeKey() {
        return mIntData[TYPE_KEY];
    }

    public long getHash() {
        return mLongData[HASHCODE];
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getRawValue() {
        return null;
    }

    /**
     * Serializing the data.
     * 
     * @param mOut
     *            target to serialize.
     */
    public void serialize(final ITTSink mOut) {
        for (final long longVal : mLongData) {
            mOut.writeLong(longVal);
        }
        for (final int intVal : mIntData) {
            mOut.writeInt(intVal);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void setNodeKey(final long mNodeKey) {
        mLongData[NODE_KEY] = mNodeKey;
    }

    /**
     * Setting the parent key.
     * 
     * @param parentKey
     *            the key for the parent.
     */
    public void setParentKey(final long parentKey) {
        mLongData[PARENT_KEY] = parentKey;
    }

    /**
     * Setting the type of this node.
     * 
     * @param mValueType
     *            to be set.
     */
    public void setType(final int mValueType) {
        mIntData[TYPE_KEY] = mValueType;
    }

    /**
     * setting hash to current node.
     * 
     * @param paramHash
     *            to be set
     */
    public final void setHash(final long paramHash) {
        mLongData[HASHCODE] = paramHash;
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
    public boolean equals(final Object mObj) {
        return this.hashCode() == mObj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final AbsNode mNode) {
        final long nodeKey = (mNode).getNodeKey();
        if (mLongData[NODE_KEY] < nodeKey) {
            return -1;
        } else if (mLongData[NODE_KEY] == nodeKey) {
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

    protected static long[] cloneData(final long[] mInput) {
        final long[] data = new long[mInput.length];
        System.arraycopy(mInput, 0, data, 0, data.length);
        return data;
    }

    protected static int[] cloneData(final int[] mInput) {
        final int[] data = new int[mInput.length];
        System.arraycopy(mInput, 0, data, 0, data.length);
        return data;
    }

    protected static byte[] cloneData(final byte[] mInput) {
        final byte[] value = new byte[mInput.length];
        System.arraycopy(mInput, 0, value, 0, value.length);
        return value;
    }

}
