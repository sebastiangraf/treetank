/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.pagelayer;

import org.treetank.nodelayer.INode;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.IConstants;
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
    case IConstants.ELEMENT:
      mParentKey = mNodeKey - in.readLong();
      mFirstChildKey = mNodeKey - in.readLong();
      mLeftSiblingKey = mNodeKey - in.readLong();
      mRightSiblingKey = mNodeKey - in.readLong();
      mChildCount = in.readLong();
      mAttributes = new Attribute[in.readByte()];
      for (int i = 0, l = mAttributes.length; i < l; i++) {
        mAttributes[i] =
            new Attribute(mNodeKey + i + 1, mNodeKey, in.readInt(), in
                .readInt(), in.readInt(), in.readByteArray());
      }
      mNamespaces = new Namespace[in.readByte()];
      for (int i = 0, l = mNamespaces.length; i < l; i++) {
        mNamespaces[i] = new Namespace(in.readInt(), in.readInt());
      }
      mLocalPartKey = in.readInt();
      mURIKey = in.readInt();
      mPrefixKey = in.readInt();
      mValue = UTF.EMPTY;
      break;

    case IConstants.TEXT:
      mParentKey = mNodeKey - in.readLong();
      mFirstChildKey = IConstants.NULL_KEY;
      mLeftSiblingKey = in.readLong();
      mRightSiblingKey = in.readLong();
      mChildCount = 0L;
      mAttributes = null;
      mNamespaces = null;
      mLocalPartKey = 0;
      mURIKey = 0;
      mPrefixKey = 0;
      mValue = in.readByteArray();
      break;

    default:
      mParentKey = mNodeKey - in.readLong();
      mFirstChildKey = mNodeKey - in.readLong();
      mLeftSiblingKey = mNodeKey - in.readLong();
      mRightSiblingKey = mNodeKey - in.readLong();
      mChildCount = in.readLong();
      mAttributes = new Attribute[in.readByte()];
      for (int i = 0, l = mAttributes.length; i < l; i++) {
        mAttributes[i] =
            new Attribute(mNodeKey + i + 1, mNodeKey, in.readInt(), in
                .readInt(), in.readInt(), in.readByteArray());
      }
      mNamespaces = new Namespace[in.readByte()];
      for (int i = 0, l = mNamespaces.length; i < l; i++) {
        mNamespaces[i] = new Namespace(in.readInt(), in.readInt());
      }
      mLocalPartKey = in.readInt();
      mURIKey = in.readInt();
      mPrefixKey = in.readInt();
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
    case IConstants.ELEMENT:
      out.writeLong(mNodeKey - mParentKey);
      out.writeLong(mNodeKey - mFirstChildKey);
      out.writeLong(mNodeKey - mLeftSiblingKey);
      out.writeLong(mNodeKey - mRightSiblingKey);
      out.writeLong(mChildCount);
      out.writeByte((byte) mAttributes.length);
      for (int i = 0, l = mAttributes.length; i < l; i++) {
        out.writeInt(mAttributes[i].getLocalPartKey());
        out.writeInt(mAttributes[i].getURIKey());
        out.writeInt(mAttributes[i].getPrefixKey());
        out.writeByteArray(mAttributes[i].getValue());
      }
      out.writeByte((byte) mNamespaces.length);
      for (int i = 0, l = mNamespaces.length; i < l; i++) {
        out.writeInt(mNamespaces[i].getURIKey());
        out.writeInt(mNamespaces[i].getPrefixKey());
      }
      out.writeInt(mLocalPartKey);
      out.writeInt(mURIKey);
      out.writeInt(mPrefixKey);
      break;
    case IConstants.TEXT:
      out.writeLong(mNodeKey - mParentKey);
      out.writeLong(mLeftSiblingKey);
      out.writeLong(mRightSiblingKey);
      out.writeByteArray(mValue);
      break;
    default:
      out.writeLong(mNodeKey - mParentKey);
      out.writeLong(mNodeKey - mFirstChildKey);
      out.writeLong(mNodeKey - mLeftSiblingKey);
      out.writeLong(mNodeKey - mRightSiblingKey);
      out.writeLong(mChildCount);
      out.writeByte((byte) mAttributes.length);
      for (int i = 0, l = mAttributes.length; i < l; i++) {
        out.writeInt(mAttributes[i].getLocalPartKey());
        out.writeInt(mAttributes[i].getURIKey());
        out.writeInt(mAttributes[i].getPrefixKey());
        out.writeByteArray(mAttributes[i].getValue());
      }
      out.writeByte((byte) mNamespaces.length);
      for (int i = 0, l = mNamespaces.length; i < l; i++) {
        out.writeInt(mNamespaces[i].getURIKey());
        out.writeInt(mNamespaces[i].getPrefixKey());
      }
      out.writeInt(mLocalPartKey);
      out.writeInt(mURIKey);
      out.writeInt(mPrefixKey);
      out.writeByteArray(mValue);
    }
  }

  public final static long nodePageKey(final long nodeKey) {
    return nodeKey >> (IConstants.NDP_NODE_COUNT_EXPONENT + IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
  }

  public final static int nodePageOffset(final long nodeKey) {
    return (int) (nodeKey
        - ((nodeKey >> (IConstants.NDP_NODE_COUNT_EXPONENT + IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT)) << (IConstants.NDP_NODE_COUNT_EXPONENT + IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT)) >> IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
  }

  public final static long keyBase(final long nodePageKey) {
    return nodePageKey << (IConstants.NDP_NODE_COUNT_EXPONENT + IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
  }

}
