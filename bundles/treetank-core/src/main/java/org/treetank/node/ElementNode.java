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
    public void serialize(final ITTSink mNodeOut) {
        super.serialize(mNodeOut);
        if (mAttributeKeys != null) {
            for (int i = 0, l = mAttributeKeys.size(); i < l; i++) {
                mNodeOut.writeLong(mAttributeKeys.get(i));
            }
        }
        if (mNamespaceKeys != null) {
            for (int i = 0, l = mNamespaceKeys.size(); i < l; i++) {
                mNodeOut.writeLong(mNamespaceKeys.get(i));
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
            new ElementNode(ENodes.cloneData(mLongData), ENodes.cloneData(mIntData), attList, namespaceList);
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
        return new ElementNode(longData, intData, new ArrayList<Long>(), new ArrayList<Long>());
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
