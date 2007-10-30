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

import java.io.IOException;

import org.treetank.api.IConstants;
import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.AbstractNode;
import org.treetank.pagelayer.UberPage;

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
   * @param sessionState State of the session.
   * @param transactionState State of this transaction.
   */
  protected WriteTransaction(
      final SessionState sessionState,
      final WriteTransactionState transactionState) {
    super(sessionState, transactionState);
  }

  /**
   * {@inheritDoc}
   */
  public final long insertElementAsFirstChild(
      final String localPart,
      final String uri,
      final String prefix) {

    assertNotClosed();
    assertIsSelected();

    setCurrentNode(((WriteTransactionState) getTransactionState())
        .createElementNode(
            getCurrentNode().getNodeKey(),
            IConstants.NULL_KEY,
            IConstants.NULL_KEY,
            getCurrentNode().getFirstChildKey(),
            ((WriteTransactionState) getTransactionState())
                .createNameKey(localPart),
            ((WriteTransactionState) getTransactionState()).createNameKey(uri),
            ((WriteTransactionState) getTransactionState())
                .createNameKey(prefix)));

    updateParentAfterInsert(true);

    if (getCurrentNode().getChildCount() > 0) {
      updateRightSibling();
    }

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long insertTextAsFirstChild(final byte[] value) {

    assertNotClosed();
    assertIsSelected();

    setCurrentNode(((WriteTransactionState) getTransactionState())
        .createTextNode(
            getCurrentNode().getNodeKey(),
            IConstants.NULL_KEY,
            IConstants.NULL_KEY,
            value));

    updateParentAfterInsert(true);

    if (getCurrentNode().getChildCount() > 0) {
      updateRightSibling();
    }

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long insertFullTextAsFirstChild(final int localPartKey) {

    assertNotClosed();
    assertIsSelected();

    setCurrentNode(((WriteTransactionState) getTransactionState())
        .createFullTextNode(
            getCurrentNode().getNodeKey(),
            IConstants.NULL_KEY,
            IConstants.NULL_KEY,
            getCurrentNode().getFirstChildKey(),
            localPartKey));

    updateParentAfterInsert(true);

    if (getCurrentNode().getChildCount() > 0) {
      updateRightSibling();
    }

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long insertElementAsRightSibling(
      final String localPart,
      final String uri,
      final String prefix) {

    assertNotClosed();
    assertIsSelected();

    if (getCurrentNode().getNodeKey() == IConstants.DOCUMENT_KEY) {
      throw new IllegalStateException("Root node can not have siblings.");
    }

    // Create new right sibling node.
    setCurrentNode(((WriteTransactionState) getTransactionState())
        .createElementNode(
            getCurrentNode().getParentKey(),
            IConstants.NULL_KEY,
            getCurrentNode().getNodeKey(),
            getCurrentNode().getRightSiblingKey(),
            ((WriteTransactionState) getTransactionState())
                .createNameKey(localPart),
            ((WriteTransactionState) getTransactionState()).createNameKey(uri),
            ((WriteTransactionState) getTransactionState())
                .createNameKey(prefix)));

    updateParentAfterInsert(false);
    updateLeftSibling();
    updateRightSibling();

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long insertTextAsRightSibling(final byte[] value) {

    assertNotClosed();
    assertIsSelected();

    if (getCurrentNode().getNodeKey() == IConstants.DOCUMENT_KEY) {
      throw new IllegalStateException("Root node can not have siblings.");
    }

    // Create new right sibling node.
    setCurrentNode(((WriteTransactionState) getTransactionState())
        .createTextNode(getCurrentNode().getParentKey(), getCurrentNode()
            .getNodeKey(), getCurrentNode().getRightSiblingKey(), value));

    updateParentAfterInsert(false);
    updateLeftSibling();
    updateRightSibling();

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long insertFullTextAsRightSibling(final int localPartKey) {

    assertNotClosed();
    assertIsSelected();

    if (getCurrentNode().getNodeKey() == IConstants.DOCUMENT_KEY) {
      throw new IllegalStateException("Root node can not have siblings.");
    }

    // Create new right sibling node.
    setCurrentNode(((WriteTransactionState) getTransactionState())
        .createFullTextNode(
            getCurrentNode().getParentKey(),
            IConstants.NULL_KEY,
            getCurrentNode().getNodeKey(),
            getCurrentNode().getRightSiblingKey(),
            localPartKey));

    updateParentAfterInsert(false);
    updateLeftSibling();
    updateRightSibling();

    return getCurrentNode().getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final void insertAttribute(
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) {

    assertNotClosed();
    assertIsSelected();

    prepareCurrentNode().insertAttribute(
        ((WriteTransactionState) getTransactionState())
            .createNameKey(localPart),
        ((WriteTransactionState) getTransactionState()).createNameKey(uri),
        ((WriteTransactionState) getTransactionState()).createNameKey(prefix),
        value);
  }

  /**
   * {@inheritDoc}
   */
  public final void insertNamespace(final String uri, final String prefix) {

    assertNotClosed();
    assertIsSelected();

    prepareCurrentNode().insertNamespace(
        ((WriteTransactionState) getTransactionState()).createNameKey(uri),
        ((WriteTransactionState) getTransactionState()).createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void remove() {

    assertNotClosed();
    assertIsSelected();

    if (getCurrentNode().isDocument()) {
      throw new IllegalStateException("Document node can not be removed.");
    }

    // Remember all related nodes.
    AbstractNode node = (AbstractNode) getCurrentNode();
    AbstractNode parent = (AbstractNode) node.getParent(this);
    AbstractNode leftSibling = (AbstractNode) node.getLeftSibling(this);
    AbstractNode rightSibling = (AbstractNode) node.getRightSibling(this);

    // Remove old node.
    ((WriteTransactionState) getTransactionState()).removeNode(node
        .getNodeKey());

    // Adapt left sibling node if there is one.
    if (leftSibling != null) {
      leftSibling =
          ((WriteTransactionState) getTransactionState())
              .prepareNode(leftSibling.getNodeKey());
      if (rightSibling != null) {
        leftSibling.setRightSiblingKey(rightSibling.getNodeKey());
      } else {
        leftSibling.setRightSiblingKey(IConstants.NULL_KEY);
      }
    }

    // Adapt right sibling node if there is one.
    if (rightSibling != null) {
      rightSibling =
          ((WriteTransactionState) getTransactionState())
              .prepareNode(rightSibling.getNodeKey());
      if (leftSibling != null) {
        rightSibling.setLeftSiblingKey(leftSibling.getNodeKey());
      } else {
        rightSibling.setLeftSiblingKey(IConstants.NULL_KEY);
      }
    }

    // Adapt parent.
    parent =
        ((WriteTransactionState) getTransactionState()).prepareNode(parent
            .getNodeKey());
    parent.decrementChildCount();
    if (parent.getFirstChildKey() == node.getNodeKey()) {
      if (rightSibling != null) {
        parent.setFirstChildKey(rightSibling.getNodeKey());
      } else {
        parent.setFirstChildKey(IConstants.NULL_KEY);
      }
    }

    // Set current node.
    if (rightSibling != null) {
      setCurrentNode(rightSibling);
      return;
    }

    if (leftSibling != null) {
      setCurrentNode(leftSibling);
      return;
    }

    setCurrentNode(parent);

  }

  /**
   * {@inheritDoc}
   */
  public final void setAttribute(
      final int index,
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) {

    assertNotClosed();
    assertIsSelected();

    prepareCurrentNode().setAttribute(
        index,
        ((WriteTransactionState) getTransactionState())
            .createNameKey(localPart),
        ((WriteTransactionState) getTransactionState()).createNameKey(uri),
        ((WriteTransactionState) getTransactionState()).createNameKey(prefix),
        value);
  }

  /**
   * {@inheritDoc}
   */
  public final void setNamespace(
      final int index,
      final String uri,
      final String prefix) {

    assertNotClosed();
    assertIsSelected();

    prepareCurrentNode().setNamespace(
        index,
        ((WriteTransactionState) getTransactionState()).createNameKey(uri),
        ((WriteTransactionState) getTransactionState()).createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void setLocalPart(final String localPart) {

    assertNotClosed();
    assertIsSelected();

    prepareCurrentNode().setLocalPartKey(
        ((WriteTransactionState) getTransactionState())
            .createNameKey(localPart));
  }

  /**
   * {@inheritDoc}
   */
  public final void setURI(final String uri) {

    assertNotClosed();
    assertIsSelected();

    prepareCurrentNode().setURIKey(
        ((WriteTransactionState) getTransactionState()).createNameKey(uri));
  }

  /**
   * {@inheritDoc}
   */
  public void setPrefix(final String prefix) {

    assertNotClosed();
    assertIsSelected();

    prepareCurrentNode().setPrefixKey(
        ((WriteTransactionState) getTransactionState()).createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void setValue(final byte[] value) {

    assertNotClosed();
    assertIsSelected();

    prepareCurrentNode().setValue(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void close() {

    assertNotClosed();

    getTransactionState().close();
    getSessionState().closeWriteTransaction();
    setSessionState(null);
    setTransactionState(null);
    setCurrentNode(null);

    setClosed();
  }

  /**
   * {@inheritDoc}
   */
  public final void commit() throws IOException {

    assertNotClosed();

    // Commit uber page.
    final UberPage uberPage =
        ((WriteTransactionState) getTransactionState())
            .commit(getSessionState().getSessionConfiguration());

    // Remember succesfully committed uber page in session state.
    getSessionState().setLastCommittedUberPage(uberPage);

    // Reset internal transaction state to new uber page.
    setTransactionState(getSessionState().getWriteTransactionState());
  }

  /**
   * {@inheritDoc}
   */
  public final void abort() {

    assertNotClosed();

    // Reset internal transaction state to last committed uber page.
    setTransactionState(getSessionState().getWriteTransactionState());
  }

  private final AbstractNode prepareCurrentNode() {
    final AbstractNode modNode =
        ((WriteTransactionState) getTransactionState())
            .prepareNode(getCurrentNode().getNodeKey());
    setCurrentNode(modNode);

    return modNode;
  }

  private final void updateParentAfterInsert(final boolean updateFirstChild) {
    final AbstractNode parentNode =
        ((WriteTransactionState) getTransactionState())
            .prepareNode(getCurrentNode().getParentKey());
    parentNode.incrementChildCount();
    if (updateFirstChild) {
      parentNode.setFirstChildKey(getCurrentNode().getNodeKey());
    }
  }

  private final void updateRightSibling() {
    if (getCurrentNode().hasRightSibling()) {
      final AbstractNode rightSiblingNode =
          ((WriteTransactionState) getTransactionState())
              .prepareNode(getCurrentNode().getRightSiblingKey());
      rightSiblingNode.setLeftSiblingKey(getCurrentNode().getNodeKey());
    }
  }

  private final void updateLeftSibling() {
    final AbstractNode leftSiblingNode =
        ((WriteTransactionState) getTransactionState())
            .prepareNode(getCurrentNode().getLeftSiblingKey());
    leftSiblingNode.setRightSiblingKey(getCurrentNode().getNodeKey());
  }

}
