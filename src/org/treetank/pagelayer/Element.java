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

package org.treetank.pagelayer;

import java.util.Arrays;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

public final class Element implements INode, InternalNode {

  private long mNodeKey;

  private long mParentKey;

  private long mFirstChildKey;

  private long mLeftSiblingKey;

  private long mRightSiblingKey;

  private long mChildCount;

  private INode[] mAttributes;

  private Namespace[] mNamespaces;

  private int mLocalPartKey;

  private int mURIKey;

  private int mPrefixKey;

  public Element(
      final long nodeKey,
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int localPartKey,
      final int uriKey,
      final int prefixKey) {
    mNodeKey = nodeKey;
    mParentKey = parentKey;
    mFirstChildKey = firstChildKey;
    mLeftSiblingKey = leftSiblingKey;
    mRightSiblingKey = rightSiblingKey;
    mChildCount = 0;
    mAttributes = new Attribute[0];
    mNamespaces = new Namespace[0];
    mLocalPartKey = localPartKey;
    mURIKey = uriKey;
    mPrefixKey = prefixKey;
  }

  public Element(final long nodeKey) {
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

  public Element(final INode node) {
    mNodeKey = node.getNodeKey();
    mParentKey = node.getParentKey();
    mFirstChildKey = node.getFirstChildKey();
    mLeftSiblingKey = node.getLeftSiblingKey();
    mRightSiblingKey = node.getRightSiblingKey();
    mChildCount = node.getChildCount();
    mAttributes = new INode[node.getAttributeCount()];
    for (int i = 0, l = mAttributes.length; i < l; i++) {
      mAttributes[i] = new Attribute(node.getAttribute(i));
    }
    mNamespaces = new Namespace[node.getNamespaceCount()];
    for (int i = 0, l = mNamespaces.length; i < l; i++) {
      mNamespaces[i] = new Namespace(node.getNamespace(i));
    }
    mLocalPartKey = node.getLocalPartKey();
    mURIKey = node.getURIKey();
    mPrefixKey = node.getPrefixKey();
  }

  public Element(final long nodeKey, final FastByteArrayReader in) {

    // Always read node key and kind.
    mNodeKey = nodeKey;

    // Read according to node kind.
    mParentKey = mNodeKey - in.readVarLong();
    mFirstChildKey = mNodeKey - in.readVarLong();
    mLeftSiblingKey = mNodeKey - in.readVarLong();
    mRightSiblingKey = mNodeKey - in.readVarLong();
    mChildCount = in.readVarLong();
    mAttributes = new Attribute[in.readByte()];
    for (int i = 0, l = mAttributes.length; i < l; i++) {
      mAttributes[i] =
          new Attribute(mNodeKey + i + 1, mNodeKey, in.readVarInt(), in
              .readVarInt(), in.readVarInt(), in.readByteArray());
    }
    mNamespaces = new Namespace[in.readByte()];
    for (int i = 0, l = mNamespaces.length; i < l; i++) {
      mNamespaces[i] = new Namespace(in.readVarInt(), in.readVarInt());
    }
    mLocalPartKey = in.readVarInt();
    mURIKey = in.readVarInt();
    mPrefixKey = in.readVarInt();

  }

  public final boolean isDocument() {
    return false;
  }

  public final boolean isElement() {
    return true;
  }

  public final boolean isAttribute() {
    return false;
  }

  public final boolean isText() {
    return false;
  }

  public final long getNodeKey() {
    return mNodeKey;
  }

  public final void setNodeKey(final long nodeKey) {
    mNodeKey = nodeKey;
  }

  public final boolean hasParent() {
    return (mParentKey != IConstants.NULL_KEY);
  }

  public final long getParentKey() {
    return mParentKey;
  }

  public final INode getParent(final IReadTransaction rtx) {
    return rtx.moveTo(mParentKey);
  }

  public final void setParentKey(final long parentKey) {
    mParentKey = parentKey;
  }

  public final boolean hasFirstChild() {
    return (mFirstChildKey != IConstants.NULL_KEY);
  }

  public final long getFirstChildKey() {
    return mFirstChildKey;
  }

  public final INode getFirstChild(final IReadTransaction rtx) {
    return rtx.moveTo(mFirstChildKey);
  }

  public final void setFirstChildKey(final long firstChildKey) {
    mFirstChildKey = firstChildKey;
  }

  public final boolean hasLeftSibling() {
    return (mLeftSiblingKey != IConstants.NULL_KEY);
  }

  public final long getLeftSiblingKey() {
    return mLeftSiblingKey;
  }

  public final INode getLeftSibling(final IReadTransaction rtx) {
    return rtx.moveTo(mLeftSiblingKey);
  }

  public final void setLeftSiblingKey(final long leftSiblingKey) {
    mLeftSiblingKey = leftSiblingKey;
  }

  public final boolean hasRightSibling() {
    return (mRightSiblingKey != IConstants.NULL_KEY);
  }

  public final long getRightSiblingKey() {
    return mRightSiblingKey;
  }

  public final INode getRightSibling(final IReadTransaction rtx) {
    return rtx.moveTo(mRightSiblingKey);
  }

  public final void setRightSiblingKey(final long rightSiblingKey) {
    mRightSiblingKey = rightSiblingKey;
  }

  public final long getChildCount() {
    return mChildCount;
  }

  public final void setChildCount(final long childCount) {
    mChildCount = childCount;
  }

  public final void incrementChildCount() {
    mChildCount += 1;
  }

  public final void decrementChildCount() {
    mChildCount -= 1;
  }

  public final int getAttributeCount() {
    return mAttributes == null ? 0 : mAttributes.length;
  }

  public final INode getAttribute(final int index) {
    return mAttributes[index];
  }

  public final void setAttribute(
      final int index,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
    mAttributes[index] =
        new Attribute(
            mNodeKey + index + 1,
            mNodeKey,
            localPartKey,
            uriKey,
            prefixKey,
            value);
  }

  public final void insertAttribute(
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {

    INode[] tmp = new INode[mAttributes.length + 1];
    System.arraycopy(mAttributes, 0, tmp, 0, mAttributes.length);
    mAttributes = tmp;

    mAttributes[mAttributes.length - 1] =
        new Attribute(
            mNodeKey + mAttributes.length,
            mNodeKey,
            localPartKey,
            uriKey,
            prefixKey,
            value);
  }

  public final int getNamespaceCount() {
    return mNamespaces == null ? 0 : mNamespaces.length;
  }

  public final Namespace getNamespace(final int index) {
    return mNamespaces[index];
  }

  public final void setNamespace(
      final int index,
      final int uriKey,
      final int prefixKey) {
    mNamespaces[index] = new Namespace(uriKey, prefixKey);
  }

  public final void insertNamespace(final int uriKey, final int prefixKey) {

    Namespace[] tmp = new Namespace[mAttributes.length + 1];
    System.arraycopy(mNamespaces, 0, tmp, 0, mNamespaces.length);
    mNamespaces = tmp;

    mNamespaces[mNamespaces.length - 1] = new Namespace(uriKey, prefixKey);
  }

  public final int getKind() {
    return IConstants.ELEMENT;
  }

  public final void setKind(final byte kind) {
  }

  public final int getLocalPartKey() {
    return mLocalPartKey;
  }

  public final String getLocalPart(final IReadTransaction rtx) {
    return rtx.nameForKey(mLocalPartKey);
  }

  public final void setLocalPartKey(final int localPartKey) {
    mLocalPartKey = localPartKey;
  }

  public final int getPrefixKey() {
    return mPrefixKey;
  }

  public final String getPrefix(final IReadTransaction rtx) {
    return rtx.nameForKey(mPrefixKey);
  }

  public final void setPrefixKey(final int prefixKey) {
    mPrefixKey = prefixKey;
  }

  public final int getURIKey() {
    return mURIKey;
  }

  public final String getURI(final IReadTransaction rtx) {
    return rtx.nameForKey(mURIKey);
  }

  public final void setURIKey(final int uriKey) {
    mURIKey = uriKey;
  }

  public final byte[] getValue() {
    return null;
  }

  public final void setValue(final byte[] value) {
  }

  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(mNodeKey - mParentKey);
    out.writeVarLong(mNodeKey - mFirstChildKey);
    out.writeVarLong(mNodeKey - mLeftSiblingKey);
    out.writeVarLong(mNodeKey - mRightSiblingKey);
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
        + this.mNodeKey
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

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(mAttributes);
    result = prime * result + (int) (mChildCount ^ (mChildCount >>> 32));
    result = prime * result + (int) (mFirstChildKey ^ (mFirstChildKey >>> 32));
    result =
        prime * result + (int) (mLeftSiblingKey ^ (mLeftSiblingKey >>> 32));
    result = prime * result + mLocalPartKey;
    result = prime * result + Arrays.hashCode(mNamespaces);
    result = prime * result + (int) (mNodeKey ^ (mNodeKey >>> 32));
    result = prime * result + (int) (mParentKey ^ (mParentKey >>> 32));
    result = prime * result + mPrefixKey;
    result =
        prime * result + (int) (mRightSiblingKey ^ (mRightSiblingKey >>> 32));
    result = prime * result + mURIKey;
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if ((obj == null) || (mNodeKey != ((INode) obj).getNodeKey())) {
      return false;
    } else {
      return true;
    }
  }

}
