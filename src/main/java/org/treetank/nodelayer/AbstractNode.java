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

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastByteArrayWriter;
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

  /** Node key is common to all node kinds. */
  private long mNodeKey;

  /**
   * Constructor to set node key.
   * 
   * @param nodeKey Key of node.
   */
  public AbstractNode(final long nodeKey) {
    mNodeKey = nodeKey;
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
  public boolean isFullText() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isFullTextRoot() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isFullTextLeaf() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public final long getNodeKey() {
    return mNodeKey;
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
  public AbstractNode getAttribute(final int index) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public AbstractNode getNamespace(final int index) {
    return null;
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
  public int getPrefixKey() {
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
  public byte[] getValue() {
    return null;
  }

  public void serialize(final FastByteArrayWriter out) {
  }

  public final void setNodeKey(final long nodeKey) {
    mNodeKey = nodeKey;
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

  public void setAttribute(
      final int index,
      final int nameKey,
      final int uriKey,
      final int valueType,
      final byte[] value) {
  }

  public void insertAttribute(
      final int nameKey,
      final int uriKey,
      final int valueType,
      final byte[] value) {
  }

  public void setNamespace(
      final int index,
      final int uriKey,
      final int prefixKey) {
  }

  public void insertNamespace(final int uriKey, final int prefixKey) {
  }

  public void setKind(final byte kind) {
  }

  public void setNameKey(final int nameKey) {
  }

  public void setPrefixKey(final int prefixKey) {
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
    return (int) mNodeKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    return ((obj != null) && (mNodeKey == ((AbstractNode) obj).getNodeKey()));
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(final AbstractNode node) {
    final long nodeKey = ((AbstractNode) node).getNodeKey();
    if (mNodeKey < nodeKey) {
      return -1;
    } else if (mNodeKey == nodeKey) {
      return 0;
    } else {
      return 1;
    }
  }

}
