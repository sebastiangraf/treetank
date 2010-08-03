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
     * Creating new element.
     * 
     * @param mLongBuilder
     *            array with longs
     * @param mIntBuilder
     *            array with ints
     * @param mAttributeKeys
     *            attr keys
     * @param mNamespaceKeys
     *            namespace keys
     */
    ElementNode(final long[] mLongBuilder, final int[] mIntBuilder, final List<Long> mAttributeKeys,
        final List<Long> mNamespaceKeys) {
        super(mLongBuilder, mIntBuilder);
        this.mAttributeKeys = mAttributeKeys;
        this.mNamespaceKeys = mNamespaceKeys;
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
    public long getAttributeKey(final int mIndex) {
        if (mAttributeKeys.size() <= mIndex) {
            return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        }
        return mAttributeKeys.get(mIndex);
    }

    /**
     * {@inheritDoc}
     */
    public void insertAttribute(final long mAttributeKey) {
        mAttributeKeys.add(mAttributeKey);
        mIntData[ATTRIBUTE_COUNT]++;
    }

    /**
     * Removing an attribute.
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
    public long getNamespaceKey(final int mIndex) {
        if (mNamespaceKeys.size() <= mIndex) {
            return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        }
        return mNamespaceKeys.get(mIndex);
    }

    /**
     * {@inheritDoc}
     */
    public void insertNamespace(final long mNamespaceKey) {
        mNamespaceKeys.add(mNamespaceKey);
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
    @Override
    public int getNameKey() {
        return mIntData[NAME_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int mLocalPartKey) {
        mIntData[NAME_KEY] = mLocalPartKey;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink mOut) {
        super.serialize(mOut);
        if (mAttributeKeys != null) {
            for (int i = 0, l = mAttributeKeys.size(); i < l; i++) {
                mOut.writeLong(mAttributeKeys.get(i));
            }
        }
        if (mNamespaceKeys != null) {
            for (int i = 0, l = mNamespaceKeys.size(); i < l; i++) {
                mOut.writeLong(mNamespaceKeys.get(i));
            }
        }
    }

    @Override
    public AbsNode clone() {
        final List<Long> attList = new ArrayList<Long>(mAttributeKeys.size());
        final List<Long> namespaceList = new ArrayList<Long>(mNamespaceKeys.size());
        for (final Long i : mAttributeKeys) {
            attList.add(i);
        }
        for (final Long i : mNamespaceKeys) {
            namespaceList.add(i);
        }

        final AbsNode toClone =
            new ElementNode(AbsNode.cloneData(mLongData), AbsNode.cloneData(mIntData), attList, namespaceList);
        return toClone;
    }

    public static AbsNode createData(final long mNodeKey, final long parentKey, final long mLeftSibKey,
        final long rightSibKey, final long mFirstChild, final long mChildCount, final int mNameKey,
        final int mUriKey, final int mType) {
        final long[] longData = new long[ENodes.ELEMENT_KIND.getLongSize()];
        final int[] intData = new int[ENodes.ELEMENT_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = mNodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        longData[AbsStructNode.LEFT_SIBLING_KEY] = mLeftSibKey;
        longData[AbsStructNode.RIGHT_SIBLING_KEY] = rightSibKey;
        longData[AbsStructNode.FIRST_CHILD_KEY] = mFirstChild;
        longData[AbsStructNode.CHILD_COUNT] = mChildCount;
        intData[ElementNode.NAME_KEY] = mNameKey;
        intData[ElementNode.URI_KEY] = mUriKey;
        intData[AbsNode.TYPE_KEY] = mType;
        intData[ElementNode.ATTRIBUTE_COUNT] = 0;
        intData[ElementNode.NAMESPACE_COUNT] = 0;
        return ENodes.ELEMENT_KIND.createNodeFromScratch(longData, intData, null);
    }

    public static AbsNode createData(final long mNodeKey, final ElementNode mNode) {
        return createData(mNodeKey, mNode.getParentKey(), mNode.getLeftSiblingKey(), mNode
            .getRightSiblingKey(), mNode.getFirstChildKey(), mNode.getChildCount(), mNode.getNameKey(), mNode
            .getURIKey(), mNode.getTypeKey());
    }

    @Override
    public String toString() {
        final StringBuilder returnVal = new StringBuilder(super.toString());
        returnVal.append("\n\tname key: ").append(getNameKey()).append("\n\turi key: ").append(getURIKey())
            .append(getNameKey()).append("\n\tnamespaces: ").append(mNamespaceKeys.toString()).append(
                "\n\tattributes: ").append(mAttributeKeys.toString()).toString();
        return returnVal.toString();
    }

}
