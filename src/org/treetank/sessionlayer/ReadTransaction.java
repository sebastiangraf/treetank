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
import org.treetank.api.IReadTransaction;
import org.treetank.nodelayer.AbstractNode;

/**
 * <h1>ReadTransaction</h1>
 * 
 * <p>
 * Read-only transaction wiht single-threaded cursor semantics. Each
 * read-only transaction works on a given revision key.
 * </p>
 */
public class ReadTransaction implements IReadTransaction {

  /** Session state this write transaction is bound to. */
  private SessionState mSessionState;

  /** State of transaction including all cached stuff. */
  private ReadTransactionState mTransactionState;

  /** Strong reference to currently selected node. */
  private AbstractNode mCurrentNode;

  /** Tracks whether the transaction is closed. */
  private boolean mClosed;

  /**
   * Constructor.
   * 
   * @param sessionState Session state to work with.
   * @param transactionState Transaction state to work with.
   */
  protected ReadTransaction(
      final SessionState sessionState,
      final ReadTransactionState transactionState) {
    mSessionState = sessionState;
    mTransactionState = transactionState;
    mCurrentNode = null;
    mClosed = false;
    moveToDocumentRoot();
  }

  /**
   * {@inheritDoc}
   */
  public final long getRevisionNumber() {
    assertNotClosed();
    return mTransactionState.getRevisionRootPage().getRevisionNumber();
  }

  /**
   * {@inheritDoc}
   */
  public final long getRevisionSize() {
    assertNotClosed();
    return mTransactionState.getRevisionRootPage().getRevisionSize();
  }

