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
 * $Id: ElementNode.java 4550 2009-02-05 09:25:46Z graf $
 */

package com.treetank.node;

import java.util.ArrayList;
import java.util.List;

import com.treetank.io.ITTSink;
import com.treetank.settings.EFixed;

/**
 * <h1>ElementNode</h1>
 * 
 * <p>
 * Node representing an XML element.
 * </p>
 */
public final class ElementNode extends AbsStructNode {

    protected static final int NAME_KEY = 1;

    protected static final int URI_KEY = 2;

    protected static final int ATTRIBUTE_COUNT = 3;

    protected static final int NAMESPACE_COUNT = 4;

    /** Keys of attributes. */
    private List<Long> mAttributeKeys;

    /** Keys of namespace declarations. */
    private List<Long> mNamespaceKeys;

    /**
     * Creating new element
     * 
     * @param longBuilder
     *            array with longs
     * @param intBuilder
     *            array with ints
     * @param attributeKeys
     *            attr keys
     * @param namespaceKeys
     *            namespace keys
     */
    ElementNode(final long[] longBuilder, final int[] intBuilder,
            final List<Long> attributeKeys, final List<Long> namespaceKeys) {
        super(longBuilder, intBuilder);
        mAttributeKeys = attributeKeys;
        mNamespaceKeys = namespaceKeys;
    }

    /**
     * {@inheritDoc}
     */
    public int getAttributeCount() {
        return mIntData[ATTRIBUTE_COUNT];
    }

    /**
     * {@inheritDoc}
     */
    public long getAttributeKey(final int index) {
        if (mAttributeKeys.size() <= index) {
            return (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
        }
        return mAttributeKeys.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public void insertAttribute(final long attributeKey) {
        mAttributeKeys.add(attributeKey);
        mIntData[ATTRIBUTE_COUNT]++;
    }

    /**
     * Removing an attribute
     * 
     * @param attributeKey
     *            the key of the attribute to be removed
     */
    public void removeAttribute(final long attributeKey) {
        mAttributeKeys.remove(attributeKey);
        mIntData[ATTRIBUTE_COUNT]--;
    }

    /**
     * {@inheritDoc}
     */
    public int getNamespaceCount() {
        return mIntData[NAMESPACE_COUNT];
    }

    /**
     * {@inheritDoc}
     */
    public long getNamespaceKey(final int index) {
        if (mNamespaceKeys.size() <= index) {
            return (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
        }
        return mNamespaceKeys.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public void insertNamespace(final long namespaceKey) {
        mNamespaceKeys.add(namespaceKey);
        mIntData[NAMESPACE_COUNT]++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.ELEMENT_KIND;
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
    public void setNameKey(final int localPartKey) {
        mIntData[NAME_KEY] = localPartKey;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink out) {
        super.serialize(out);
        if (mAttributeKeys != null) {
            for (int i = 0, l = mAttributeKeys.size(); i < l; i++) {
                out.writeLong(mAttributeKeys.get(i));
            }
        }
        if (mNamespaceKeys != null) {
            for (int i = 0, l = mNamespaceKeys.size(); i < l; i++) {
                out.writeLong(mNamespaceKeys.get(i));
            }
        }
    }

    @Override
    public AbsNode clone() {
        final List<Long> attList = new ArrayList<Long>(mAttributeKeys.size());
        final List<Long> namespaceList = new ArrayList<Long>(
                mNamespaceKeys.size());
        for (final Long i : mAttributeKeys) {
            attList.add(i);
        }
        for (final Long i : mNamespaceKeys) {
            namespaceList.add(i);
        }

        final AbsNode toClone = new ElementNode(AbsNode.cloneData(mLongData),
                AbsNode.cloneData(mIntData), attList, namespaceList);
        return toClone;
    }

    public final static AbsNode createData(final long nodeKey,
            final long parentKey, final long leftSibKey,
            final long rightSibKey, final long firstChild,
            final long childCount, final int nameKey, final int uriKey,
            final int type) {
        final long[] longData = new long[ENodes.ELEMENT_KIND.getLongSize()];
        final int[] intData = new int[ENodes.ELEMENT_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = nodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        longData[AbsStructNode.LEFT_SIBLING_KEY] = leftSibKey;
        longData[AbsStructNode.RIGHT_SIBLING_KEY] = rightSibKey;
        longData[AbsStructNode.FIRST_CHILD_KEY] = firstChild;
        longData[AbsStructNode.CHILD_COUNT] = childCount;
        intData[ElementNode.NAME_KEY] = nameKey;
        intData[ElementNode.URI_KEY] = uriKey;
        intData[AbsNode.TYPE_KEY] = type;
        intData[ElementNode.ATTRIBUTE_COUNT] = 0;
        intData[ElementNode.NAMESPACE_COUNT] = 0;
        return ENodes.ELEMENT_KIND.createNodeFromScratch(longData, intData,
                null);
    }

    public final static AbsNode createData(final long nodeKey,
            final ElementNode node) {
        return createData(nodeKey, node.getParentKey(),
                node.getLeftSiblingKey(), node.getRightSiblingKey(),
                node.getFirstChildKey(), node.getChildCount(),
                node.getNameKey(), node.getURIKey(), node.getTypeKey());
    }

    @Override
    public String toString() {
        final StringBuilder returnVal = new StringBuilder(super.toString());
        returnVal.append("\n\tname key: ").append(getNameKey())
                .append("\n\turi key: ").append(getURIKey())
                .append(getNameKey()).append("\n\tnamespaces: ")
                .append(mNamespaceKeys.toString()).append("\n\tattributes: ")
                .append(mAttributeKeys.toString()).toString();
        return returnVal.toString();
    }

}
