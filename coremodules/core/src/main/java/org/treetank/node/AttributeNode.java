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
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.delegates.ValNodeDelegate;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IValNode;

import com.google.common.hash.Hasher;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * <h1>AttributeNode</h1>
 * 
 * <p>
 * Node representing an attribute.
 * </p>
 */
public final class AttributeNode implements INode, IValNode, INameNode {

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
     * @param pDel
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
    public ENode getKind() {
        return ENode.ATTRIBUTE_KIND;
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
     * Delegate method for setNodeKey.
     * 
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(final long pNodeKey) {
        mDel.setNodeKey(pNodeKey);
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
     * Delegate method for setParentKey.
     * 
     * @param pParentKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(final long pParentKey) {
        mDel.setParentKey(pParentKey);
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
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(final long pHash) {
        mDel.setHash(pHash);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuilder builder = new StringBuilder(mDel.toString());
        builder.append("\n");
        builder.append(mNameDel.toString());
        builder.append("\n");
        builder.append(mValDel.toString());
        return builder.toString();
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
     * Delegate method for setTypeKey.
     * 
     * @param pTypeKey
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(final int pTypeKey) {
        mDel.setTypeKey(pTypeKey);
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
     * Delegate method for getNameKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NameNodeDelegate#getNameKey()
     */
    public int getNameKey() {
        return mNameDel.getNameKey();
    }

    /**
     * Delegate method for getURIKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NameNodeDelegate#getURIKey()
     */
    public int getURIKey() {
        return mNameDel.getURIKey();
    }

    /**
     * Delegate method for setNameKey.
     * 
     * @param pNameKey
     * @see org.treetank.node.delegates.NameNodeDelegate#setNameKey(int)
     */
    public void setNameKey(final int pNameKey) {
        mNameDel.setNameKey(pNameKey);
    }

    /**
     * Delegate method for setURIKey.
     * 
     * @param pUriKey
     * @see org.treetank.node.delegates.NameNodeDelegate#setURIKey(int)
     */
    public void setURIKey(final int pUriKey) {
        mNameDel.setURIKey(pUriKey);
    }

    /**
     * Delegate method for getRawValue.
     * 
     * @return
     * @see org.treetank.node.delegates.ValNodeDelegate#getRawValue()
     */
    public byte[] getRawValue() {
        return mValDel.getRawValue();
    }

    /**
     * Delegate method for setValue.
     * 
     * @param pVal
     * @see org.treetank.node.delegates.ValNodeDelegate#setValue(byte[])
     */
    public void setValue(final byte[] pVal) {
        mValDel.setValue(pVal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        Hasher hc = IConstants.HF.newHasher();
        hc.putInt(mDel.hashCode());
        hc.putInt(mValDel.hashCode());
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
     * Getting the inlying {@link ValNodeDelegate}.
     * 
     * @return
     */
    ValNodeDelegate getValNodeDelegate() {
        return mValDel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.write(mDel.getByteRepresentation());
        pOutput.write(mNameDel.getByteRepresentation());
        pOutput.write(mValDel.getByteRepresentation());
        return pOutput.toByteArray();
    }

}
