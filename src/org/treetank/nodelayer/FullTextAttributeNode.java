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

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>FullTextAttributeNode</h1>
 * 
 * <p>
 * Node representing fulltext attribute node.
 * </p>
 */
public final class FullTextAttributeNode extends AbstractNode {

  /** Key of parent node. */
  private long mParentKey;

  /** Key of first child. */
  private long mFirstChildKey;

  /** Key of left sibling. */
  private long mLeftSiblingKey;

  /** Key of right sibling. */
  private long mRightSiblingKey;

  /**
   * Create new element node.
   * 
   * @param nodeKey Key of node.
   * @param parentKey Key of parent.
   * @param firstChildKey Key of first child.
   * @param leftSiblingKey Key of left sibling.
   * @param rightSiblingKey Key of right sibling.
   */
  public FullTextAttributeNode(
      final long nodeKey,
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey) {
    super(nodeKey);
    mParentKey = parentKey;
    mFirstChildKey = firstChildKey;
    mLeftSiblingKey = leftSiblingKey;
    mRightSiblingKey = rightSiblingKey;
  }

  /**
   * Clone element node.
   * 
   * @param node Element node to clone.
   */
  public FullTextAttributeNode(final INode node) {
    super(node.getNodeKey());
    mParentKey = node.getParentKey();
    mFirstChildKey = node.getFirstChildKey();
    mLeftSiblingKey = node.getLeftSiblingKey();
    mRightSiblingKey = node.getRightSiblingKey();
  }

  /**
   * Read element node.
   * 
   * @param nodeKey Key to assign to read element node.
   * @param in Input bytes to read from.
   */
  public FullTextAttributeNode(final long nodeKey, final FastByteArrayReader in) {
    super(nodeKey);

    // Read according to node kind.
    mParentKey = getNodeKey() - in.readVarLong();
    mFirstChildKey = getNodeKey() - in.readVarLong();
    mLeftSiblingKey = getNodeKey() - in.readVarLong();
    mRightSiblingKey = getNodeKey() - in.readVarLong();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean isFullTextAttribute() {
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
  public final boolean hasFirstChild() {
    return (mFirstChildKey != IConstants.NULL_KEY);
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
  public final INode getFirstChild(final IReadTransaction rtx) {
    return rtx.moveTo(mFirstChildKey);
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
    return IConstants.FULLTEXT_ATTRIBUTE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(getNodeKey() - mParentKey);
    out.writeVarLong(getNodeKey() - mFirstChildKey);
    out.writeVarLong(getNodeKey() - mLeftSiblingKey);
    out.writeVarLong(getNodeKey() - mRightSiblingKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "FullTextNode "
        + "\n\tnodeKey: "
        + this.getNodeKey()
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
