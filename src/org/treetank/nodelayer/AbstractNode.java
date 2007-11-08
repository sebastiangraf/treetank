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
 * $Id: INode.java 3268 2007-10-25 13:16:01Z kramis $
 */

package org.treetank.nodelayer;

import org.treetank.api.IConstants;
import org.treetank.utils.FastByteArrayWriter;

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
public abstract class AbstractNode implements Comparable<AbstractNode> {

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
    return IConstants.NULL_KEY;
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
    return IConstants.NULL_KEY;
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
    return IConstants.NULL_KEY;
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
    return IConstants.NULL_KEY;
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
  public int getLocalPartKey() {
    return IConstants.NULL_NAME;
  }

  /**
   * {@inheritDoc}
   */
  public int getURIKey() {
    return IConstants.NULL_NAME;
  }

  /**
   * {@inheritDoc}
   */
  public int getPrefixKey() {
    return IConstants.NULL_NAME;
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
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
  }

  public void insertAttribute(
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
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

  public void setLocalPartKey(final int localPartKey) {
  }

  public void setPrefixKey(final int prefixKey) {
  }

  public void setURIKey(final int uriKey) {
  }

  public void setValue(final byte[] value) {
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
