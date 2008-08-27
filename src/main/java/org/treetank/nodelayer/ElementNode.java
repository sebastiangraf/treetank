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
 * $Id$
 */

package org.treetank.nodelayer;

import java.nio.ByteBuffer;

import org.treetank.api.IReadTransaction;

/**
 * <h1>ElementNode</h1>
 * 
 * <p>
 * Node representing an XML element.
 * </p>
 */
public final class ElementNode extends AbstractNode {

  /** Key of parent node. */
  private long mParentKey;

  /** Key of first child. */
  private long mFirstChildKey;

  /** Key of left sibling. */
  private long mLeftSiblingKey;

  /** Key of right sibling. */
  private long mRightSiblingKey;

  /** Number of children including text and element nodes. */
  private long mChildCount;
  
  /** Number of attributes. */
  private int mAttributeCount;
  
  /** Number of namespace declarations. */
  private int mNamespaceCount;

  /** Attributes of node. */
  private AbstractNode[] mAttributes;

  /** Namespaces of node. */
  private AbstractNode[] mNamespaces;

  /** Key of qualified name. */
  private int mNameKey;

  /** Key of URI. */
  private int mURIKey;

  /** Type of node. */
  private int mType;

  /**
   * Create new element node.
   * 
   * @param nodeKey Key of node.
   * @param parentKey Key of parent.
   * @param firstChildKey Key of first child.
   * @param leftSiblingKey Key of left sibling.
   * @param rightSiblingKey Key of right sibling.
   * @param nameKey Key of local part.
   * @param uriKey Key of URI.
   */
  public ElementNode(
      final long nodeKey,
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int nameKey,
      final int uriKey,
      final int type) {
    super(nodeKey);
    mParentKey = parentKey;
    mFirstChildKey = firstChildKey;
    mLeftSiblingKey = leftSiblingKey;
    mRightSiblingKey = rightSiblingKey;
    mChildCount = 0;
    mAttributeCount = 0;
    mNamespaceCount = 0;
    mAttributes = new AttributeNode[0];
    mNamespaces = new NamespaceNode[0];
    mNameKey = nameKey;
    mURIKey = uriKey;
    mType = type;
  }

  /**
   * Clone element node.
   * 
   * @param node Element node to clone.
   */
  public ElementNode(final AbstractNode node) {
    super(node.getNodeKey());
    mParentKey = node.getParentKey();
    mFirstChildKey = node.getFirstChildKey();
    mLeftSiblingKey = node.getLeftSiblingKey();
    mRightSiblingKey = node.getRightSiblingKey();
    mChildCount = node.getChildCount();
    mAttributeCount = node.getAttributeCount();
    mNamespaceCount = node.getNamespaceCount();
    mAttributes = new AttributeNode[node.getAttributeCount()];
    for (int i = 0, l = mAttributes.length; i < l; i++) {
      mAttributes[i] = new AttributeNode(node.getAttribute(i));
    }
    mNamespaces = new NamespaceNode[node.getNamespaceCount()];
    for (int i = 0, l = mNamespaces.length; i < l; i++) {
      mNamespaces[i] = new NamespaceNode(node.getNamespace(i));
    }
    mNameKey = node.getNameKey();
    mURIKey = node.getURIKey();
    mType = node.getTypeKey();
  }

