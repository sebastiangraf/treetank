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

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.IByteBuffer;
import org.treetank.utils.IConstants;

/**
 * <h1>AbstractNode</h1>
 * 
 * <p>
 * Abstract node class to implement all methods required with INode.
 * To reduce implementation overhead in subclasses it implements all
 * methods but does silently not do anything there. A subclass must only
 * implement those methods that are required to provide proper subclass
 * functionality.
 * </p>
 */
public abstract class AbstractNode implements IItem, Comparable<AbstractNode> {

  protected static final int NODE_KEY = 0;

  /** Node key is common to all node kinds. */
  protected long[] mData;

  /**
   * Constructor to set node key.
   * 
   * @param nodeKey Key of node.
   */
  public AbstractNode(final int size, final long nodeKey) {
    mData = new long[size];
    mData[NODE_KEY] = nodeKey;
  }

  /**
   * Constructor to set node key.
   * 
   * @param nodeKey Key of node.
   */
  public AbstractNode(final AbstractNode node) {
    mData = new long[node.mData.length];
    System.arraycopy(node.mData, 0, mData, 0, mData.length);
  }

  /**
   * Read node.
   * 
   * @param nodeKey Key of text node.
   * @param in Input bytes to read node from.
   */
  public AbstractNode(final int size, final long nodeKey, final IByteBuffer in) {
    mData = new long[size];
    in.get(mData);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isNode() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isDocumentRoot() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isElement() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAttribute() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isText() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public final long getNodeKey() {
    return mData[NODE_KEY];
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasParent() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public long getParentKey() {
    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasFirstChild() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public long getFirstChildKey() {
    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasLeftSibling() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public long getLeftSiblingKey() {
    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasRightSibling() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public long getRightSiblingKey() {
    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public long getChildCount() {
    return 0L;
  }

  /**
   * {@inheritDoc}
   */
  public int getAttributeCount() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  public int getNamespaceCount() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  public long getAttributeKey(final int index) {
    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public long getNamespaceKey(final int index) {
    return IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public int getKind() {
    return IConstants.UNKNOWN;
  }

  /**
   * {@inheritDoc}
   */
  public int getNameKey() {
    return IReadTransaction.NULL_NAME_KEY;
  }

  /**
   * {@inheritDoc}
   */
  public int getURIKey() {
    return IReadTransaction.NULL_NAME_KEY;
  }

  public int getTypeKey() {
    return IConstants.UNKNOWN;
  }

  /**
   * {@inheritDoc}
   */
  public byte[] getRawValue() {
    return null;
  }

  public void serialize(final IByteBuffer out) {
    out.putAll(mData);
  }

  public final void setNodeKey(final long nodeKey) {
    mData[NODE_KEY] = nodeKey;
  }

  public void setParentKey(final long parentKey) {
  }

  public void setFirstChildKey(final long firstChildKey) {
  }

  public void setLeftSiblingKey(final long leftSiblingKey) {
  }

  public void setRightSiblingKey(final long rightSiblingKey) {
  }

  public void setChildCount(final long childCount) {
  }

  public void incrementChildCount() {
  }

  public void decrementChildCount() {
  }

  public void insertAttribute(final long attributeKey) {
  }

  public void insertNamespace(final long namespaceKey) {
  }

  public void setKind(final byte kind) {
  }

  public void setNameKey(final int nameKey) {
  }

  public void setURIKey(final int uriKey) {
  }

  public void setValue(final int valueType, final byte[] value) {
  }

  public void setType(final int valueType) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return (int) mData[NODE_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    return ((obj != null) && (mData[NODE_KEY] == ((AbstractNode) obj).mData[NODE_KEY]));
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(final AbstractNode node) {
    final long nodeKey = ((AbstractNode) node).getNodeKey();
    if (mData[NODE_KEY] < nodeKey) {
      return -1;
    } else if (mData[NODE_KEY] == nodeKey) {
      return 0;
    } else {
      return 1;
    }
  }

}
