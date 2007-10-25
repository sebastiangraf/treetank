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

public final class ElementNode extends Node {

  private long mParentKey;

  private long mFirstChildKey;

  private long mLeftSiblingKey;

  private long mRightSiblingKey;

  private long mChildCount;

  private AttributeNode[] mAttributes;

  private NamespaceNode[] mNamespaces;

  private int mLocalPartKey;

  private int mURIKey;

  private int mPrefixKey;

  public ElementNode(
      final long nodeKey,
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int localPartKey,
      final int uriKey,
      final int prefixKey) {
    super(nodeKey);
    mParentKey = parentKey;
    mFirstChildKey = firstChildKey;
    mLeftSiblingKey = leftSiblingKey;
    mRightSiblingKey = rightSiblingKey;
    mChildCount = 0;
    mAttributes = new AttributeNode[0];
    mNamespaces = new NamespaceNode[0];
    mLocalPartKey = localPartKey;
    mURIKey = uriKey;
    mPrefixKey = prefixKey;
  }

  public ElementNode(final long nodeKey) {
    this(
        nodeKey,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        (int) IConstants.NULL_KEY,
        (int) IConstants.NULL_KEY,
        (int) IConstants.NULL_KEY);
  }

  public ElementNode(final INode node) {
    super(node.getNodeKey());
    mParentKey = node.getParentKey();
    mFirstChildKey = node.getFirstChildKey();
    mLeftSiblingKey = node.getLeftSiblingKey();
    mRightSiblingKey = node.getRightSiblingKey();
    mChildCount = node.getChildCount();
    mAttributes = new AttributeNode[node.getAttributeCount()];
    for (int i = 0, l = mAttributes.length; i < l; i++) {
      mAttributes[i] = new AttributeNode(node.getAttribute(i));
    }
    mNamespaces = new NamespaceNode[node.getNamespaceCount()];
    for (int i = 0, l = mNamespaces.length; i < l; i++) {
      mNamespaces[i] = new NamespaceNode(node.getNamespace(i));
    }
    mLocalPartKey = node.getLocalPartKey();
    mURIKey = node.getURIKey();
    mPrefixKey = node.getPrefixKey();
  }

  public ElementNode(final long nodeKey, final FastByteArrayReader in) {
    super(nodeKey);

    // Read according to node kind.
    mParentKey = getNodeKey() - in.readVarLong();
    mFirstChildKey = getNodeKey() - in.readVarLong();
    mLeftSiblingKey = getNodeKey() - in.readVarLong();
    mRightSiblingKey = getNodeKey() - in.readVarLong();
    mChildCount = in.readVarLong();
    mAttributes = new AttributeNode[in.readByte()];
    for (int i = 0, l = mAttributes.length; i < l; i++) {
      mAttributes[i] =
          new AttributeNode(
              getNodeKey() + i + 1,
              getNodeKey(),
              in.readVarInt(),
              in.readVarInt(),
              in.readVarInt(),
              in.readByteArray());
    }
    mNamespaces = new NamespaceNode[in.readByte()];
    for (int i = 0, l = mNamespaces.length; i < l; i++) {
      mNamespaces[i] = new NamespaceNode(in.readVarInt(), in.readVarInt());
    }
    mLocalPartKey = in.readVarInt();
    mURIKey = in.readVarInt();
    mPrefixKey = in.readVarInt();

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
    return mAttributes == null ? 0 : mAttributes.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final INode getAttribute(final int index) {
    return mAttributes[index];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setAttribute(
      final int index,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
    mAttributes[index] =
        new AttributeNode(
            getNodeKey() + index + 1,
            getNodeKey(),
            localPartKey,
            uriKey,
            prefixKey,
            value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertAttribute(
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {

    AttributeNode[] tmp = new AttributeNode[mAttributes.length + 1];
    System.arraycopy(mAttributes, 0, tmp, 0, mAttributes.length);
    mAttributes = tmp;

    mAttributes[mAttributes.length - 1] =
        new AttributeNode(
            getNodeKey() + mAttributes.length,
            getNodeKey(),
            localPartKey,
            uriKey,
            prefixKey,
            value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getNamespaceCount() {
    return mNamespaces == null ? 0 : mNamespaces.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final NamespaceNode getNamespace(final int index) {
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
    mNamespaces[index] = new NamespaceNode(uriKey, prefixKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void insertNamespace(final int uriKey, final int prefixKey) {

    NamespaceNode[] tmp = new NamespaceNode[mAttributes.length + 1];
    System.arraycopy(mNamespaces, 0, tmp, 0, mNamespaces.length);
    mNamespaces = tmp;

    mNamespaces[mNamespaces.length - 1] = new NamespaceNode(uriKey, prefixKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getKind() {
    return IConstants.ELEMENT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getLocalPartKey() {
    return mLocalPartKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getLocalPart(final IReadTransaction rtx) {
    return rtx.nameForKey(mLocalPartKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setLocalPartKey(final int localPartKey) {
    mLocalPartKey = localPartKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getPrefixKey() {
    return mPrefixKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getPrefix(final IReadTransaction rtx) {
    return rtx.nameForKey(mPrefixKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setPrefixKey(final int prefixKey) {
    mPrefixKey = prefixKey;
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
  public final String getURI(final IReadTransaction rtx) {
    return rtx.nameForKey(mURIKey);
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
  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(getNodeKey() - mParentKey);
    out.writeVarLong(getNodeKey() - mFirstChildKey);
    out.writeVarLong(getNodeKey() - mLeftSiblingKey);
    out.writeVarLong(getNodeKey() - mRightSiblingKey);
    out.writeVarLong(mChildCount);
    out.writeByte((byte) mAttributes.length);
    for (int i = 0, l = mAttributes.length; i < l; i++) {
      out.writeVarInt(mAttributes[i].getLocalPartKey());
      out.writeVarInt(mAttributes[i].getURIKey());
      out.writeVarInt(mAttributes[i].getPrefixKey());
      out.writeByteArray(mAttributes[i].getValue());
    }
    out.writeByte((byte) mNamespaces.length);
    for (int i = 0, l = mNamespaces.length; i < l; i++) {
      out.writeVarInt(mNamespaces[i].getURIKey());
      out.writeVarInt(mNamespaces[i].getPrefixKey());
    }
    out.writeVarInt(mLocalPartKey);
    out.writeVarInt(mURIKey);
    out.writeVarInt(mPrefixKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Node "
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
