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
package org.treetank.node.delegates;

import static org.treetank.node.IConstants.NULL_NODE;

import org.treetank.node.IConstants;
import org.treetank.node.interfaces.IStructNode;

import com.google.common.hash.Hasher;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Delegate method for all nodes building up the structure. That means that all
 * nodes representing trees in Treetank are represented by an instance of the
 * interface {@link IStructNode} namely containing the position of all related
 * siblings, the first-child and all nodes defined by the {@link NodeDelegate} as well.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class StructNodeDelegate implements IStructNode {

    /** Pointer to the first child of the current node. */
    private long mFirstChild;
    /** Pointer to the right sibling of the current node. */
    private long mRightSibling;
    /** Pointer to the left sibling of the current node. */
    private long mLeftSibling;
    /** Pointer to the number of children. */
    private long mChildCount;
    /** Delegate for common node information. */
    private final NodeDelegate mDelegate;

    /**
     * Constructor.
     * 
     * @param pDel
     *            to be set
     * @param pFirstChild
     *            to be set
     * @param pRightSib
     *            to be set
     * @param pLeftSib
     *            to be set
     * @param pChildCount
     *            to be set
     */
    public StructNodeDelegate(final NodeDelegate pDel, final long pFirstChild, final long pRightSib,
        final long pLeftSib, final long pChildCount) {
        mDelegate = pDel;
        mFirstChild = pFirstChild;
        mRightSibling = pRightSib;
        mLeftSibling = pLeftSib;
        mChildCount = pChildCount;
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
    public boolean hasFirstChild() {
        return mFirstChild != NULL_NODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLeftSibling() {
        return mLeftSibling != NULL_NODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRightSibling() {
        return mRightSibling != NULL_NODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getChildCount() {
        return mChildCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFirstChildKey() {
        return mFirstChild;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLeftSiblingKey() {
        return mLeftSibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRightSiblingKey() {
        return mRightSibling;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRightSiblingKey(final long pKey) {
        mRightSibling = pKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeftSiblingKey(final long pKey) {
        mLeftSibling = pKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstChildKey(final long pKey) {
        mFirstChild = pKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementChildCount() {
        mChildCount--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementChildCount() {
        mChildCount++;
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
     * Delegate method for setNodeKey.
     * 
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(long pNodeKey) {
        mDelegate.setNodeKey(pNodeKey);
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return the key of the parent
     * @see org.treetank.node.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDelegate.getParentKey();
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pParentKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(long pParentKey) {
        mDelegate.setParentKey(pParentKey);
    }

    /**
     * Delegate method for getHash.
     * 
     * @return the hash
     * @see org.treetank.node.delegates.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDelegate.getHash();
    }

    /**
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(long pHash) {
        mDelegate.setHash(pHash);
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return the type of the node
     * @see org.treetank.node.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    /**
     * Delegate method for setTypeKey.
     * 
     * @param pTypeKey
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(int pTypeKey) {
        mDelegate.setTypeKey(pTypeKey);
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
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        Hasher hc = IConstants.HF.newHasher();
        hc.putLong(mChildCount);
        hc.putInt(mDelegate.hashCode());
        hc.putLong(mFirstChild);
        hc.putLong(mLeftSibling);
        hc.putLong(mLeftSibling);
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
        builder.append("StructNodeDelegate [mFirstChild=");
        builder.append(mFirstChild);
        builder.append(", mRightSibling=");
        builder.append(mRightSibling);
        builder.append(", mLeftSibling=");
        builder.append(mLeftSibling);
        builder.append(", mChildCount=");
        builder.append(mChildCount);
        builder.append(", mDelegate=");
        builder.append(mDelegate);
        builder.append("]");
        return builder.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeLong(getFirstChildKey());
        pOutput.writeLong(getRightSiblingKey());
        pOutput.writeLong(getLeftSiblingKey());
        pOutput.writeLong(getChildCount());
        return pOutput.toByteArray();
    }
}
