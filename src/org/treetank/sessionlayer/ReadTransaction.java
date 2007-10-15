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
 * $Id:ReadTransaction.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IReadTransactionState;

/**
 * <h1>ReadTransaction</h1>
 * 
 * <p>
 * Read-only transaction wiht single-threaded cursor semantics. Each
 * read-only transaction works on a given revision key.
 * </p>
 */
public class ReadTransaction implements IReadTransaction {

  /** State of transaction including all cached stuff. */
  private final IReadTransactionState mState;

  /** Strong reference to currently selected node. */
  private INode mCurrentNode;

  /**
   * Constructor.
   * 
   * @param state Transaction state to work with.
   */
  protected ReadTransaction(final IReadTransactionState state) {
    mState = state;
    setCurrentNode(null);
  }

  /**
   * {@inheritDoc}
   */
  public final long revisionKey() {
    return getState().getRevisionRootPage().getRevisionKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long revisionSize() {
    return getState().getRevisionRootPage().getNodeCount();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isSelected() {
    return (getCurrentNode() != null);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToRoot() throws Exception {
    return moveTo(IConstants.ROOT_KEY);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveTo(final long nodeKey) throws Exception {
    if (nodeKey != IConstants.NULL_KEY) {
      setCurrentNode(getState().getNode(nodeKey));
      return true;
    } else {
      setCurrentNode(null);
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToParent() throws Exception {
    return moveTo(getCurrentNode().getParentKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToFirstChild() throws Exception {
    return moveTo(getCurrentNode().getFirstChildKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToLeftSibling() throws Exception {
    return moveTo(getCurrentNode().getLeftSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToRightSibling() throws Exception {
    return moveTo(getCurrentNode().getRightSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToAttribute(final int index) throws Exception {
    setCurrentNode(getCurrentNode().getAttribute(index));
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public final INode getNode() {
    assertIsSelected();
    return getCurrentNode();
  }

  /**
   * {@inheritDoc}
   */
  public final long getNodeKey() {
    assertIsSelected();
    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getParentKey() {
    assertIsSelected();
    return getCurrentNode().getParentKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getFirstChildKey() {
    assertIsSelected();
    return getCurrentNode().getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getLeftSiblingKey() {
    assertIsSelected();
    return getCurrentNode().getLeftSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getRightSiblingKey() {
    assertIsSelected();
    return getCurrentNode().getRightSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getChildCount() {
    assertIsSelected();
    return getCurrentNode().getChildCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeCount() {
    assertIsSelected();
    return getCurrentNode().getAttributeCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespaceCount() {
    assertIsSelected();
    return getCurrentNode().getNamespaceCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getKind() {
    assertIsSelected();
    return getCurrentNode().getKind();
  }

  /**
   * {@inheritDoc}
   */
  public final int getLocalPartKey() {
    assertIsSelected();
    return getCurrentNode().getLocalPartKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getLocalPart() throws Exception {
    assertIsSelected();
    return getState().getName(getCurrentNode().getLocalPartKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getURIKey() {
    assertIsSelected();
    return getCurrentNode().getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getURI() throws Exception {
    assertIsSelected();
    return getState().getName(getCurrentNode().getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getPrefixKey() {
    assertIsSelected();
    return getCurrentNode().getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getPrefix() throws Exception {
    assertIsSelected();
    return getState().getName(getCurrentNode().getPrefixKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getValue() {
    assertIsSelected();
    return getCurrentNode().getValue();
  }

  /**
   * {@inheritDoc}
   */
  public final int keyForName(final String name) {
    return name.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  public final String nameForKey(final int key) throws Exception {
    return getState().getName(key);
  }

  /**
   * Assert that a node is selected.
   */
  protected void assertIsSelected() {
    if (getCurrentNode() == null) {
      throw new IllegalStateException("No node selected.");
    }
  }

  /**
   * @return The state of this transaction.
   */
  protected final IReadTransactionState getState() {
    return mState;
  }

  /**
   * @param currentNode The current node to set.
   */
  protected final void setCurrentNode(final INode currentNode) {
    mCurrentNode = currentNode;
  }

  /**
   * @return The current node.
   */
  protected final INode getCurrentNode() {
    return mCurrentNode;
  }

}
