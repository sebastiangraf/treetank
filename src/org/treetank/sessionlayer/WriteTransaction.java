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
  public final long index(final String token, final long nodeKey) {

    // Make sure we always operate from the full text root node.
    moveToFullTextRoot();

    // Add characters to inverted index consisting of a prefix tree.
    long tokenKey = IConstants.NULL_KEY;
    for (final char character : token.toCharArray()) {
      if (hasFirstChild()) {
        moveToFirstChild();
        while (isFullText()
            && (getLocalPartKey() != character)
            && hasRightSibling()) {
          moveToRightSibling();
        }
        if (getLocalPartKey() != character) {
          moveToParent();
          tokenKey = insertFullTextAsFirstChild(character);
        } else {
          tokenKey = getNodeKey();
        }
      } else {
        tokenKey = insertFullTextAsFirstChild(character);
      }
    }

    // Add key into list of keys containing the token.
    if (hasFirstChild()) {
      // Make sure that full text nodes come first.
      moveToFirstChild();
      while (isFullText() && hasRightSibling()) {
        moveToRightSibling();
      }
      insertFullTextLeafAsRightSibling(nodeKey);
    } else {
      insertFullTextLeafAsFirstChild(nodeKey);
    }

    return tokenKey;

  }

  /**
   * {@inheritDoc}
   */
  public final long insertElementAsFirstChild(
      final String localPart,
      final String uri,
      final String prefix) {
    return insertFirstChild(((WriteTransactionState) getTransactionState())
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
  }

  /**
   * {@inheritDoc}
   */
  public final long insertTextAsFirstChild(final byte[] value) {
    return insertFirstChild(((WriteTransactionState) getTransactionState())
        .createTextNode(
            getCurrentNode().getNodeKey(),
            IConstants.NULL_KEY,
            getCurrentNode().getFirstChildKey(),
            value));
  }

  /**
   * {@inheritDoc}
   */
  public final long insertFullTextAsFirstChild(final int localPartKey) {
    return insertFirstChild(((WriteTransactionState) getTransactionState())
        .createFullTextNode(
            getCurrentNode().getNodeKey(),
            IConstants.NULL_KEY,
            IConstants.NULL_KEY,
            getCurrentNode().getFirstChildKey(),
            localPartKey));
  }

  /**
   * {@inheritDoc}
   */
  public final long insertFullTextLeafAsFirstChild(final long firstChildKey) {
    return insertFirstChild(((WriteTransactionState) getTransactionState())
        .createFullTextLeafNode(
            getCurrentNode().getNodeKey(),
            firstChildKey,
            IConstants.NULL_KEY,
            getCurrentNode().getFirstChildKey()));
  }

  /**
   * {@inheritDoc}
   */
  public final long insertElementAsRightSibling(
      final String localPart,
      final String uri,
      final String prefix) {
    return insertRightSibling(((WriteTransactionState) getTransactionState())
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
  }

  /**
   * {@inheritDoc}
   */
  public final long insertTextAsRightSibling(final byte[] value) {
    return insertRightSibling(((WriteTransactionState) getTransactionState())
        .createTextNode(getCurrentNode().getParentKey(), getCurrentNode()
            .getNodeKey(), getCurrentNode().getRightSiblingKey(), value));
  }

  /**
   * {@inheritDoc}
   */
  public final long insertFullTextAsRightSibling(final int localPartKey) {
    return insertRightSibling(((WriteTransactionState) getTransactionState())
        .createFullTextNode(
            getCurrentNode().getParentKey(),
            IConstants.NULL_KEY,
            getCurrentNode().getNodeKey(),
            getCurrentNode().getRightSiblingKey(),
            localPartKey));
  }

  /**
   * {@inheritDoc}
   */
  public final long insertFullTextLeafAsRightSibling(final long firstChildKey) {
    return insertRightSibling(((WriteTransactionState) getTransactionState())
        .createFullTextLeafNode(
            getCurrentNode().getParentKey(),
            firstChildKey,
            getCurrentNode().getNodeKey(),
            getCurrentNode().getRightSiblingKey()));
  }

  /**
   * {@inheritDoc}
   */
  public final void insertAttribute(
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) {

    assertNotClosedAndSelected();

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

    assertNotClosedAndSelected();

    prepareCurrentNode().insertNamespace(
        ((WriteTransactionState) getTransactionState()).createNameKey(uri),
        ((WriteTransactionState) getTransactionState()).createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void remove() {

    assertNotClosedAndSelected();

    if (getCurrentNode().isDocumentRoot()) {
      throw new IllegalStateException("Document node can not be removed.");
    }

    // Remember all related nodes.
    AbstractNode node = null;
    AbstractNode leftSibling = null;
    AbstractNode rightSibling = null;
    AbstractNode parent = null;

    node = (AbstractNode) getCurrentNode();
    if (hasLeftSibling()) {
      moveToLeftSibling();
      leftSibling = (AbstractNode) getCurrentNode();
      moveToRightSibling();
    }
    if (hasRightSibling()) {
      moveToRightSibling();
      rightSibling = (AbstractNode) getCurrentNode();
    }
    moveToParent();
    parent = (AbstractNode) getCurrentNode();

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

    assertNotClosedAndSelected();

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

    assertNotClosedAndSelected();

    prepareCurrentNode().setNamespace(
        index,
        ((WriteTransactionState) getTransactionState()).createNameKey(uri),
        ((WriteTransactionState) getTransactionState()).createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void setLocalPart(final String localPart) {

    assertNotClosedAndSelected();

    prepareCurrentNode().setLocalPartKey(
        ((WriteTransactionState) getTransactionState())
            .createNameKey(localPart));
  }

  /**
   * {@inheritDoc}
   */
  public final void setURI(final String uri) {

    assertNotClosedAndSelected();

    prepareCurrentNode().setURIKey(
        ((WriteTransactionState) getTransactionState()).createNameKey(uri));
  }

  /**
   * {@inheritDoc}
   */
  public void setPrefix(final String prefix) {

    assertNotClosedAndSelected();

    prepareCurrentNode().setPrefixKey(
        ((WriteTransactionState) getTransactionState()).createNameKey(prefix));
  }

  /**
   * {@inheritDoc}
   */
  public final void setValue(final byte[] value) {

    assertNotClosedAndSelected();

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

  private final long insertFirstChild(final AbstractNode node) {

    assertNotClosedAndSelected();

    setCurrentNode(node);

    updateParentAfterInsert(true);
    updateRightSibling();

    return node.getNodeKey();
  }

  private final long insertRightSibling(final AbstractNode node) {

    assertNotClosedAndSelected();

    if (getCurrentNode().getNodeKey() == IConstants.DOCUMENT_ROOT_KEY) {
      throw new IllegalStateException("Root node can not have siblings.");
    }

    setCurrentNode(node);

    updateParentAfterInsert(false);
    updateLeftSibling();
    updateRightSibling();

    return node.getNodeKey();
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
