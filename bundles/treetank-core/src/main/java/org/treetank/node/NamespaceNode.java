/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.node;

import java.util.Arrays;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IVisitor;

/**
 * <h1>NamespaceNode</h1>
 * 
 * <p>
 * Node representing a namespace.
 * </p>
 */
public final class NamespaceNode extends AbsNode {

    protected static final int NAME_KEY = 1;
    protected static final int URI_KEY = 2;

    /**
     * Constructor.
     * 
     * @param mLongBuilder
     *            building long data
     * @param mIntBuilder
     *            building int data
     */
    NamespaceNode(final long[] mLongBuilder, final int[] mIntBuilder) {
        super(mLongBuilder, mIntBuilder);
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
        return mIntData[NAME_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int mNameKey) {
        mIntData[NAME_KEY] = mNameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return mIntData[URI_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int mUriKey) {
        mIntData[URI_KEY] = mUriKey;
    }

    /** {@inheritDoc} */
    @Override
    public AbsNode clone() {
        final AbsNode toClone = new NamespaceNode(AbsNode.cloneData(mLongData), AbsNode.cloneData(mIntData));
        return toClone;
    }

    public static AbsNode createData(final long mNodeKey, final long parentKey, final int mUriKey,
        final int prefixKey) {
        final long[] longData = new long[ENodes.NAMESPACE_KIND.getLongSize()];
        final int[] intData = new int[ENodes.NAMESPACE_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = mNodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        intData[NamespaceNode.URI_KEY] = mUriKey;
        intData[NamespaceNode.NAME_KEY] = prefixKey;
        return new NamespaceNode(longData, intData);
    }

    public static AbsNode createData(final long mNodeKey, final NamespaceNode mNode) {
        return createData(mNodeKey, mNode.getParentKey(), mNode.getURIKey(), mNode.getNameKey());
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
        result = prime * result + Arrays.hashCode(mIntData);
        return result;
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IItem> T accept(final IReadTransaction paramTransaction) {
        return (T)paramTransaction.getNode(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);    
    }
}
