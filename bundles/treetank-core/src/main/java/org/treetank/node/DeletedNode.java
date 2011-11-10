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
import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;

/**
 * If a node is deleted, it will be encapsulated over this class.
 * 
 * @author Sebastian Graf
 * 
 */
public final class DeletedNode implements IItem {

    /**
     * Delegate for common data.
     */
    private final NodeDelegate mDelegate;

    /**
     * Constructor.
     * 
     * @param paramNode
     *            nodekey to be replaced with a deletednode
     * @param paramParent
     *            parent of this key.
     */
    public DeletedNode(final long paramNode, final long paramParent, final long paramHash) {
        mDelegate = new NodeDelegate(paramNode, paramParent, paramHash);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink mNodeOut) {
        mDelegate.serialize(mNodeOut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.DELETE_KIND;
    }

    @Override
    public IItem clone() {
        final IItem toClone =
            new DeletedNode(mDelegate.getNodeKey(), mDelegate.getParentKey(), mDelegate.getHash());
        return toClone;
    }

    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        // Do nothing.
    }

    /**
     * Delegate method for setHash.
     * 
     * @param paramHash
     * @see org.treetank.node.NodeDelegate#setHash(long)
     */
    public void setHash(long paramHash) {
        mDelegate.setHash(paramHash);
    }

    /**
     * Delegate method for getHash.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDelegate.getHash();
    }

    /**
     * Delegate method for setNodeKey.
     * 
     * @param paramKey
     * @see org.treetank.node.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(long paramKey) {
        mDelegate.setNodeKey(paramKey);
    }

    /**
     * Delegate method for getNodeKey.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#getNodeKey()
     */
    public long getNodeKey() {
        return mDelegate.getNodeKey();
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDelegate.getParentKey();
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDelegate.hasParent();
    }

    /**
     * Delegate method for getNameKey.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#getNameKey()
     */
    public int getNameKey() {
        return mDelegate.getNameKey();
    }

    /**
     * Delegate method for getURIKey.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#getURIKey()
     */
    public int getURIKey() {
        return mDelegate.getURIKey();
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    /**
     * Delegate method for getRawValue.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#getRawValue()
     */
    public byte[] getRawValue() {
        return mDelegate.getRawValue();
    }

    /**
     * Delegate method for setNameKey.
     * 
     * @param paramNameKey
     * @see org.treetank.node.NodeDelegate#setNameKey(int)
     */
    public void setNameKey(int paramNameKey) {
        mDelegate.setNameKey(paramNameKey);
    }

    /**
     * Delegate method for setURIKey.
     * 
     * @param paramUriKey
     * @see org.treetank.node.NodeDelegate#setURIKey(int)
     */
    public void setURIKey(int paramUriKey) {
        mDelegate.setURIKey(paramUriKey);
    }

    /**
     * Delegate method for setValue.
     * 
     * @param paramUriKey
     * @param paramVal
     * @see org.treetank.node.NodeDelegate#setValue(int, byte[])
     */
    public void setValue(int paramUriKey, byte[] paramVal) {
        mDelegate.setValue(paramUriKey, paramVal);
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param paramKey
     * @see org.treetank.node.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(long paramKey) {
        mDelegate.setParentKey(paramKey);
    }

    /**
     * Delegate method for setType.
     * 
     * @param paramType
     * @see org.treetank.node.NodeDelegate#setType(int)
     */
    public void setType(int paramType) {
        mDelegate.setType(paramType);
    }

    /**
     * Delegate method for toString.
     * 
     * @return
     * @see org.treetank.node.NodeDelegate#toString()
     */
    public String toString() {
        return mDelegate.toString();
    }

}
