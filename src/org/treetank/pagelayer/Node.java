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
 * $Id$
 */

package org.treetank.pagelayer;

import java.util.Arrays;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ITransactionNode;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.UTF;

public final class Node implements INode, ITransactionNode {

  private IReadTransaction mRTX;

  private long mNodeKey;

  private long mParentKey;

  private long mFirstChildKey;

  private long mLeftSiblingKey;

  private long mRightSiblingKey;

  private long mChildCount;

  private INode[] mAttributes;

  private Namespace[] mNamespaces;

  private int mKind;

  private int mLocalPartKey;

  private int mURIKey;

  private int mPrefixKey;

  private byte[] mValue;

  public Node(
      final long nodeKey,
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int kind,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
    mNodeKey = nodeKey;
    mParentKey = parentKey;
    mFirstChildKey = firstChildKey;
    mLeftSiblingKey = leftSiblingKey;
    mRightSiblingKey = rightSiblingKey;
    mChildCount = 0;
    mAttributes = new Attribute[0];
    mNamespaces = new Namespace[0];
    mKind = kind;
    mLocalPartKey = localPartKey;
    mURIKey = uriKey;
    mPrefixKey = prefixKey;
    mValue = value;
  }

  public Node(final long nodeKey) {
    this(
        nodeKey,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.DOCUMENT,
        (int) IConstants.NULL_KEY,
        (int) IConstants.NULL_KEY,
        (int) IConstants.NULL_KEY,
        UTF.EMPTY);
  }

  public Node(final INode node) {
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
    mKind = node.getKind();
    mLocalPartKey = node.getLocalPartKey();
    mURIKey = node.getURIKey();
    mPrefixKey = node.getPrefixKey();
    mValue = node.getValue();
  }

  public Node(final long nodeKey, final FastByteArrayReader in) {

    // Always read node key and kind.
    mNodeKey = nodeKey;
    mKind = in.readByte();

    // Read according to node kind.
    switch (mKind) {
    case IConstants.DOCUMENT:
      mParentKey = IConstants.NULL_KEY;
      mFirstChildKey = mNodeKey - in.readVarLong();
      mLeftSiblingKey = IConstants.NULL_KEY;
      mRightSiblingKey = IConstants.NULL_KEY;
      mChildCount = in.readVarLong();
      break;

    case IConstants.ELEMENT:
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
      mValue = UTF.EMPTY;
      break;

    case IConstants.TEXT:
      mParentKey = mNodeKey - in.readVarLong();
      mFirstChildKey = IConstants.NULL_KEY;
      mLeftSiblingKey = in.readVarLong();
      mRightSiblingKey = in.readVarLong();
      mChildCount = 0L;
      mAttributes = null;
      mNamespaces = null;
      mLocalPartKey = 0;
      mURIKey = 0;
      mPrefixKey = 0;
      mValue = in.readByteArray();
      break;

    default:
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
      mValue = in.readByteArray();
    }
  }

  public final void setTransaction(final IReadTransaction rtx) {
    mRTX = rtx;
  }

  public final long getNodeKey() {
    return mNodeKey;
  }

  public final void setNodeKey(final long nodeKey) {
    mNodeKey = nodeKey;
  }

  public final long getParentKey() {
    return mParentKey;
  }

  public final INode getParent() {
    return mRTX.moveTo(mParentKey);
  }

  public final void setParentKey(final long parentKey) {
    mParentKey = parentKey;
  }

  public final long getFirstChildKey() {
    return mFirstChildKey;
  }

  public final INode getFirstChild() {
    return mRTX.moveTo(mFirstChildKey);
  }

  public final void setFirstChildKey(final long firstChildKey) {
    mFirstChildKey = firstChildKey;
  }

  public final long getLeftSiblingKey() {
    return mLeftSiblingKey;
  }

  public final INode getLeftSibling() {
    return mRTX.moveTo(mLeftSiblingKey);
  }

  public final void setLeftSiblingKey(final long leftSiblingKey) {
    mLeftSiblingKey = leftSiblingKey;
  }

  public final long getRightSiblingKey() {
    return mRightSiblingKey;
  }

  public final INode getRightSibling() {
    return mRTX.moveTo(mRightSiblingKey);
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
    return mKind;
  }

  public final void setKind(final byte kind) {
    mKind = kind;
  }

  public final int getLocalPartKey() {
    return mLocalPartKey;
  }

  public final String getLocalPart() {
    return mRTX.nameForKey(mLocalPartKey);
  }

  public final void setLocalPartKey(final int localPartKey) {
    mLocalPartKey = localPartKey;
  }

  public final int getPrefixKey() {
    return mPrefixKey;
  }

  public final String getPrefix() {
    return mRTX.nameForKey(mPrefixKey);
  }

  public final void setPrefixKey(final int prefixKey) {
    mPrefixKey = prefixKey;
  }

  public final int getURIKey() {
    return mURIKey;
  }

  public final String getURI() {
    return mRTX.nameForKey(mURIKey);
  }

  public final void setURIKey(final int uriKey) {
    mURIKey = uriKey;
  }

  public final byte[] getValue() {
    return mValue;
  }

  public final void setValue(final byte[] value) {
    mValue = value;
  }

  public final void serialize(final FastByteArrayWriter out) {

    // Guarantee kind to be stored.
    out.writeByte((byte) mKind);

    // Adaptive serialization according to node kind.
    switch (mKind) {

    case IConstants.DOCUMENT:
      out.writeVarLong(mNodeKey - mFirstChildKey);
      out.writeVarLong(mChildCount);
      break;

    case IConstants.ELEMENT:
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
      break;
    case IConstants.TEXT:
      out.writeVarLong(mNodeKey - mParentKey);
      out.writeVarLong(mLeftSiblingKey);
      out.writeVarLong(mRightSiblingKey);
      out.writeByteArray(mValue);
      break;
    default:
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
      out.writeByteArray(mValue);
    }
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
    result = prime * result + mKind;
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
    result = prime * result + Arrays.hashCode(mValue);
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
