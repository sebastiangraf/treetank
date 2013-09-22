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
package org.treetank.data.delegates;

import static com.google.common.base.Objects.toStringHelper;
import static org.treetank.data.IConstants.NULL_NODE;
import static org.treetank.data.IConstants.TYPE_KEY;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.data.IConstants;
import org.treetank.data.interfaces.ITreeData;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * Delegate method for all nodes. That means that all nodes stored in Treetank
 * are represented by an instance of the interface {@link ITreeData} namely
 * containing the position in the tree related to a parent-node, the related
 * type and the corresponding hash recursivly computed.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodeDelegate implements ITreeData {

    /**
     * Enum for NodeValueFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum NodeDelegateFunnel implements Funnel<org.treetank.api.IData> {
        INSTANCE;
        public void funnel(org.treetank.api.IData data, PrimitiveSink into) {
            final ITreeData from = (ITreeData)data;
            into.putLong(from.getDataKey()).putLong(from.getParentKey());
        }
    }

    /** Key of the current node. Must be unique for all nodes. */
    private long mNodeKey;
    /** Key of the parent node. */
    private long mParentKey;
    /** Hash of the parent node. */
    private long mHash;
    /**
     * TypeKey of the parent node. Can be referenced later on over special
     * pages.
     */
    private int mTypeKey;

    /**
     * Constructor.
     * 
     * @param pNodeKey
     *            to be represented by this delegate.
     * @param pParentKey
     *            to be represented by this delegate
     * @param pHash
     *            to be represented by this delegate
     */
    public NodeDelegate(final long pNodeKey, final long pParentKey, final long pHash) {
        mNodeKey = pNodeKey;
        mParentKey = pParentKey;
        mHash = pHash;
        mTypeKey = TYPE_KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getKind() {
        return IConstants.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDataKey() {
        return mNodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getParentKey() {
        return mParentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentKey(final long pParentKey) {
        this.mParentKey = pParentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getHash() {
        return mHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHash(final long pHash) {
        this.mHash = pHash;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(mHash ^ (mHash >>> 32));
        result = prime * result + (int)(mNodeKey ^ (mNodeKey >>> 32));
        result = prime * result + (int)(mParentKey ^ (mParentKey >>> 32));
        result = prime * result + mTypeKey;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mDataKey", mNodeKey).add("mParentKey", mParentKey).add("mHash",
            mHash).add("mTypeKey", mTypeKey).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeKey() {
        return mTypeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTypeKey(int pTypeKey) {
        this.mTypeKey = pTypeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParent() {
        return mParentKey != NULL_NODE;
    }

    /**
     * Serializing to given dataput
     * 
     * @param pOutput
     *            to serialize to
     * @throws TTIOException
     */
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeLong(getDataKey());
            pOutput.writeLong(getParentKey());
            pOutput.writeLong(getHash());
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public Funnel<org.treetank.api.IData> getFunnel() {
        return NodeDelegateFunnel.INSTANCE;
    }

}
