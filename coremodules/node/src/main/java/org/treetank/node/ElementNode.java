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
import static org.treetank.node.IConstants.NULL_NODE;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.treetank.exception.TTIOException;
import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * <h1>ElementNode</h1>
 * 
 * <p>
 * Node representing an XML element.
 * </p>
 */
public final class ElementNode implements INode, IStructNode, INameNode {

    /**
     * Enum for ElementFunnel.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    enum ElementNodeFunnel implements Funnel<org.treetank.api.IData> {
        INSTANCE;
        public void funnel(org.treetank.api.IData data, PrimitiveSink into) {
            final ElementNode from = (ElementNode)data;
            from.mDel.getFunnel().funnel(from, into);
            from.mStrucDel.getFunnel().funnel(from, into);
            from.mNameDel.getFunnel().funnel(from, into);
            for (long key : from.mAttributeKeys) {
                into.putLong(key);
            }
            for (long key : from.mNamespaceKeys) {
                into.putLong(key);
            }
        }
    }
    /** Delegate for common node information. */
    private final NodeDelegate mDel;
    /** Delegate for struct node information. */
    private final StructNodeDelegate mStrucDel;
    /** Delegate for name node information. */
    private final NameNodeDelegate mNameDel;

    /** Keys of attributes. */
    private final List<Long> mAttributeKeys;

    /** Keys of namespace declarations. */
    private final List<Long> mNamespaceKeys;

    /**
     * Constructor
     * 
     * @param pDel
     *            {@link NodeDelegate} to be set
     * @param pStrucDel
     *            {@link StructNodeDelegate} to be set
     * @param pNameDel
     *            {@link NameNodeDelegate} to be set
     * @param pAttributeKeys
     *            keys of attributes to be set
     * @param pNamespaceKeys
     *            keys of namespaces to be set
     */
    public ElementNode(final NodeDelegate pDel, final StructNodeDelegate pStrucDel,
        final NameNodeDelegate pNameDel, final List<Long> pAttributeKeys, final List<Long> pNamespaceKeys) {
        mDel = pDel;
        mStrucDel = pStrucDel;
        mNameDel = pNameDel;
        mAttributeKeys = pAttributeKeys;
        mNamespaceKeys = pNamespaceKeys;
    }

    /**
     * Getting the count of attributes.
     * 
     * @return the count of attributes
     */
    public int getAttributeCount() {
        return mAttributeKeys.size();
    }

    /**
     * Getting the attribute key for an given index.
     * 
     * @param pIndex
     *            index of the attribute
     * @return the attribute key
     */
    public long getAttributeKey(final int pIndex) {
        if (mAttributeKeys.size() <= pIndex) {
            return NULL_NODE;
        }
        return mAttributeKeys.get(pIndex);
    }

    /**
     * Inserting an attribute.
     * 
     * @param pAttrKey
     *            the new attribute key
     */
    public void insertAttribute(final long pAttrKey) {
        mAttributeKeys.add(pAttrKey);
    }

    /**
     * Removing an attribute.
     * 
     * @param pAttrKey
     *            the key of the attribute to be removed
     */
    public void removeAttribute(final long pAttrKey) {
        mAttributeKeys.remove(pAttrKey);
    }

    /**
     * Getting the count of namespaces.
     * 
     * @return the count of namespaces
     */
    public int getNamespaceCount() {
        return mNamespaceKeys.size();
    }

    /**
     * Getting the namespace key for an given index.
     * 
     * @param pNamespaceKey
     *            index of the namespace
     * @return the namespace key
     */
    public long getNamespaceKey(final int pNamespaceKey) {
        if (mNamespaceKeys.size() <= pNamespaceKey) {
            return NULL_NODE;
        }
        return mNamespaceKeys.get(pNamespaceKey);
    }

    /**
     * Inserting a namespace.
     * 
     * @param pNamespaceKey
     *            new namespace key
     */
    public void insertNamespace(final long pNamespaceKey) {
        mNamespaceKeys.add(pNamespaceKey);
    }

    /**
     * Removing a namepsace.
     * 
     * @param pNamespaceKey
     *            the key of the namespace to be removed
     */
    public void removeNamespace(final long pNamespaceKey) {
        mAttributeKeys.remove(pNamespaceKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDataKey() {
        return mDel.getDataKey();
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
    public void setParentKey(final long pParentKey) {
        mDel.setParentKey(pParentKey);
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
    public void setHash(final long pHash) {
        mDel.setHash(pHash);
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
    public void setTypeKey(int pTypeKey) {
        mDel.setTypeKey(pTypeKey);
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
    public int getNameKey() {
        return mNameDel.getNameKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return mNameDel.getURIKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(int pNameKey) {
        mNameDel.setNameKey(pNameKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(int pUriKey) {
        mNameDel.setURIKey(pUriKey);
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
    public long getFirstChildKey() {
        return mStrucDel.getFirstChildKey();
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
    public void setRightSiblingKey(long pKey) {
        mStrucDel.setRightSiblingKey(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLeftSiblingKey(long pKey) {
        mStrucDel.setLeftSiblingKey(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstChildKey(long pKey) {
        mStrucDel.setFirstChildKey(pKey);
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
    public int getKind() {
        return IConstants.ELEMENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return mNameDel.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mDel", mDel).add("mStrucDel", mStrucDel).add("mNameDel", mNameDel)
            .add("mAttributeKeys", mAttributeKeys).add("mNamespaceKeys", mNamespaceKeys).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.ELEMENT);
            mDel.serialize(pOutput);
            mStrucDel.serialize(pOutput);
            mNameDel.serialize(pOutput);
            pOutput.writeInt(getAttributeCount());
            for (int i = 0; i < getAttributeCount(); i++) {
                pOutput.writeLong(getAttributeKey(i));
            }
            pOutput.writeInt(getNamespaceCount());
            for (int i = 0; i < getNamespaceCount(); i++) {
                pOutput.writeLong(getNamespaceKey(i));
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public Funnel<org.treetank.api.IData> getFunnel() {
        return ElementNodeFunnel.INSTANCE;
    }

}
