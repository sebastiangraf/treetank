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

package org.treetank.sessionlayer;

import org.treetank.api.IItem;
import org.treetank.api.IItemList;
import org.treetank.api.IReadTransaction;
import org.treetank.nodelayer.AbstractNode;
import org.treetank.utils.TypedValue;

/**
 * <h1>ReadTransaction</h1>
 * 
 * <p>
 * Read-only transaction wiht single-threaded cursor semantics. Each
 * read-only transaction works on a given revision key.
 * </p>
 */
public class ReadTransaction implements IReadTransaction {

  /** ID of transaction. */
  private final long mTransactionID;

  /** Session state this write transaction is bound to. */
  private SessionState mSessionState;

  /** State of transaction including all cached stuff. */
  private ReadTransactionState mTransactionState;

  /** Strong reference to currently selected node. */
  private IItem mCurrentNode;

  /** Is the cursor currently pointing to an attribute? */
  private boolean mIsAttribute;

  /** Tracks whether the transaction is closed. */
  private boolean mClosed;

  /**
   * Constructor.
   * 
   * @param transactionID ID of transaction.
   * @param sessionState Session state to work with.
   * @param transactionState Transaction state to work with.
   */
  protected ReadTransaction(
      final long transactionID,
      final SessionState sessionState,
      final ReadTransactionState transactionState) {
    mTransactionID = transactionID;
    mSessionState = sessionState;
    mTransactionState = transactionState;
    mCurrentNode = getTransactionState().getNode(DOCUMENT_ROOT_KEY);
    mIsAttribute = false;
    mClosed = false;
  }

