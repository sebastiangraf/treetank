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
import com.treetank.api.IStructuralItem;
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
import com.treetank.settings.EDatabaseSetting;
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
     * How is the Hash for this storage computed?
     */
    public enum HashKind {
        /** Rolling hash, only nodes on ancestor axis are touched. */
        Rolling,
        /** Postorder hash, all nodes on ancestor plus postorder are at least read. */
        Postorder,
        /** No hash structure after all. */
        None;
    }

    /** Prime for computing the hash. */
    private static final int PRIME = 3;

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

    /** Hash kind of Structure. */
    private final HashKind mHashKind;

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
        mHashKind =
            HashKind.valueOf(getSessionState().mDatabaseConfiguration.getProps().getProperty(
                EDatabaseSetting.HASHKIND_TYPE.name()));
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
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, 0, mQname);

            setCurrentNode(node);
            adaptForInsert(node, true);
            adaptHashesWithAdd();

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
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, 0, mQname);

            setCurrentNode(node);
            adaptForInsert(node, false);
            adaptHashesWithAdd();

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

            setCurrentNode(node);
            adaptForInsert(node, true);
            adaptHashesWithAdd();

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

            setCurrentNode(node);
            adaptForInsert(node, false);
            adaptHashesWithAdd();

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

            setCurrentNode(node);
            adaptForInsert(node, false);

            adaptHashesWithAdd();
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

            setCurrentNode(node);
            adaptForInsert(node, false);
            adaptHashesWithAdd();
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
            adaptHashesWithRemove();

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
            adaptHashesWithRemove();
            moveToParent();
        } else if (getCurrentNode().getKind() == ENodes.NAMESPACE_KIND) {
            final AbsNode node = (AbsNode)getCurrentNode();

            final ElementNode parent =
                (ElementNode)getTransactionState().prepareNodeForModification(node.getParentKey());
            parent.removeNamespace(node.getNodeKey());
            getTransactionState().finishNodeModification(parent);
            adaptHashesWithRemove();
            moveToParent();
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

        adaptHashedWithUpdate(oldNode.hashCode());

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

        adaptHashedWithUpdate(oldNode.getHash());
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

        adaptHashedWithUpdate(oldNode.getHash());

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

    /**
     * Checking write access and intermediate commit.
     * 
     * @throws TreetankException
     *             if anything weird happens.
     */
    private void checkAccessAndCommit() throws TreetankException {
        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();
    }

    // ////////////////////////////////////////////////////////////
    // insert operation
    // //////////////////////////////////////////////////////////

    /**
     * Adapting everything for insert operations.
     * 
     * @param paramNewNode
     *            pointer of the new node to be inserted
     * @param paramAsFirstChild
     *            should the new element added as a first child?
     * @throws TreetankIOException
     *             if anything weird happens
     */
    private void adaptForInsert(final AbsNode paramNewNode, final boolean paramAsFirstChild)
        throws TreetankIOException {

        if (paramNewNode instanceof AbsStructNode) {
            final AbsStructNode strucNode = (AbsStructNode)paramNewNode;
            final AbsStructNode parent =
                (AbsStructNode)getTransactionState().prepareNodeForModification(paramNewNode.getParentKey());
            parent.incrementChildCount();
            if (paramAsFirstChild) {
                parent.setFirstChildKey(paramNewNode.getNodeKey());
            }
            getTransactionState().finishNodeModification(parent);

            if (strucNode.hasRightSibling()) {
                final AbsStructNode rightSiblingNode =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(
                        strucNode.getRightSiblingKey());
                rightSiblingNode.setLeftSiblingKey(paramNewNode.getNodeKey());
                getTransactionState().finishNodeModification(rightSiblingNode);
            }
            if (strucNode.hasLeftSibling()) {
                final AbsStructNode leftSiblingNode =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(
                        strucNode.getLeftSiblingKey());
                leftSiblingNode.setRightSiblingKey(paramNewNode.getNodeKey());
                getTransactionState().finishNodeModification(leftSiblingNode);
            }
        }

    }

    // ////////////////////////////////////////////////////////////
    // end of insert operation
    // ////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////
    // update operation
    // ////////////////////////////////////////////////////////////

    /**
     * Creating a new element for an existing node.
     * 
     * @param paramOldNode
     *            new Node to be cloned
     * @throws TreetankIOException
     *             if anything weird happens
     * @return an {@link AbsNode} which is cloned
     */
    private AbsNode createNodeToModify(final AbsNode paramOldNode) throws TreetankIOException {
        AbsNode newNode = null;
        switch (paramOldNode.getKind()) {
        case ELEMENT_KIND:
            newNode = getTransactionState().createElementNode((ElementNode)paramOldNode);
            break;
        case ATTRIBUTE_KIND:
            newNode = getTransactionState().createAttributeNode((AttributeNode)paramOldNode);
            break;
        case NAMESPACE_KIND:
            newNode = getTransactionState().createNamespaceNode((NamespaceNode)paramOldNode);
            break;
        case TEXT_KIND:
            newNode = getTransactionState().createTextNode((TextNode)paramOldNode);
            break;
        default:
            break;
        }
        return newNode;
    }

    /**
     * Adapting everything for update operations.
     * 
     * @param paramOldNode
     *            pointer of the old node to be replaces
     * @param paramNewNode
     *            pointer of new node to be inserted
     * @throws TreetankIOException
     *             if anything weird happens
     */
    private void adaptForUpdate(final AbsStructNode paramOldNode, final AbsStructNode paramNewNode)
        throws TreetankIOException {
        // Adapt left sibling node if there is one.
        if (paramOldNode.hasLeftSibling()) {
            final AbsStructNode leftSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(
                    paramOldNode.getLeftSiblingKey());
            leftSibling.setRightSiblingKey(paramNewNode.getNodeKey());
            getTransactionState().finishNodeModification(leftSibling);
        }

        // Adapt right sibling node if there is one.
        if (paramOldNode.hasRightSibling()) {
            final AbsStructNode rightSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(
                    paramOldNode.getRightSiblingKey());
            rightSibling.setLeftSiblingKey(paramNewNode.getNodeKey());
            getTransactionState().finishNodeModification(rightSibling);
        }

        // Adapt parent, if node has now left sibling it is a first child.
        if (!paramOldNode.hasLeftSibling()) {
            final AbsStructNode parent =
                (AbsStructNode)getTransactionState().prepareNodeForModification(paramOldNode.getParentKey());
            parent.setFirstChildKey(paramNewNode.getNodeKey());
            getTransactionState().finishNodeModification(parent);
        }

        // Adapt first child + all childs
        if (paramOldNode.hasFirstChild()) {
            moveToFirstChild();
            final AbsStructNode firstChild = (AbsStructNode)getCurrentNode();
            setCurrentNode(firstChild);
            do {
                final AbsNode node =
                    getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
                node.setParentKey(paramNewNode.getNodeKey());
                getTransactionState().finishNodeModification(node);
            } while (moveToRightSibling());
        }

        // Caring about attributes and namespaces
        if (paramOldNode.getKind() == ENodes.ELEMENT_KIND) {
            // setting the attributes and namespaces
            for (int i = 0; i < ((ElementNode)paramOldNode).getAttributeCount(); i++) {
                ((ElementNode)paramNewNode).insertAttribute(((ElementNode)paramOldNode).getAttributeKey(i));
                final AbsNode node =
                    getTransactionState().prepareNodeForModification(
                        ((ElementNode)paramOldNode).getAttributeKey(i));
                node.setParentKey(paramNewNode.getNodeKey());
                getTransactionState().finishNodeModification(node);
            }
            for (int i = 0; i < ((ElementNode)paramOldNode).getNamespaceCount(); i++) {
                ((ElementNode)paramNewNode).insertNamespace(((ElementNode)paramOldNode).getNamespaceKey(i));
                final AbsNode node =
                    getTransactionState().prepareNodeForModification(
                        ((ElementNode)paramOldNode).getNamespaceKey(i));
                node.setParentKey(paramNewNode.getNodeKey());
                getTransactionState().finishNodeModification(node);
            }
        }

    }

    // ////////////////////////////////////////////////////////////
    // end of update operation
    // ////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////
    // remove operation
    // ////////////////////////////////////////////////////////////

    /**
     * Adapting everything for remove operations.
     * 
     * @param paramOldNode
     *            pointer of the old node to be replaces
     * @throws TreetankIOException
     *             if anything weird happens
     */
    private void adaptForRemove(final AbsStructNode paramOldNode) throws TreetankIOException {

        // Adapt left sibling node if there is one.
        if (paramOldNode.hasLeftSibling()) {
            final AbsStructNode leftSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(
                    paramOldNode.getLeftSiblingKey());
            leftSibling.setRightSiblingKey(paramOldNode.getRightSiblingKey());
            getTransactionState().finishNodeModification(leftSibling);
        }

        // Adapt right sibling node if there is one.
        if (paramOldNode.hasRightSibling()) {
            final AbsStructNode rightSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(
                    paramOldNode.getRightSiblingKey());
            rightSibling.setLeftSiblingKey(paramOldNode.getLeftSiblingKey());
            getTransactionState().finishNodeModification(rightSibling);
        }

        // Adapt parent, if node has now left sibling it is a first child.
        final AbsStructNode parent =
            (AbsStructNode)getTransactionState().prepareNodeForModification(paramOldNode.getParentKey());
        if (!paramOldNode.hasLeftSibling()) {
            parent.setFirstChildKey(paramOldNode.getRightSiblingKey());
        }
        parent.decrementChildCount();
        getTransactionState().finishNodeModification(parent);

        if (paramOldNode.getKind() == ENodes.ELEMENT_KIND) {
            // removing attributes
            for (int i = 0; i < ((ElementNode)paramOldNode).getAttributeCount(); i++) {
                moveTo(((ElementNode)paramOldNode).getAttributeKey(i));
                getTransactionState().removeNode((AbsNode)this.getCurrentNode());
            }
            // removing namespaces
            moveTo(paramOldNode.getNodeKey());
            for (int i = 0; i < ((ElementNode)paramOldNode).getNamespaceCount(); i++) {
                moveTo(((ElementNode)paramOldNode).getNamespaceKey(i));
                getTransactionState().removeNode((AbsNode)this.getCurrentNode());
            }
        }

        // Remove old node.
        getTransactionState().removeNode((AbsNode)paramOldNode);
    }

    // ////////////////////////////////////////////////////////////
    // end of remove operation
    // ////////////////////////////////////////////////////////////

    /**
     * Making an intermediate commit based on set attributes.
     * 
     * @throws TreetankException
     *             if commit fails.
     */
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

    /**
     * Adapting the structure with a hash for all ancestors only with insert.
     * 
     * @throws TreetankIOException
     *             of anything weird happened.
     */
    private void adaptHashesWithAdd() throws TreetankIOException {
        switch (mHashKind) {
        case Rolling:
            rollingAdd();
            break;
        case Postorder:
            postorderAdd();
            break;
        default:
        }

    }

    /**
     * Adapting the structure with a hash for all ancestors only with remove.
     * 
     * @throws TreetankIOException
     *             of anything weird happened.
     */
    private void adaptHashesWithRemove() throws TreetankIOException {
        switch (mHashKind) {
        case Rolling:
            rollingRemove();
            break;
        case Postorder:
            postorderRemove();
            break;
        default:
        }
    }

    /**
     * Adapting the structure with a hash for all ancestors only with update.
     * 
     * @param paramOldHash
     *            paramOldHash to be removed
     * @throws TreetankIOException
     *             of anything weird happened.
     */
    private void adaptHashedWithUpdate(final long paramOldHash) throws TreetankIOException {
        switch (mHashKind) {
        case Rolling:
            rollingUpdate(paramOldHash);
            break;
        case Postorder:
            postorderAdd();
            break;
        default:
        }
    }

    /**
     * Removal operation for postorder hash computation.
     * 
     * @throws TreetankIOException
     *             if anything weird happens
     */
    public void postorderRemove() throws TreetankIOException {
        moveTo(getCurrentNode().getParentKey());
        postorderAdd();
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with insert.
     * 
     * @throws TreetankIOException
     *             of anything weird happened.
     */
    private void postorderAdd() throws TreetankIOException {
        // start with hash to add
        final IItem startNode = getCurrentNode();
        // long for adapting the hash of the parent
        long hashCodeForParent = 0;
        // adapting the parent if the current node is no structural one.
        if (!(getCurrentNode() instanceof IStructuralItem)) {
            getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
            getCurrentNode().setHash(getCurrentNode().hashCode());
            getTransactionState().finishNodeModification(getCurrentNode());
            moveTo(getCurrentNode().getParentKey());
        }
        // Cursor to root
        IStructuralItem cursorToRoot;
        do {
            cursorToRoot =
                (IStructuralItem)getTransactionState().prepareNodeForModification(
                    getCurrentNode().getNodeKey());
            hashCodeForParent = getCurrentNode().hashCode() + hashCodeForParent * PRIME;
            // Caring about attributes and namespaces if node is an element.
            if (cursorToRoot.getKind() == ENodes.ELEMENT_KIND) {
                final ElementNode currentElement = (ElementNode)cursorToRoot;
                // setting the attributes and namespaces
                for (int i = 0; i < ((ElementNode)cursorToRoot).getAttributeCount(); i++) {
                    moveTo(currentElement.getAttributeKey(i));
                    hashCodeForParent = getCurrentNode().hashCode() + hashCodeForParent * PRIME;
                }
                for (int i = 0; i < ((ElementNode)cursorToRoot).getNamespaceCount(); i++) {
                    moveTo(currentElement.getNamespaceKey(i));
                    hashCodeForParent = getCurrentNode().hashCode() + hashCodeForParent * PRIME;
                }
                moveTo(cursorToRoot.getNodeKey());
            }

            // Caring about the children of a node
            if (moveTo(getNodeIfStructural().getFirstChildKey())) {
                do {
                    hashCodeForParent = getCurrentNode().getHash() + hashCodeForParent * PRIME;
                } while (moveTo(getNodeIfStructural().getRightSiblingKey()));
                moveTo(getNodeIfStructural().getParentKey());
            }

            // setting hash and resetting hash
            cursorToRoot.setHash(hashCodeForParent);
            getTransactionState().finishNodeModification(cursorToRoot);
            hashCodeForParent = 0;
        } while (moveTo(cursorToRoot.getParentKey()));

        setCurrentNode(startNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with update.
     * 
     * @param paramOldHash
     *            paramOldHash to be removed
     * @throws TreetankIOException
     *             of anything weird happened.
     */
    private void rollingUpdate(final long paramOldHash) throws TreetankIOException {
        final IItem newNode = getCurrentNode();
        final long newNodeHash = newNode.hashCode();
        long resultNew = newNode.hashCode();

        // go the path to the root
        do {
            getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
            if (getCurrentNode().getNodeKey() == newNode.getNodeKey()) {
                resultNew = getCurrentNode().getHash() - paramOldHash;
                resultNew = resultNew + newNodeHash;
            } else {
                resultNew = getCurrentNode().getHash() - (paramOldHash * PRIME);
                resultNew = resultNew + newNodeHash * PRIME;
            }
            getCurrentNode().setHash(resultNew);
            getTransactionState().finishNodeModification(getCurrentNode());

        } while (moveTo(getCurrentNode().getParentKey()));

        setCurrentNode(newNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with remove.
     * 
     * @throws TreetankIOException
     *             of anything weird happened.
     */
    private void rollingRemove() throws TreetankIOException {
        final IItem startNode = getCurrentNode();
        long hashToRemove = startNode.getHash();
        long hashToAdd = 0;
        long newHash = 0;
        // go the path to the root
        do {
            getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
            if (getCurrentNode().getNodeKey() == startNode.getNodeKey()) {
                // the begin node is always null
                newHash = 0;
            } else if (getCurrentNode().getNodeKey() == startNode.getParentKey()) {
                // the parent node is just removed
                newHash = getCurrentNode().getHash() - (hashToRemove * PRIME);
                hashToRemove = getCurrentNode().getHash();
            } else {
                // the ancestors are all touched regarding the modification
                newHash = getCurrentNode().getHash() - (hashToRemove * PRIME);
                newHash = newHash + hashToAdd * PRIME;
                hashToRemove = getCurrentNode().getHash();
            }
            getCurrentNode().setHash(newHash);
            hashToAdd = newHash;
            getTransactionState().finishNodeModification(getCurrentNode());
        } while (moveTo(getCurrentNode().getParentKey()));

        setCurrentNode(startNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with insert.
     * 
     * @throws TreetankIOException
     *             of anything weird happened.
     */
    private void rollingAdd() throws TreetankIOException {
        // start with hash to add
        final IItem startNode = getCurrentNode();
        long hashToAdd = startNode.hashCode();
        long newHash = 0;
        long possibleOldHash = 0;
        // go the path to the root
        do {
            getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
            if (getCurrentNode().getNodeKey() == startNode.getNodeKey()) {
                // at the beginning, take the hashcode of the node only
                newHash = hashToAdd;
            } else if (getCurrentNode().getNodeKey() == startNode.getParentKey()) {
                // at the parent level, just add the node
                possibleOldHash = getCurrentNode().getHash();
                newHash = possibleOldHash + hashToAdd * PRIME;
                hashToAdd = newHash;
            } else {
                // at the rest, remove the existing old key for this element and add the new one
                newHash = getCurrentNode().getHash() - (possibleOldHash * PRIME);
                newHash = newHash + hashToAdd * PRIME;
                hashToAdd = newHash;
                possibleOldHash = getCurrentNode().getHash();
            }
            getCurrentNode().setHash(newHash);
            getTransactionState().finishNodeModification(getCurrentNode());
        } while (moveTo(getCurrentNode().getParentKey()));
        setCurrentNode(startNode);
    }
}
