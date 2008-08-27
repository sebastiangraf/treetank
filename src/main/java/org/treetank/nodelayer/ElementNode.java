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
import java.util.ArrayList;
import java.util.List;

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

  /** Keys of attributes. */
  private List<Long> mAttributeKeys;

  /** Keys of namespace declarations. */
  private List<Long> mNamespaceKeys;

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
    mAttributeKeys = new ArrayList<Long>(0);
    mNamespaceKeys = new ArrayList<Long>(0);
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
    mAttributeKeys = new ArrayList<Long>(node.getAttributeCount());
    mNamespaceKeys = new ArrayList<Long>(node.getNamespaceCount());
    for (int i = 0, l = node.getAttributeCount(); i < l; i++) {
      mAttributeKeys.add(node.getAttributeKey(i));
    }
    for (int i = 0, l = node.getNamespaceCount(); i < l; i++) {
      mNamespaceKeys.add(node.getNamespaceKey(i));
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
    mParentKey = in.getLong();
    mFirstChildKey = in.getLong();
    mLeftSiblingKey = in.getLong();
    mRightSiblingKey = in.getLong();
    mChildCount = in.getLong();
    mAttributeKeys = new ArrayList<Long>(0);
    mNamespaceKeys = new ArrayList<Long>(0);
    for (int i = 0, l = in.getInt(); i < l; i++) {
      mAttributeKeys.add(in.getLong());
    }
    for (int i = 0, l = in.getInt(); i < l; i++) {
      mNamespaceKeys.add(in.getLong());
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
    return mAttributeKeys.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getAttributeKey(final int index) {
    return mAttributeKeys.get(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertAttribute(final long attributeKey) {
    mAttributeKeys.add(attributeKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getNamespaceCount() {
    return mNamespaceKeys.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getNamespaceKey(final int index) {
    return mNamespaceKeys.get(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertNamespace(final long namespaceKey) {
    mNamespaceKeys.add(namespaceKey);
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
    out.putLong(mParentKey);
    out.putLong(mFirstChildKey);
    out.putLong(mLeftSiblingKey);
    out.putLong(mRightSiblingKey);
    out.putLong(mChildCount);
    out.putInt(mAttributeKeys.size());
    for (int i = 0, l = mAttributeKeys.size(); i < l; i++) {
      out.putLong(mAttributeKeys.get(i));
    }
    out.putInt(mNamespaceKeys.size());
    for (int i = 0, l = mNamespaceKeys.size(); i < l; i++) {
      out.putLong(mNamespaceKeys.get(i));
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
