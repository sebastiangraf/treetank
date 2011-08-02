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

import javax.xml.namespace.QName;

import org.treetank.api.*;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.axis.LevelOrderAxis;
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
import org.treetank.utils.ItemList;
import org.treetank.utils.TypedValue;

/**
 * <h1>WriteTransaction</h1>
 * 
 * <p>
 * Single-threaded instance of only write transaction per session.
 * </p>
 * 
 * <p>
 * All methods throw {@link NullPointerException}s in case of null values for reference parameters.
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

    /** Maximum number of node modifications before auto commit. */
    private final int mMaxNodeCount;

    /** Modification counter. */
    private long mModificationCount;

    /** Hash kind of Structure. */
    private final HashKind mHashKind;

    /**
     * Constructor.
     * 
     * @param paramTransactionID
     *            ID of transaction
     * @param paramSessionState
     *            state of the session
     * @param paramTransactionState
     *            state of this transaction
     * @param paramMaxNodeCount
     *            maximum number of node modifications before auto commit
     * @param paramMaxTime
     *            maximum number of seconds before auto commit
     * @throws TTIOException
     *             if the reading of the props is failing
     * @throws TTUsageException
     *             if paramMaxNodeCount < 0 or paramMaxTime < 0
     */
    protected WriteTransaction(final long paramTransactionID, final SessionState paramSessionState,
        final WriteTransactionState paramTransactionState, final int paramMaxNodeCount, final int paramMaxTime)
        throws TTIOException, TTUsageException {
        super(paramSessionState, paramTransactionID, paramTransactionState);

        // Do not accept negative values.
        if ((paramMaxNodeCount < 0) || (paramMaxTime < 0)) {
            throw new TTUsageException("Negative arguments are not accepted.");
        }

        // Only auto commit by node modifications if it is more then 0.
        mMaxNodeCount = paramMaxNodeCount;
        mModificationCount = 0L;

        mHashKind = paramSessionState.mSessionConfig.mDBConfig.mHashKind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsFirstChild(final QName mQName) throws AbsTTException,
        NullPointerException {
        if (mQName == null) {
            throw new NullPointerException("mQName may not be null!");
        }
        if (getCurrentNode() instanceof ElementNode || getCurrentNode() instanceof DocumentRootNode) {

            checkAccessAndCommit();

            final long parentKey = getCurrentNode().getNodeKey();
            final long leftSibKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getFirstChildKey();
            final ElementNode node =
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, 0, mQName);

            setCurrentNode(node);
            adaptForInsert(node, EInsert.ASFIRSTCHILD);
            adaptHashesWithAdd();

            return node.getNodeKey();
        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             if paramFromKey < 0
     */
    @Override
    public synchronized long moveSubtreeToFirstChild(final long paramFromKey) throws AbsTTException,
        IllegalArgumentException {
        if (paramFromKey < 0) {
            throw new IllegalArgumentException("Argument must be a valid node key!");
        }

        final IItem nodeToMove = getTransactionState().getNode(paramFromKey);

        if (nodeToMove instanceof AbsStructNode && getCurrentNode().getKind() == ENodes.ELEMENT_KIND) {
            checkAccessAndCommit();

            final ElementNode nodeAnchor = (ElementNode)getCurrentNode();

            // Adapt hashes.
            adaptHashesForMove((AbsStructNode)nodeToMove);

            // Adapt pointers and merge sibling text nodes.
            adaptForMove((AbsStructNode)nodeToMove, nodeAnchor, EInsert.ASFIRSTCHILD);
            setCurrentNode(nodeAnchor);
            adaptHashesWithAdd();

            return nodeToMove.getNodeKey();
        } else {
            throw new TTUsageException(
                "Move is not allowed if moved node is not an ElementNode and the node isn't inserted at an element node!");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             if paramFromKey < 0
     */
    @Override
    public synchronized long moveSubtreeToRightSibling(final long paramFromKey) throws AbsTTException {
        if (paramFromKey < 0) {
            throw new IllegalArgumentException("Argument must be a valid node key!");
        }

        final IItem nodeToMove = getTransactionState().getNode(paramFromKey);

        if (nodeToMove instanceof AbsStructNode && getCurrentNode() instanceof AbsStructNode) {
            checkAccessAndCommit();

            final AbsStructNode nodeAnchor = (AbsStructNode)getCurrentNode();

            // Adapt hashes.
            adaptHashesForMove((AbsStructNode)nodeToMove);

            // Adapt pointers and merge sibling text nodes.
            adaptForMove((AbsStructNode)nodeToMove, nodeAnchor, EInsert.ASRIGHTSIBLING);
            setCurrentNode(nodeAnchor);
            adaptHashesWithAdd();

            return nodeToMove.getNodeKey();
        } else {
            throw new TTUsageException(
                "Move is not allowed if moved node is not an ElementNode and the node isn't inserted at an element node!");
        }
    }

    /**
     * Adapt hashes for move operation ("remove" phase).
     * 
     * @param paramNodeToMove
     *            node which implements {@link IStructuralItem} and is moved
     */
    private void adaptHashesForMove(final IStructuralItem paramNodeToMove) {
        assert paramNodeToMove != null;
        setCurrentNode(paramNodeToMove);
        // while (((AbsStructNode)getCurrentNode()).hasFirstChild()) {
        // moveToFirstChild();
        // }
        try {
            adaptHashesWithRemove();
        } catch (final TTIOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Adapting everything for move operations.
     * 
     * @param paramFromNode
     *            root {@link AbsStructNode} of the subtree to be moved
     * @param paramToNode
     *            the {@link AbsStructNode} which is the anchor of the new subtree
     * @param paramInsert
     *            determines if it has to be inserted as a first child or a right sibling
     * @throws AbsTTException
     *             if removing a node fails after merging text nodes
     */
    private void adaptForMove(final IStructuralItem paramFromNode, final IStructuralItem paramToNode,
        final EInsert paramInsert) throws AbsTTException {
        assert paramFromNode != null;
        assert paramToNode != null;
        assert paramInsert != null;

        // Modify nodes where the subtree has been moved from.
        // ==============================================================================
        final AbsStructNode parent =
            (AbsStructNode)getTransactionState().prepareNodeForModification(paramFromNode.getParentKey());
        parent.decrementChildCount();
        // Adapt first child key of former parent.
        if (parent.getFirstChildKey() == paramFromNode.getNodeKey()) {
            parent.setFirstChildKey(paramFromNode.getRightSiblingKey());
        }
        getTransactionState().finishNodeModification(parent);

        // Adapt right sibling key of former left sibling.
        if (paramFromNode.hasLeftSibling()) {
            final AbsStructNode leftSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(
                    paramFromNode.getLeftSiblingKey());
            leftSibling.setRightSiblingKey(paramFromNode.getRightSiblingKey());
            getTransactionState().finishNodeModification(leftSibling);
        }

        // Adapt left sibling key of former right sibling.
        if (paramFromNode.hasRightSibling()) {
            final AbsStructNode rightSibling =
                (AbsStructNode)getTransactionState().prepareNodeForModification(
                    paramFromNode.getRightSiblingKey());
            rightSibling.setLeftSiblingKey(paramFromNode.getLeftSiblingKey());
            getTransactionState().finishNodeModification(rightSibling);
        }

        // Merge text nodes.
        if (paramFromNode.hasLeftSibling() && paramFromNode.hasRightSibling()) {
            moveTo(paramFromNode.getLeftSiblingKey());
            if (getCurrentNode() != null && getCurrentNode().getKind() == ENodes.TEXT_KIND) {
                final StringBuilder builder = new StringBuilder(getValueOfCurrentNode());
                moveTo(paramFromNode.getRightSiblingKey());
                if (getCurrentNode() != null && getCurrentNode().getKind() == ENodes.TEXT_KIND) {
                    builder.append(getValueOfCurrentNode());
                    remove();
                    moveTo(paramFromNode.getLeftSiblingKey());
                    setValue(builder.toString());
                }
            }
        }

        // Modify nodes where the subtree has been moved to.
        // ==============================================================================
        paramInsert.processMove(paramFromNode, paramToNode, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsRightSibling(final QName paramQName) throws AbsTTException {
        if (paramQName == null) {
            throw new NullPointerException("paramQName may not be null!");
        }
        if (getCurrentNode() instanceof AbsStructNode) {

            checkAccessAndCommit();

            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getRightSiblingKey();
            final ElementNode node =
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, 0, paramQName);

            setCurrentNode(node);
            adaptForInsert(node, EInsert.ASRIGHTSIBLING);
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
    public synchronized long insertTextAsFirstChild(final String paramValueAsString) throws AbsTTException {
        if (paramValueAsString == null) {
            throw new NullPointerException("paramValueAsString may not be null!");
        }
        if (getCurrentNode() instanceof ElementNode || getCurrentNode() instanceof DocumentRootNode) {

            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(paramValueAsString);
            final long parentKey = getCurrentNode().getNodeKey();
            final long leftSibKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getFirstChildKey();
            final TextNode node =
                getTransactionState().createTextNode(parentKey, leftSibKey, rightSibKey, value);

            setCurrentNode(node);
            adaptForInsert(node, EInsert.ASFIRSTCHILD);
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
    public synchronized long insertTextAsRightSibling(final String paramValueAsString) throws AbsTTException {
        if (paramValueAsString == null) {
            throw new NullPointerException("paramValueAsString may not be null!");
        }

        if (getCurrentNode().getKind() == ENodes.ELEMENT_KIND) {
            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(paramValueAsString);
            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getRightSiblingKey();
            final TextNode node =
                getTransactionState().createTextNode(parentKey, leftSibKey, rightSibKey, value);

            setCurrentNode(node);
            adaptForInsert(node, EInsert.ASRIGHTSIBLING);
            adaptHashesWithAdd();

            return node.getNodeKey();

        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an element node!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertAttribute(final QName paramQName, final String paramValueAsString)
        throws AbsTTException {
        if (paramQName == null || paramValueAsString == null) {
            throw new NullPointerException("QName and Value of attribute may not be null!");
        }
        if (getCurrentNode() instanceof ElementNode) {

            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(paramValueAsString);
            final long elementKey = getCurrentNode().getNodeKey();
            final AttributeNode node =
                getTransactionState().createAttributeNode(elementKey, paramQName, value);

            final AbsNode parentNode = getTransactionState().prepareNodeForModification(node.getParentKey());
            ((ElementNode)parentNode).insertAttribute(node.getNodeKey());
            getTransactionState().finishNodeModification(parentNode);

            setCurrentNode(node);
            adaptForInsert(node, EInsert.ASNONSTRUCTURAL);

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
    public synchronized long insertNamespace(final QName paramQName) throws AbsTTException {
        if (paramQName == null) {
            throw new NullPointerException("QName may not be null!");
        }
        if (getCurrentNode() instanceof ElementNode) {

            checkAccessAndCommit();

            final int uriKey = getTransactionState().createNameKey(paramQName.getNamespaceURI());
            // final String name =
            // paramQName.getPrefix().isEmpty() ? "xmlns" : "xmlns:" + paramQName.getPrefix();
            final int prefixKey = getTransactionState().createNameKey(paramQName.getPrefix());
            final long elementKey = getCurrentNode().getNodeKey();

            final NamespaceNode node =
                getTransactionState().createNamespaceNode(elementKey, uriKey, prefixKey);

            final AbsNode parentNode = getTransactionState().prepareNodeForModification(node.getParentKey());
            ((ElementNode)parentNode).insertNamespace(node.getNodeKey());
            getTransactionState().finishNodeModification(parentNode);

            setCurrentNode(node);
            adaptForInsert(node, EInsert.ASNONSTRUCTURAL);
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
            throw new TTUsageException("Document root can not be removed.");
        } else if (getCurrentNode() instanceof AbsStructNode) {
            final AbsStructNode node = (AbsStructNode)getCurrentNode();
            // Remove subtree.
            for (final AbsAxis desc = new DescendantAxis(this, false); desc.hasNext(); desc.next()) {
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
    public synchronized void setQName(final QName paramName) throws TTIOException, NullPointerException {
        if (paramName == null) {
            throw new NullPointerException("Name may not be null!");
        }
        assertNotClosed();
        mModificationCount++;
        final long oldHash = getCurrentNode().hashCode();

        final AbsNode node = getTransactionState().prepareNodeForModification(getCurrentNode().getNodeKey());
        node.setNameKey(getTransactionState().createNameKey(WriteTransactionState.buildName(paramName)));
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
        if (paramUri == null) {
            throw new NullPointerException("URI may not be null!");
        }
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
        if (paramValue == null) {
            throw new NullPointerException("Value may not be null!");
        }
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
    public synchronized void setValue(final String paramValue) throws TTIOException {
        if (paramValue == null) {
            throw new NullPointerException("Value may not be null!");
        }
        setValue(getTransactionState().createNameKey("xs:untyped"), TypedValue.getBytes(paramValue));
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTUsageException
     *             if paramRevision < 0 or paramRevision > maxCommitedRev
     * @throws TTIOException
     *             if an I/O operation fails
     */
    @Override
    public void revertTo(final long paramRevision) throws TTUsageException, TTIOException {
        if (paramRevision < 0) {
            throw new IllegalArgumentException("paramRevision parameter must be >= 0");
        }
        assertNotClosed();
        mSessionState.assertValidRevision(paramRevision);
        getTransactionState().close();
        // Reset internal transaction state to new uber page.
        setTransactionState(mSessionState.createWriteTransactionState(getTransactionID(), paramRevision,
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
        mSessionState.setLastCommittedUberPage(uberPage);

        // Reset modification counter.
        mModificationCount = 0L;

        getTransactionState().close();
        // Reset internal transaction state to new uber page.
        setTransactionState(mSessionState.createWriteTransactionState(getTransactionID(),
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
        setTransactionState(mSessionState.createWriteTransactionState(getTransactionID(), revisionToSet,
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
            // Release all state immediately.
            getTransactionState().close();
            mSessionState.closeWriteTransaction(getTransactionID());
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
     *             if anything weird happens
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
     * @param paramInsert
     *            determines the position where to insert
     * @throws TTIOException
     *             if anything weird happens
     */
    private void adaptForInsert(final AbsNode paramNewNode, final EInsert paramInsert) throws TTIOException {
        assert paramNewNode != null;
        assert paramInsert != null;

        if (paramNewNode instanceof AbsStructNode) {
            final AbsStructNode strucNode = (AbsStructNode)paramNewNode;
            final AbsStructNode parent =
                (AbsStructNode)getTransactionState().prepareNodeForModification(paramNewNode.getParentKey());
            parent.incrementChildCount();
            if (paramInsert == EInsert.ASFIRSTCHILD) {
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
        assert paramOldNode != null;

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
    private void postorderRemove() throws TTIOException {
        moveTo(getCurrentNode().getParentKey());
        postorderAdd();
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with insert.
     * 
     * @throws TTIOException
     *             if anything weird happened
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
     *             if anything weird happened
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
     *             if anything weird happened
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
     *             if anything weird happened
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

    /** {@inheritDoc} */
    @Override
    public long copySubtreeAsFirstChild(final long paramNodeKey, final long paramRevision)
        throws AbsTTException {
        final IReadTransaction rtx = getTransaction(paramRevision, paramNodeKey);
        rtx.getNode().acceptVisitor(new InsertSubtreeVisitor(rtx, this, EInsert.ASFIRSTCHILD));
        return paramNodeKey;
    }

    /** {@inheritDoc} */
    @Override
    public long copySubtreeAsRightSibling(final long paramNodeKey, final long paramRevision)
        throws AbsTTException {
        final IReadTransaction rtx = getTransaction(paramRevision, paramNodeKey);
        rtx.getNode().acceptVisitor(new InsertSubtreeVisitor(rtx, this, EInsert.ASRIGHTSIBLING));
        return paramNodeKey;
    }

    /**
     * Get an instance from a {@link IReadTransaction} transaction implementation.
     * 
     * @param paramNodeKey
     *            node key of the root node of the subtree to copy
     * @param paramRevision
     *            revision from which to copy a subtree
     * @param paramDatabase
     *            database reference which implements the {@link IDatabase} interface
     * @return reference on an implementation of the {@link IReadTransaction} interface
     * @throws AbsTTException
     *             if setup of Treetank fails
     */
    private IReadTransaction getTransaction(final long paramRevision, final long paramNodeKey)
        throws AbsTTException {
        checkParams(paramNodeKey, paramRevision);
        final IReadTransaction rtx = mSessionState.beginReadTransaction(paramRevision, new ItemList());
        rtx.moveTo(paramNodeKey);
        if (rtx.getNode().getKind() != ENodes.TEXT_KIND || rtx.getNode().getKind() != ENodes.ELEMENT_KIND) {
            throw new IllegalStateException("Node to insert must be a structural node (Text or Element)!");
        }
        return rtx;
    }

    /**
     * Check parameters.
     * 
     * @param paramNodeKey
     *            node key of the root node of the subtree to copy
     * @param paramRevision
     *            revision from which to copy a subtree
     * @param paramDatabase
     *            database reference which implements the {@link IDatabase} interface
     * @throws IllegalArgumentException
     *             if an invalid node key is specified
     * @throws NullPointerException
     *             if the database reference is null
     */
    private void checkParams(final long paramNodeKey, final long paramRevision) {
        if (paramNodeKey < 1) {
            throw new IllegalArgumentException("Node key parameter of copied subtree root must be > 1!");
        }

        mSessionState.assertValidRevision(paramRevision);
    }
}
