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

import java.util.List;

import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.settings.EFixed;

/**
 * <h1>ElementNode</h1>
 * 
 * <p>
 * Node representing an XML element.
 * </p>
 */
public final class ElementNode implements IStructNode, INameNode {

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
            return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
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
            return (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
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
     * Delegate method for getNodeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getNodeKey()
     */
    public long getNodeKey() {
        return mDel.getNodeKey();
    }

    /**
     * Delegate method for setNodeKey.
     * 
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(final long pNodeKey) {
        mDel.setNodeKey(pNodeKey);
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDel.getParentKey();
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pParentKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(final long pParentKey) {
        mDel.setParentKey(pParentKey);
    }

    /**
     * Delegate method for getHash.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDel.getHash();
    }

    /**
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(final long pHash) {
        mDel.setHash(pHash);
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDel.getTypeKey();
    }

    /**
     * Delegate method for setTypeKey.
     * 
     * @param pTypeKey
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(int pTypeKey) {
        mDel.setTypeKey(pTypeKey);
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDel.hasParent();
    }

    /**
     * Delegate method for getNameKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NameNodeDelegate#getNameKey()
     */
    public int getNameKey() {
        return mNameDel.getNameKey();
    }

    /**
     * Delegate method for getURIKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NameNodeDelegate#getURIKey()
     */
    public int getURIKey() {
        return mNameDel.getURIKey();
    }

    /**
     * Delegate method for setNameKey.
     * 
     * @param pNameKey
     * @see org.treetank.node.delegates.NameNodeDelegate#setNameKey(int)
     */
    public void setNameKey(int pNameKey) {
        mNameDel.setNameKey(pNameKey);
    }

    /**
     * Delegate method for setURIKey.
     * 
     * @param pUriKey
     * @see org.treetank.node.delegates.NameNodeDelegate#setURIKey(int)
     */
    public void setURIKey(int pUriKey) {
        mNameDel.setURIKey(pUriKey);
    }

    /**
     * Delegate method for hasFirstChild.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#hasFirstChild()
     */
    public boolean hasFirstChild() {
        return mStrucDel.hasFirstChild();
    }

    /**
     * Delegate method for hasLeftSibling.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#hasLeftSibling()
     */
    public boolean hasLeftSibling() {
        return mStrucDel.hasLeftSibling();
    }

    /**
     * Delegate method for hasRightSibling.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#hasRightSibling()
     */
    public boolean hasRightSibling() {
        return mStrucDel.hasRightSibling();
    }

    /**
     * Delegate method for getChildCount.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#getChildCount()
     */
    public long getChildCount() {
        return mStrucDel.getChildCount();
    }

    /**
     * Delegate method for getFirstChildKey.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#getFirstChildKey()
     */
    public long getFirstChildKey() {
        return mStrucDel.getFirstChildKey();
    }

    /**
     * Delegate method for getLeftSiblingKey.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#getLeftSiblingKey()
     */
    public long getLeftSiblingKey() {
        return mStrucDel.getLeftSiblingKey();
    }

    /**
     * Delegate method for getRightSiblingKey.
     * 
     * @return
     * @see org.treetank.node.delegates.StructNodeDelegate#getRightSiblingKey()
     */
    public long getRightSiblingKey() {
        return mStrucDel.getRightSiblingKey();
    }

    /**
     * Delegate method for setRightSiblingKey.
     * 
     * @param pKey
     * @see org.treetank.node.delegates.StructNodeDelegate#setRightSiblingKey(long)
     */
    public void setRightSiblingKey(long pKey) {
        mStrucDel.setRightSiblingKey(pKey);
    }

    /**
     * Delegate method for setLeftSiblingKey.
     * 
     * @param pKey
     * @see org.treetank.node.delegates.StructNodeDelegate#setLeftSiblingKey(long)
     */
    public void setLeftSiblingKey(long pKey) {
        mStrucDel.setLeftSiblingKey(pKey);
    }

    /**
     * Delegate method for setFirstChildKey.
     * 
     * @param pKey
     * @see org.treetank.node.delegates.StructNodeDelegate#setFirstChildKey(long)
     */
    public void setFirstChildKey(long pKey) {
        mStrucDel.setFirstChildKey(pKey);
    }

    /**
     * Delegate method for decrementChildCount.
     * 
     * @see org.treetank.node.delegates.StructNodeDelegate#decrementChildCount()
     */
    public void decrementChildCount() {
        mStrucDel.decrementChildCount();
    }

    /**
     * Delegate method for incrementChildCount.
     * 
     * @see org.treetank.node.delegates.StructNodeDelegate#incrementChildCount()
     */
    public void incrementChildCount() {
        mStrucDel.incrementChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENode getKind() {
        return ENode.ELEMENT_KIND;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(mDel.toString());
        builder.append(mStrucDel.toString());
        builder.append(mNameDel.toString());
        builder.append("\n\tnamespaces: ");
        builder.append(mNamespaceKeys.toString());
        builder.append("\n\tattributes: ");
        builder.append(mAttributeKeys.toString());
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mNameDel == null) ? 0 : mNameDel.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElementNode other = (ElementNode)obj;
        if (mNameDel == null) {
            if (other.mNameDel != null)
                return false;
        } else if (!mNameDel.equals(other.mNameDel))
            return false;
        return true;
    }

    /**
     * Getting the inlying {@link NodeDelegate}.
     * 
     * @return
     */
    NodeDelegate getNodeDelegate() {
        return mDel;
    }

    /**
     * Getting the inlying {@link StructNodeDelegate}.
     * 
     * @return
     */
    StructNodeDelegate getStrucNodeDelegate() {
        return mStrucDel;
    }

    /**
     * Getting the inlying {@link NameNodeDelegate}.
     * 
     * @return
     */
    NameNodeDelegate getNameNodeDelegate() {
        return mNameDel;
    }

}
