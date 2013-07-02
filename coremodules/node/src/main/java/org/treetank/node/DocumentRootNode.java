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

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataOutput;
import java.io.IOException;

import org.treetank.exception.TTIOException;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * <h1>DocumentNode</h1>
 * 
 * <p>
 * Node representing the root of a document. This node is guaranteed to exist in revision 0 and can not be
 * removed.
 * </p>
 */
public final class DocumentRootNode implements INode, IStructNode {

    /**
     * Enum for DocRootFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum DocumentNodeFunnel implements Funnel<org.treetank.api.INode> {
        INSTANCE;
        public void funnel(org.treetank.api.INode node, PrimitiveSink into) {
            final DocumentRootNode from = (DocumentRootNode)node;
            from.mDel.getFunnel().funnel(from, into);
            from.mStrucDel.getFunnel().funnel(from, into);
        }
    }

    /** Delegate for common node information. */
    private final NodeDelegate mDel;

    /** Delegate for struct node information. */
    private final StructNodeDelegate mStrucDel;

    /**
     * Constructor.
     * 
     * @param pNodeDel
     *            delegate for node properties
     * @param pStrucDel
     *            delegate for struct properties
     */
    public DocumentRootNode(final NodeDelegate pNodeDel, final StructNodeDelegate pStrucDel) {
        mDel = pNodeDel;
        mStrucDel = pStrucDel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getKind() {
        return IConstants.ROOT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNodeKey() {
        return mDel.getNodeKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getParentKey() {
        return mDel.getParentKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getHash() {
        return mDel.getHash();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHash(final long pHash) {
        mDel.setHash(pHash);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return mDel.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object pObj) {
        return pObj.hashCode() == hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeKey() {
        return mDel.getTypeKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParent() {
        return mDel.hasParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasFirstChild() {
        return mStrucDel.hasFirstChild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLeftSibling() {
        return mStrucDel.hasLeftSibling();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRightSibling() {
        return mStrucDel.hasRightSibling();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getChildCount() {
        return mStrucDel.getChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFirstChildKey() {
        return mStrucDel.getFirstChildKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLeftSiblingKey() {
        return mStrucDel.getLeftSiblingKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRightSiblingKey() {
        return mStrucDel.getRightSiblingKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRightSiblingKey(final long pKey) {
        mStrucDel.setRightSiblingKey(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeftSiblingKey(final long pKey) {
        mStrucDel.setLeftSiblingKey(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstChildKey(final long pKey) {
        mStrucDel.setFirstChildKey(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrementChildCount() {
        mStrucDel.decrementChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementChildCount() {
        mStrucDel.incrementChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentKey(final long pKey) {
        mStrucDel.setParentKey(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTypeKey(final int pTypeKey) {
        mStrucDel.setTypeKey(pTypeKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mDel", mDel).add("mStrucDel", mStrucDel).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.ROOT);
            mDel.serialize(pOutput);
            mStrucDel.serialize(pOutput);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public Funnel<org.treetank.api.INode> getFunnel() {
        return DocumentNodeFunnel.INSTANCE;
    }

}
