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
import org.treetank.node.delegates.ValNodeDelegate;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.node.interfaces.IValNode;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * <h1>TextNode</h1>
 * 
 * <p>
 * Node representing a text node.
 * </p>
 */
public final class TextNode implements IStructNode, IValNode, INode {

    /**
     * Enum for TextNodeFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum TextNodeFunnel implements Funnel<org.treetank.api.INode> {
        INSTANCE;
        public void funnel(org.treetank.api.INode node, PrimitiveSink into) {
            final TextNode from = (TextNode)node;
            from.mDel.getFunnel().funnel(from, into);
            from.mStrucDel.getFunnel().funnel(from, into);
            from.mValDel.getFunnel().funnel(from, into);
        }
    }

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
    public long getHash() {
        return mDel.getHash();
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
    public boolean hasParent() {
        return mDel.hasParent();
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
    public void setParentKey(final long pParentKey) {
        mDel.setParentKey(pParentKey);
    }

    /**
     * /** {@inheritDoc}
     */
    @Override
    public void setTypeKey(final int pTypeKey) {
        mDel.setTypeKey(pTypeKey);
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
    public void setRightSiblingKey(final long pRightSiblingKey) {
        mStrucDel.setRightSiblingKey(pRightSiblingKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeftSiblingKey(final long pLeftSiblingKey) {
        mStrucDel.setLeftSiblingKey(pLeftSiblingKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstChildKey(final long pFirstChildKey) {
        mStrucDel.setFirstChildKey(pFirstChildKey);
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
    public String toString() {
        return toStringHelper(this).add("mDel", mDel).add("mValDel", mValDel).add("mStrucDel", mStrucDel)
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.TEXT);
            mDel.serialize(pOutput);
            mStrucDel.serialize(pOutput);
            mValDel.serialize(pOutput);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public Funnel<org.treetank.api.INode> getFunnel() {
        return TextNodeFunnel.INSTANCE;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDel == null) ? 0 : mDel.hashCode());
        result = prime * result + ((mStrucDel == null) ? 0 : mStrucDel.hashCode());
        result = prime * result + ((mValDel == null) ? 0 : mValDel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

}
