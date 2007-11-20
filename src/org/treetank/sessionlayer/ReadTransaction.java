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

package org.treetank.sessionlayer;

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
    mClosed = false;
    mCurrentNode = getTransactionState().getNode(DOCUMENT_ROOT_KEY);
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
  public final boolean moveTo(final long nodeKey) {
    assertNotClosed();
    // Do nothing if this node is already selected.
    if (mCurrentNode.getNodeKey() == nodeKey) {
      return true;
    }
    // Find node by its key.
    if (nodeKey != NULL_NODE_KEY) {
      final AbstractNode oldNode = mCurrentNode;
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
  public final boolean moveToToken(final String token) {
    assertNotClosed();

    final AbstractNode oldNode = mCurrentNode;

    moveToFullTextRoot();
    boolean contained = true;
    for (final char character : token.toCharArray()) {
      if (hasFirstChild()) {
        moveToFirstChild();
        while (isFullTextKind()
            && (getLocalPartKey() != character)
            && hasRightSibling()) {
          moveToRightSibling();
        }
        contained = contained && (getLocalPartKey() == character);
      } else {
        contained = false;
      }
    }

    if (contained) {
      return true;
    } else {
      mCurrentNode = oldNode;
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
  public final boolean moveToFullTextRoot() {
    return moveTo(FULLTEXT_ROOT_KEY);
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
  public final int getAttributeLocalPartKey(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index).getLocalPartKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeLocalPart(final int index) {
    assertNotClosed();
    return nameForKey(mCurrentNode.getAttribute(index).getLocalPartKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributePrefixKey(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index).getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributePrefix(final int index) {
    assertNotClosed();
    return nameForKey(mCurrentNode.getAttribute(index).getPrefixKey());
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
  public final int getAttributeValueType(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index).getValueType();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeValueAsAtom(final int index) {
    assertNotClosed();
    return TypedValue.atomize(
        mCurrentNode.getAttribute(index).getValueType(),
        mCurrentNode.getAttribute(index).getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getAttributeValueAsByteArray(final int index) {
    assertNotClosed();
    return mCurrentNode.getAttribute(index).getValue();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAttributeValueAsString(final int index) {
    assertNotClosed();
    if (mCurrentNode.getAttribute(index).getValueType() != STRING_TYPE) {
      throw new IllegalStateException(
          "Can not get string if type of value is not string.");
    }
    return TypedValue.parseString(mCurrentNode.getAttribute(index).getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeValueAsInt(final int index) {
    assertNotClosed();
    if (mCurrentNode.getAttribute(index).getValueType() != INT_TYPE) {
      throw new IllegalStateException(
          "Can not get int if type of value is not int.");
    }
    return TypedValue.parseInt(mCurrentNode.getAttribute(index).getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final long getAttributeValueAsLong(final int index) {
    assertNotClosed();
    if (mCurrentNode.getAttribute(index).getValueType() != LONG_TYPE) {
      throw new IllegalStateException(
          "Can not get long if type of value is not long.");
    }
    return TypedValue.parseLong(mCurrentNode.getAttribute(index).getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean getAttributeValueAsBoolean(final int index) {
    assertNotClosed();
    if (mCurrentNode.getAttribute(index).getValueType() != BOOLEAN_TYPE) {
      throw new IllegalStateException(
          "Can not get boolean if type of value is not boolean.");
    }
    return TypedValue.parseBoolean(mCurrentNode.getAttribute(index).getValue());
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
  public final boolean isFullTextKind() {
    assertNotClosed();
    return mCurrentNode.isFullText();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isFullTextLeafKind() {
    assertNotClosed();
    return mCurrentNode.isFullTextLeaf();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isFullTextRootKind() {
    assertNotClosed();
    return mCurrentNode.isFullTextRoot();
  }

  /**
   * {@inheritDoc}
   */
  public final int getLocalPartKey() {
    assertNotClosed();
    return mCurrentNode.getLocalPartKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getLocalPart() {
    assertNotClosed();
    return mTransactionState.getName(mCurrentNode.getLocalPartKey());
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
  public final int getPrefixKey() {
    assertNotClosed();
    return mCurrentNode.getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getPrefix() {
    assertNotClosed();
    return mTransactionState.getName(mCurrentNode.getPrefixKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getValueType() {
    assertNotClosed();
    return mCurrentNode.getValueType();
  }

  /**
   * {@inheritDoc}
   */
  public final String getValueAsAtom() {
    assertNotClosed();
    return TypedValue.atomize(mCurrentNode.getValueType(), mCurrentNode
        .getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getValueAsByteArray() {
    assertNotClosed();
    return mCurrentNode.getValue();
  }

  /**
   * {@inheritDoc}
   */
  public final String getValueAsString() {
    assertNotClosed();
    if (mCurrentNode.getValueType() != STRING_TYPE) {
      throw new IllegalStateException(
          "Can not get string if type of value is not string.");
    }
    return TypedValue.parseString(mCurrentNode.getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final int getValueAsInt() {
    assertNotClosed();
    if (mCurrentNode.getValueType() != INT_TYPE) {
      throw new IllegalStateException(
          "Can not get int if type of value is not int.");
    }
    return TypedValue.parseInt(mCurrentNode.getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final long getValueAsLong() {
    assertNotClosed();
    if (mCurrentNode.getValueType() != LONG_TYPE) {
      throw new IllegalStateException(
          "Can not get long if type of value is not long.");
    }
    return TypedValue.parseLong(mCurrentNode.getValue());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean getValueAsBoolean() {
    assertNotClosed();
    if (mCurrentNode.getValueType() != BOOLEAN_TYPE) {
      throw new IllegalStateException(
          "Can not get boolean if type of value is not boolean.");
    }
    return TypedValue.parseBoolean(mCurrentNode.getValue());
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
        + getValueAsByteArray();
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
