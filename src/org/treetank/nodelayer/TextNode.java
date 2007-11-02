/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: Node.java 3268 2007-10-25 13:16:01Z kramis $
 */

package org.treetank.nodelayer;

import java.util.Arrays;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>TextNode</h1>
 * 
 * <p>Node representing a text node.</p>
 */
public final class TextNode extends AbstractNode {

  /** Key of parent node. */
  private long mParentKey;

  /** Key of left sibling. */
  private long mLeftSiblingKey;

  /** Key of right sibling. */
  private long mRightSiblingKey;

  /** Text value. */
  private byte[] mValue;

  /** Fulltext of node. */
  private FullTextAttributeNode[] mFullTextAttributes;

  /**
   * Create text node.
   * 
   * @param nodeKey Key of node.
   * @param parentKey Key of parent.
   * @param leftSiblingKey Key of left sibling.
   * @param rightSiblingKey Key of right sibling.
   * @param value Text value.
   */
  public TextNode(
      final long nodeKey,
      final long parentKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final byte[] value) {
    super(nodeKey);
    mParentKey = parentKey;
    mLeftSiblingKey = leftSiblingKey;
    mRightSiblingKey = rightSiblingKey;
    mValue = value;
    mFullTextAttributes = new FullTextAttributeNode[0];
  }

  /**
   * Clone text node.
   * 
   * @param node Text node to clone.
   */
  public TextNode(final INode node) {
    super(node.getNodeKey());
    mParentKey = node.getParentKey();
    mLeftSiblingKey = node.getLeftSiblingKey();
    mRightSiblingKey = node.getRightSiblingKey();
    mValue = node.getValue();
    mFullTextAttributes =
        new FullTextAttributeNode[node.getFullTextAttributeCount()];
    for (int i = 0, l = mFullTextAttributes.length; i < l; i++) {
      mFullTextAttributes[i] =
          new FullTextAttributeNode(node.getFullTextAttributeByOffset(i));
    }
  }

  /**
   * Read text node.
   * 
   * @param nodeKey Key of text node.
   * @param in Input bytes to read node from.
   */
  public TextNode(final long nodeKey, final FastByteArrayReader in) {
    super(nodeKey);
    mParentKey = getNodeKey() - in.readVarLong();
    mLeftSiblingKey = in.readVarLong();
    mRightSiblingKey = in.readVarLong();
    mValue = in.readByteArray();
    mFullTextAttributes = new FullTextAttributeNode[in.readByte()];
    for (int i = 0, l = mFullTextAttributes.length; i < l; i++) {
      mFullTextAttributes[i] =
          new FullTextAttributeNode(in.readVarLong(), getNodeKey(), in
              .readVarLong(), in.readVarLong());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean isText() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasParent() {
    return (mParentKey != IConstants.NULL_KEY);
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
  public final INode getParent(final IReadTransaction rtx) {
    return rtx.moveTo(mParentKey);
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
  public final boolean hasLeftSibling() {
    return (mLeftSiblingKey != IConstants.NULL_KEY);
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
  public final INode getLeftSibling(final IReadTransaction rtx) {
    return rtx.moveTo(mLeftSiblingKey);
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
    return (mRightSiblingKey != IConstants.NULL_KEY);
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
  public final INode getRightSibling(final IReadTransaction rtx) {
    return rtx.moveTo(mRightSiblingKey);
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
  public final int getKind() {
    return IConstants.TEXT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final byte[] getValue() {
    return mValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setValue(final byte[] value) {
    mValue = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getFullTextAttributeCount() {
    return mFullTextAttributes == null ? 0 : mFullTextAttributes.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final FullTextAttributeNode getFullTextAttributeByTokenKey(
      final long tokenKey) {
    final int index = binarySearch(tokenKey);
    if (index >= 0) {
      return mFullTextAttributes[index];
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final FullTextAttributeNode getFullTextAttributeByOffset(
      final int offset) {
    return mFullTextAttributes[offset];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setFullTextAttribute(
      final long nodeKey,
      final long leftSiblingKey,
      final long rightSiblingKey) {

    final int index = binarySearch(nodeKey);
    if (index < 0) {
      throw new IllegalArgumentException("Node key "
          + nodeKey
          + " does not exist and can thus not be set.");
    }

    mFullTextAttributes[index] =
        new FullTextAttributeNode(
            nodeKey,
            getParentKey(),
            leftSiblingKey,
            rightSiblingKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertFullTextAttribute(
      final long nodeKey,
      final long leftSiblingKey,
      final long rightSiblingKey) {

    FullTextAttributeNode[] tmp =
        new FullTextAttributeNode[mFullTextAttributes.length + 1];
    System
        .arraycopy(mFullTextAttributes, 0, tmp, 0, mFullTextAttributes.length);
    mFullTextAttributes = tmp;

    mFullTextAttributes[mFullTextAttributes.length - 1] =
        new FullTextAttributeNode(
            nodeKey,
            getNodeKey(),
            leftSiblingKey,
            rightSiblingKey);

    Arrays.sort(mFullTextAttributes);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(getNodeKey() - mParentKey);
    out.writeVarLong(mLeftSiblingKey);
    out.writeVarLong(mRightSiblingKey);
    out.writeByteArray(mValue);
    out.writeByte((byte) mFullTextAttributes.length);
    for (int i = 0, l = mFullTextAttributes.length; i < l; i++) {
      out.writeVarLong(mFullTextAttributes[i].getNodeKey());
      out.writeVarLong(mFullTextAttributes[i].getLeftSiblingKey());
      out.writeVarLong(mFullTextAttributes[i].getRightSiblingKey());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "TextNode "
        + "\n\tnodeKey: "
        + getNodeKey()
        + "\n\tparentKey: "
        + mParentKey
        + "\n\tleftSiblingKey: "
        + mLeftSiblingKey
        + "\n\trightSiblingKey: "
        + mRightSiblingKey;
  }

  /**
   * Binary search on full text attribute nodes. Every modifying operation
   * must sort the array of full text attributes before it returns. This
   * is required because the array must provide random access on the node key.
   */
  private final int binarySearch(final long nodeKey) {
    int low = 0;
    int high = mFullTextAttributes.length - 1;
    while (low <= high) {
      int mid = (low + high) >> 1;
      if (mFullTextAttributes[mid].getNodeKey() > nodeKey) {
        high = mid - 1;
      } else if (mFullTextAttributes[mid].getNodeKey() < nodeKey) {
        low = mid + 1;
      } else {
        return mid;
      }
    }
    return -1;
  }

}
