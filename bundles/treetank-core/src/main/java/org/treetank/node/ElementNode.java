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

    protected static final int NAME_KEY = 4;

    protected static final int URI_KEY = 8;

    protected static final int ATTRIBUTE_COUNT = 12;

    protected static final int NAMESPACE_COUNT = 16;

    /** Keys of attributes. */
    private final List<Long> mAttributeKeys;

    /** Keys of namespace declarations. */
    private final List<Long> mNamespaceKeys;

    /**
     * Creating new element.
     * 
     * @param paramByteBuilder
     *            array with bytes
     * @param paramPointerBuilder
     *            array with pointers
     * @param paramAttributeKeys
     *            attr keys
     * @param paramNamespaceKeys
     *            namespace keys
     */
    ElementNode(final byte[] paramByteBuilder, final byte[] paramPointerBuilder,
        final List<Long> paramAttributeKeys, final List<Long> paramNamespaceKeys) {
        super(paramByteBuilder, paramPointerBuilder);
        mAttributeKeys = paramAttributeKeys;
        mNamespaceKeys = paramNamespaceKeys;
    }

    /**
     * Getting the count of attributes.
     * 
     * @return the count of attributes
     */
    public int getAttributeCount() {
        return readIntBytes(ATTRIBUTE_COUNT);
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
     * Inserting an attribute.
     * 
     * @param paramAttributeKey
     *            the new attribute key
     */
    public void insertAttribute(final long paramAttributeKey) {
        mAttributeKeys.add(paramAttributeKey);

        final int curAttrCount = readIntBytes(ATTRIBUTE_COUNT);
        writeIntBytes(ATTRIBUTE_COUNT, curAttrCount + 1);
    }

    /**
     * Removing an attribute.
     * 
     * @param paramAttributeKey
     *            the key of the attribute to be removed
     */
    public void removeAttribute(final long paramAttributeKey) {
        mAttributeKeys.remove(paramAttributeKey);

        final int curAttrCount = readIntBytes(ATTRIBUTE_COUNT);
        writeIntBytes(ATTRIBUTE_COUNT, curAttrCount - 1);
    }

    /**
     * Getting the count of namespaces.
     * 
     * @return the count of namespaces
     */
    public int getNamespaceCount() {
        return readIntBytes(NAMESPACE_COUNT);
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

        final int curNSCount = readIntBytes(NAMESPACE_COUNT);
        writeIntBytes(NAMESPACE_COUNT, curNSCount + 1);
    }

    /**
     * Removing a namepsace.
     * 
     * @param paramNamespaceKey
     *            the key of the namespace to be removed
     */
    public void removeNamespace(final long paramNamespaceKey) {
        mAttributeKeys.remove(paramNamespaceKey);

        final int curNSCount = readIntBytes(NAMESPACE_COUNT);
        writeIntBytes(NAMESPACE_COUNT, curNSCount - 1);
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
        return readIntBytes(NAME_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int mLocalPartKey) {
        writeIntBytes(NAME_KEY, mLocalPartKey);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink mNodeOut) {
        super.serialize(mNodeOut);
        if (mAttributeKeys != null) {
            for (int i = 0, l = mAttributeKeys.size(); i < l; i++) {
                byte[] mBuffer = longToByteArray(mAttributeKeys.get(i));
                for (byte aByte : mBuffer) {
                    mNodeOut.writeByte(aByte);
                }
            }
        }
        if (mNamespaceKeys != null) {
            for (int i = 0, l = mNamespaceKeys.size(); i < l; i++) {
                byte[] mBuffer = longToByteArray(mNamespaceKeys.get(i));
                for (byte aByte : mBuffer) {
                    mNodeOut.writeByte(aByte);
                }
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
            new ElementNode(ENodes.cloneData(mByteData), ENodes.cloneData(mPointerData), attList,
                namespaceList);
        return toClone;
    }

    public static AbsNode createData(final long mNodeKey, final long mParentKey, final long mLeftSibKey,
        final long mRightSibKey, final long mFirstChild, final long mChildCount, final int mNameKey,
        final int mUriKey, final int mType, final long oldHash) {

        final byte[] byteData = new byte[ENodes.ELEMENT_KIND.getByteSize()];

        final byte[] pointerData = new byte[ENodes.ELEMENT_KIND.getPointerSize()];

        int mCount = AbsNode.NODE_KEY;
        for (byte aByte : longToByteArray(mNodeKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsNode.PARENT_KEY;
        for (byte aByte : longToByteArray(mParentKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.LEFT_SIBLING_KEY;
        for (byte aByte : longToByteArray(mLeftSibKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.RIGHT_SIBLING_KEY;
        for (byte aByte : longToByteArray(mRightSibKey)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.FIRST_CHILD_KEY;
        for (byte aByte : longToByteArray(mFirstChild)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsStructNode.CHILD_COUNT;
        for (byte aByte : longToByteArray(mChildCount)) {
            pointerData[mCount++] = aByte;
        }

        mCount = AbsNode.HASHCODE;
        for (byte aByte : longToByteArray(oldHash)) {
            pointerData[mCount++] = aByte;
        }

        mCount = ElementNode.NAME_KEY;
        for (byte aByte : intToByteArray(mNameKey)) {
            byteData[mCount++] = aByte;
        }

        mCount = ElementNode.URI_KEY;
        for (byte aByte : intToByteArray(mUriKey)) {
            byteData[mCount++] = aByte;
        }

        mCount = AbsNode.TYPE_KEY;
        for (byte aByte : intToByteArray(mType)) {
            byteData[mCount++] = aByte;
        }

        mCount = ElementNode.ATTRIBUTE_COUNT;
        for (byte aByte : intToByteArray(0)) {
            byteData[mCount++] = aByte;
        }

        mCount = ElementNode.NAMESPACE_COUNT;
        for (byte aByte : intToByteArray(0)) {
            byteData[mCount++] = aByte;
        }

        return new ElementNode(byteData, pointerData, new ArrayList<Long>(), new ArrayList<Long>());
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
        result = prime * result + Arrays.hashCode(mByteData);
        // result = prime * result + mAttributeKeys.hashCode();
        // result = prime * result + mNamespaceKeys.hashCode();
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void acceptVisitor(final IVisitor paramVisitor) {
        paramVisitor.visit(this);
    }

}
