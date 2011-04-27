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
     * @param mNodeOut
     *            target to serialize.
     */
    public void serialize(final ITTSink mNodeOut) {

        for (final long longVal : mLongData) {
            mNodeOut.writeLong(longVal);
        }
        for (final int intVal : mIntData) {
            mNodeOut.writeInt(intVal);
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
    public final boolean equals(final Object mObj) {
        return this.hashCode() == mObj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int compareTo(final AbsNode mNode) {
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

}
