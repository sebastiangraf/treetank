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
 * <h1>TextNode</h1>
 * 
 * <p>Node representing a text node.</p>
 */
public final class FullTextLeafNode extends AbstractNode {

  /** Key of parent node. */
  private long mParentKey;

  private long mFirstChildKey;

  /** Key of left sibling. */
  private long mLeftSiblingKey;

  /** Key of right sibling. */
  private long mRightSiblingKey;

  /**
   * Create text node.
   * 
   * @param nodeKey Key of node.
   * @param parentKey Key of parent.
   * @param firstChildKey Key of first child.
   * @param leftSiblingKey Key of left sibling.
   * @param rightSiblingKey Key of right sibling.
   */
  public FullTextLeafNode(
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
   * Clone text node.
   * 
   * @param node Text node to clone.
   */
  public FullTextLeafNode(final AbstractNode node) {
    super(node.getNodeKey());
    mParentKey = node.getParentKey();
    mFirstChildKey = node.getFirstChildKey();
    mLeftSiblingKey = node.getLeftSiblingKey();
    mRightSiblingKey = node.getRightSiblingKey();
  }

  /**
   * Read text node.
   * 
   * @param nodeKey Key of text node.
   * @param in Input bytes to read node from.
   */
  public FullTextLeafNode(final long nodeKey, final ByteBuffer in) {
    super(nodeKey);
    mParentKey = getNodeKey() - in.getLong();
    mFirstChildKey = getNodeKey() - in.getLong();
    mLeftSiblingKey = getNodeKey() - in.getLong();
    mRightSiblingKey = getNodeKey() - in.getLong();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean isFullTextLeaf() {
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
  public final int getKind() {
    return IReadTransaction.FULLTEXT_LEAF_KIND;
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
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "FulleTextLeafNode "
        + "\n\tnodeKey: "
        + getNodeKey()
        + "\n\tparentKey: "
        + mParentKey
        + "\n\tfirstChildKey: "
        + mFirstChildKey
        + "\n\tleftSiblingKey: "
        + mLeftSiblingKey
        + "\n\trightSiblingKey: "
        + mRightSiblingKey;
  }

}
