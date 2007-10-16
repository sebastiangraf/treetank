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
 * $Id:WriteTransaction.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import org.treetank.api.IConstants;
import org.treetank.api.IWriteTransaction;
import org.treetank.api.IWriteTransactionState;
import org.treetank.pagelayer.Node;
import org.treetank.utils.UTF;

/**
 * <h1>WriteTransaction</h1>
 * 
 * <p>
 * Single-threaded instance of only write transaction per session.
 * </p>
 */
public final class WriteTransaction extends ReadTransaction
    implements
    IWriteTransaction {

  /**
   * Constructor.
   * 
   * @param initRevisionRootPage Revision root page to work with.
   */
  protected WriteTransaction(final IWriteTransactionState state) {
    super(state);
  }

  /**
   * {@inheritDoc}
   */
  public final long insertRoot(final String document) throws Exception {

    if (getState().getRevisionRootPage().getNodeCount() != 0) {
      throw new IllegalStateException("Root node already exists.");
    }

    // Create new root node.
    setCurrentNode(((IWriteTransactionState) getState()).createNode(
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.NULL_KEY,
        IConstants.DOCUMENT,
        ((IWriteTransactionState) getState()).createNameKey(""),
        ((IWriteTransactionState) getState()).createNameKey(""),
        ((IWriteTransactionState) getState()).createNameKey(""),
        UTF.convert(document)));

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long insertFirstChild(
      final int kind,
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) throws Exception {

    assertIsSelected();

    // Insert new node in place of current first child.
    if (getCurrentNode().getChildCount() > 0) {

      // Create new first child node.
      setCurrentNode(((IWriteTransactionState) getState()).createNode(
          getCurrentNode().getNodeKey(),
          IConstants.NULL_KEY,
          IConstants.NULL_KEY,
          getCurrentNode().getFirstChildKey(),
          kind,
          ((IWriteTransactionState) getState()).createNameKey(localPart),
          ((IWriteTransactionState) getState()).createNameKey(uri),
          ((IWriteTransactionState) getState()).createNameKey(prefix),
          value));

      // Change existing first child node.
      if (getCurrentNode().getRightSiblingKey() != IConstants.NULL_KEY) {
        final Node rightSiblingNode =
            ((IWriteTransactionState) getState()).prepareNode(getCurrentNode()
                .getRightSiblingKey());
        rightSiblingNode.setLeftSiblingKey(getCurrentNode().getNodeKey());
      }

      // Change parent node.
      final Node parentNode =
          ((IWriteTransactionState) getState()).prepareNode(getCurrentNode()
              .getParentKey());
      parentNode.setFirstChildKey(getCurrentNode().getNodeKey());
      parentNode.incrementChildCount();

      // Insert new node as first child.
    } else {

      // Create new first child node.
      setCurrentNode(((IWriteTransactionState) getState()).createNode(
          getCurrentNode().getNodeKey(),
          IConstants.NULL_KEY,
          IConstants.NULL_KEY,
          IConstants.NULL_KEY,
          kind,
          ((IWriteTransactionState) getState()).createNameKey(localPart),
          ((IWriteTransactionState) getState()).createNameKey(uri),
          ((IWriteTransactionState) getState()).createNameKey(prefix),
          value));

      // Change parent node.
      final Node parentNode =
          ((IWriteTransactionState) getState()).prepareNode(getCurrentNode()
              .getParentKey());
      parentNode.setFirstChildKey(getCurrentNode().getNodeKey());
      parentNode.incrementChildCount();

    }

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long insertTextAsFirstChild(final byte[] value) throws Exception {
    return insertFirstChild(IConstants.TEXT, "", "", "", value);
  }

  /**
   * {@inheritDoc}
   */
  public final long insertRightSibling(
      final int kind,
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) throws Exception {

    assertIsSelected();

    if (getCurrentNode().getNodeKey() == IConstants.ROOT_KEY) {
      throw new IllegalStateException("Root node can not have siblings.");
    }

    // Create new right sibling node.
    setCurrentNode(((IWriteTransactionState) getState()).createNode(
        getCurrentNode().getParentKey(),
        IConstants.NULL_KEY,
        getCurrentNode().getNodeKey(),
        getCurrentNode().getRightSiblingKey(),
        kind,
        ((IWriteTransactionState) getState()).createNameKey(localPart),
        ((IWriteTransactionState) getState()).createNameKey(uri),
        ((IWriteTransactionState) getState()).createNameKey(prefix),
        value));

    // Adapt parent node.
    final Node parentNode =
        ((IWriteTransactionState) getState()).prepareNode(getCurrentNode()
            .getParentKey());
    parentNode.incrementChildCount();

    // Adapt left sibling node.
    final Node leftSiblingNode =
        ((IWriteTransactionState) getState()).prepareNode(getCurrentNode()
            .getLeftSiblingKey());
    leftSiblingNode.setRightSiblingKey(getCurrentNode().getNodeKey());

    // Adapt right sibling node.
    if (getCurrentNode().getRightSiblingKey() != IConstants.NULL_KEY) {
      final Node rightSiblingNode =
          ((IWriteTransactionState) getState()).prepareNode(getCurrentNode()
              .getRightSiblingKey());
      rightSiblingNode.setLeftSiblingKey(getCurrentNode().getNodeKey());
    }

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long insertTextAsRightSibling(final byte[] value)
      throws Exception {
    return insertRightSibling(IConstants.TEXT, "", "", "", value);
  }

  /**
   * {@inheritDoc}
   */
  public final void insertAttribute(
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) throws Exception {
    assertIsSelected();
    setCurrentNode(((IWriteTransactionState) getState())
        .prepareNode(getCurrentNode().getNodeKey()));
    ((Node) getCurrentNode()).insertAttribute(((IWriteTransactionState) getState())
        .createNameKey(localPart), ((IWriteTransactionState) getState())
        .createNameKey(uri), ((IWriteTransactionState) getState())
        .createNameKey(prefix), value);
  }

  /**
   * {@inheritDoc}
   */
  public final void insertNamespace(final String uri, final String prefix)
      throws Exception {
    assertIsSelected();
    setCurrentNode(((IWriteTransactionState) getState())
        .prepareNode(getCurrentNode().getNodeKey()));
    ((Node) getCurrentNode()).insertNamespace(((IWriteTransactionState) getState())
        .createNameKey(uri), ((IWriteTransactionState) getState())
        .createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void remove() throws Exception {
    assertIsSelected();
    if (getCurrentNode().getChildCount() > 0) {
      throw new IllegalStateException("INode "
          + getCurrentNode().getNodeKey()
          + " has "
          + getCurrentNode().getChildCount()
          + " child(ren) and can not be removed.");
    }

    if (getCurrentNode().getNodeKey() == IConstants.ROOT_KEY) {
      throw new IllegalStateException("Root node can not be removed.");
    }

    // Remember left and right sibling keys.
    final long parentKey = getCurrentNode().getParentKey();
    final long nodeKey = getCurrentNode().getNodeKey();
    final long leftSiblingNodeKey = getCurrentNode().getLeftSiblingKey();
    final long rightSiblingNodeKey = getCurrentNode().getRightSiblingKey();

    // Remove old node.
    ((IWriteTransactionState) getState()).removeNode(nodeKey);

    // Get and adapt parent node.
    setCurrentNode(((IWriteTransactionState) getState()).prepareNode(parentKey));
    ((Node) getCurrentNode()).decrementChildCount();
    ((Node) getCurrentNode()).setFirstChildKey(rightSiblingNodeKey);

    // Adapt left sibling node if there is one.
    if (leftSiblingNodeKey != IConstants.NULL_KEY) {
      final Node leftSiblingNode =
          ((IWriteTransactionState) getState()).prepareNode(leftSiblingNodeKey);
      leftSiblingNode.setRightSiblingKey(rightSiblingNodeKey);
    }

    // Adapt right sibling node if there is one.
    if (rightSiblingNodeKey != IConstants.NULL_KEY) {
      final Node rightSiblingNode =
          ((IWriteTransactionState) getState()).prepareNode(rightSiblingNodeKey);
      rightSiblingNode.setLeftSiblingKey(leftSiblingNodeKey);
    }

  }

  /**
   * {@inheritDoc}
   */
  public final void setAttribute(
      final int index,
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) throws Exception {
    assertIsSelected();
    setCurrentNode(((IWriteTransactionState) getState())
        .prepareNode(getCurrentNode().getNodeKey()));
    ((Node) getCurrentNode()).setAttribute(index, ((IWriteTransactionState) getState())
        .createNameKey(localPart), ((IWriteTransactionState) getState())
        .createNameKey(uri), ((IWriteTransactionState) getState())
        .createNameKey(prefix), value);
  }

  /**
   * {@inheritDoc}
   */
  public final void setNamespace(
      final int index,
      final String uri,
      final String prefix) throws Exception {
    assertIsSelected();
    setCurrentNode(((IWriteTransactionState) getState())
        .prepareNode(getCurrentNode().getNodeKey()));
    ((Node) getCurrentNode()).setNamespace(index, ((IWriteTransactionState) getState())
        .createNameKey(uri), ((IWriteTransactionState) getState())
        .createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void setLocalPart(final String localPart) throws Exception {
    assertIsSelected();
    setCurrentNode(((IWriteTransactionState) getState())
        .prepareNode(getCurrentNode().getNodeKey()));
    ((Node) getCurrentNode()).setLocalPartKey(((IWriteTransactionState) getState())
        .createNameKey(localPart));
  }

  /**
   * {@inheritDoc}
   */
  public final void setURI(final String uri) throws Exception {
    assertIsSelected();
    setCurrentNode(((IWriteTransactionState) getState())
        .prepareNode(getCurrentNode().getNodeKey()));
    ((Node) getCurrentNode()).setURIKey(((IWriteTransactionState) getState())
        .createNameKey(uri));
  }

  /**
   * {@inheritDoc}
   */
  public void setPrefix(final String prefix) throws Exception {
    assertIsSelected();
    setCurrentNode(((IWriteTransactionState) getState())
        .prepareNode(getCurrentNode().getNodeKey()));
    ((Node) getCurrentNode()).setPrefixKey(((IWriteTransactionState) getState())
        .createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void setValue(final byte[] value) throws Exception {
    assertIsSelected();
    setCurrentNode(((IWriteTransactionState) getState())
        .prepareNode(getCurrentNode().getNodeKey()));
    ((Node) getCurrentNode()).setValue(value);
  }

}
