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

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

public final class Document implements INode, InternalNode {

  private long mNodeKey;

  private long mFirstChildKey;

  private long mChildCount;

  public Document(final long nodeKey, final long firstChildKey) {
    mNodeKey = nodeKey;
    mFirstChildKey = firstChildKey;
    mChildCount = 0;
  }

  public Document() {
    this(IConstants.ROOT_KEY, IConstants.NULL_KEY);
  }

  public Document(final InternalNode node) {
    mNodeKey = node.getNodeKey();
    mFirstChildKey = node.getFirstChildKey();
    mChildCount = node.getChildCount();
  }

  public Document(final FastByteArrayReader in) {
    mFirstChildKey = mNodeKey - in.readVarLong();
    mChildCount = in.readVarLong();
  }

  public final boolean isDocument() {
    return true;
  }

  public final boolean isElement() {
    return false;
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
    return false;
  }

  public final long getParentKey() {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final INode getParent(final IReadTransaction rtx) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setParentKey(final long parentKey) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
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
    return false;
  }

  public final long getLeftSiblingKey() {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final INode getLeftSibling(final IReadTransaction rtx) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setLeftSiblingKey(final long leftSiblingKey) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final boolean hasRightSibling() {
    return false;
  }

  public final long getRightSiblingKey() {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final INode getRightSibling(final IReadTransaction rtx) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setRightSiblingKey(final long rightSiblingKey) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
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
    return 0;
  }

  public final INode getAttribute(final int index) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setAttribute(
      final int index,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void insertAttribute(
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final int getNamespaceCount() {
    return 0;
  }

  public final Namespace getNamespace(final int index) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setNamespace(
      final int index,
      final int uriKey,
      final int prefixKey) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void insertNamespace(final int uriKey, final int prefixKey) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final int getKind() {
    return IConstants.DOCUMENT;
  }

  public final void setKind(final byte kind) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final int getLocalPartKey() {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final String getLocalPart(final IReadTransaction rtx) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setLocalPartKey(final int localPartKey) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final int getPrefixKey() {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final String getPrefix(final IReadTransaction rtx) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setPrefixKey(final int prefixKey) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final int getURIKey() {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final String getURI(final IReadTransaction rtx) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setURIKey(final int uriKey) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final byte[] getValue() {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void setValue(final byte[] value) {
    throw new UnsupportedOperationException(
        "Document does not implement this method.");
  }

  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarLong(mNodeKey - mFirstChildKey);
    out.writeVarLong(mChildCount);
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
        + "\n\tfirstChildKey: "
        + this.mFirstChildKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return (int) mNodeKey;
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
