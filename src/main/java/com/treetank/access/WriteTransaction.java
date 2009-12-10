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
 * $Id: WriteTransaction.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.access;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.node.AbstractNode;
import com.treetank.node.AttributeNode;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.page.UberPage;
import com.treetank.settings.EFixed;
import com.treetank.utils.TypedValue;

/**
 * <h1>WriteTransaction</h1>
 * 
 * <p>
 * Single-threaded instance of only write transaction per session.
 * </p>
 */
public final class WriteTransaction extends ReadTransaction implements
        IWriteTransaction {

    /** Maximum number of node modifications before auto commit. */
    private final int mMaxNodeCount;

    /** Scheduler to commit after mMaxTime seconds. */
    private ScheduledExecutorService mCommitScheduler;

    /** Modification counter. */
    private long mModificationCount;

    /**
     * Constructor.
     * 
     * @param transactionID
     *            ID of transaction.
     * @param sessionState
     *            State of the session.
     * @param transactionState
     *            State of this transaction.
     * @param maxNodeCount
     *            Maximum number of node modifications before auto commit.
     * @param maxTime
     *            Maximum number of seconds before auto commit.
     */
    protected WriteTransaction(final long transactionID,
            final SessionState sessionState,
            final WriteTransactionState transactionState,
            final int maxNodeCount, final int maxTime)
            throws TreetankIOException {
        super(transactionID, sessionState, transactionState);

        // Do not accept negative values.
        if ((maxNodeCount < 0) || (maxTime < 0)) {
            throw new IllegalArgumentException(
                    "Negative arguments are not accepted.");
        }

        // Only auto commit by node modifications if it is more then 0.
        mMaxNodeCount = maxNodeCount;
        mModificationCount = 0L;

        // Only auto commit by time if the time is more than 0 seconds.
        if (maxTime > 0) {
            mCommitScheduler = Executors.newScheduledThreadPool(1);
            mCommitScheduler.scheduleAtFixedRate(new Runnable() {
                public final void run() {
                    if (mModificationCount > 0) {
                        try {
                            commit();
                        } catch (final TreetankIOException exc) {
                            throw new IllegalStateException(exc);
                        }
                    }
                }
            }, 0, maxTime, TimeUnit.SECONDS);
        } else {
            mCommitScheduler = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertElementAsFirstChild(final String name,
            final String uri) throws TreetankIOException {
        return insertFirstChild(((WriteTransactionState) getTransactionState())
                .createElementNode(getCurrentNode().getNodeKey(),
                        (Long) EFixed.NULL_NODE_KEY
                                .getStandardProperty(),
                        (Long) EFixed.NULL_NODE_KEY
                                .getStandardProperty(), getCurrentNode()
                                .getFirstChildKey(),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(name),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(uri),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey("xs:untyped")));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertTextAsFirstChild(final int valueType,
            final byte[] value) throws TreetankIOException {
        return insertFirstChild(((WriteTransactionState) getTransactionState())
                .createTextNode(getCurrentNode().getNodeKey(),
                        (Long) EFixed.NULL_NODE_KEY
                                .getStandardProperty(), getCurrentNode()
                                .getFirstChildKey(), valueType, value));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertTextAsFirstChild(final String value)
            throws TreetankIOException {
        return insertTextAsFirstChild(
                ((WriteTransactionState) getTransactionState())
                        .createNameKey("xs:untyped"), TypedValue
                        .getBytes(value));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertElementAsRightSibling(final String name,
            final String uri) throws TreetankException {
        return insertRightSibling(((WriteTransactionState) getTransactionState())
                .createElementNode(getCurrentNode().getParentKey(),
                        (Long) EFixed.NULL_NODE_KEY
                                .getStandardProperty(), getCurrentNode()
                                .getNodeKey(), getCurrentNode()
                                .getRightSiblingKey(),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(name),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(uri),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey("xs:untyped")));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertTextAsRightSibling(final int valueType,
            final byte[] value) throws TreetankException {
        return insertRightSibling(((WriteTransactionState) getTransactionState())
                .createTextNode(getCurrentNode().getParentKey(),
                        getCurrentNode().getNodeKey(), getCurrentNode()
                                .getRightSiblingKey(), valueType, value));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertTextAsRightSibling(final String value)
            throws TreetankException {
        return insertTextAsRightSibling(
                ((WriteTransactionState) getTransactionState())
                        .createNameKey("xs:untyped"), TypedValue
                        .getBytes(value));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertAttribute(final String name,
            final String uri, final int valueType, final byte[] value)
            throws TreetankIOException {
        return insertAttribute(((WriteTransactionState) getTransactionState())
                .createAttributeNode(getCurrentNode().getNodeKey(),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(name),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(uri),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey("xs:untypedAtomic"), value));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertAttribute(final String name,
            final String uri, final String value) throws TreetankIOException {
        return insertAttribute(name, uri,
                ((WriteTransactionState) getTransactionState())
                        .createNameKey("xs:untypedAtomic"), TypedValue
                        .getBytes(value));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertNamespace(final String uri,
            final String prefix) throws TreetankIOException {
        return insertNamespace(((WriteTransactionState) getTransactionState())
                .createNamespaceNode(getCurrentNode().getNodeKey(),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(uri),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(prefix)));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void remove() throws TreetankException {

        assertNotClosed();
        mModificationCount++;
        // Remember all related nodes.
        AbstractNode node = null;
        AbstractNode leftSibling = null;
        AbstractNode rightSibling = null;
        AbstractNode parent = null;

        node = (AbstractNode) getCurrentNode();

        if (getCurrentNode().isDocumentRoot()) {
            throw new TreetankUsageException("Root node can not be removed.");
        } else if (getCurrentNode().isElement() || getCurrentNode().isText()) {

            node = (AbstractNode) getCurrentNode();
            if (node.hasLeftSibling()) {
                moveToLeftSibling();
                leftSibling = (AbstractNode) getCurrentNode();
                moveToRightSibling();
            }
            if (node.hasRightSibling()) {
                moveToRightSibling();
                rightSibling = (AbstractNode) getCurrentNode();
            }
            moveToParent();
            parent = (AbstractNode) getCurrentNode();

            // Remove old node.
            ((WriteTransactionState) getTransactionState()).removeNode(node);

            // Adapt left sibling node if there is one.
            if (leftSibling != null) {
                leftSibling = setUpNodeModification(leftSibling.getNodeKey());
                if (rightSibling != null) {
                    leftSibling.setRightSiblingKey(rightSibling.getNodeKey());
                } else {
                    leftSibling
                            .setRightSiblingKey((Long) EFixed.NULL_NODE_KEY
                                    .getStandardProperty());
                }
                tearDownNodeModification(leftSibling);
            }

            // Adapt right sibling node if there is one.
            if (rightSibling != null) {
                rightSibling = setUpNodeModification(rightSibling.getNodeKey());
                if (leftSibling != null) {
                    rightSibling.setLeftSiblingKey(leftSibling.getNodeKey());
                } else {
                    rightSibling
                            .setLeftSiblingKey((Long) EFixed.NULL_NODE_KEY
                                    .getStandardProperty());
                }
                tearDownNodeModification(rightSibling);
            }

            // Adapt parent.
            parent = setUpNodeModification(parent.getNodeKey());
            parent.decrementChildCount();
            if (parent.getFirstChildKey() == node.getNodeKey()) {
                if (rightSibling != null) {
                    parent.setFirstChildKey(rightSibling.getNodeKey());
                } else {
                    parent
                            .setFirstChildKey((Long) EFixed.NULL_NODE_KEY
                                    .getStandardProperty());
                }
            }
            tearDownNodeModification(parent);

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
        } else if (getCurrentNode().isAttribute()) {
            moveToParent();

            parent = setUpNodeModification(getCurrentNode().getNodeKey());
            ((ElementNode) parent).removeAttribute(node.getNodeKey());
            tearDownNodeModification(parent);
        }

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setName(final String name)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbstractNode node = setUpNodeModification(getCurrentNode()
                .getNodeKey());

        node.setNameKey(((WriteTransactionState) getTransactionState())
                .createNameKey(name));
        setCurrentNode(node);
        tearDownNodeModification(node);

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setURI(final String uri)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbstractNode node = setUpNodeModification(getCurrentNode()
                .getNodeKey());
        node.setURIKey(((WriteTransactionState) getTransactionState())
                .createNameKey(uri));
        setCurrentNode(node);
        tearDownNodeModification(node);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setValue(final int valueType, final byte[] value)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbstractNode node = setUpNodeModification(getCurrentNode()
                .getNodeKey());
        node.setValue(valueType, value);
        setCurrentNode(node);
        tearDownNodeModification(node);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setValue(final String value)
            throws TreetankIOException {
        setValue(((WriteTransactionState) getTransactionState())
                .createNameKey("xs:untyped"), TypedValue.getBytes(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws TreetankException {
        if (!isClosed()) {
            // Make sure to commit all dirty data.
            if (mModificationCount > 0) {
                throw new TreetankUsageException(
                        "Must commit/abort transaction first");
            }
            // Make sure to cancel the periodic commit task if it was started.
            if (mCommitScheduler != null) {
                mCommitScheduler.shutdownNow();
                mCommitScheduler = null;
            }
            // Release all state immediately.
            getTransactionState().close();
            getSessionState().closeWriteTransaction(getTransactionID());
            setSessionState(null);
            setTransactionState(null);
            setCurrentNode(null);
            // Remember that we are closed.
            setClosed();
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void commit() throws TreetankIOException {

        assertNotClosed();

        // Commit uber page.
        UberPage uberPage;
        try {
            uberPage = ((WriteTransactionState) getTransactionState())
                    .commit(getSessionState().getSessionConfiguration());

            // Remember succesfully committed uber page in session state.
            getSessionState().setLastCommittedUberPage(uberPage);

            // Reset modification counter.
            mModificationCount = 0L;

            getTransactionState().close();
        } catch (TreetankIOException e) {
            throw new RuntimeException(e);
        }
        // Reset internal transaction state to new uber page.
        setTransactionState(getSessionState().createWriteTransactionState());

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void abort() throws TreetankIOException {

        assertNotClosed();

        // Reset modification counter.
        mModificationCount = 0L;

        getTransactionState().close();

        // Reset internal transaction state to last committed uber page.
        setTransactionState(getSessionState().createWriteTransactionState());
    }

    private void intermediateCommitIfRequired() throws TreetankIOException {
        assertNotClosed();
        if ((mMaxNodeCount > 0) && (mModificationCount > mMaxNodeCount)) {
            commit();
        }
    }

    private long insertFirstChild(final AbstractNode node)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();

        setCurrentNode(node);

        updateParentAfterInsert(true);
        updateRightSibling();

        return node.getNodeKey();
    }

    private long insertRightSibling(final AbstractNode node)
            throws TreetankException {

        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();

        if (getCurrentNode().getNodeKey() == (Long) EFixed.ROOT_NODE_KEY
                .getStandardProperty()) {
            throw new TreetankUsageException("Root node can not have siblings.");
        }

        setCurrentNode(node);

        updateParentAfterInsert(false);
        updateLeftSibling();
        updateRightSibling();

        return node.getNodeKey();
    }

    private long insertAttribute(final AttributeNode node)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();

        if (!getCurrentNode().isElement()) {
            throw new IllegalStateException(
                    "Only element nodes can have attributes.");
        }

        setCurrentNode(node);

        final AbstractNode parentNode = setUpNodeModification(node
                .getParentKey());
        parentNode.insertAttribute(node.getNodeKey());
        tearDownNodeModification(parentNode);

        return node.getNodeKey();
    }

    private long insertNamespace(final NamespaceNode node)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();

        if (!getCurrentNode().isElement()) {
            throw new IllegalStateException(
                    "Only element nodes can have namespaces.");
        }

        setCurrentNode(node);

        final AbstractNode parentNode = setUpNodeModification(node
                .getParentKey());
        parentNode.insertNamespace(node.getNodeKey());
        tearDownNodeModification(parentNode);

        return node.getNodeKey();
    }

    private void updateParentAfterInsert(final boolean updateFirstChild)
            throws TreetankIOException {
        final AbstractNode parentNode = setUpNodeModification(getCurrentNode()
                .getParentKey());
        parentNode.incrementChildCount();
        if (updateFirstChild) {
            parentNode.setFirstChildKey(getCurrentNode().getNodeKey());
        }
        tearDownNodeModification(parentNode);

    }

    private void updateRightSibling() throws TreetankIOException {
        if (getCurrentNode().hasRightSibling()) {
            final AbstractNode rightSiblingNode = setUpNodeModification(getCurrentNode()
                    .getRightSiblingKey());
            rightSiblingNode.setLeftSiblingKey(getCurrentNode().getNodeKey());
            tearDownNodeModification(rightSiblingNode);

        }
    }

    private void updateLeftSibling() throws TreetankIOException {
        final AbstractNode leftSiblingNode = setUpNodeModification(getCurrentNode()
                .getLeftSiblingKey());
        leftSiblingNode.setRightSiblingKey(getCurrentNode().getNodeKey());
        tearDownNodeModification(leftSiblingNode);

    }

    private AbstractNode setUpNodeModification(final long nodeKey)
            throws TreetankIOException {
        final AbstractNode modNode = ((WriteTransactionState) getTransactionState())
                .prepareNodeForModification(nodeKey);
        return modNode;
    }

    private void tearDownNodeModification(final AbstractNode node)
            throws TreetankIOException {
        ((WriteTransactionState) getTransactionState())
                .finishNodeModification(node);
    }
}
