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

    protected static final int NAME_KEY = 2;
    protected static final int URI_KEY = 3;

    NamespaceNode(final long[] builder) {
        super(builder);
        // mData[NAME_KEY] = builder.getNameKey();
        // mData[URI_KEY] = builder.getUriKey();
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
        return (int) mData[NAME_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int nameKey) {
        mData[NAME_KEY] = nameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return (int) mData[URI_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int uriKey) {
        mData[URI_KEY] = uriKey;
    }

    @Override
    public AbsNode clone() {
        final AbsNode toClone = new NamespaceNode(AbsNode.cloneData(mData));
        return toClone;
    }

    public static final long[] createData(final long nodeKey,
            final long parentKey, final int uriKey, final int prefixKey) {
        final long[] data = new long[ENodes.NAMESPACE_KIND.getSize()];
        data[AbsNode.NODE_KEY] = nodeKey;
        data[AbsNode.PARENT_KEY] = parentKey;
        data[NamespaceNode.URI_KEY] = uriKey;
        data[NamespaceNode.NAME_KEY] = prefixKey;
        return data;
    }

    public static final long[] createData(final long nodeKey,
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
