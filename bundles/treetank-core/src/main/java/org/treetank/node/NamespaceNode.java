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
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;

/**
 * <h1>NamespaceNode</h1>
 * 
 * <p>
 * Node representing a namespace.
 * </p>
 */
public final class NamespaceNode extends AbsNode implements INode, INameNode {

    protected static final int NAME_KEY = 4;
    protected static final int URI_KEY = 8;

    /**
     * Constructor.
     * 
     * @param mLongBuilder
     *            building long data
     * @param mIntBuilder
     *            building int data
     */
    NamespaceNode(final byte[] mByteBuilder, final byte[] mPointerBuilder) {
        super(mByteBuilder, mPointerBuilder);
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
        return readIntBytes(NAME_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int mNameKey) {
        writeIntBytes(NAME_KEY, mNameKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return readIntBytes(URI_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int mUriKey) {
        writeIntBytes(URI_KEY, mUriKey);
    }

    /** {@inheritDoc} */
    @Override
    public AbsNode clone() {
        return new NamespaceNode(ENodes.cloneData(mByteData), ENodes.cloneData(mPointerData));
    }

    public static AbsNode createData(final long mNodeKey, final long mParentKey, final int mUriKey,
        final int prefixKey) {

        final byte[] byteData = new byte[ENodes.NAMESPACE_KIND.getIntSize()];

        final byte[] pointerData = new byte[ENodes.NAMESPACE_KIND.getLongSize()];

        int mCount = AbsNode.NODE_KEY;
        for (byte aByte : longToByteArray(mNodeKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsNode.PARENT_KEY;
        for (byte aByte : longToByteArray(mParentKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = NamespaceNode.URI_KEY;
        for (byte aByte : intToByteArray(mUriKey)) {
            byteData[mCount++] = aByte;
        }

        mCount = NamespaceNode.NAME_KEY;
        for (byte aByte : intToByteArray(prefixKey)) {
            byteData[mCount++] = aByte;
        }

        return new NamespaceNode(byteData, pointerData);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder returnVal = new StringBuilder(super.toString());
        returnVal.append("\n\ttype key: ").append(getTypeKey()).append("\n\tname key: ").append(getNameKey())
            .toString();
        return returnVal.toString();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 98807;
        int result = 1;
        result = prime * result + Arrays.hashCode(mByteData);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);
    }
}
