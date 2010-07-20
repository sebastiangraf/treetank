/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: NamespaceNode.java 4550 2009-02-05 09:25:46Z graf $
 */

package com.treetank.node;

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
     * Constructor
     * 
     * @param longBuilder
     *            building long data
     * @param intBuilder
     *            building int data
     */
    NamespaceNode(final long[] longBuilder, final int[] intBuilder) {
        super(longBuilder, intBuilder);
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
    public int getNameKey() {
        return mIntData[NAME_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public void setNameKey(final int nameKey) {
        mIntData[NAME_KEY] = nameKey;
    }

    /**
     * {@inheritDoc}
     */
    public int getURIKey() {
        return mIntData[URI_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public void setURIKey(final int uriKey) {
        mIntData[URI_KEY] = uriKey;
    }

    @Override
    public AbsNode clone() {
        final AbsNode toClone = new NamespaceNode(AbsNode.cloneData(mLongData),
                AbsNode.cloneData(mIntData));
        return toClone;
    }

    public static final AbsNode createData(final long nodeKey,
            final long parentKey, final int uriKey, final int prefixKey) {
        final long[] longData = new long[ENodes.NAMESPACE_KIND.getLongSize()];
        final int[] intData = new int[ENodes.NAMESPACE_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = nodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        intData[NamespaceNode.URI_KEY] = uriKey;
        intData[NamespaceNode.NAME_KEY] = prefixKey;
        return new NamespaceNode(longData, intData);
    }

    public static final AbsNode createData(final long nodeKey,
            final NamespaceNode node) {
        return createData(nodeKey, node.getParentKey(), node.getURIKey(),
                node.getNameKey());
    }

    @Override
    public String toString() {
        final StringBuilder returnVal = new StringBuilder(super.toString());
        returnVal.append("\n\ttype key: ").append(getTypeKey())
                .append("\n\tname key: ").append(getNameKey()).toString();
        return returnVal.toString();
    }

}
