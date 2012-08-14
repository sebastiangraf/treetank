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

import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.delegates.ValNodeDelegate;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.node.interfaces.IValNode;

import com.google.common.hash.Hasher;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * <h1>TextNode</h1>
 * 
 * <p>
 * Node representing a text node.
 * </p>
 */
public final class TextNode implements IStructNode, IValNode, INode {

    /** Delegate for common node information. */
    private final NodeDelegate mDel;

    /** Delegate for common value node information. */
    private final ValNodeDelegate mValDel;

    /** Delegate for common struct node information. */
    private final StructNodeDelegate mStrucDel;

    /**
     * Constructor for TextNode.
     * 
     * @param pDel
     *            Delegate for <code>INode</code> implementation.
     * @param pValDel
     *            Delegate for {@link IValNode} implementation.
     * @param pStrucDel
     *            Delegate for {@link IStructNode} implementation.
     */
    public TextNode(final NodeDelegate pDel, final StructNodeDelegate pStrucDel, final ValNodeDelegate pValDel) {
        mDel = pDel;
        mValDel = pValDel;
        mStrucDel = pStrucDel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getKind() {
        return IConstants.TEXT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawValue() {
        return mValDel.getRawValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final byte[] pVal) {
        mValDel.setValue(pVal);
    }

    /** {@inheritDoc} */
    @Override
    public long getFirstChildKey() {
        return mStrucDel.getFirstChildKey();
    }

    /**
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.node.delegates.ValNodeDelegate#setHash(long)
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
     * @param pParentKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(final long pParentKey) {
        mDel.setParentKey(pParentKey);
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
     * Delegate method for hasFirstChild.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#hasFirstChild()
     */
    public boolean hasFirstChild() {
        return mStrucDel.hasFirstChild();
    }

    /**
     * Delegate method for hasLeftSibling.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#hasLeftSibling()
     */
    public boolean hasLeftSibling() {
        return mStrucDel.hasLeftSibling();
    }

    /**
     * Delegate method for hasRightSibling.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#hasRightSibling()
     */
    public boolean hasRightSibling() {
        return mStrucDel.hasRightSibling();
    }

    /**
     * Delegate method for getChildCount.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#getChildCount()
     */
    public long getChildCount() {
        return mStrucDel.getChildCount();
    }

    /**
     * Delegate method for getLeftSiblingKey.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#getLeftSiblingKey()
     */
    public long getLeftSiblingKey() {
        return mStrucDel.getLeftSiblingKey();
    }

    /**
     * Delegate method for getRightSiblingKey.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#getRightSiblingKey()
     */
    public long getRightSiblingKey() {
        return mStrucDel.getRightSiblingKey();
    }

    /**
     * Delegate method for setRightSiblingKey.
     * 
     * @param pRightSiblingKey
     * @see org.treetank.node.delegates.StructNodeDelegate#setRightSiblingKey(long)
     */
    public void setRightSiblingKey(final long pRightSiblingKey) {
        mStrucDel.setRightSiblingKey(pRightSiblingKey);
    }

    /**
     * Delegate method for setLeftSiblingKey.
     * 
     * @param pLeftSiblingKey
     * @see org.treetank.node.delegates.StructNodeDelegate#setLeftSiblingKey(long)
     */
    public void setLeftSiblingKey(final long pLeftSiblingKey) {
        mStrucDel.setLeftSiblingKey(pLeftSiblingKey);
    }

    /**
     * Delegate method for setFirstChildKey.
     * 
     * @param pFirstChildKey
     * @see org.treetank.node.delegates.StructNodeDelegate#setFirstChildKey(long)
     */
    public void setFirstChildKey(final long pFirstChildKey) {
        mStrucDel.setFirstChildKey(pFirstChildKey);
    }

    /**
     * Delegate method for decrementChildCount.
     * 
     * @see org.treetank.node.delegates.StructNodeDelegate#decrementChildCount()
     */
    public void decrementChildCount() {
        mStrucDel.decrementChildCount();
    }

    /**
     * Delegate method for incrementChildCount.
     * 
     * @see org.treetank.node.delegates.StructNodeDelegate#incrementChildCount()
     */
    public void incrementChildCount() {
        mStrucDel.incrementChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        Hasher hc = IConstants.HF.newHasher();
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TextNode [mDel=");
        builder.append(mDel);
        builder.append(", mValDel=");
        builder.append(mValDel);
        builder.append(", mStrucDel=");
        builder.append(mStrucDel);
        builder.append("]");
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
     * Getting the inlying {@link ValNodeDelegate}.
     * 
     * @return
     */
    ValNodeDelegate getValNodeDelegate() {
        return mValDel;
    }

    /**
     * Getting the inlying {@link StructNodeDelegate}.
     * 
     * @return
     */
    StructNodeDelegate getStrucNodeDelegate() {
        return mStrucDel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeInt(IConstants.TEXT);
        pOutput.write(mDel.getByteRepresentation());
        pOutput.write(mStrucDel.getByteRepresentation());
        pOutput.write(mValDel.getByteRepresentation());
        return pOutput.toByteArray();
    }

}