  /**
   * {@inheritDoc}
   */
  public final long getRevisionTimestamp() {
    assertNotClosed();
    return mTransactionState.getRevisionRootPage().getRevisionTimestamp();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isSelected() {
    assertNotClosed();
    return (mCurrentNode != null);
  }

  /**
   * {@inheritDoc}
   */
  public final long moveTo(final long nodeKey) {
    assertNotClosed();
    if (nodeKey != IConstants.NULL_KEY) {
      mCurrentNode = mTransactionState.getNode(nodeKey);
      if (mCurrentNode != null) {
        return nodeKey;
      } else {
        return IConstants.NULL_KEY;
      }
    } else {
      mCurrentNode = null;
      return IConstants.NULL_KEY;
    }
  }

  /**
   * {@inheritDoc}
   */
  public final long moveToDocumentRoot() {
    assertNotClosed();
    return moveTo(IConstants.DOCUMENT_ROOT_KEY);
  }

  /**
   * {@inheritDoc}
   */
  public final long moveToFullTextRoot() {
    assertNotClosed();
    return moveTo(IConstants.FULLTEXT_ROOT_KEY);
  }

  /**
   * {@inheritDoc}
   */
  public final long moveToParent() {
    assertNotClosedAndSelected();
    return moveTo(mCurrentNode.getParentKey());
  }

  /**
   * {@inheritDoc}
   */
  public final long moveToFirstChild() {
    assertNotClosedAndSelected();
    return moveTo(mCurrentNode.getFirstChildKey());
  }

  /**
   * {@inheritDoc}
   */
  public final long moveToLeftSibling() {
    assertNotClosedAndSelected();
    return moveTo(mCurrentNode.getLeftSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final long moveToRightSibling() {
    assertNotClosedAndSelected();
    return moveTo(mCurrentNode.getRightSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final long moveToAttribute(final int index) {
    assertNotClosedAndSelected();
    mCurrentNode = mCurrentNode.getAttribute(index);
    return mCurrentNode.getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getNodeKey() {
    assertNotClosedAndSelected();
    return mCurrentNode.getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasParent() {
    assertNotClosedAndSelected();
    return mCurrentNode.hasParent();
  }

  /**
   * {@inheritDoc}
   */
  public final long getParentKey() {
    assertNotClosedAndSelected();
    return mCurrentNode.getParentKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasFirstChild() {
    assertNotClosedAndSelected();
    return mCurrentNode.hasFirstChild();
  }

  /**
   * {@inheritDoc}
   */
  public final long getFirstChildKey() {
    assertNotClosedAndSelected();
    return mCurrentNode.getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasLeftSibling() {
    assertNotClosedAndSelected();
    return mCurrentNode.hasLeftSibling();
  }

  /**
   * {@inheritDoc}
   */
  public final long getLeftSiblingKey() {
    assertNotClosedAndSelected();
    return mCurrentNode.getLeftSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasRightSibling() {
    assertNotClosedAndSelected();
    return mCurrentNode.hasRightSibling();
  }

  /**
   * {@inheritDoc}
   */
  public final long getRightSiblingKey() {
    assertNotClosedAndSelected();
    return mCurrentNode.getRightSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getChildCount() {
    assertNotClosedAndSelected();
    return mCurrentNode.getChildCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeCount() {
    assertNotClosedAndSelected();
    return mCurrentNode.getAttributeCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeLocalPartKey(final int index) {
    assertNotClosedAndSelected();
    return mCurrentNode.getAttribute(index).getLocalPartKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeLocalPart(final int index) {
    assertNotClosedAndSelected();
    return nameForKey(mCurrentNode.getAttribute(index).getLocalPartKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributePrefixKey(final int index) {
    assertNotClosedAndSelected();
    return mCurrentNode.getAttribute(index).getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributePrefix(final int index) {
    assertNotClosedAndSelected();
    return nameForKey(mCurrentNode.getAttribute(index).getPrefixKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeURIKey(final int index) {
    assertNotClosedAndSelected();
    return mCurrentNode.getAttribute(index).getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeURI(final int index) {
    assertNotClosedAndSelected();
    return nameForKey(mCurrentNode.getAttribute(index).getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getAttributeValue(final int index) {
    assertNotClosedAndSelected();
    return mCurrentNode.getAttribute(index).getValue();
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespaceCount() {
    assertNotClosedAndSelected();
    return mCurrentNode.getNamespaceCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespacePrefixKey(final int index) {
    assertNotClosedAndSelected();
    return mCurrentNode.getNamespace(index).getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getNamespacePrefix(final int index) {
    assertNotClosedAndSelected();
    return nameForKey(mCurrentNode.getNamespace(index).getPrefixKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespaceURIKey(final int index) {
    assertNotClosedAndSelected();
    return mCurrentNode.getNamespace(index).getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getNamespaceURI(final int index) {
    assertNotClosedAndSelected();
    return nameForKey(mCurrentNode.getNamespace(index).getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getKind() {
    assertNotClosedAndSelected();
    return mCurrentNode.getKind();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isDocumentRoot() {
    assertNotClosedAndSelected();
    return mCurrentNode.isDocumentRoot();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isElement() {
    assertNotClosedAndSelected();
    return mCurrentNode.isElement();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isAttribute() {
    assertNotClosedAndSelected();
    return mCurrentNode.isAttribute();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isText() {
    assertNotClosedAndSelected();
    return mCurrentNode.isText();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isFullText() {
    assertNotClosedAndSelected();
    return mCurrentNode.isFullText();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isFullTextLeaf() {
    assertNotClosedAndSelected();
    return mCurrentNode.isFullTextLeaf();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isFullTextRoot() {
    assertNotClosedAndSelected();
    return mCurrentNode.isFullTextRoot();
  }

  /**
   * {@inheritDoc}
   */
  public final int getLocalPartKey() {
    assertNotClosedAndSelected();
    return mCurrentNode.getLocalPartKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getLocalPart() {
    assertNotClosedAndSelected();
    return mTransactionState.getName(mCurrentNode.getLocalPartKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getURIKey() {
    assertNotClosedAndSelected();
    return mCurrentNode.getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getURI() {
    assertNotClosedAndSelected();
    return mTransactionState.getName(mCurrentNode.getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getPrefixKey() {
    assertNotClosedAndSelected();
    return mCurrentNode.getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getPrefix() {
    assertNotClosedAndSelected();
    return mTransactionState.getName(mCurrentNode.getPrefixKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getValue() {
    assertNotClosedAndSelected();
    return mCurrentNode.getValue();
  }

  /**
   * {@inheritDoc}
   */
  public final int keyForName(final String name) {
    assertNotClosed();
    return name.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  public final String nameForKey(final int key) {
    assertNotClosed();
    return mTransactionState.getName(key);
  }

  /**
   * {@inheritDoc}
   */
  public void close() {
    if (!mClosed) {
      // Close own state.
      mTransactionState.close();

      // Callback on session to make sure everything is cleaned up.
      mSessionState.closeReadTransaction();

      // Immediately release all references.
      mSessionState = null;
      mTransactionState = null;
      mCurrentNode = null;

      mClosed = true;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    assertNotClosed();
    String localPart = "";
    try {
      localPart = getLocalPart();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return "Node "
        + this.getNodeKey()
        + "\nwith name: "
        + localPart
        + "\nand value:"
        + getValue();
  }

  /**
   * Test both whether transaction is closed and a node is selected.
   */
  protected final void assertNotClosedAndSelected() {
    if (mClosed) {
      throw new IllegalStateException("Transaction is already closed.");
    }
    if (mCurrentNode == null) {
      throw new IllegalStateException("No node selected.");
    }
  }

  /**
   * Assert that a node is selected.
   */
  protected final void assertIsSelected() {
    if (mCurrentNode == null) {
      throw new IllegalStateException("No node selected.");
    }
  }

  /**
   * Set state to closed.
   */
  protected final void setClosed() {
    mClosed = true;
  }

  /**
   * Is the transaction closed?
   * 
   * @return True if the transaction was closed.
   */
  protected final boolean isClosed() {
    return mClosed;
  }

  /**
   * Make sure that the session is not yet closed when calling this method.
   */
  protected final void assertNotClosed() {
    if (mClosed) {
      throw new IllegalStateException("Transaction is already closed.");
    }
  }

  /**
   * Getter for superclasses.
   * 
   * @return The state of this transaction.
   */
  protected final ReadTransactionState getTransactionState() {
    return mTransactionState;
  }

  /**
   * Replace the state of the transaction.
   * 
   * @param transactionState State of transaction.
   */
  protected final void setTransactionState(
      final ReadTransactionState transactionState) {
    mTransactionState = transactionState;
  }

  /**
   * Getter for superclasses.
   * 
   * @return The session state.
   */
  protected final SessionState getSessionState() {
    return mSessionState;
  }

  /**
   * Set session state.
   * 
   * @param sessionState Session state to set.
   */
  protected final void setSessionState(final SessionState sessionState) {
    mSessionState = sessionState;
  }

  /**
   * Getter for superclasses.
   * 
   * @return The current node.
   */
  protected final AbstractNode getCurrentNode() {
    return mCurrentNode;
  }

  /**
   * Setter for superclasses.
   * 
   * @param currentNode The current node to set.
   */
  protected final void setCurrentNode(final AbstractNode currentNode) {
    mCurrentNode = currentNode;
  }

}
