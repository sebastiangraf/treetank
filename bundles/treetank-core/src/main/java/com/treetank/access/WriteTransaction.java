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
import com.treetank.node.AbsNode;
import com.treetank.node.AbsStructNode;
import com.treetank.node.AttributeNode;
import com.treetank.node.DocumentRootNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.node.TextNode;
import com.treetank.page.UberPage;
import com.treetank.settings.EFixed;
import com.treetank.utils.LogWrapper;
import com.treetank.utils.TypedValue;

import org.slf4j.LoggerFactory;

/**
 * <h1>WriteTransaction</h1>
 * 
 * <p>
 * Single-threaded instance of only write transaction per session.
 * </p>
 */
public final class WriteTransaction extends ReadTransaction implements IWriteTransaction {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(WriteTransaction.class));
    
    /** Maximum number of node modifications before auto commit. */
    private final int mMaxNodeCount;

    /** Scheduler to commit after mMaxTime seconds. */
    private ScheduledExecutorService mCommitScheduler;

    /** Modification counter. */
    private long mModificationCount;

    /**
     * Constructor.
     * 
     * @param mTransactionID
     *            ID of transaction.
     * @param mSessionState
     *            State of the session.
     * @param mTransactionState
     *            State of this transaction.
     * @param maxNodeCount
     *            Maximum number of node modifications before auto commit.
     * @param maxTime
     *            Maximum number of seconds before auto commit.
     * @throws TreetankException
     *             if the reading of the props is failing or properties are not
     *             valid    
     */
    protected WriteTransaction(final long mTransactionID, final SessionState mSessionState,
        final WriteTransactionState mTransactionState, final int maxNodeCount, final int maxTime)
        throws TreetankException {
        super(mTransactionID, mSessionState, mTransactionState);

        // Do not accept negative values.
        if ((maxNodeCount < 0) || (maxTime < 0)) {
            throw new TreetankUsageException("Negative arguments are not accepted.");
        }

        // Only auto commit by node modifications if it is more then 0.
        mMaxNodeCount = maxNodeCount;
        mModificationCount = 0L;

        // Only auto commit by time if the time is more than 0 seconds.
        if (maxTime > 0) {
            mCommitScheduler = Executors.newScheduledThreadPool(1);
            mCommitScheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    if (mModificationCount > 0) {
                        try {
                            commit();
                        } catch (final TreetankException exc) {
                            LOGWRAPPER.error(exc);
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
    public synchronized long insertElementAsFirstChild(final QName mQname) throws TreetankException {
        if (getCurrentNode() instanceof ElementNode || getCurrentNode() instanceof DocumentRootNode) {

            final long parentKey = getCurrentNode().getNodeKey();
            final long leftSibKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getFirstChildKey();
            final ElementNode node =
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, mQname);
            return insertFirstChild(node);
        } else {
            throw new TreetankUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    public synchronized long insertElementAsRightSibling(final QName mQname) throws TreetankException {
        if (getCurrentNode() instanceof AbsStructNode) {

            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getRightSiblingKey();
            final ElementNode node =
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, mQname);
            return insertRightSibling(node);
        } else {
            throw new TreetankUsageException(
                "Insert is not allowed if current node is not an StructuralNode (either Text or Element)!");
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertTextAsFirstChild(final String mValueAsString) throws TreetankException {
        if (getCurrentNode() instanceof ElementNode || getCurrentNode() instanceof DocumentRootNode) {
            final byte[] value = TypedValue.getBytes(mValueAsString);
            final long parentKey = getCurrentNode().getNodeKey();
            final long leftSibKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getFirstChildKey();
            final TextNode node =
                getTransactionState().createTextNode(parentKey, leftSibKey, rightSibKey, value);
            return insertFirstChild(node);
        } else {
            throw new TreetankUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertTextAsRightSibling(final String mValueAsString) throws TreetankException {
        if (getCurrentNode() instanceof AbsStructNode) {
            final byte[] value = TypedValue.getBytes(mValueAsString);
            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getRightSiblingKey();
            final TextNode node =
                getTransactionState().createTextNode(parentKey, leftSibKey, rightSibKey, value);
            return insertRightSibling(node);
        } else {
            throw new TreetankUsageException(
                "Insert is not allowed if current node is not an StructuralNode (either Text or Element)!");
        }
    }

    public synchronized long insertAttribute(final QName mQname, final String mValueAsString)
        throws TreetankException {
        if (getCurrentNode() instanceof ElementNode) {
            final byte[] value = TypedValue.getBytes(mValueAsString);
            final long elementKey = getCurrentNode().getNodeKey();
            final AttributeNode node = getTransactionState().createAttributeNode(elementKey, mQname, value);
            return insertAttribute(node);

        } else {
            throw new TreetankUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long insertNamespace(final String mUri, final String mPrefix) throws 
    TreetankException {
        if (getCurrentNode() instanceof ElementNode) {
            final long parentKey = getCurrentNode().getNodeKey();
            // TODO check against QName class
            final int uriKey = getTransactionState().createNameKey(mUri);
            final int prefixKey = getTransactionState().createNameKey(mPrefix);

            final NamespaceNode node =
                getTransactionState().createNamespaceNode(parentKey, uriKey, prefixKey);
            return insertNamespace(node);
        } else {
            throw new TreetankUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void remove() throws TreetankException {

        assertNotClosed();
        mModificationCount++;
        final AbsNode node = (AbsNode)getCurrentNode();

        if (node.getKind() == ENodes.ROOT_KIND) {
            throw new TreetankUsageException("Root node can not be removed.");
        } else if (node instanceof AbsStructNode) {

            adaptNeighbours((AbsStructNode)node, null);

            // removing subtree
            final IAxis desc = new DescendantAxis(this, false);
            while (desc.hasNext()) {
                desc.next();
                removeIncludingRelated();

            }
            moveTo(node.getNodeKey());
            removeIncludingRelated();

            // Set current node.
            if (((AbsStructNode)node).hasRightSibling()) {
                moveTo(((AbsStructNode)node).getRightSiblingKey());
            } else if (((AbsStructNode)node).hasLeftSibling()) {
                moveTo(((AbsStructNode)node).getLeftSiblingKey());
            } else {
                moveTo(node.getParentKey());
            }

        } else if (getCurrentNode().getKind() == ENodes.ATTRIBUTE_KIND) {
            moveToParent();

            final ElementNode parent =
                (ElementNode)getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
            parent.removeAttribute(node.getNodeKey());
            getTransactionState().finishNodeModification(parent);
        }

    }

    private void removeIncludingRelated() throws TreetankIOException {
        final IItem node = getCurrentNode();
        if (node.getKind() == ENodes.ELEMENT_KIND) {
            // removing attributes
            for (int i = 0; i < ((ElementNode)node).getAttributeCount(); i++) {
                moveTo(((ElementNode)node).getAttributeKey(i));
                getTransactionState().removeNode((AbsNode)this.getCurrentNode());
            }
            // removing namespaces
            moveTo(node.getNodeKey());
            for (int i = 0; i < ((ElementNode)node).getNamespaceCount(); i++) {
                moveTo(((ElementNode)node).getNamespaceKey(i));
                getTransactionState().removeNode((AbsNode)this.getCurrentNode());
            }
        }
        // Remove old node.
        getTransactionState().removeNode((AbsNode)node);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setName(final String mName) throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbsNode oldNode = (AbsNode)getCurrentNode();
        final AbsNode newNode = createNodeToModify(oldNode);

        if (oldNode instanceof AbsStructNode) {
            adaptNeighbours((AbsStructNode)oldNode, (AbsStructNode)newNode);
        }
        newNode.setNameKey(((WriteTransactionState)getTransactionState()).createNameKey(mName));
        ((WriteTransactionState)getTransactionState()).removeNode(oldNode);
        setCurrentNode(newNode);

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setURI(final String mUri) throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbsNode oldNode = (AbsNode)getCurrentNode();
        final AbsNode newNode = createNodeToModify(oldNode);

        if (oldNode instanceof AbsStructNode) {
            adaptNeighbours((AbsStructNode)oldNode, (AbsStructNode)newNode);
        }
        newNode.setURIKey(((WriteTransactionState)getTransactionState()).createNameKey(mUri));
        ((WriteTransactionState)getTransactionState()).removeNode(oldNode);
        setCurrentNode(newNode);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setValue(final int mValueType, final byte[] mValue) throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbsNode oldNode = (AbsNode)getCurrentNode();
        final AbsNode newNode = createNodeToModify(oldNode);

        if (oldNode instanceof AbsStructNode) {
            adaptNeighbours((AbsStructNode)oldNode, (AbsStructNode)newNode);
        }
        newNode.setValue(mValueType, mValue);
        getTransactionState().removeNode(oldNode);
        setCurrentNode(newNode);

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setValue(final String mValue) throws TreetankIOException {
        setValue(getTransactionState().createNameKey("xs:untyped"), TypedValue.getBytes(mValue));
    }

    private final AbsNode createNodeToModify(final AbsNode mOldNode) throws TreetankIOException {
        AbsNode newNode = null;
        switch (mOldNode.getKind()) {
        case ELEMENT_KIND:
            newNode = getTransactionState().createElementNode((ElementNode)mOldNode);
            break;
        case ATTRIBUTE_KIND:
            newNode = getTransactionState().createAttributeNode((AttributeNode)mOldNode);
            break;
        case NAMESPACE_KIND:
            newNode = getTransactionState().createNamespaceNode((NamespaceNode)mOldNode);
            break;
        case TEXT_KIND:
            newNode = getTransactionState().createTextNode((TextNode)mOldNode);
            break;
        default:
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
                throw new TreetankUsageException("Must commit/abort transaction first");
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
        setTransactionState(getSessionState().createWriteTransactionState(getTransactionID(), revision,
            getRevisionNumber() - 1));
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
        final UberPage uberPage = getTransactionState().commit();

        // Remember succesfully committed uber page in session state.
        getSessionState().setLastCommittedUberPage(uberPage);

        // Reset modification counter.
        mModificationCount = 0L;

        getTransactionState().close();
        // Reset internal transaction state to new uber page.
        setTransactionState(getSessionState().createWriteTransactionState(getTransactionID(),
            getRevisionNumber(), getRevisionNumber()));

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
        setTransactionState(getSessionState().createWriteTransactionState(getTransactionID(), revisionToSet,
            revisionToSet));
    }

    private void intermediateCommitIfRequired() throws TreetankException {
        assertNotClosed();
        if ((mMaxNodeCount > 0) && (mModificationCount > mMaxNodeCount)) {
            commit();
        }
    }

    private long insertFirstChild(final AbsNode mNode) throws TreetankException {

        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();

        setCurrentNode(mNode);

        updateParentAfterInsert(true);
        updateRightSibling();

        return mNode.getNodeKey();
    }

    private long insertRightSibling(final AbsNode mNode) throws TreetankException {

        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();

        if (getCurrentNode().getNodeKey() == (Long)EFixed.ROOT_NODE_KEY.getStandardProperty()) {
            throw new TreetankUsageException("Root node can not have siblings.");
        }

        setCurrentNode(mNode);

        updateParentAfterInsert(false);
        updateLeftSibling();
        updateRightSibling();

        return mNode.getNodeKey();
    }

    private long insertAttribute(final AttributeNode mNode) throws TreetankException {

        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();

        if (getCurrentNode().getKind() != ENodes.ELEMENT_KIND) {
            throw new IllegalStateException("Only element nodes can have attributes.");
        }

        setCurrentNode(mNode);

        final AbsNode parentNode = getTransactionState().prepareNodeForModification(mNode.getParentKey());
        ((ElementNode)parentNode).insertAttribute(mNode.getNodeKey());
        getTransactionState().finishNodeModification(parentNode);

        return mNode.getNodeKey();
    }

    private long insertNamespace(final NamespaceNode mNode) throws TreetankException {

        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();

        if (getCurrentNode().getKind() != ENodes.ELEMENT_KIND) {
            throw new IllegalStateException("Only element nodes can have namespaces.");
        }

        setCurrentNode(mNode);

        final AbsNode parentNode = getTransactionState().prepareNodeForModification(mNode.getParentKey());
        ((ElementNode)parentNode).insertNamespace(mNode.getNodeKey());
        getTransactionState().finishNodeModification(parentNode);

        return mNode.getNodeKey();
    }

    private void updateParentAfterInsert(final boolean mUpdateFirstChild) throws TreetankIOException {
        final AbsStructNode parentNode =
            (AbsStructNode)getTransactionState().prepareNodeForModification(getCurrentNode().getParentKey());
        parentNode.incrementChildCount();
        if (mUpdateFirstChild) {
            parentNode.setFirstChildKey(getCurrentNode().getNodeKey());
        }
        getTransactionState().finishNodeModification(parentNode);

    }

    private void updateRightSibling() throws TreetankIOException {
        final AbsStructNode current = (AbsStructNode)getCurrentNode();

        if (current.hasRightSibling()) {
            final AbsStructNode rightSiblingNode =
                (AbsStructNode)getTransactionState().prepareNodeForModification(current.getRightSiblingKey());
            rightSiblingNode.setLeftSiblingKey(getCurrentNode().getNodeKey());
            getTransactionState().finishNodeModification(rightSiblingNode);

        }
    }

    private void updateLeftSibling() throws TreetankIOException {
        final AbsStructNode current = (AbsStructNode)getCurrentNode();
        if (current.hasLeftSibling()) {
            final AbsStructNode leftSiblingNode =
                (AbsStructNode)getTransactionState().prepareNodeForModification(current.getLeftSiblingKey());
            leftSiblingNode.setRightSiblingKey(getCurrentNode().getNodeKey());
            getTransactionState().finishNodeModification(leftSiblingNode);
        }
    }

    private void adaptNeighbours(final AbsStructNode mOldNode, final AbsStructNode mNewNode)
        throws TreetankIOException {

        // // Remember all related nodes.
        AbsStructNode leftSibling = null;
        AbsStructNode rightSibling = null;
        AbsStructNode parent = null;
        AbsStructNode firstChild = null;

        // getting the neighbourhood
        if (mOldNode.hasLeftSibling()) {
            moveToLeftSibling();
            leftSibling = (AbsStructNode)getCurrentNode();
            moveToRightSibling();
        }
        if (mOldNode.hasRightSibling()) {
            moveToRightSibling();
            rightSibling = (AbsStructNode)getCurrentNode();
            moveToLeftSibling();
        }
        if (!moveToParent()) {
            throw new IllegalStateException("Node has no parent!");
        }
        parent = (AbsStructNode)getCurrentNode();
        moveTo(mOldNode.getNodeKey());
        if (mOldNode.hasFirstChild()) {
            moveToFirstChild();
            firstChild = (AbsStructNode)getCurrentNode();
        }
        moveTo(mOldNode.getNodeKey());

        // Adapt left sibling node if there is one.
        if (leftSibling != null) {
            leftSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(leftSibling.getNodeKey());
            if (mNewNode == null) {
                if (rightSibling != null) {
                    ((AbsStructNode)leftSibling).setRightSiblingKey(rightSibling.getNodeKey());
                } else {
                    leftSibling.setRightSiblingKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
                }
            } else {
                leftSibling.setRightSiblingKey(mNewNode.getNodeKey());
            }
            getTransactionState().finishNodeModification(leftSibling);
        }

        // Adapt right sibling node if there is one.
        if (rightSibling != null) {
            rightSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(rightSibling.getNodeKey());
            if (mNewNode == null) {
                if (leftSibling != null) {
                    rightSibling.setLeftSiblingKey(leftSibling.getNodeKey());
                } else {
                    rightSibling.setLeftSiblingKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
                }
            } else {
                rightSibling.setLeftSiblingKey(mNewNode.getNodeKey());
            }
            getTransactionState().finishNodeModification(rightSibling);
        }

        // Adapt parent.
        parent = (AbsStructNode)getTransactionState().prepareNodeForModification(parent.getNodeKey());
        if (mNewNode == null) {
            parent.decrementChildCount();
        }
        if (parent.getFirstChildKey() == mOldNode.getNodeKey()) {
            if (mNewNode == null) {
                if (rightSibling != null) {
                    parent.setFirstChildKey(rightSibling.getNodeKey());
                } else {
                    parent.setFirstChildKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
                }
            } else {
                parent.setFirstChildKey(mNewNode.getNodeKey());
            }
        }
        getTransactionState().finishNodeModification(parent);

        // adapt associated nodes
        if (mNewNode != null) {
            if (firstChild != null) {
                setCurrentNode(firstChild);
                do {
                    final AbsNode node =
                        getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
                    node.setParentKey(mNewNode.getNodeKey());
                    getTransactionState().finishNodeModification(node);
                } while(moveToRightSibling());
            }
            if (mOldNode.getKind() == ENodes.ELEMENT_KIND) {
                // setting the attributes and namespaces
                for (int i = 0; i < ((ElementNode)mOldNode).getAttributeCount(); i++) {
                    ((ElementNode)mNewNode).insertAttribute(((ElementNode)mOldNode).getAttributeKey(i));
                    AbsNode node =
                        getTransactionState().prepareNodeForModification(
                            ((ElementNode)mOldNode).getAttributeKey(i));
                    node.setParentKey(mNewNode.getNodeKey());
                    getTransactionState().finishNodeModification(node);
                }
                for (int i = 0; i < ((ElementNode)mOldNode).getNamespaceCount(); i++) {
                    ((ElementNode)mNewNode).insertNamespace(((ElementNode)mOldNode).getNamespaceKey(i));
                    AbsNode node =
                        getTransactionState().prepareNodeForModification(
                            ((ElementNode)mOldNode).getNamespaceKey(i));
                    node.setParentKey(mNewNode.getNodeKey());
                    getTransactionState().finishNodeModification(node);
                }
            }
        }

    }

    /**
     * Getter for superclasses.
     * 
     * @return The state of this transaction.
     */
    @Override
    public WriteTransactionState getTransactionState() {
        return (WriteTransactionState)super.getTransactionState();
    }

}
