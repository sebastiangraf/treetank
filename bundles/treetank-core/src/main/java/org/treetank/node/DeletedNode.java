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

import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.interfaces.INode;

/**
 * If a node is deleted, it will be encapsulated over this class.
 * 
 * @author Sebastian Graf
 * 
 */
public final class DeletedNode implements INode {

    /**
     * Delegate for common data.
     */
    private final NodeDelegate mDel;

    /**
     * Constructor.
     * 
     * @param paramNode
     *            nodekey to be replaced with a deletednode
     * @param paramParent
     *            parent of this key.
     */
    public DeletedNode(final NodeDelegate pDel) {
        mDel = pDel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink pSink) {
        mDel.serialize(pSink);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.DELETE_KIND;
    }

    /**
     * Delegate method for getNodeKey.
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getNodeKey()
     */
    public long getNodeKey() {
        return mDel.getNodeKey();
    }

    /**
     * Delegate method for setNodeKey.
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(long pNodeKey) {
        mDel.setNodeKey(pNodeKey);
    }

    /**
     * Delegate method for getParentKey.
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDel.getParentKey();
    }

    /**
     * Delegate method for setParentKey.
     * @param pParentKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(long pParentKey) {
        mDel.setParentKey(pParentKey);
    }

    /**
     * Delegate method for getHash.
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDel.getHash();
    }

    /**
     * Delegate method for setHash.
     * @param pHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(long pHash) {
        mDel.setHash(pHash);
    }

    /**
     * Delegate method for acceptVisitor.
     * @param pVisitor
     * @see org.treetank.node.delegates.NodeDelegate#acceptVisitor(org.treetank.api.IVisitor)
     */
    public void acceptVisitor(IVisitor pVisitor) {
        mDel.acceptVisitor(pVisitor);
    }

    /**
     * Delegate method for getTypeKey.
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDel.getTypeKey();
    }

    /**
     * Delegate method for setTypeKey.
     * @param pTypeKey
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(int pTypeKey) {
        mDel.setTypeKey(pTypeKey);
    }

    /**
     * Delegate method for hasParent.
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDel.hasParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public INode clone() {
        final INode toClone = new DeletedNode(mDel.clone());
        return toClone;
    }




    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDel == null) ? 0 : mDel.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeletedNode other = (DeletedNode) obj;
        if (mDel == null) {
            if (other.mDel != null)
                return false;
        } else if (!mDel.equals(other.mDel))
            return false;
        return true;
    }

    /**
     * Delegate method for toString.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#toString()
     */
    public String toString() {
        return mDel.toString();
    }

}