  /**
   * {@inheritDoc}
   */
  public final long getTransactionID() {
    return mTransactionID;
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
  public final long getRevisionTimestamp() {
    assertNotClosed();
    return mTransactionState.getRevisionRootPage().getRevisionTimestamp();
  }

  /**
   * {@inheritDoc}
   */
  public final long getNodeCount() {
    assertNotClosed();
    return mTransactionState.getRevisionRootPage().getRevisionSize();
  }

  /**
   * {@inheritDoc}
   */
  public boolean moveTo(final long nodeKey) {
    assertNotClosed();
    // Do nothing if this node is already selected.
    if ((mCurrentNode.getNodeKey() == nodeKey) && !mIsAttribute) {
      return true;
    }
    if (nodeKey != NULL_NODE_KEY) {
      mIsAttribute = false;
      // Remember old node and fetch new one.
      final IItem oldNode = mCurrentNode;
      try {
        mCurrentNode = mTransactionState.getNode(nodeKey);
      } catch (Exception e) {
        mCurrentNode = null;
      }
      if (mCurrentNode != null) {
        return true;
      } else {
        mCurrentNode = oldNode;
        return false;
      }
    } else {
      return false;
    }
  }

  /**
    * {@inheritDoc}
    */
  public final boolean moveToDocumentRoot() {
    return moveTo(DOCUMENT_ROOT_KEY);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToParent() {
    return moveTo(mCurrentNode.getParentKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToFirstChild() {
    return moveTo(mCurrentNode.getFirstChildKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToLeftSibling() {
    return moveTo(mCurrentNode.getLeftSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToRightSibling() {
    return moveTo(mCurrentNode.getRightSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToAttribute(final int index) {
    final AbstractNode attributeNode = mCurrentNode.getAttribute(index);
    if (attributeNode != null) {
      mCurrentNode = attributeNode;
      mIsAttribute = true;
      return true;
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  public final long getNodeKey() {
    assertNotClosed();
    return mCurrentNode.getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasParent() {
    assertNotClosed();
    return mCurrentNode.hasParent();
  }

  /**
   * {@inheritDoc}
   */
  public final long getParentKey() {
    assertNotClosed();
    return mCurrentNode.getParentKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasFirstChild() {
    assertNotClosed();
    return mCurrentNode.hasFirstChild();
  }

  /**
   * {@inheritDoc}
   */
  public final long getFirstChildKey() {
    assertNotClosed();
    return mCurrentNode.getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasLeftSibling() {
    assertNotClosed();
    return mCurrentNode.hasLeftSibling();
  }

  /**
   * {@inheritDoc}
   */
  public final long getLeftSiblingKey() {
    assertNotClosed();
    return mCurrentNode.getLeftSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasRightSibling() {
    assertNotClosed();
    return mCurrentNode.hasRightSibling();
  }

  /**
   * {@inheritDoc}
   */
  public final long getRightSiblingKey() {
    assertNotClosed();
    return mCurrentNode.getRightSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getChildCount() {
    assertNotClosed();
    return mCurrentNode.getChildCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeCount() {
    assertNotClosed();
    return mCurrentNode.getAttributeCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeNameKey(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index).getNameKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeName(final int index) {
    assertNotClosed();
    return nameForKey(mCurrentNode.getAttribute(index).getNameKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getAttributeRawName(final int index) {
    assertNotClosed();
    return rawNameForKey(mCurrentNode.getAttribute(index).getNameKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeURIKey(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index).getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeURI(final int index) {
    assertNotClosed();
    return nameForKey(mCurrentNode.getAttribute(index).getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeTypeKey(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index).getTypeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeType(final int index) {
    assertNotClosed();
    return mTransactionState.getName(mCurrentNode
        .getAttribute(index)
        .getTypeKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getAttributeRawType(final int index) {
    assertNotClosed();
    return mTransactionState.getRawName(mCurrentNode
        .getAttribute(index)
        .getTypeKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getAttributeRawValue(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index).getRawValue();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeValue(final int index) {
    assertNotClosed();
    return TypedValue.parseString(mCurrentNode
        .getAttribute(index)
        .getRawValue());
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespaceCount() {
    assertNotClosed();
    return mCurrentNode.getNamespaceCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespacePrefixKey(final int index) {
    assertNotClosed();
    return mCurrentNode.getNamespace(index).getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getNamespacePrefix(final int index) {
    assertNotClosed();
    return nameForKey(mCurrentNode.getNamespace(index).getPrefixKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespaceURIKey(final int index) {
    assertNotClosed();
    return mCurrentNode.getNamespace(index).getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getNamespaceURI(final int index) {
    assertNotClosed();
    return nameForKey(mCurrentNode.getNamespace(index).getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getKind() {
    assertNotClosed();
    return mCurrentNode.getKind();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isDocumentRootKind() {
    assertNotClosed();
    return mCurrentNode.isDocumentRoot();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isElementKind() {
    assertNotClosed();
    return mCurrentNode.isElement();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isAttributeKind() {
    assertNotClosed();
    return mCurrentNode.isAttribute();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isTextKind() {
    assertNotClosed();
    return mCurrentNode.isText();
  }

  /**
   * {@inheritDoc}
   */
  public final int getNameKey() {
    assertNotClosed();
    return mCurrentNode.getNameKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getName() {
    assertNotClosed();
    return mTransactionState.getName(mCurrentNode.getNameKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getRawName() {
    assertNotClosed();
    return mTransactionState.getRawName(mCurrentNode.getNameKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getURIKey() {
    assertNotClosed();
    return mCurrentNode.getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getURI() {
    assertNotClosed();
    return mTransactionState.getName(mCurrentNode.getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final String getType() {
    assertNotClosed();
    return mTransactionState.getName(mCurrentNode.getTypeKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getTypeKey() {
    assertNotClosed();
    return mCurrentNode.getTypeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getRawType() {
    assertNotClosed();
    return mTransactionState.getRawName(mCurrentNode.getTypeKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getRawValue() {
    assertNotClosed();
    return mCurrentNode.getRawValue();
  }

  /**
   * {@inheritDoc}
   */
  public final String getValue() {
    assertNotClosed();
    return TypedValue.parseString(mCurrentNode.getRawValue());
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
  public final byte[] rawNameForKey(final int key) {
    assertNotClosed();
    return mTransactionState.getRawName(key);
  }

  /**
   * {@inheritDoc}
   */
  public final IItemList getItemList() {
    return mTransactionState.getItemList();
  }

  /**
   * {@inheritDoc}
   */
  public void close() {
    if (!mClosed) {
      // Close own state.
      mTransactionState.close();

      // Callback on session to make sure everything is cleaned up.
      mSessionState.closeReadTransaction(mTransactionID);

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
    String name = "";
    try {
      name = getName();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return "Node "
        + this.getNodeKey()
        + "\nwith name: "
        + name
        + "\nand value:"
        + getRawValue();
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
  public final ReadTransactionState getTransactionState() {
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
  public final SessionState getSessionState() {
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
  protected final IItem getCurrentNode() {
    return mCurrentNode;
  }

  /**
   * Setter for superclasses.
   * 
   * @param currentNode The current node to set.
   */
  protected final void setCurrentNode(final IItem currentNode) {
    mCurrentNode = currentNode;
  }

}
