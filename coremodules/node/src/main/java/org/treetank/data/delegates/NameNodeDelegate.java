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
/**
 * 
 */
package org.treetank.data.delegates;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.data.interfaces.ITreeData;
import org.treetank.data.interfaces.ITreeNameData;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * Delegate method for all nodes containing \"naming\"-data. That means that
 * different fixed defined names are represented by the nodes delegating the
 * calls of the interface {@link ITreeNameData} to this class. Mainly, keys are
 * stored referencing later on to the string stored in dedicated pages.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NameNodeDelegate implements ITreeData, ITreeNameData {
    /**
     * Enum for AtomicValueFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum NameNodeDelegateFunnel implements Funnel<org.treetank.api.IData> {
        INSTANCE;
        public void funnel(org.treetank.api.IData data, PrimitiveSink into) {
            final ITreeNameData from = (ITreeNameData)data;
            into.putInt(from.getNameKey()).putInt(from.getURIKey());
        }
    }

    /** Node delegate, containing basic node information. */
    private final NodeDelegate mDelegate;
    /** Key of the name. The name contains the prefix as well. */
    private int mNameKey;
    /** URI of the related namespace. */
    private int mUriKey;

    /**
     * Constructor.
     * 
     * @param pDel
     *            page delegator
     * @param pNameKey
     *            namekey to be stored
     * @param pUriKey
     *            urikey to be stored
     */
    public NameNodeDelegate(final NodeDelegate pDel, final int pNameKey, final int pUriKey) {
        mDelegate = pDel;
        mNameKey = pNameKey;
        mUriKey = pUriKey;
    }

    /**
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.data.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(final long pHash) {
        mDelegate.setHash(pHash);
    }

    /**
     * Delegate method for getHash.
     * 
     * @return the hash
     * @see org.treetank.data.delegates.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDelegate.getHash();
    }

    /**
     * Delegate method for getNodeKey.
     * 
     * @return the key of the node
     * @see org.treetank.data.delegates.NodeDelegate#getDataKey()
     */
    public long getDataKey() {
        return mDelegate.getDataKey();
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return the key of the parent node
     * @see org.treetank.data.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDelegate.getParentKey();
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return if the node has a parent
     * @see org.treetank.data.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDelegate.hasParent();
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return the key of the type
     * @see org.treetank.data.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pNodeKey
     * @see org.treetank.data.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(final long pNodeKey) {
        mDelegate.setParentKey(pNodeKey);
    }

    /**
     * Delegate method for setType.
     * 
     * @param pTypeKey
     * @see org.treetank.data.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(final int pTypeKey) {
        mDelegate.setTypeKey(pTypeKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getKind() {
        return mDelegate.getKind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return mNameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return mUriKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int pNameKey) {
        mNameKey = pNameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int pUriKey) {
        mUriKey = pUriKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mDelegate", mDelegate).add("mNameKey", mNameKey).add("mUriKey",
            mUriKey).toString();
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
            pOutput.writeInt(getNameKey());
            pOutput.writeInt(getURIKey());
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public Funnel<org.treetank.api.IData> getFunnel() {
        return NameNodeDelegateFunnel.INSTANCE;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDelegate == null) ? 0 : mDelegate.hashCode());
        result = prime * result + mNameKey;
        result = prime * result + mUriKey;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

}
