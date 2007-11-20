/*
 * Copyright (c) 2007, Marc Kramis
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

import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.IConstants;

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

  /** Type of value. */
  private int mValueType;

  /** Typed value of node. */
  private byte[] mValue;

  /**
   * Create text node.
   * 
   * @param nodeKey Key of node.
   * @param parentKey Key of parent.
   * @param leftSiblingKey Key of left sibling.
   * @param rightSiblingKey Key of right sibling.
   * @param valueType Type of value.
   * @param value Text value.
   */
  public TextNode(
      final long nodeKey,
      final long parentKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int valueType,
      final byte[] value) {
    super(nodeKey);
    mParentKey = parentKey;
    mLeftSiblingKey = leftSiblingKey;
    mRightSiblingKey = rightSiblingKey;
    mValueType = valueType;
    mValue = value;
  }

  /**
   * Clone text node.
   * 
   * @param node Text node to clone.
   */
  public TextNode(final AbstractNode node) {
    super(node.getNodeKey());
    mParentKey = node.getParentKey();
    mLeftSiblingKey = node.getLeftSiblingKey();
    mRightSiblingKey = node.getRightSiblingKey();
    mValueType = node.getValueType();
    mValue = node.getValue();
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
    mValueType = in.readByte();
    mValue = in.readByteArray();
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
  public final void setRightSiblingKey(final long rightSiblingKey) {
    mRightSiblingKey = rightSiblingKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getKind() {
    return IConstants.TEXT_KIND;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getValueType() {
    return mValueType;
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
  public final void setValue(final int valueType, final byte[] value) {
    mValueType = valueType;
    mValue = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(getNodeKey() - mParentKey);
    out.writeVarLong(mLeftSiblingKey);
    out.writeVarLong(mRightSiblingKey);
    out.writeByte((byte) mValueType);
    out.writeByteArray(mValue);
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

}
