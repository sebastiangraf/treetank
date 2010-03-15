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

import javax.xml.namespace.QName;

import com.treetank.api.IAxis;
import com.treetank.api.IItem;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
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
            final int maxNodeCount, final int maxTime) throws TreetankException {
        super(transactionID, sessionState, transactionState);

        // Do not accept negative values.
        if ((maxNodeCount < 0) || (maxTime < 0)) {
            throw new TreetankUsageException(
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
                        } catch (final TreetankException exc) {
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
    public synchronized long insertElementAsFirstChild(final QName qname)
            throws TreetankException {
        return insertFirstChild(((WriteTransactionState) getTransactionState())
                .createElementNode(getCurrentNode().getNodeKey(),
                        (Long) EFixed.NULL_NODE_KEY.getStandardProperty(),
                        (Long) EFixed.NULL_NODE_KEY.getStandardProperty(),
                        getCurrentNode().getFirstChildKey(),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(buildName(qname)),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(qname.getNamespaceURI()),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey("xs:untyped")));
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public synchronized long insertElementAsFirstChild(final String name,
            final String uri) throws TreetankException {
        return insertElementAsFirstChild(buildQName(uri, name));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertTextAsFirstChild(final int valueType,
            final byte[] value) throws TreetankException {
        return insertFirstChild(((WriteTransactionState) getTransactionState())
                .createTextNode(getCurrentNode().getNodeKey(),
                        (Long) EFixed.NULL_NODE_KEY.getStandardProperty(),
                        getCurrentNode().getFirstChildKey(), valueType, value));
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertTextAsFirstChild(final String value)
            throws TreetankException {
        return insertTextAsFirstChild(
                ((WriteTransactionState) getTransactionState())
                        .createNameKey("xs:untyped"), TypedValue
                        .getBytes(value));
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public synchronized long insertElementAsRightSibling(final String name,
            final String uri) throws TreetankException {

        return insertElementAsRightSibling(buildQName(uri, name));

    }

    public synchronized long insertElementAsRightSibling(final QName qname)
            throws TreetankException {
        return insertRightSibling(((WriteTransactionState) getTransactionState())
                .createElementNode(getCurrentNode().getParentKey(),
                        (Long) EFixed.NULL_NODE_KEY.getStandardProperty(),
                        getCurrentNode().getNodeKey(), getCurrentNode()
                                .getRightSiblingKey(),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(buildName(qname)),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(qname.getNamespaceURI()),
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

    public synchronized long insertAttribute(final QName qname,
            final String value) throws TreetankException {

        return insertAttribute(((WriteTransactionState) getTransactionState())
                .createAttributeNode(getCurrentNode().getNodeKey(),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(buildName(qname)),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey(qname.getNamespaceURI()),
                        ((WriteTransactionState) getTransactionState())
                                .createNameKey("xs:untypedAtomic"), TypedValue
                                .getBytes(value)));

    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public synchronized long insertAttribute(final String name,
            final String uri, final String value) throws TreetankException {
        final QName qname = buildQName(uri, name);

        return insertAttribute(qname, value);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertNamespace(final String uri,
            final String prefix) throws TreetankException {
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
        final AbstractNode node = (AbstractNode) getCurrentNode();

        if (getCurrentNode().isDocumentRoot()) {
            throw new TreetankUsageException("Root node can not be removed.");
        } else if (getCurrentNode().isElement() || getCurrentNode().isText()) {

            adaptNeighbours(node, null);

            // removing subtree
            final IAxis desc = new DescendantAxis(this, false);
            while (desc.hasNext()) {
                desc.next();
                removeIncludingRelated();

            }
            moveTo(node.getNodeKey());
            removeIncludingRelated();

            // Set current node.
            if (node.hasRightSibling()) {
                moveTo(node.getRightSiblingKey());
                return;
            }

            if (node.hasLeftSibling()) {
                moveTo(node.getLeftSiblingKey());
                return;
            }

            moveTo(node.getParentKey());

        } else if (getCurrentNode().isAttribute()) {
            moveToParent();

            AbstractNode parent = setUpNodeModification(getCurrentNode()
                    .getNodeKey());
            ((ElementNode) parent).removeAttribute(node.getNodeKey());
            tearDownNodeModification(parent);
        }

    }

    private final void removeIncludingRelated() throws TreetankIOException {
        final IItem node = getCurrentNode();
        // removing attributes
        for (int i = 0; i < node.getAttributeCount(); i++) {
            moveTo(node.getAttributeKey(i));
            ((WriteTransactionState) getTransactionState())
                    .removeNode((AbstractNode) this.getCurrentNode());
        }
        // removing namespaces
        moveTo(node.getNodeKey());
        for (int i = 0; i < node.getNamespaceCount(); i++) {
            moveTo(node.getNamespaceKey(i));
            ((WriteTransactionState) getTransactionState())
                    .removeNode((AbstractNode) this.getCurrentNode());
        }
        // Remove old node.
        ((WriteTransactionState) getTransactionState())
                .removeNode((AbstractNode) node);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setName(final String name)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbstractNode oldNode = (AbstractNode) getCurrentNode();
        final AbstractNode newNode = createNodeToModify(oldNode);

        adaptNeighbours(oldNode, newNode);
        newNode.setNameKey(((WriteTransactionState) getTransactionState())
                .createNameKey(name));
        ((WriteTransactionState) getTransactionState()).removeNode(oldNode);
        setCurrentNode(newNode);

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setURI(final String uri)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbstractNode oldNode = (AbstractNode) getCurrentNode();
        final AbstractNode newNode = createNodeToModify(oldNode);

        adaptNeighbours(oldNode, newNode);
        newNode.setURIKey(((WriteTransactionState) getTransactionState())
                .createNameKey(uri));
        ((WriteTransactionState) getTransactionState()).removeNode(oldNode);
        setCurrentNode(newNode);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setValue(final int valueType, final byte[] value)
            throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbstractNode oldNode = (AbstractNode) getCurrentNode();
        final AbstractNode newNode = createNodeToModify(oldNode);

        adaptNeighbours(oldNode, newNode);
        newNode.setValue(valueType, value);
        ((WriteTransactionState) getTransactionState()).removeNode(oldNode);
        setCurrentNode(newNode);

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setValue(final String value)
            throws TreetankIOException {
        setValue(((WriteTransactionState) getTransactionState())
                .createNameKey("xs:untyped"), TypedValue.getBytes(value));
    }

    private final AbstractNode createNodeToModify(final AbstractNode oldNode)
            throws TreetankIOException {
        AbstractNode newNode = null;
        switch (oldNode.getKind()) {
        case ELEMENT_KIND:
            newNode = ((WriteTransactionState) getTransactionState())
                    .createElementNode(oldNode.getParentKey(), oldNode
                            .getFirstChildKey(), oldNode.getLeftSiblingKey(),
                            oldNode.getRightSiblingKey(), oldNode.getNameKey(),
                            oldNode.getURIKey(), oldNode.getTypeKey());
            break;
        case ATTRIBUTE_KIND:
            newNode = ((WriteTransactionState) getTransactionState())
                    .createAttributeNode(oldNode.getParentKey(), oldNode
                            .getNameKey(), oldNode.getURIKey(), oldNode
                            .getTypeKey(), oldNode.getRawValue());
            break;
        case NAMESPACE_KIND:
            newNode = ((WriteTransactionState) getTransactionState())
                    .createNamespaceNode(oldNode.getParentKey(), oldNode
                            .getURIKey(), oldNode.getNameKey());
            break;

        case TEXT_KIND:
            newNode = ((WriteTransactionState) getTransactionState())
                    .createTextNode(oldNode.getNodeKey(), oldNode
                            .getLeftSiblingKey(), oldNode.getRightSiblingKey(),
                            oldNode.getTypeKey(), oldNode.getRawValue());
            break;

        }
        return newNode;
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
    @Override
    public void revertTo(final long revision) throws TreetankException {
        assertNotClosed();
        getSessionState().assertValidRevision(revision);
        getTransactionState().close();
        // Reset internal transaction state to new uber page.
        setTransactionState(getSessionState().createWriteTransactionState(
                getTransactionID(), revision, getRevisionNumber() - 1));
        // Reset modification counter.
        mModificationCount = 0L;
        moveToDocumentRoot();

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void commit() throws TreetankException {

        assertNotClosed();

        // Commit uber page.
        UberPage uberPage = ((WriteTransactionState) getTransactionState())
                .commit();

        // Remember succesfully committed uber page in session state.
        getSessionState().setLastCommittedUberPage(uberPage);

        // Reset modification counter.
        mModificationCount = 0L;

        getTransactionState().close();
        // Reset internal transaction state to new uber page.
        setTransactionState(getSessionState().createWriteTransactionState(
                getTransactionID(), getRevisionNumber(), getRevisionNumber()));

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void abort() throws TreetankIOException {

        assertNotClosed();

        // Reset modification counter.
        mModificationCount = 0L;

        getTransactionState().close();

        long revisionToSet = 0;
        if (!getTransactionState().getUberPage().isBootstrap()) {
            revisionToSet = getRevisionNumber() - 1;
        }

        // Reset internal transaction state to last committed uber page.
        setTransactionState(getSessionState().createWriteTransactionState(
                getTransactionID(), revisionToSet, revisionToSet));
    }

    private final QName buildQName(final String uri, final String name) {
        QName qname;
        if (name.contains(":")) {
            qname = new QName(uri, name.split(":")[1], name.split(":")[0]);
        } else {
            qname = new QName(uri, name, "");
        }
        return qname;
    }

    private final String buildName(final QName qname) {
        String name;
        if (qname.getPrefix().isEmpty()) {
            name = qname.getLocalPart();
        } else {
            name = new StringBuilder(qname.getPrefix()).append(":").append(
                    qname.getLocalPart()).toString();
        }
        return name;
    }

    private void intermediateCommitIfRequired() throws TreetankException {
        assertNotClosed();
        if ((mMaxNodeCount > 0) && (mModificationCount > mMaxNodeCount)) {
            commit();
        }
    }

    private long insertFirstChild(final AbstractNode node)
            throws TreetankException {

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
            throws TreetankException {

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
            throws TreetankException {

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

    private void adaptNeighbours(final AbstractNode oldNode,
            final AbstractNode newNode) throws TreetankIOException {

        // Remember all related nodes.
        AbstractNode leftSibling = null;
        AbstractNode rightSibling = null;
        AbstractNode parent = null;
        AbstractNode firstChild = null;

        // getting the neighbourhood
        if (oldNode.hasLeftSibling()) {
            moveToLeftSibling();
            leftSibling = (AbstractNode) getCurrentNode();
            moveToRightSibling();
        }
        if (oldNode.hasRightSibling()) {
            moveToRightSibling();
            rightSibling = (AbstractNode) getCurrentNode();
            moveToLeftSibling();
        }
        if (!moveToParent()) {
            throw new IllegalStateException("Node has no parent!");
        }
        parent = (AbstractNode) getCurrentNode();
        moveTo(oldNode.getNodeKey());
        if (oldNode.hasFirstChild()) {
            moveToFirstChild();
            firstChild = (AbstractNode) getCurrentNode();
        }
        moveTo(oldNode.getNodeKey());

        // Adapt left sibling node if there is one.
        if (leftSibling != null) {
            leftSibling = setUpNodeModification(leftSibling.getNodeKey());
            if (newNode == null) {
                if (rightSibling != null) {
                    leftSibling.setRightSiblingKey(rightSibling.getNodeKey());
                } else {
                    leftSibling.setRightSiblingKey((Long) EFixed.NULL_NODE_KEY
                            .getStandardProperty());
                }
            } else {
                leftSibling.setRightSiblingKey(newNode.getNodeKey());
            }
            tearDownNodeModification(leftSibling);
        }

        // Adapt right sibling node if there is one.
        if (rightSibling != null) {
            rightSibling = setUpNodeModification(rightSibling.getNodeKey());
            if (newNode == null) {
                if (leftSibling != null) {
                    rightSibling.setLeftSiblingKey(leftSibling.getNodeKey());
                } else {
                    rightSibling.setLeftSiblingKey((Long) EFixed.NULL_NODE_KEY
                            .getStandardProperty());
                }
            } else {
                rightSibling.setLeftSiblingKey(newNode.getNodeKey());
            }
            tearDownNodeModification(rightSibling);
        }

        // Adapt parent.
        parent = setUpNodeModification(parent.getNodeKey());
        if (newNode == null) {
            parent.decrementChildCount();
        }
        if (parent.getFirstChildKey() == oldNode.getNodeKey()) {
            if (newNode == null) {
                if (rightSibling != null) {
                    parent.setFirstChildKey(rightSibling.getNodeKey());
                } else {
                    parent.setFirstChildKey((Long) EFixed.NULL_NODE_KEY
                            .getStandardProperty());
                }
            } else {
                parent.setFirstChildKey(newNode.getNodeKey());
            }
        }
        tearDownNodeModification(parent);

        // adapt associated nodes
        if (newNode != null) {
            if (firstChild != null) {
                setCurrentNode(firstChild);
                do {
                    final AbstractNode node = setUpNodeModification(getCurrentNode()
                            .getNodeKey());
                    node.setParentKey(newNode.getNodeKey());
                    tearDownNodeModification(node);
                } while (moveToRightSibling());
            }
            // setting the attributes and namespaces
            for (int i = 0; i < oldNode.getAttributeCount(); i++) {
                newNode.insertAttribute(oldNode.getAttributeKey(i));
                AbstractNode node = setUpNodeModification(oldNode
                        .getAttributeKey(i));
                node.setParentKey(newNode.getNodeKey());
                tearDownNodeModification(node);
            }
            for (int i = 0; i < oldNode.getNamespaceCount(); i++) {
                newNode.insertNamespace(oldNode.getNamespaceKey(i));
                AbstractNode node = setUpNodeModification(oldNode
                        .getNamespaceKey(i));
                node.setParentKey(newNode.getNodeKey());
                tearDownNodeModification(node);
            }
            newNode.setChildCount(oldNode.getChildCount());
        }

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
