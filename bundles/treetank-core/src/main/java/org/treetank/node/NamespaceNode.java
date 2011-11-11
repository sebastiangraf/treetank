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

import java.util.Arrays;

import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.delegates.ValNodeDelegate;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;

/**
 * <h1>NamespaceNode</h1>
 * 
 * <p>
 * Node representing a namespace.
 * </p>
 */
public final class NamespaceNode implements INode, INameNode {

    private final NodeDelegate mDelegate;
    private final NameNodeDelegate mNameDelegate;

    /**
     * Constructor.
     * 
     * @param mLongBuilder
     *            building long data
     * @param mIntBuilder
     *            building int data
     */
    public NamespaceNode(final NodeDelegate paramDelegate, final NameNodeDelegate paramNameDelegate) {
        mDelegate = paramDelegate;
        mNameDelegate = paramNameDelegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.NAMESPACE_KIND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return mNameDelegate.getNameKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int mNameKey) {
        mNameDelegate.setNameKey(mNameKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return mNameDelegate.getURIKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int paramUriKey) {
        mNameDelegate.setURIKey(paramUriKey);
    }

    /** {@inheritDoc} */
    @Override
    public NamespaceNode clone() {
        return new NamespaceNode(mDelegate.clone(), mNameDelegate.clone());
    }

    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);
    }

    /**
     * Delegate method for setHash.
     * 
     * @param paramHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(long paramHash) {
        mDelegate.setHash(paramHash);
    }

    /**
     * Delegate method for getHash.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDelegate.getHash();
    }

    /**
     * Delegate method for setNodeKey.
     * 
     * @param paramKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(long paramKey) {
        mDelegate.setNodeKey(paramKey);
    }

    /**
     * Delegate method for getNodeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getNodeKey()
     */
    public long getNodeKey() {
        return mDelegate.getNodeKey();
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDelegate.getParentKey();
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDelegate.hasParent();
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    /**
     * Delegate method for serialize.
     * 
     * @param paramSink
     * @see org.treetank.node.delegates.NodeDelegate#serialize(org.treetank.io.ITTSink)
     */
    public void serialize(ITTSink paramSink) {
        mDelegate.serialize(paramSink);
        mNameDelegate.serialize(paramSink);
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param paramKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(long paramKey) {
        mDelegate.setParentKey(paramKey);
    }

    /**
     * Delegate method for setType.
     * 
     * @param paramType
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(int paramType) {
        mDelegate.setTypeKey(paramType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDelegate == null) ? 0 : mDelegate.hashCode());
        result = prime * result + ((mNameDelegate == null) ? 0 : mNameDelegate.hashCode());
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
        NamespaceNode other = (NamespaceNode)obj;
        if (mDelegate == null) {
            if (other.mDelegate != null)
                return false;
        } else if (!mDelegate.equals(other.mDelegate))
            return false;
        if (mNameDelegate == null) {
            if (other.mNameDelegate != null)
                return false;
        } else if (!mNameDelegate.equals(other.mNameDelegate))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(mDelegate.toString());
        builder.append("\n");
        builder.append(mNameDelegate.toString());
        return builder.toString();
    }
}
