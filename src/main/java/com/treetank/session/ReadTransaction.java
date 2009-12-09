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
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.settings.EFixed;
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
            final ReadTransactionState transactionState)
            throws TreetankIOException {
        mTransactionID = transactionID;
        mSessionState = sessionState;
        mTransactionState = transactionState;
        mCurrentNode = getTransactionState().getNode(
                (Long) EFixed.ROOT_NODE_KEY.getStandardProperty());
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
    public final long getRevisionNumber() throws TreetankIOException {
        assertNotClosed();
        return mTransactionState.getActualRevisionRootPage().getRevision();
    }

    /**
     * {@inheritDoc}
     */
    public final long getRevisionTimestamp() throws TreetankIOException {
        assertNotClosed();
        return mTransactionState.getActualRevisionRootPage()
                .getRevisionTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    public final long getNodeCount() throws TreetankIOException {
        assertNotClosed();
        return mTransactionState.getActualRevisionRootPage().getRevisionSize();
    }

    /**
     * {@inheritDoc}
     */
    public boolean moveTo(final long nodeKey) {
        assertNotClosed();
        if (nodeKey != (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty()) {
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
        return moveTo((Long) EFixed.ROOT_NODE_KEY
                .getStandardProperty());
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
    public void close() throws TreetankException {
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
        final StringBuilder builder = new StringBuilder();
        if (getNode().isAttribute() || getNode().isElement()) {
            builder.append("Name of Node: ");
            builder.append(getNameOfCurrentNode());
            builder.append("\n");
        }
        if (getNode().isAttribute() || getNode().isText()) {
            builder.append("Value of Node: ");
            builder.append(getValueOfCurrentNode());
            builder.append("\n");
        }
        if (getNode().isDocumentRoot()) {
            builder.append("Node is DocumentRoot");
            builder.append("\n");
        }
        builder.append(getNode().toString());

        return builder.toString();
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
