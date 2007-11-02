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
  private INode mCurrentNode;

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
  public final INode moveTo(final long nodeKey) {
    assertNotClosed();
    if (nodeKey != IConstants.NULL_KEY) {
      try {
        mCurrentNode = mTransactionState.getNode(nodeKey);
      } catch (Exception e) {
        mCurrentNode = null;
      }
    } else {
      mCurrentNode = null;
    }
    return mCurrentNode;
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveTo(final INode node) {
    return moveTo(node.getNodeKey());
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveToDocumentRoot() {
    return moveTo(IConstants.DOCUMENT_ROOT_KEY);
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveToFullTextRoot() {
    return moveTo(IConstants.FULLTEXT_ROOT_KEY);
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveToParent() {
    return moveTo(mCurrentNode.getParentKey());
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveToFirstChild() {
    return moveTo(mCurrentNode.getFirstChildKey());
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveToLeftSibling() {
    return moveTo(mCurrentNode.getLeftSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveToRightSibling() {
    return moveTo(mCurrentNode.getRightSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveToReference() {
    return moveTo(mCurrentNode.getReferenceKey());
  }

  /**
   * {@inheritDoc}
   */
  public final INode moveToAttribute(final int index) {
    assertNotClosed();
    mCurrentNode = mCurrentNode.getAttribute(index);
    return mCurrentNode;
  }

  /**
   * {@inheritDoc}
   */
  public final INode getNode() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode;
  }

  /**
   * {@inheritDoc}
   */
  public final long getNodeKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasReference() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.hasReference();
  }

  /**
   * {@inheritDoc}
   */
  public final long getReferenceKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getReferenceKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasParent() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.hasParent();
  }

  /**
   * {@inheritDoc}
   */
  public final long getParentKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getParentKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasFirstChild() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.hasFirstChild();
  }

  /**
   * {@inheritDoc}
   */
  public final long getFirstChildKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasLeftSibling() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.hasLeftSibling();
  }

  /**
   * {@inheritDoc}
   */
  public final long getLeftSiblingKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getLeftSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasRightSibling() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.hasRightSibling();
  }

  /**
   * {@inheritDoc}
   */
  public final long getRightSiblingKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getRightSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getChildCount() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getChildCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeCount() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getAttributeCount();
  }

  /**
   * {@inheritDoc}
   */
  public INode getAttribute(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index);
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespaceCount() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getNamespaceCount();
  }

  /**
   * {@inheritDoc}
   */
  public INode getNamespace(final int index) {
    assertNotClosed();
    return mCurrentNode.getNamespace(index);
  }

  /**
   * {@inheritDoc}
   */
  public final int getFullTextAttributeCount() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getFullTextAttributeCount();
  }

  /**
   * {@inheritDoc}
   */
  public INode getFullTextAttribute(final long tokenKey) {
    assertNotClosed();
    return mCurrentNode.getFullTextAttributeByTokenKey(tokenKey);
  }

  /**
   * {@inheritDoc}
   */
  public final int getKind() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getKind();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isDocumentRoot() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.isDocumentRoot();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isElement() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.isElement();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isAttribute() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.isAttribute();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isText() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.isText();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isFullText() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.isFullText();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isFullTextRoot() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.isFullTextRoot();
  }

  /**
   * {@inheritDoc}
   */
  public final int getLocalPartKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getLocalPartKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getLocalPart() {
    assertNotClosed();
    assertIsSelected();
    return mTransactionState.getName(mCurrentNode.getLocalPartKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getURIKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getURI() {
    assertNotClosed();
    assertIsSelected();
    return mTransactionState.getName(mCurrentNode.getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getPrefixKey() {
    assertNotClosed();
    assertIsSelected();
    return mCurrentNode.getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getPrefix() {
    assertNotClosed();
    assertIsSelected();
    return mTransactionState.getName(mCurrentNode.getPrefixKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getValue() {
    assertNotClosed();
    assertIsSelected();
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
    assertNotClosed();

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
  protected final INode getCurrentNode() {
    return mCurrentNode;
  }

  /**
   * Setter for superclasses.
   * 
   * @param currentNode The current node to set.
   */
  protected final void setCurrentNode(final INode currentNode) {
    mCurrentNode = currentNode;
  }

}
