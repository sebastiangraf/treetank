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
import org.treetank.utils.UTF;

public final class Text implements INode, InternalNode {

  private long mNodeKey;

  private long mParentKey;

  private long mLeftSiblingKey;

  private long mRightSiblingKey;

  private byte[] mValue;

  public Text(
      final long nodeKey,
      final long parentKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final byte[] value) {
    mNodeKey = nodeKey;
    mParentKey = parentKey;
    mLeftSiblingKey = leftSiblingKey;
    mRightSiblingKey = rightSiblingKey;
    mValue = value;
  }

  public Text(final long nodeKey) {
    this(
        nodeKey,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        UTF.EMPTY);
  }

  public Text(final INode node) {
    mNodeKey = node.getNodeKey();
    mParentKey = node.getParentKey();
    mLeftSiblingKey = node.getLeftSiblingKey();
    mRightSiblingKey = node.getRightSiblingKey();
    mValue = node.getValue();
  }

  public Text(final long nodeKey, final FastByteArrayReader in) {

    // Always read node key and kind.
    mNodeKey = nodeKey;

    mParentKey = mNodeKey - in.readVarLong();
    mLeftSiblingKey = in.readVarLong();
    mRightSiblingKey = in.readVarLong();
    mValue = in.readByteArray();

  }

  public final boolean isDocument() {
    return false;
  }

  public final boolean isElement() {
    return false;
  }

  public final boolean isAttribute() {
    return false;
  }

  public final boolean isText() {
    return true;
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
    return false;
  }

  public final long getFirstChildKey() {
    return IConstants.NULL_KEY;
  }

  public final INode getFirstChild(final IReadTransaction rtx) {
    return null;
  }

  public final void setFirstChildKey(final long firstChildKey) {
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
    return 0L;
  }

  public final void setChildCount(final long childCount) {
  }

  public final void incrementChildCount() {
  }

  public final void decrementChildCount() {
  }

  public final int getAttributeCount() {
    return 0;
  }

  public final INode getAttribute(final int index) {
    return null;
  }

  public final void setAttribute(
      final int index,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
  }

  public final void insertAttribute(
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
  }

  public final int getNamespaceCount() {
    return 0;
  }

  public final Namespace getNamespace(final int index) {
    return null;
  }

  public final void setNamespace(
      final int index,
      final int uriKey,
      final int prefixKey) {
  }

  public final void insertNamespace(final int uriKey, final int prefixKey) {
  }

  public final int getKind() {
    return IConstants.TEXT;
  }

  public final void setKind(final byte kind) {
  }

  public final int getLocalPartKey() {
    return -1;
  }

  public final String getLocalPart(final IReadTransaction rtx) {
    return null;
  }

  public final void setLocalPartKey(final int localPartKey) {
  }

  public final int getPrefixKey() {
    return -1;
  }

  public final String getPrefix(final IReadTransaction rtx) {
    return null;
  }

  public final void setPrefixKey(final int prefixKey) {
  }

  public final int getURIKey() {
    return -1;
  }

  public final String getURI(final IReadTransaction rtx) {
    return null;
  }

  public final void setURIKey(final int uriKey) {
  }

  public final byte[] getValue() {
    return mValue;
  }

  public final void setValue(final byte[] value) {
    mValue = value;
  }

  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(mNodeKey - mParentKey);
    out.writeVarLong(mLeftSiblingKey);
    out.writeVarLong(mRightSiblingKey);
    out.writeByteArray(mValue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Node "
        + "\n\tnodeKey: "
        + this.mNodeKey
        + "\n\tparentKey: "
        + this.mParentKey
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
    result =
        prime * result + (int) (mLeftSiblingKey ^ (mLeftSiblingKey >>> 32));
    result = prime * result + (int) (mNodeKey ^ (mNodeKey >>> 32));
    result = prime * result + (int) (mParentKey ^ (mParentKey >>> 32));
    result =
        prime * result + (int) (mRightSiblingKey ^ (mRightSiblingKey >>> 32));
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
