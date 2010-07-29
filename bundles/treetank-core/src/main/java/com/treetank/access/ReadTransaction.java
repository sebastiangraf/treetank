/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.access;

import javax.xml.namespace.QName;

import com.treetank.api.IItem;
import com.treetank.api.IItemList;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.settings.EFixed;
import com.treetank.utils.NamePageHash;
import com.treetank.utils.TypedValue;

/**
 * <h1>ReadTransaction</h1>
 * 
 * <p>
 * Read-only transaction wiht single-threaded cursor semantics. Each read-only transaction works on a given
 * revision key.
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
     * @throws TreetankIOException
     *             if something odd happens within the creation process.
     */
    protected ReadTransaction(final long transactionID, final SessionState sessionState,
        final ReadTransactionState transactionState) throws TreetankIOException {
        mTransactionID = transactionID;
        mSessionState = sessionState;
        mTransactionState = transactionState;
        mCurrentNode = getTransactionState().getNode((Long)EFixed.ROOT_NODE_KEY.getStandardProperty());
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
        return mTransactionState.getActualRevisionRootPage().getRevisionTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean moveTo(final long mNodeKey) {
        assertNotClosed();
        if (mNodeKey != (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            // Remember old node and fetch new one.
            final IItem oldNode = mCurrentNode;
            try {
                mCurrentNode = mTransactionState.getNode(mNodeKey);
            } catch (final Exception e) {
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
        return moveTo((Long)EFixed.ROOT_NODE_KEY.getStandardProperty());
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
        final AbsStructNode node = checkNode(mCurrentNode);
        return node == null ? false : moveTo(node.getFirstChildKey());
    }

    /**
     * {@inheritDoc}
     */
    public final boolean moveToLeftSibling() {
        final AbsStructNode node = checkNode(mCurrentNode);
        return node == null ? false : moveTo(node.getLeftSiblingKey());
    }

    /**
     * {@inheritDoc}
     */
    public final boolean moveToRightSibling() {
        final AbsStructNode node = checkNode(mCurrentNode);
        return node == null ? false : moveTo(node.getRightSiblingKey());
    }

    /**
     * {@inheritDoc}
     */
    public final boolean moveToAttribute(final int mIndex) {
        if (mCurrentNode.getKind() == ENodes.ELEMENT_KIND) {
            return moveTo(((ElementNode)mCurrentNode).getAttributeKey(mIndex));
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final boolean moveToNamespace(final int mIndex) {
        if (mCurrentNode.getKind() == ENodes.ELEMENT_KIND) {
            return moveTo(((ElementNode)mCurrentNode).getNamespaceKey(mIndex));
        } else {
            return false;
        }

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
    public final QName getQNameOfCurrentNode() {
        assertNotClosed();
        final String name = mTransactionState.getName(mCurrentNode.getNameKey());
        final String uri = mTransactionState.getName(mCurrentNode.getURIKey());
        return name == null ? null : buildQName(uri, name);
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
    public final int keyForName(final String mName) {
        assertNotClosed();
        return NamePageHash.generateHashForString(mName);
    }

    /**
     * {@inheritDoc}
     */
    public final String nameForKey(final int mKey) {
        assertNotClosed();
        return mTransactionState.getName(mKey);
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
        if (getNode().getKind() == ENodes.ATTRIBUTE_KIND || getNode().getKind() == ENodes.ELEMENT_KIND) {
            builder.append("Name of Node: ");
            builder.append(getQNameOfCurrentNode().toString());
            builder.append("\n");
        }
        if (getNode().getKind() == ENodes.ATTRIBUTE_KIND || getNode().getKind() == ENodes.TEXT_KIND) {
            builder.append("Value of Node: ");
            builder.append(getValueOfCurrentNode());
            builder.append("\n");
        }
        if (getNode().getKind() == ENodes.ROOT_KIND) {
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
    public final boolean isClosed() {
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
    public ReadTransactionState getTransactionState() {
        return mTransactionState;
    }

    /**
     * Replace the state of the transaction.
     * 
     * @param transactionState
     *            State of transaction.
     */
    protected final void setTransactionState(final ReadTransactionState transactionState) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMaxNodeKey() throws TreetankIOException {
        return getTransactionState().getActualRevisionRootPage().getMaxNodeKey();
    }

    /**
     * Building QName out of uri and name. The name can have the prefix denoted
     * with ":";
     * 
     * @param mUri
     *            the namespaceuri
     * @param mName
     *            the name including a possible prefix
     * @return the QName obj
     */
    protected static final QName buildQName(final String mUri, final String mName) {
        QName qname;
        if (mName.contains(":")) {
            qname = new QName(mUri, mName.split(":")[1], mName.split(":")[0]);
        } else {
            qname = new QName(mUri, mName);
        }
        return qname;
    }

    /**
     * Check method if node is a structural node.
     * 
     * @param mItem
     *            to be checked
     * @return an {@link AbsStructNode} instance if node is a structural one
     */
    protected static AbsStructNode checkNode(final IItem mItem) {
        if (mItem instanceof AbsStructNode) {
            return (AbsStructNode)mItem;
        } else {
            return null;
        }
    }

}