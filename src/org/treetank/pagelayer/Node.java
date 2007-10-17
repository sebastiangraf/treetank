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

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.UTF;

public final class Node implements INode {

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

  public Node(final long nodeKey, final FastByteArrayReader in)
      throws Exception {

    // Always read node key and kind.
    mNodeKey = nodeKey;
    mKind = in.readByte();

    // Read according to node kind.
    switch (mKind) {
    case IConstants.DOCUMENT:
      mParentKey = IConstants.NULL_KEY;
      mFirstChildKey = mNodeKey - in.readPseudoLong();
      mLeftSiblingKey = IConstants.NULL_KEY;
      mRightSiblingKey = IConstants.NULL_KEY;
      mChildCount = in.readPseudoLong();
      break;

    case IConstants.ELEMENT:
      mParentKey = mNodeKey - in.readPseudoLong();
      mFirstChildKey = mNodeKey - in.readPseudoLong();
      mLeftSiblingKey = mNodeKey - in.readPseudoLong();
      mRightSiblingKey = mNodeKey - in.readPseudoLong();
      mChildCount = in.readPseudoLong();
      mAttributes = new Attribute[in.readByte()];
      for (int i = 0, l = mAttributes.length; i < l; i++) {
        mAttributes[i] =
            new Attribute(mNodeKey + i + 1, mNodeKey, in.readPseudoInt(), in
                .readPseudoInt(), in.readPseudoInt(), in.readByteArray());
      }
      mNamespaces = new Namespace[in.readByte()];
      for (int i = 0, l = mNamespaces.length; i < l; i++) {
        mNamespaces[i] = new Namespace(in.readPseudoInt(), in.readPseudoInt());
      }
      mLocalPartKey = in.readPseudoInt();
      mURIKey = in.readPseudoInt();
      mPrefixKey = in.readPseudoInt();
      mValue = UTF.EMPTY;
      break;

    case IConstants.TEXT:
      mParentKey = mNodeKey - in.readPseudoLong();
      mFirstChildKey = IConstants.NULL_KEY;
      mLeftSiblingKey = in.readPseudoLong();
      mRightSiblingKey = in.readPseudoLong();
      mChildCount = 0L;
      mAttributes = null;
      mNamespaces = null;
      mLocalPartKey = 0;
      mURIKey = 0;
      mPrefixKey = 0;
      mValue = in.readByteArray();
      break;

    default:
      mParentKey = mNodeKey - in.readPseudoLong();
      mFirstChildKey = mNodeKey - in.readPseudoLong();
      mLeftSiblingKey = mNodeKey - in.readPseudoLong();
      mRightSiblingKey = mNodeKey - in.readPseudoLong();
      mChildCount = in.readPseudoLong();
      mAttributes = new Attribute[in.readByte()];
      for (int i = 0, l = mAttributes.length; i < l; i++) {
        mAttributes[i] =
            new Attribute(mNodeKey + i + 1, mNodeKey, in.readPseudoInt(), in
                .readPseudoInt(), in.readPseudoInt(), in.readByteArray());
      }
      mNamespaces = new Namespace[in.readByte()];
      for (int i = 0, l = mNamespaces.length; i < l; i++) {
        mNamespaces[i] = new Namespace(in.readPseudoInt(), in.readPseudoInt());
      }
      mLocalPartKey = in.readPseudoInt();
      mURIKey = in.readPseudoInt();
      mPrefixKey = in.readPseudoInt();
      mValue = in.readByteArray();
    }
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

  public final void setParentKey(final long parentKey) {
    mParentKey = parentKey;
  }

  public final long getFirstChildKey() {
    return mFirstChildKey;
  }

  public final void setFirstChildKey(final long firstChildKey) {
    mFirstChildKey = firstChildKey;
  }

  public final long getLeftSiblingKey() {
    return mLeftSiblingKey;
  }

  public final void setLeftSiblingKey(final long leftSiblingKey) {
    mLeftSiblingKey = leftSiblingKey;
  }

  public final long getRightSiblingKey() {
    return mRightSiblingKey;
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

  public final void setLocalPartKey(final int localPartKey) {
    mLocalPartKey = localPartKey;
  }

  public final int getPrefixKey() {
    return mPrefixKey;
  }

  public final void setPrefixKey(final int prefixKey) {
    mPrefixKey = prefixKey;
  }

  public final int getURIKey() {
    return mURIKey;
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

  public final void serialize(final FastByteArrayWriter out) throws Exception {

    // Guarantee kind to be stored.
    out.writeByte((byte) mKind);

    // Adaptive serialization according to node kind.
    switch (mKind) {

    case IConstants.DOCUMENT:
      out.writePseudoLong(mNodeKey - mFirstChildKey);
      out.writePseudoLong(mChildCount);
      break;

    case IConstants.ELEMENT:
      out.writePseudoLong(mNodeKey - mParentKey);
      out.writePseudoLong(mNodeKey - mFirstChildKey);
      out.writePseudoLong(mNodeKey - mLeftSiblingKey);
      out.writePseudoLong(mNodeKey - mRightSiblingKey);
      out.writePseudoLong(mChildCount);
      out.writeByte((byte) mAttributes.length);
      for (int i = 0, l = mAttributes.length; i < l; i++) {
        out.writePseudoInt(mAttributes[i].getLocalPartKey());
        out.writePseudoInt(mAttributes[i].getURIKey());
        out.writePseudoInt(mAttributes[i].getPrefixKey());
        out.writeByteArray(mAttributes[i].getValue());
      }
      out.writeByte((byte) mNamespaces.length);
      for (int i = 0, l = mNamespaces.length; i < l; i++) {
        out.writePseudoInt(mNamespaces[i].getURIKey());
        out.writePseudoInt(mNamespaces[i].getPrefixKey());
      }
      out.writePseudoInt(mLocalPartKey);
      out.writePseudoInt(mURIKey);
      out.writePseudoInt(mPrefixKey);
      break;
    case IConstants.TEXT:
      out.writePseudoLong(mNodeKey - mParentKey);
      out.writePseudoLong(mLeftSiblingKey);
      out.writePseudoLong(mRightSiblingKey);
      out.writeByteArray(mValue);
      break;
    default:
      out.writePseudoLong(mNodeKey - mParentKey);
      out.writePseudoLong(mNodeKey - mFirstChildKey);
      out.writePseudoLong(mNodeKey - mLeftSiblingKey);
      out.writePseudoLong(mNodeKey - mRightSiblingKey);
      out.writePseudoLong(mChildCount);
      out.writeByte((byte) mAttributes.length);
      for (int i = 0, l = mAttributes.length; i < l; i++) {
        out.writePseudoInt(mAttributes[i].getLocalPartKey());
        out.writePseudoInt(mAttributes[i].getURIKey());
        out.writePseudoInt(mAttributes[i].getPrefixKey());
        out.writeByteArray(mAttributes[i].getValue());
      }
      out.writeByte((byte) mNamespaces.length);
      for (int i = 0, l = mNamespaces.length; i < l; i++) {
        out.writePseudoInt(mNamespaces[i].getURIKey());
        out.writePseudoInt(mNamespaces[i].getPrefixKey());
      }
      out.writePseudoInt(mLocalPartKey);
      out.writePseudoInt(mURIKey);
      out.writePseudoInt(mPrefixKey);
      out.writeByteArray(mValue);
    }
  }

}
