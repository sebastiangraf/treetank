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

import java.util.ArrayList;
import java.util.List;

import org.treetank.api.IReadTransaction;
import org.treetank.utils.IByteBuffer;

/**
 * <h1>ElementNode</h1>
 * 
 * <p>
 * Node representing an XML element.
 * </p>
 */
public final class ElementNode extends AbstractNode {

  private static final int SIZE = 11;

  private static final int PARENT_KEY = 1;

  private static final int FIRST_CHILD_KEY = 2;

  private static final int LEFT_SIBLING_KEY = 3;

  private static final int RIGHT_SIBLING_KEY = 4;

  private static final int CHILD_COUNT = 5;

  private static final int NAME_KEY = 6;

  private static final int URI_KEY = 7;

  private static final int TYPE = 8;

  private static final int ATTRIBUTE_COUNT = 9;

  private static final int NAMESPACE_COUNT = 10;

  /** Keys of attributes. */
  private List<Long> mAttributeKeys;

  /** Keys of namespace declarations. */
  private List<Long> mNamespaceKeys;

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
   * @param type the type of the element.
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
    super(SIZE, nodeKey);
    mData[PARENT_KEY] = nodeKey - parentKey;
    mData[FIRST_CHILD_KEY] = nodeKey - firstChildKey;
    mData[LEFT_SIBLING_KEY] = nodeKey - leftSiblingKey;
    mData[RIGHT_SIBLING_KEY] = nodeKey - rightSiblingKey;
    mData[CHILD_COUNT] = 0;
    mData[NAME_KEY] = nameKey;
    mData[URI_KEY] = uriKey;
    mData[TYPE] = type;
    mData[ATTRIBUTE_COUNT] = 0;
    mData[NAMESPACE_COUNT] = 0;
    mAttributeKeys = null;
    mNamespaceKeys = null;
  }

  /**
   * Clone element node.
   * 
   * @param node Element node to clone.
   */
  public ElementNode(final AbstractNode node) {
    super(node);
    if (mData[ATTRIBUTE_COUNT] > 0) {
      mAttributeKeys = new ArrayList<Long>((int) mData[ATTRIBUTE_COUNT]);
      for (int i = 0, l = (int) mData[ATTRIBUTE_COUNT]; i < l; i++) {
        mAttributeKeys.add(node.getAttributeKey(i));
      }
    }
    if (mData[NAMESPACE_COUNT] > 0) {
      mNamespaceKeys = new ArrayList<Long>((int) mData[NAMESPACE_COUNT]);
      for (int i = 0, l = (int) mData[NAMESPACE_COUNT]; i < l; i++) {
        mNamespaceKeys.add(node.getNamespaceKey(i));
      }
    }
  }

  /**
   * Read element node.
   * 
   * @param nodeKey Key to assign to read element node.
   * @param in Input bytes to read from.
   */
  public ElementNode(final long nodeKey, final IByteBuffer in) {
    super(SIZE, nodeKey, in);

    if (mData[ATTRIBUTE_COUNT] > 0) {
      mAttributeKeys = new ArrayList<Long>((int) mData[ATTRIBUTE_COUNT]);
      long[] attributes = in.getAll((int) mData[ATTRIBUTE_COUNT]);
      for (int i = 0; i < mData[ATTRIBUTE_COUNT]; i++) {
        mAttributeKeys.add(attributes[i]);
      }
    }
    if (mData[NAMESPACE_COUNT] > 0) {
      mNamespaceKeys = new ArrayList<Long>((int) mData[NAMESPACE_COUNT]);
      long[] namespaces = in.getAll((int) mData[NAMESPACE_COUNT]);
      for (int i = 0; i < mData[NAMESPACE_COUNT]; i++) {
        mNamespaceKeys.add(namespaces[i]);
      }
    }
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
    return ((mData[NODE_KEY] - mData[PARENT_KEY]) != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getParentKey() {
    return mData[NODE_KEY] - mData[PARENT_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setParentKey(final long parentKey) {
    mData[PARENT_KEY] = mData[NODE_KEY] - parentKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasFirstChild() {
    return ((mData[NODE_KEY] - mData[FIRST_CHILD_KEY]) != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getFirstChildKey() {
    return mData[NODE_KEY] - mData[FIRST_CHILD_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setFirstChildKey(final long firstChildKey) {
    mData[FIRST_CHILD_KEY] = mData[NODE_KEY] - firstChildKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasLeftSibling() {
    return ((mData[NODE_KEY] - mData[LEFT_SIBLING_KEY]) != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getLeftSiblingKey() {
    return mData[NODE_KEY] - mData[LEFT_SIBLING_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setLeftSiblingKey(final long leftSiblingKey) {
    mData[LEFT_SIBLING_KEY] = mData[NODE_KEY] - leftSiblingKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasRightSibling() {
    return ((mData[NODE_KEY] - mData[RIGHT_SIBLING_KEY]) != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getRightSiblingKey() {
    return mData[NODE_KEY] - mData[RIGHT_SIBLING_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setRightSiblingKey(final long rightSiblingKey) {
    mData[RIGHT_SIBLING_KEY] = mData[NODE_KEY] - rightSiblingKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getChildCount() {
    return mData[CHILD_COUNT];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setChildCount(final long childCount) {
    mData[CHILD_COUNT] = childCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void incrementChildCount() {
    mData[CHILD_COUNT] += 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void decrementChildCount() {
    mData[CHILD_COUNT] -= 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getAttributeCount() {
    return (int) mData[ATTRIBUTE_COUNT];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getAttributeKey(final int index) {
    if (mAttributeKeys == null) {
      return IReadTransaction.NULL_NODE_KEY;
    }
    return mAttributeKeys.get(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertAttribute(final long attributeKey) {
    if (mAttributeKeys == null) {
      mAttributeKeys = new ArrayList<Long>(1);
    }
    mAttributeKeys.add(attributeKey);
    mData[ATTRIBUTE_COUNT] += 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getNamespaceCount() {
    return (int) mData[NAMESPACE_COUNT];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getNamespaceKey(final int index) {
    if (mNamespaceKeys == null) {
      return IReadTransaction.NULL_NODE_KEY;
    }
    return mNamespaceKeys.get(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertNamespace(final long namespaceKey) {
    if (mNamespaceKeys == null) {
      mNamespaceKeys = new ArrayList<Long>(1);
    }
    mNamespaceKeys.add(namespaceKey);
    mData[NAMESPACE_COUNT] += 1;
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
    return (int) mData[NAME_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setNameKey(final int localPartKey) {
    mData[NAME_KEY] = localPartKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getURIKey() {
    return (int) mData[URI_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setURIKey(final int uriKey) {
    mData[URI_KEY] = uriKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getTypeKey() {
    return (int) mData[TYPE];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setType(final int valueType) {
    mData[TYPE] = valueType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final IByteBuffer out) {
    super.serialize(out);
    if (mAttributeKeys != null) {
      long[] attributes = new long[mAttributeKeys.size()];
      for (int i = 0, l = mAttributeKeys.size(); i < l; i++) {
        attributes[i] = mAttributeKeys.get(i);
      }
      out.putAll(attributes);
    }
    if (mNamespaceKeys != null) {
      long[] namespaces = new long[mNamespaceKeys.size()];
      for (int i = 0, l = mNamespaceKeys.size(); i < l; i++) {
        namespaces[i] = mNamespaceKeys.get(i);
      }
      out.putAll(namespaces);
    }
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
        + this.mData[CHILD_COUNT]
        + "\n\tparentKey: "
        + (mData[NODE_KEY] - mData[PARENT_KEY])
        + "\n\tfirstChildKey: "
        + (mData[NODE_KEY] - mData[FIRST_CHILD_KEY])
        + "\n\tleftSiblingKey: "
        + (mData[NODE_KEY] - mData[LEFT_SIBLING_KEY])
        + "\n\trightSiblingKey: "
        + (mData[NODE_KEY] - mData[RIGHT_SIBLING_KEY]);
  }

}
