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

public final class Attribute implements INode, ITransactionNode {

  private IReadTransaction mRTX;

  private long mNodeKey;

  private long mParentKey;

  private int mLocalPartKey;

  private int mURIKey;

  private int mPrefixKey;

  private byte[] mValue;

  public Attribute(
      final long nodeKey,
      final long parentKey,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
    mNodeKey = nodeKey;
    mParentKey = parentKey;
    mLocalPartKey = localPartKey;
    mURIKey = uriKey;
    mPrefixKey = prefixKey;
    mValue = value;
  }

  public Attribute(final INode attribute) {
    mNodeKey = attribute.getNodeKey();
    mParentKey = attribute.getParentKey();
    mLocalPartKey = attribute.getLocalPartKey();
    mURIKey = attribute.getURIKey();
    mPrefixKey = attribute.getPrefixKey();
    mValue = attribute.getValue();
  }

  public final void setTransaction(final IReadTransaction rtx) {
    mRTX = rtx;
  }

  public final long getNodeKey() {
    return mNodeKey;
  }

  public final void setNodeKey(final long nodeKey) {
    this.mNodeKey = nodeKey;
  }

  public final int getLocalPartKey() {
    return mLocalPartKey;
  }

  public final String getLocalPart() {
    return mRTX.nameForKey(mLocalPartKey);
  }

  public final void setLocalPartKey(final int localPartKey) {
    this.mLocalPartKey = localPartKey;
  }

  public final long getParentKey() {
    return mParentKey;
  }

  public final INode getParent() {
    return mRTX.moveTo(mParentKey);
  }

  public final void setParentKey(final long parentKey) {
    this.mParentKey = parentKey;
  }

  public final int getPrefixKey() {
    return mPrefixKey;
  }

  public final String getPrefix() {
    return mRTX.nameForKey(mPrefixKey);
  }

  public final void setPrefixKey(final int prefixKey) {
    this.mPrefixKey = prefixKey;
  }

  public final int getURIKey() {
    return mURIKey;
  }

  public final String getURI() {
    return mRTX.nameForKey(mURIKey);
  }

  public final void setURIKey(final int uriKey) {
    this.mURIKey = uriKey;
  }

  public final byte[] getValue() {
    return mValue;
  }

  public final void setValue(final byte[] value) {
    this.mValue = value;
  }

  public final INode getAttribute(final int index) {
    return null;
  }

  public final int getAttributeCount() {
    return 0;
  }

  public final long getChildCount() {
    return 0L;
  }

  public final long getFirstChildKey() {
    return IConstants.NULL_KEY;
  }

  public final INode getFirstChild() {
    return null;
  }

  public final int getKind() {
    return IConstants.ATTRIBUTE;
  }

  public final long getLeftSiblingKey() {
    return IConstants.NULL_KEY;
  }

  public final INode getLeftSibling() {
    return null;
  }

  public final long getRightSiblingKey() {
    return IConstants.NULL_KEY;
  }

  public final INode getRightSibling() {
    return null;
  }

  public final Namespace getNamespace(final int index) {
    return null;
  }

  public final int getNamespaceCount() {
    return 0;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + mLocalPartKey;
    result = prime * result + (int) (mNodeKey ^ (mNodeKey >>> 32));
    result = prime * result + (int) (mParentKey ^ (mParentKey >>> 32));
    result = prime * result + mPrefixKey;
    result = prime * result + mURIKey;
    result = prime * result + Arrays.hashCode(mValue);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if ((obj == null) || (mNodeKey != ((INode) obj).getNodeKey())) {
      return false;
    } else {
      return true;
    }
  }

}
