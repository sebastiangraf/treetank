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

package org.treetank.data;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.data.delegates.NameNodeDelegate;
import org.treetank.data.delegates.NodeDelegate;
import org.treetank.data.delegates.StructNodeDelegate;
import org.treetank.data.delegates.ValNodeDelegate;
import org.treetank.data.interfaces.ITreeData;
import org.treetank.data.interfaces.ITreeNameData;
import org.treetank.data.interfaces.ITreeValData;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * <h1>AttributeNode</h1>
 * 
 * <p>
 * Node representing an attribute.
 * </p>
 */
public final class AttributeNode implements ITreeData, ITreeValData, ITreeNameData {

    /**
     * Enum for AttributeValueFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum AttributeNodeFunnel implements Funnel<org.treetank.api.IData> {
        INSTANCE;
        public void funnel(org.treetank.api.IData data, PrimitiveSink into) {
            final AttributeNode from = (AttributeNode)data;
            from.mDel.getFunnel().funnel(from, into);
            from.mNameDel.getFunnel().funnel(from, into);
            from.mValDel.getFunnel().funnel(from, into);
        }
    }

    /** Delegate for common node information. */
    private final NodeDelegate mDel;

    /** Delegate for name node information. */
    private final NameNodeDelegate mNameDel;

    /** Delegate for val node information. */
    private final ValNodeDelegate mValDel;

    /**
     * Creating an attribute.
     * 
     * @param pDel
     *            {@link NodeDelegate} to be set
     * @param pNameDel
     *            {@link StructNodeDelegate} to be set
     * @param pValDel
     *            {@link ValNodeDelegate} to be set
     * 
     */
    public AttributeNode(final NodeDelegate pDel, final NameNodeDelegate pNameDel,
        final ValNodeDelegate pValDel) {
        mDel = pDel;
        mNameDel = pNameDel;
        mValDel = pValDel;
    }

    /** {@inheritDoc} */
    @Override
    public int getKind() {
        return IConstants.ATTRIBUTE;
    }

    /**
     * Delegate method for getNodeKey.
     * 
     * @return the current key
     * @see org.treetank.data.delegates.NodeDelegate#getDataKey()
     */
    public long getDataKey() {
        return mDel.getDataKey();
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return the parent key
     * @see org.treetank.data.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDel.getParentKey();
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pParentKey
     * @see org.treetank.data.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(final long pParentKey) {
        mDel.setParentKey(pParentKey);
    }

    /**
     * Delegate method for getHash.
     * 
     * @return the current hash
     * @see org.treetank.data.delegates.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDel.getHash();
    }

    /**
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.data.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(final long pHash) {
        mDel.setHash(pHash);
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return the key of the type
     * @see org.treetank.data.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDel.getTypeKey();
    }

    /**
     * Delegate method for setTypeKey.
     * 
     * @param pTypeKey
     * @see org.treetank.data.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(final int pTypeKey) {
        mDel.setTypeKey(pTypeKey);
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return if the node has a parent
     * @see org.treetank.data.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDel.hasParent();
    }

    /**
     * Delegate method for getNameKey.
     * 
     * @return the name key
     * @see org.treetank.data.delegates.NameNodeDelegate#getNameKey()
     */
    public int getNameKey() {
        return mNameDel.getNameKey();
    }

    /**
     * Delegate method for getURIKey.
     * 
     * @return the uri key
     * @see org.treetank.data.delegates.NameNodeDelegate#getURIKey()
     */
    public int getURIKey() {
        return mNameDel.getURIKey();
    }

    /**
     * Delegate method for setNameKey.
     * 
     * @param pNameKey
     * @see org.treetank.data.delegates.NameNodeDelegate#setNameKey(int)
     */
    public void setNameKey(final int pNameKey) {
        mNameDel.setNameKey(pNameKey);
    }

    /**
     * Delegate method for setURIKey.
     * 
     * @param pUriKey
     * @see org.treetank.data.delegates.NameNodeDelegate#setURIKey(int)
     */
    public void setURIKey(final int pUriKey) {
        mNameDel.setURIKey(pUriKey);
    }

    /**
     * Delegate method for getRawValue.
     * 
     * @return the raw value of the node
     * @see org.treetank.data.delegates.ValNodeDelegate#getRawValue()
     */
    public byte[] getRawValue() {
        return mValDel.getRawValue();
    }

    /**
     * Delegate method for setValue.
     * 
     * @param pVal
     * @see org.treetank.data.delegates.ValNodeDelegate#setValue(byte[])
     */
    public void setValue(final byte[] pVal) {
        mValDel.setValue(pVal);
    }

    /**
     * Getting the inlying {@link NodeDelegate}.
     * 
     * @return the node delegate
     */
    NodeDelegate getNodeDelegate() {
        return mDel;
    }

    /**
     * Getting the inlying {@link NameNodeDelegate}.
     * 
     * @return the namenodedelegate
     */
    NameNodeDelegate getNameNodeDelegate() {
        return mNameDel;
    }

    /**
     * Getting the inlying {@link ValNodeDelegate}.
     * 
     * @return the valnodedelegate
     */
    ValNodeDelegate getValNodeDelegate() {
        return mValDel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mDel", mDel).add("mNameDel", mNameDel).add("mValDel", mValDel)
            .toString();
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
            pOutput.writeInt(IConstants.ATTRIBUTE);
            mDel.serialize(pOutput);
            mNameDel.serialize(pOutput);
            mValDel.serialize(pOutput);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public Funnel<org.treetank.api.IData> getFunnel() {
        return AttributeNodeFunnel.INSTANCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDel == null) ? 0 : mDel.hashCode());
        result = prime * result + ((mNameDel == null) ? 0 : mNameDel.hashCode());
        result = prime * result + ((mValDel == null) ? 0 : mValDel.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

}