  /**
   * Read element node.
   * 
   * @param nodeKey Key to assign to read element node.
   * @param in Input bytes to read from.
   */
  public ElementNode(final long nodeKey, final ByteBuffer in) {
    super(nodeKey);

    // Read according to node kind.
    mParentKey = getNodeKey() - in.getLong();
    mFirstChildKey = getNodeKey() - in.getLong();
    mLeftSiblingKey = getNodeKey() - in.getLong();
    mRightSiblingKey = getNodeKey() - in.getLong();
    mChildCount = in.getLong();
    mAttributeCount = in.getInt();
    mNamespaceCount = in.getInt();
    mAttributes = new AttributeNode[in.get()];
    for (int i = 0, l = mAttributes.length; i < l; i++) {
      mAttributes[i] = new AttributeNode(getNodeKey(), in);
    }
    mNamespaces = new NamespaceNode[in.get()];
    for (int i = 0, l = mNamespaces.length; i < l; i++) {
      mNamespaces[i] = new NamespaceNode(getNodeKey(), in);
    }
    mNameKey = in.getInt();
    mURIKey = in.getInt();
    mType = in.getInt();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean isElement() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasParent() {
    return (mParentKey != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getParentKey() {
    return mParentKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setParentKey(final long parentKey) {
    mParentKey = parentKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasFirstChild() {
    return (mFirstChildKey != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getFirstChildKey() {
    return mFirstChildKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setFirstChildKey(final long firstChildKey) {
    mFirstChildKey = firstChildKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasLeftSibling() {
    return (mLeftSiblingKey != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getLeftSiblingKey() {
    return mLeftSiblingKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setLeftSiblingKey(final long leftSiblingKey) {
    mLeftSiblingKey = leftSiblingKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasRightSibling() {
    return (mRightSiblingKey != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getRightSiblingKey() {
    return mRightSiblingKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setRightSiblingKey(final long rightSiblingKey) {
    mRightSiblingKey = rightSiblingKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getChildCount() {
    return mChildCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setChildCount(final long childCount) {
    mChildCount = childCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void incrementChildCount() {
    mChildCount += 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void decrementChildCount() {
    mChildCount -= 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getAttributeCount() {
    return mAttributeCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final AbstractNode getAttribute(final int index) {
    return mAttributes[index];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setAttribute(
      final int index,
      final int nameKey,
      final int uriKey,
      final int valueType,
      final byte[] value) {
    mAttributes[index] =
        new AttributeNode(getNodeKey(), nameKey, uriKey, valueType, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertAttribute(
      final int nameKey,
      final int uriKey,
      final int valueType,
      final byte[] value) {
    
    mAttributeCount += 1;

    AttributeNode[] tmp = new AttributeNode[mAttributes.length + 1];
    System.arraycopy(mAttributes, 0, tmp, 0, mAttributes.length);
    mAttributes = tmp;

    mAttributes[mAttributes.length - 1] =
        new AttributeNode(getNodeKey(), nameKey, uriKey, valueType, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getNamespaceCount() {
    return mNamespaceCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final AbstractNode getNamespace(final int index) {
    return mNamespaces[index];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setNamespace(
      final int index,
      final int uriKey,
      final int prefixKey) {
    mNamespaces[index] = new NamespaceNode(getNodeKey(), uriKey, prefixKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertNamespace(final int uriKey, final int prefixKey) {

    mNamespaceCount += 1;
    
    NamespaceNode[] tmp = new NamespaceNode[mNamespaces.length + 1];
    System.arraycopy(mNamespaces, 0, tmp, 0, mNamespaces.length);
    mNamespaces = tmp;

    mNamespaces[mNamespaces.length - 1] =
        new NamespaceNode(getNodeKey(), uriKey, prefixKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getKind() {
    return IReadTransaction.ELEMENT_KIND;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getNameKey() {
    return mNameKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setNameKey(final int localPartKey) {
    mNameKey = localPartKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getURIKey() {
    return mURIKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setURIKey(final int uriKey) {
    mURIKey = uriKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getTypeKey() {
    return mType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setType(final int valueType) {
    mType = valueType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final ByteBuffer out) {
    out.putLong(getNodeKey() - mParentKey);
    out.putLong(getNodeKey() - mFirstChildKey);
    out.putLong(getNodeKey() - mLeftSiblingKey);
    out.putLong(getNodeKey() - mRightSiblingKey);
    out.putLong(mChildCount);
    out.putInt(mAttributeCount);
    out.putInt(mNamespaceCount);
    out.put((byte) mAttributes.length);
    for (int i = 0, l = mAttributes.length; i < l; i++) {
      mAttributes[i].serialize(out);
    }
    out.put((byte) mNamespaces.length);
    for (int i = 0, l = mNamespaces.length; i < l; i++) {
      mNamespaces[i].serialize(out);
    }
    out.putInt(mNameKey);
    out.putInt(mURIKey);
    out.putInt(mType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "ElementNode "
        + "\n\tnodeKey: "
        + this.getNodeKey()
        + "\n\tchildcount: "
        + this.mChildCount
        + "\n\tparentKey: "
        + this.mParentKey
        + "\n\tfirstChildKey: "
        + this.mFirstChildKey
        + "\n\tleftSiblingKey: "
        + this.mLeftSiblingKey
        + "\n\trightSiblingKey: "
        + this.mRightSiblingKey;
  }

}
