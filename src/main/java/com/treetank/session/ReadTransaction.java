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
 * $Id: ReadTransaction.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.session;

import com.treetank.api.IItem;
import com.treetank.api.IItemList;
import com.treetank.api.IReadTransaction;
import com.treetank.utils.NamePageHash;
import com.treetank.utils.TypedValue;

/**
 * <h1>ReadTransaction</h1>
 * 
 * <p>
 * Read-only transaction wiht single-threaded cursor semantics. Each read-only
 * transaction works on a given revision key.
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

	/** Tracks whether the transaction is closed. */
	private boolean mClosed;

	/**
	 * Constructor.
	 * 
	 * @param transactionID
	 *            ID of transaction.
	 * @param sessionState
	 *            Session state to work with.
	 * @param transactionState
	 *            Transaction state to work with.
	 */
	protected ReadTransaction(final long transactionID,
			final SessionState sessionState,
			final ReadTransactionState transactionState) {
		mTransactionID = transactionID;
		mSessionState = sessionState;
		mTransactionState = transactionState;
		mCurrentNode = getTransactionState().getNode(DOCUMENT_ROOT_KEY);
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
		if (nodeKey != NULL_NODE_KEY) {
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
		return moveTo(mCurrentNode.getAttributeKey(index));
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean moveToNamespace(final int index) {
		return moveTo(mCurrentNode.getNamespaceKey(index));
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final long getNodeKey() {
		assertNotClosed();
		return mCurrentNode.getNodeKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final boolean hasParent() {
		assertNotClosed();
		return mCurrentNode.hasParent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final long getParentKey() {
		assertNotClosed();
		return mCurrentNode.getParentKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final boolean hasFirstChild() {
		assertNotClosed();
		return mCurrentNode.hasFirstChild();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final long getFirstChildKey() {
		assertNotClosed();
		return mCurrentNode.getFirstChildKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final boolean hasLeftSibling() {
		assertNotClosed();
		return mCurrentNode.hasLeftSibling();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final long getLeftSiblingKey() {
		assertNotClosed();
		return mCurrentNode.getLeftSiblingKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final boolean hasRightSibling() {
		assertNotClosed();
		return mCurrentNode.hasRightSibling();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final long getRightSiblingKey() {
		assertNotClosed();
		return mCurrentNode.getRightSiblingKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final long getChildCount() {
		assertNotClosed();
		return mCurrentNode.getChildCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final int getAttributeCount() {
		assertNotClosed();
		return mCurrentNode.getAttributeCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final int getNamespaceCount() {
		assertNotClosed();
		return mCurrentNode.getNamespaceCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final int getKind() {
		assertNotClosed();
		return mCurrentNode.getKind();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final boolean isDocumentRootKind() {
		assertNotClosed();
		return mCurrentNode.isDocumentRoot();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final boolean isElementKind() {
		assertNotClosed();
		return mCurrentNode.isElement();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final boolean isAttributeKind() {
		assertNotClosed();
		return mCurrentNode.isAttribute();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final boolean isTextKind() {
		assertNotClosed();
		return mCurrentNode.isText();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final int getNameKey() {
		assertNotClosed();
		return mCurrentNode.getNameKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final String getName() {
		assertNotClosed();
		return mTransactionState.getName(mCurrentNode.getNameKey());
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final byte[] getRawName() {
		assertNotClosed();
		return mTransactionState.getRawName(mCurrentNode.getNameKey());
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final int getURIKey() {
		assertNotClosed();
		return mCurrentNode.getURIKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final String getURI() {
		assertNotClosed();
		return mTransactionState.getName(mCurrentNode.getURIKey());
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final String getType() {
		assertNotClosed();
		return mTransactionState.getName(mCurrentNode.getTypeKey());
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final int getTypeKey() {
		assertNotClosed();
		return mCurrentNode.getTypeKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final byte[] getRawType() {
		assertNotClosed();
		return mTransactionState.getRawName(mCurrentNode.getTypeKey());
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final byte[] getRawValue() {
		assertNotClosed();
		return mCurrentNode.getRawValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public final String getValue() {
		assertNotClosed();
		return TypedValue.parseString(mCurrentNode.getRawValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getValueOfCurrentNode() {
		assertNotClosed();
		return TypedValue.parseString(mCurrentNode.getRawValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getNameOfCurrentNode() {
		assertNotClosed();
		return mTransactionState.getName(mCurrentNode.getNameKey());
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getURIOfCurrentNode() {
		assertNotClosed();
		return mTransactionState.getName(mCurrentNode.getURIKey());
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getTypeOfCurrentNode() {
		assertNotClosed();
		return mTransactionState.getName(mCurrentNode.getTypeKey());
	}

	/**
	 * {@inheritDoc}
	 */
	public final int keyForName(final String name) {
		assertNotClosed();
		return NamePageHash.generateHashForString(name);
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
		// String name = "";
		// try {
		// name = getName();
		// } catch (Exception e) {
		// throw new IllegalStateException(e);
		// }
		// return "Node "
		// + this.getNodeKey()
		// + "\nwith name: "
		// + name
		// + "\nand value:"
		// + getRawValue();
		return mCurrentNode.toString();
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
	 * @param transactionState
	 *            State of transaction.
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
	 * @param sessionState
	 *            Session state to set.
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
	 * @param currentNode
	 *            The current node to set.
	 */
	protected final void setCurrentNode(final IItem currentNode) {
		mCurrentNode = currentNode;
	}

	/**
	 * {@inheritDoc}
	 */
	public IItem getNode() {
		return this.mCurrentNode;
	}

}
