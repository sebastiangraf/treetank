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

import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;

import com.google.common.hash.Hasher;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * <h1>NamespaceNode</h1>
 * 
 * <p>
 * Node representing a namespace.
 * </p>
 */
public final class NamespaceNode implements INode, INameNode {
    /** Delegate for common node information. */
    private final NodeDelegate mDel;

    /** Delegate for name node information. */
    private final NameNodeDelegate mNameDel;

    /**
     * Constructor.
     * 
     * @param pDel
     * @param mIntBuilder
     *            building int data
     */
    public NamespaceNode(final NodeDelegate pDel, final NameNodeDelegate pNameDel) {
        mDel = pDel;
        mNameDel = pNameDel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENode getKind() {
        return ENode.NAMESPACE_KIND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return mNameDel.getNameKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int pNameKey) {
        mNameDel.setNameKey(pNameKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return mNameDel.getURIKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int pUriKey) {
        mNameDel.setURIKey(pUriKey);
    }

    /**
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(final long pHash) {
        mDel.setHash(pHash);
    }

    /**
     * Delegate method for getHash.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDel.getHash();
    }

    /**
     * Delegate method for setNodeKey.
     * 
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(final long pNodeKey) {
        mDel.setNodeKey(pNodeKey);
    }

    /**
     * Delegate method for getNodeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getNodeKey()
     */
    public long getNodeKey() {
        return mDel.getNodeKey();
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDel.getParentKey();
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDel.hasParent();
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDel.getTypeKey();
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(final long pKey) {
        mDel.setParentKey(pKey);
    }

    /**
     * Delegate method for setType.
     * 
     * @param pTypeKey
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(final int pTypeKey) {
        mDel.setTypeKey(pTypeKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        Hasher hc = IConstants.HF.newHasher();
        hc.putInt(mDel.hashCode());
        hc.putInt(mNameDel.hashCode());
        return hc.hash().asInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(mDel.toString());
        builder.append("\n");
        builder.append(mNameDel.toString());
        return builder.toString();
    }

    /**
     * Getting the inlying {@link NodeDelegate}.
     * 
     * @return
     */
    NodeDelegate getNodeDelegate() {
        return mDel;
    }

    /**
     * Getting the inlying {@link NameNodeDelegate}.
     * 
     * @return
     */
    NameNodeDelegate getNameNodeDelegate() {
        return mNameDel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeInt(IConstants.NAMESPACE);
        pOutput.write(mDel.getByteRepresentation());
        pOutput.write(mNameDel.getByteRepresentation());
        return pOutput.toByteArray();
    }
}
