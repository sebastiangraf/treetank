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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.settings.EFixed;

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
     * Getting the count of attributes.
     * 
     * @return the count of attributes
     */
    public int getAttributeCount() {
        return mIntData[ATTRIBUTE_COUNT];
    }

    /**
     * Getting the attribute key for an given index.
     * 
     * @param paramIndex
     *            index of the attribute
     * @return the attribute key
     */
    public long getAttributeKey(final int paramIndex) {
        if (mAttributeKeys.size() <= paramIndex) {
            return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        }
        return mAttributeKeys.get(paramIndex);
    }

    /**
     * Inserting a namespace.
     * 
     * @param paramAttributeKey
     *            the new attribute key
     */
    public void insertAttribute(final long paramAttributeKey) {
        mAttributeKeys.add(paramAttributeKey);
        mIntData[ATTRIBUTE_COUNT]++;
    }

    /**
     * Removing an attribute.
     * 
     * @param paramAttributeKey
     *            the key of the attribute to be removed
     */
    public void removeAttribute(final long paramAttributeKey) {
        mAttributeKeys.remove(paramAttributeKey);
        mIntData[ATTRIBUTE_COUNT]--;
    }

    /**
     * Getting the count of namespaces.
     * 
     * @return the count of namespaces
     */
    public int getNamespaceCount() {
        return mIntData[NAMESPACE_COUNT];
    }

    /**
     * Getting the namespace key for an given index.
     * 
     * @param paramIndex
     *            index of the namespace
     * @return the namespace key
     */
    public long getNamespaceKey(final int paramIndex) {
        if (mNamespaceKeys.size() <= paramIndex) {
            return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        }
        return mNamespaceKeys.get(paramIndex);
    }

    /**
     * Inserting a namespace.
     * 
     * @param paramNamespaceKey
     *            new namespace key
     */
    public void insertNamespace(final long paramNamespaceKey) {
        mNamespaceKeys.add(paramNamespaceKey);
        mIntData[NAMESPACE_COUNT]++;
    }

    /**
     * Removing a namepsace.
     * 
     * @param paramNamespaceKey
     *            the key of the namespace to be removed
     */
    public void removeNamespace(final long paramNamespaceKey) {
        mAttributeKeys.remove(paramNamespaceKey);
        mIntData[ATTRIBUTE_COUNT]--;
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
        final int mUriKey, final int mType, final long oldHash) {
        final long[] longData = new long[ENodes.ELEMENT_KIND.getLongSize()];
        final int[] intData = new int[ENodes.ELEMENT_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = mNodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        longData[AbsStructNode.LEFT_SIBLING_KEY] = mLeftSibKey;
        longData[AbsStructNode.RIGHT_SIBLING_KEY] = rightSibKey;
        longData[AbsStructNode.FIRST_CHILD_KEY] = mFirstChild;
        longData[AbsStructNode.CHILD_COUNT] = mChildCount;
        longData[AbsNode.HASHCODE] = oldHash;
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
            .getURIKey(), mNode.getTypeKey(), mNode.getHash());
    }

    @Override
    public String toString() {
        final StringBuilder returnVal = new StringBuilder(super.toString());
        returnVal.append("\n\tname key: ").append(getNameKey()).append("\n\turi key: ").append(getURIKey())
            .append("\n\tnamespaces: ").append(mNamespaceKeys.toString()).append("\n\tattributes: ").append(
                mAttributeKeys.toString()).toString();
        return returnVal.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 67819;
        int result = 1;
        result = prime * result + Arrays.hashCode(mIntData);
        // result = prime * result + mAttributeKeys.hashCode();
        // result = prime * result + mNamespaceKeys.hashCode();
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
