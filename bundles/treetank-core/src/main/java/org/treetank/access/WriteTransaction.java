/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;
import org.treetank.api.IItem;
import org.treetank.api.IStructuralItem;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.node.AbsNode;
import org.treetank.node.AbsStructNode;
import org.treetank.node.AttributeNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.node.NamespaceNode;
import org.treetank.node.TextNode;
import org.treetank.page.UberPage;
import org.treetank.settings.EFixed;
import org.treetank.utils.LogWrapper;
import org.treetank.utils.TypedValue;

/**
 * <h1>WriteTransaction</h1>
 * 
 * <p>
 * Single-threaded instance of only write transaction per session.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public class WriteTransaction extends ReadTransaction implements IWriteTransaction {

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
    private static final int PRIME = 77081;

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
     * @throws AbsTTException
     *             if the reading of the props is failing or properties are not
     *             valid
     */
    protected WriteTransaction(final long mTransactionID, final SessionState mSessionState,
        final WriteTransactionState mTransactionState, final int maxNodeCount, final int maxTime)
        throws AbsTTException {
        super(mTransactionID, mSessionState, mTransactionState);

        // Do not accept negative values.
        if ((maxNodeCount < 0) || (maxTime < 0)) {
            throw new TTUsageException("Negative arguments are not accepted.");
        }

        // Only auto commit by node modifications if it is more then 0.
        mMaxNodeCount = maxNodeCount;
        mModificationCount = 0L;

        // Only auto commit by time if the time is more than 0 seconds.
        if (maxTime > 0) {
            mCommitScheduler = Executors.newScheduledThreadPool(1);
            mCommitScheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (mModificationCount > 0) {
                        try {
                            commit();
                        } catch (final AbsTTException exc) {
                            LOGWRAPPER.error(exc);
                            throw new IllegalStateException(exc);
                        }
                    }
                }
            }, 0, maxTime, TimeUnit.SECONDS);
        } else {
            mCommitScheduler = null;
        }
        mHashKind = getSessionState().mDatabaseConfiguration.mHashKind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsFirstChild(final QName mQname) throws AbsTTException {
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
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsRightSibling(final QName mQname) throws AbsTTException {
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
            throw new TTUsageException(
                "Insert is not allowed if current node is not an StructuralNode (either Text or Element)!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsFirstChild(final String mValueAsString) throws AbsTTException {
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
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsRightSibling(final String mValueAsString) throws AbsTTException {
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
            throw new TTUsageException(
                "Insert is not allowed if current node is not an StructuralNode (either Text or Element)!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertAttribute(final QName mQname, final String mValueAsString)
        throws AbsTTException {
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
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertNamespace(final String mUri, final String mPrefix) throws AbsTTException {
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
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove() throws AbsTTException {
        checkAccessAndCommit();
        if (getCurrentNode().getKind() == ENodes.ROOT_KIND) {
            throw new TTUsageException("Root node can not be removed.");
        } else if (getCurrentNode() instanceof AbsStructNode) {
            final AbsStructNode node = (AbsStructNode)getCurrentNode();
            // // removing subtree
            final AbsAxis desc = new DescendantAxis(this, false);
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
    public synchronized void setName(final String paramName) throws TTIOException {

        assertNotClosed();
        mModificationCount++;
        final long oldHash = getCurrentNode().hashCode();

        final AbsNode node = getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
        node.setNameKey(getTransactionState().createNameKey(paramName));
        getTransactionState().finishNodeModification(node);

        // final AbsNode oldNode = (AbsNode)getCurrentNode();
        // final AbsNode newNode = createNodeToModify(oldNode);
        //
        // if (oldNode instanceof AbsStructNode) {
        // adaptForUpdate((AbsStructNode)oldNode, (AbsStructNode)newNode);
        // }
        // newNode.setNameKey(getTransactionState().createNameKey(mName));
        // getTransactionState().removeNode(oldNode);

        setCurrentNode(node);
        adaptHashedWithUpdate(oldHash);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setURI(final String paramUri) throws TTIOException {

        assertNotClosed();
        mModificationCount++;
        final long oldHash = getCurrentNode().hashCode();

        final AbsNode node = getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
        node.setURIKey(getTransactionState().createNameKey(paramUri));
        getTransactionState().finishNodeModification(node);

        // final AbsNode oldNode = (AbsNode)getCurrentNode();
        // final AbsNode newNode = createNodeToModify(oldNode);
        //
        // if (oldNode instanceof AbsStructNode) {
        // adaptForUpdate((AbsStructNode)oldNode, (AbsStructNode)newNode);
        // }
        // newNode.setURIKey(getTransactionState().createNameKey(mUri));
        // getTransactionState().removeNode(oldNode);

        setCurrentNode(node);
        adaptHashedWithUpdate(oldHash);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final int paramValueType, final byte[] paramValue) throws TTIOException {

        assertNotClosed();
        mModificationCount++;
        final long oldHash = getCurrentNode().hashCode();

        final AbsNode node = getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
        node.setValue(paramValueType, paramValue);
        getTransactionState().finishNodeModification(node);

        // final AbsNode oldNode = (AbsNode)getCurrentNode();
        // // oldNode.setValue(mValueType, mValue);
        // final AbsNode newNode = createNodeToModify(oldNode);
        //
        // if (oldNode instanceof AbsStructNode) {
        // adaptForUpdate((AbsStructNode)oldNode, (AbsStructNode)newNode);
        // }
        // newNode.setValue(mValueType, mValue);
        // getTransactionState().removeNode(oldNode);

        setCurrentNode(node);
        adaptHashedWithUpdate(oldHash);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final String mValue) throws TTIOException {
        setValue(getTransactionState().createNameKey("xs:untyped"), TypedValue.getBytes(mValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revertTo(final long revision) throws AbsTTException {
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
    public synchronized void commit() throws AbsTTException {

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
    public synchronized void abort() throws TTIOException {

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
    public synchronized void close() throws AbsTTException {
        if (!isClosed()) {
            // Make sure to commit all dirty data.
            if (mModificationCount > 0) {
                throw new TTUsageException("Must commit/abort transaction first");
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
     * @throws AbsTTException
     *             if anything weird happens.
     */
    private void checkAccessAndCommit() throws AbsTTException {
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
     * @throws TTIOException
     *             if anything weird happens
     */
    private void adaptForInsert(final AbsNode paramNewNode, final boolean paramAsFirstChild)
        throws TTIOException {

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
    // remove operation
    // ////////////////////////////////////////////////////////////

    /**
     * Adapting everything for remove operations.
     * 
     * @param paramOldNode
     *            pointer of the old node to be replaces
     * @throws TTIOException
     *             if anything weird happens
     */
    private void adaptForRemove(final AbsStructNode paramOldNode) throws TTIOException {

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
     * @throws AbsTTException
     *             if commit fails.
     */
    private void intermediateCommitIfRequired() throws AbsTTException {
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
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void adaptHashesWithAdd() throws TTIOException {
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
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void adaptHashesWithRemove() throws TTIOException {
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
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void adaptHashedWithUpdate(final long paramOldHash) throws TTIOException {
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
     * @throws TTIOException
     *             if anything weird happens
     */
    public void postorderRemove() throws TTIOException {
        moveTo(getCurrentNode().getParentKey());
        postorderAdd();
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with insert.
     * 
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void postorderAdd() throws TTIOException {
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
            synchronized (getCurrentNode()) {
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
                if (moveTo(getStructuralNode().getFirstChildKey())) {
                    do {
                        hashCodeForParent = getCurrentNode().getHash() + hashCodeForParent * PRIME;
                    } while (moveTo(getStructuralNode().getRightSiblingKey()));
                    moveTo(getStructuralNode().getParentKey());
                }

                // setting hash and resetting hash
                cursorToRoot.setHash(hashCodeForParent);
                getTransactionState().finishNodeModification(cursorToRoot);
                hashCodeForParent = 0;
            }
        } while (moveTo(cursorToRoot.getParentKey()));

        setCurrentNode(startNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with update.
     * 
     * @param paramOldHash
     *            paramOldHash to be removed
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void rollingUpdate(final long paramOldHash) throws TTIOException {
        final IItem newNode = getCurrentNode();
        final long newNodeHash = newNode.hashCode();
        long resultNew = newNode.hashCode();

        // go the path to the root
        do {
            synchronized (getCurrentNode()) {
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
            }
        } while (moveTo(getCurrentNode().getParentKey()));

        setCurrentNode(newNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with remove.
     * 
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void rollingRemove() throws TTIOException {
        final IItem startNode = getCurrentNode();
        long hashToRemove = startNode.getHash();
        long hashToAdd = 0;
        long newHash = 0;
        // go the path to the root
        do {
            synchronized (getCurrentNode()) {
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
            }
        } while (moveTo(getCurrentNode().getParentKey()));

        setCurrentNode(startNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with insert.
     * 
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void rollingAdd() throws TTIOException {
        // start with hash to add
        final IItem startNode = getCurrentNode();
        long hashToAdd = startNode.hashCode();
        long newHash = 0;
        long possibleOldHash = 0;
        // go the path to the root
        do {
            synchronized (getCurrentNode()) {
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
            }
        } while (moveTo(getCurrentNode().getParentKey()));
        setCurrentNode(startNode);
    }
}
