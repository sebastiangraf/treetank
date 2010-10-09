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
 * 
 * @author Sebastian Graf, University of Konstanz
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
    @Override
    public synchronized long insertElementAsFirstChild(final QName mQname) throws TreetankException {
        if (getCurrentNode() instanceof ElementNode || getCurrentNode() instanceof DocumentRootNode) {

            checkAccessAndCommit();

            final long parentKey = getCurrentNode().getNodeKey();
            final long leftSibKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getFirstChildKey();
            final ElementNode node =
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, mQname);

            adaptForInsert(node, true);
            setCurrentNode(node);

            return node.getNodeKey();
        } else {
            throw new TreetankUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsRightSibling(final QName mQname) throws TreetankException {
        if (getCurrentNode() instanceof AbsStructNode) {

            checkAccessAndCommit();

            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getRightSiblingKey();
            final ElementNode node =
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, mQname);

            adaptForInsert(node, false);
            setCurrentNode(node);

            return node.getNodeKey();
        } else {
            throw new TreetankUsageException(
                "Insert is not allowed if current node is not an StructuralNode (either Text or Element)!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsFirstChild(final String mValueAsString) throws TreetankException {
        if (getCurrentNode() instanceof ElementNode || getCurrentNode() instanceof DocumentRootNode) {

            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(mValueAsString);
            final long parentKey = getCurrentNode().getNodeKey();
            final long leftSibKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getFirstChildKey();
            final TextNode node =
                getTransactionState().createTextNode(parentKey, leftSibKey, rightSibKey, value);

            adaptForInsert(node, true);
            setCurrentNode(node);

            return node.getNodeKey();
        } else {
            throw new TreetankUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsRightSibling(final String mValueAsString) throws TreetankException {
        if (getCurrentNode() instanceof AbsStructNode) {

            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(mValueAsString);
            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getRightSiblingKey();
            final TextNode node =
                getTransactionState().createTextNode(parentKey, leftSibKey, rightSibKey, value);

            adaptForInsert(node, false);
            setCurrentNode(node);

            return node.getNodeKey();

        } else {
            throw new TreetankUsageException(
                "Insert is not allowed if current node is not an StructuralNode (either Text or Element)!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertAttribute(final QName mQname, final String mValueAsString)
        throws TreetankException {
        if (getCurrentNode() instanceof ElementNode) {

            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(mValueAsString);
            final long elementKey = getCurrentNode().getNodeKey();
            final AttributeNode node = getTransactionState().createAttributeNode(elementKey, mQname, value);

            final AbsNode parentNode = getTransactionState().prepareNodeForModification(node.getParentKey());
            ((ElementNode)parentNode).insertAttribute(node.getNodeKey());
            getTransactionState().finishNodeModification(parentNode);

            adaptForInsert(node, false);

            setCurrentNode(node);

            return node.getNodeKey();

        } else {
            throw new TreetankUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertNamespace(final String mUri, final String mPrefix)
        throws TreetankException {
        if (getCurrentNode() instanceof ElementNode) {

            checkAccessAndCommit();

            // TODO check against QName class
            final int uriKey = getTransactionState().createNameKey(mUri);
            final int prefixKey = getTransactionState().createNameKey(mPrefix);
            final long elementKey = getCurrentNode().getNodeKey();

            final NamespaceNode node =
                getTransactionState().createNamespaceNode(elementKey, uriKey, prefixKey);

            final AbsNode parentNode = getTransactionState().prepareNodeForModification(node.getParentKey());
            ((ElementNode)parentNode).insertNamespace(node.getNodeKey());
            getTransactionState().finishNodeModification(parentNode);

            adaptForInsert(node, false);

            setCurrentNode(node);

            return node.getNodeKey();
        } else {
            throw new TreetankUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove() throws TreetankException {
        checkAccessAndCommit();
        if (getCurrentNode().getKind() == ENodes.ROOT_KIND) {
            throw new TreetankUsageException("Root node can not be removed.");
        } else if (getCurrentNode() instanceof AbsStructNode) {
            final AbsStructNode node = (AbsStructNode)getCurrentNode();
            // // removing subtree
            final IAxis desc = new DescendantAxis(this, false);
            while (desc.hasNext()) {
                desc.next();
                getTransactionState().removeNode((AbsNode)getCurrentNode());

            }
            moveTo(node.getNodeKey());
            adaptForRemove(node);

            // Set current node.
            if (node.hasRightSibling()) {
                moveTo(node.getRightSiblingKey());
            } else if (node.hasLeftSibling()) {
                moveTo(node.getLeftSiblingKey());
            } else {
                moveTo(node.getParentKey());
            }

        } else if (getCurrentNode().getKind() == ENodes.ATTRIBUTE_KIND) {
            final AbsNode node = (AbsNode)getCurrentNode();

            final ElementNode parent =
                (ElementNode)getTransactionState().prepareNodeForModification(node.getParentKey());
            parent.removeAttribute(node.getNodeKey());
            getTransactionState().finishNodeModification(parent);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setName(final String mName) throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbsNode oldNode = (AbsNode)getCurrentNode();
        final AbsNode newNode = createNodeToModify(oldNode);

        if (oldNode instanceof AbsStructNode) {
            adaptForUpdate((AbsStructNode)oldNode, (AbsStructNode)newNode);
        }
        newNode.setNameKey(getTransactionState().createNameKey(mName));
        getTransactionState().removeNode(oldNode);
        setCurrentNode(newNode);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setURI(final String mUri) throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbsNode oldNode = (AbsNode)getCurrentNode();
        final AbsNode newNode = createNodeToModify(oldNode);

        if (oldNode instanceof AbsStructNode) {
            adaptForUpdate((AbsStructNode)oldNode, (AbsStructNode)newNode);
        }
        newNode.setURIKey(getTransactionState().createNameKey(mUri));
        getTransactionState().removeNode(oldNode);
        setCurrentNode(newNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final int mValueType, final byte[] mValue) throws TreetankIOException {

        assertNotClosed();
        mModificationCount++;

        final AbsNode oldNode = (AbsNode)getCurrentNode();
        final AbsNode newNode = createNodeToModify(oldNode);

        if (oldNode instanceof AbsStructNode) {
            adaptForUpdate((AbsStructNode)oldNode, (AbsStructNode)newNode);
        }
        newNode.setValue(mValueType, mValue);
        getTransactionState().removeNode(oldNode);
        setCurrentNode(newNode);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final String mValue) throws TreetankIOException {
        setValue(getTransactionState().createNameKey("xs:untyped"), TypedValue.getBytes(mValue));
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
    @Override
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
    @Override
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

    private void checkAccessAndCommit() throws TreetankException {
        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();
    }

    // ////////////////////////////////////////////////////////////
    // insert operation
    // //////////////////////////////////////////////////////////

    private void adaptForInsert(final AbsNode newNode, final boolean addAsFirstChild)
        throws TreetankIOException {

        if (newNode instanceof AbsStructNode) {
            final AbsStructNode strucNode = (AbsStructNode)newNode;
            final AbsStructNode parent =
                (AbsStructNode)getTransactionState().prepareNodeForModification(newNode.getParentKey());
            parent.incrementChildCount();
            if (addAsFirstChild) {
                parent.setFirstChildKey(newNode.getNodeKey());
            }
            getTransactionState().finishNodeModification(parent);

            if (strucNode.hasRightSibling()) {
                final AbsStructNode rightSiblingNode =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(
                        strucNode.getRightSiblingKey());
                rightSiblingNode.setLeftSiblingKey(newNode.getNodeKey());
                getTransactionState().finishNodeModification(rightSiblingNode);
            }
            if (strucNode.hasLeftSibling()) {
                final AbsStructNode leftSiblingNode =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(
                        strucNode.getLeftSiblingKey());
                leftSiblingNode.setRightSiblingKey(newNode.getNodeKey());
                getTransactionState().finishNodeModification(leftSiblingNode);
            }
        }

        adaptHashesWithAdd();

    }

    // ////////////////////////////////////////////////////////////
    // end of insert operation
    // ////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////
    // update operation
    // ////////////////////////////////////////////////////////////

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

    private void adaptForUpdate(final AbsStructNode mOldNode, final AbsStructNode mNewNode)
        throws TreetankIOException {
        // Adapt left sibling node if there is one.
        if (mOldNode.hasLeftSibling()) {
            final AbsStructNode leftSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(mOldNode.getLeftSiblingKey());
            leftSibling.setRightSiblingKey(mNewNode.getNodeKey());
            getTransactionState().finishNodeModification(leftSibling);
        }

        // Adapt right sibling node if there is one.
        if (mOldNode.hasRightSibling()) {
            final AbsStructNode rightSibling =
                (AbsStructNode)getTransactionState()
                    .prepareNodeForModification(mOldNode.getRightSiblingKey());
            rightSibling.setLeftSiblingKey(mNewNode.getNodeKey());
            getTransactionState().finishNodeModification(rightSibling);
        }

        // Adapt parent, if node has now left sibling it is a first child.
        if (!mOldNode.hasLeftSibling()) {
            final AbsStructNode parent =
                (AbsStructNode)getTransactionState().prepareNodeForModification(mOldNode.getParentKey());
            parent.setFirstChildKey(mNewNode.getNodeKey());
            getTransactionState().finishNodeModification(parent);
        }

        if (mOldNode.hasFirstChild()) {
            moveToFirstChild();
            final AbsStructNode firstChild = (AbsStructNode)getCurrentNode();
            setCurrentNode(firstChild);
            do {
                final AbsNode node =
                    getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
                node.setParentKey(mNewNode.getNodeKey());
                getTransactionState().finishNodeModification(node);
            } while (moveToRightSibling());
        }

        if (mOldNode.getKind() == ENodes.ELEMENT_KIND) {
            // setting the attributes and namespaces
            for (int i = 0; i < ((ElementNode)mOldNode).getAttributeCount(); i++) {
                ((ElementNode)mNewNode).insertAttribute(((ElementNode)mOldNode).getAttributeKey(i));
                final AbsNode node =
                    getTransactionState().prepareNodeForModification(
                        ((ElementNode)mOldNode).getAttributeKey(i));
                node.setParentKey(mNewNode.getNodeKey());
                getTransactionState().finishNodeModification(node);
            }
            for (int i = 0; i < ((ElementNode)mOldNode).getNamespaceCount(); i++) {
                ((ElementNode)mNewNode).insertNamespace(((ElementNode)mOldNode).getNamespaceKey(i));
                final AbsNode node =
                    getTransactionState().prepareNodeForModification(
                        ((ElementNode)mOldNode).getNamespaceKey(i));
                node.setParentKey(mNewNode.getNodeKey());
                getTransactionState().finishNodeModification(node);
            }
        }

        adaptHashedWithUpdate(mOldNode);

    }

    // ////////////////////////////////////////////////////////////
    // end of update operation
    // ////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////
    // remove operation
    // ////////////////////////////////////////////////////////////

    private void adaptForRemove(final AbsStructNode oldNode) throws TreetankIOException {

        // Adapt left sibling node if there is one.
        if (oldNode.hasLeftSibling()) {
            final AbsStructNode leftSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(oldNode.getLeftSiblingKey());
            leftSibling.setRightSiblingKey(oldNode.getRightSiblingKey());
            getTransactionState().finishNodeModification(leftSibling);
        }

        // Adapt right sibling node if there is one.
        if (oldNode.hasRightSibling()) {
            final AbsStructNode rightSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(oldNode.getRightSiblingKey());
            rightSibling.setLeftSiblingKey(oldNode.getLeftSiblingKey());
            getTransactionState().finishNodeModification(rightSibling);
        }

        // Adapt parent, if node has now left sibling it is a first child.
        final AbsStructNode parent =
            (AbsStructNode)getTransactionState().prepareNodeForModification(oldNode.getParentKey());
        if (!oldNode.hasLeftSibling()) {
            parent.setFirstChildKey(oldNode.getRightSiblingKey());
        }
        parent.decrementChildCount();
        getTransactionState().finishNodeModification(parent);

        if (oldNode.getKind() == ENodes.ELEMENT_KIND) {
            // removing attributes
            for (int i = 0; i < ((ElementNode)oldNode).getAttributeCount(); i++) {
                moveTo(((ElementNode)oldNode).getAttributeKey(i));
                getTransactionState().removeNode((AbsNode)this.getCurrentNode());
            }
            // removing namespaces
            moveTo(oldNode.getNodeKey());
            for (int i = 0; i < ((ElementNode)oldNode).getNamespaceCount(); i++) {
                moveTo(((ElementNode)oldNode).getNamespaceKey(i));
                getTransactionState().removeNode((AbsNode)this.getCurrentNode());
            }
        }

        // Remove old node.
        getTransactionState().removeNode((AbsNode)oldNode);
    }

    // ////////////////////////////////////////////////////////////
    // end of remove operation
    // ////////////////////////////////////////////////////////////

    private void intermediateCommitIfRequired() throws TreetankException {
        assertNotClosed();
        if ((mMaxNodeCount > 0) && (mModificationCount > mMaxNodeCount)) {
            commit();
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

    // private void adaptHashesWithRemove(final AbsNode oldNode) {
    // final int prime = 97;
    //
    // int result = 1;
    // result = prime * result + Arrays.hashCode(mIntData);
    //
    // int hash = oldNode.hashCode();
    //
    // }

    private void adaptHashesWithAdd() throws TreetankIOException {
        final IItem startNode = getCurrentNode();
        final int prime = 97;
        long result = 1;
        do {

            getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
            if (getCurrentNode().getHash() == 0) {
                result = prime * result + getCurrentNode().hashCode();
            } else {
                result = prime * result + getCurrentNode().getHash();
            }
            getCurrentNode().setHash(result);
            getTransactionState().finishNodeModification(getCurrentNode());

        } while (moveTo(getCurrentNode().getParentKey()));
        moveTo(startNode.getNodeKey());
    }

    private void adaptHashedWithUpdate(final IItem oldNode) throws TreetankIOException {
        final IItem newNode = getCurrentNode();
        final int prime = 97;
        long resultOld = oldNode.getHash();
        long resultNew = newNode.hashCode();

        do {
            getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
            resultOld = (resultOld - getCurrentNode().getHash()) / prime;
            if (getCurrentNode().getHash() == 0) {
                resultNew = prime * resultOld + getCurrentNode().hashCode();
            } else {
                resultNew = prime * resultOld + getCurrentNode().getHash();
            }

            getCurrentNode().setHash(resultNew);
            getTransactionState().finishNodeModification(getCurrentNode());

        } while (moveTo(getCurrentNode().getParentKey()));
        moveTo(newNode.getNodeKey());
    }

}
