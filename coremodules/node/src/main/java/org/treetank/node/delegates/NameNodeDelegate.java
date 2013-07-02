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
package org.treetank.node.delegates;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.exception.TTIOException;
import org.treetank.node.IConstants;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hasher;
import com.google.common.hash.PrimitiveSink;

/**
 * Delegate method for all nodes containing \"naming\"-data. That means that
 * different fixed defined names are represented by the nodes delegating the
 * calls of the interface {@link INameNode} to this class. Mainly, keys are
 * stored referencing later on to the string stored in dedicated pages.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NameNodeDelegate implements INode, INameNode {
    /**
     * Enum for AtomicValueFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum NameNodeDelegateFunnel implements Funnel<org.treetank.api.INode> {
        INSTANCE;
        public void funnel(org.treetank.api.INode node, PrimitiveSink into) {
            final NameNodeDelegate from = (NameNodeDelegate)node;
            into.putInt(from.mNameKey).putInt(from.mUriKey);
            from.mDelegate.getFunnel().funnel(from.mDelegate, into);
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
     * Delegate method for getNodeKey.
     * 
     * @return the key of the node
     * @see org.treetank.node.delegates.NodeDelegate#getNodeKey()
     */
    public long getNodeKey() {
        return mDelegate.getNodeKey();
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return the key of the parent node
     * @see org.treetank.node.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDelegate.getParentKey();
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return if the node has a parent
     * @see org.treetank.node.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDelegate.hasParent();
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return the key of the type
     * @see org.treetank.node.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(final long pNodeKey) {
        mDelegate.setParentKey(pNodeKey);
    }

    /**
     * Delegate method for setType.
     * 
     * @param pTypeKey
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
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
    public int hashCode() {
        Hasher hc = IConstants.HF.newHasher();
        hc.putInt(mNameKey);
        hc.putInt(mUriKey);
        return hc.hash().asInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
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
    public Funnel<org.treetank.api.INode> getFunnel() {
        return NameNodeDelegateFunnel.INSTANCE;
    }

}
