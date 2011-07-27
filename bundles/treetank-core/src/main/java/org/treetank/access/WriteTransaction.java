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

    /** Maximum number of node modifications before auto commit. */
    private final int mMaxNodeCount;

    /** Modification counter. */
    private long mModificationCount;

    /** Hash kind of Structure. */
    private final HashKind mHashKind;

    /** Determines the position of the insert. */
    private enum EInsert {
        /** Insert as first child. */
        ASFIRSTCHILD,

        /** Insert as right sibling. */
        ASRIGHTSIBLING,

        /** Insert as a non structural node. */
        ASNONSTRUCTURAL,
    }

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
    public synchronized long moveSubtreeToFirstChild(final long paramFromKey) throws AbsTTException {
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
        setCurrentNode(paramNodeToMove);
//        while (((AbsStructNode)getCurrentNode()).hasFirstChild()) {
//            moveToFirstChild();
//        }
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
     * @throws TTIOException
     *             if anything went wrong
     */
    private void adaptForMove(final IStructuralItem paramFromNode, final IStructuralItem paramToNode,
        final EInsert paramInsert) throws TTIOException {
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
                    try {
                        remove();
                    } catch (final AbsTTException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    moveTo(paramFromNode.getLeftSiblingKey());
                    setValue(builder.toString());
                }
            }
        }

        // Modify nodes where the subtree has been moved to.
        // ==============================================================================
        switch (paramInsert) {
        case ASFIRSTCHILD:
            if (paramToNode.hasFirstChild()) {
                moveTo(paramToNode.getFirstChildKey());

                if (getCurrentNode().getKind() == ENodes.TEXT_KIND
                    && paramFromNode.getKind() == ENodes.TEXT_KIND) {
                    final StringBuilder builder = new StringBuilder(getValueOfCurrentNode());

                    // Adapt right sibling key of moved node.
                    moveTo(((TextNode)getCurrentNode()).getRightSiblingKey());
                    final TextNode moved =
                        (TextNode)getTransactionState()
                            .prepareNodeForModification(paramFromNode.getNodeKey());
                    moved.setRightSiblingKey(getCurrentNode().getNodeKey());
                    getTransactionState().finishNodeModification(moved);

                    // Merge text nodes.
                    moveTo(moved.getNodeKey());
                    builder.insert(0, getValueOfCurrentNode());
                    setValue(builder.toString());
                    
                    // Remove first child.
                    moveTo(paramToNode.getFirstChildKey());
                    try {
                        remove();
                    } catch (final AbsTTException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    // Adapt left sibling key of former right sibling of first child.
                    moveTo(moved.getRightSiblingKey());
                    final AbsStructNode rightSibling =
                        (AbsStructNode)getTransactionState().prepareNodeForModification(
                            getCurrentNode().getNodeKey());
                    rightSibling.setLeftSiblingKey(paramFromNode.getNodeKey());
                    getTransactionState().finishNodeModification(rightSibling);
                } else {
                    // Adapt left sibling key of former first child.
                    final AbsStructNode oldFirstChild =
                        (AbsStructNode)getTransactionState().prepareNodeForModification(
                            paramToNode.getFirstChildKey());
                    oldFirstChild.setLeftSiblingKey(paramFromNode.getNodeKey());
                    getTransactionState().finishNodeModification(oldFirstChild);

                    // Adapt right sibling key of moved node.
                    final AbsStructNode moved =
                        (AbsStructNode)getTransactionState().prepareNodeForModification(
                            paramFromNode.getNodeKey());
                    moved.setRightSiblingKey(oldFirstChild.getNodeKey());
                    getTransactionState().finishNodeModification(moved);
                }
            } else {
                // Adapt right sibling key of moved node.
                final AbsStructNode moved =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(
                        paramFromNode.getNodeKey());
                moved.setRightSiblingKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
                getTransactionState().finishNodeModification(moved);
            }

            // Adapt first child key and childCount of parent where the subtree has to be inserted.
            final AbsStructNode newParent =
                (AbsStructNode)getTransactionState().prepareNodeForModification(paramToNode.getNodeKey());
            newParent.incrementChildCount();
            newParent.setFirstChildKey(paramFromNode.getNodeKey());
            getTransactionState().finishNodeModification(newParent);

            // Adapt left sibling key and parent key of moved node.
            final AbsStructNode moved =
                (AbsStructNode)getTransactionState().prepareNodeForModification(paramFromNode.getNodeKey());
            moved.setLeftSiblingKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
            moved.setParentKey(paramToNode.getNodeKey());
            getTransactionState().finishNodeModification(moved);
            break;
        case ASRIGHTSIBLING:
            final boolean hasMoved = moveTo(paramToNode.getRightSiblingKey());

            if (paramFromNode.getKind() == ENodes.TEXT_KIND && paramToNode.getKind() == ENodes.TEXT_KIND) {
                moveTo(paramToNode.getNodeKey());
                final StringBuilder builder = new StringBuilder(getValueOfCurrentNode());

                // Adapt left sibling key of former right sibling of first child.
                final AbsStructNode rightSibling =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(
                        ((TextNode)getCurrentNode()).getRightSiblingKey());
                rightSibling.setLeftSiblingKey(paramFromNode.getNodeKey());
                getTransactionState().finishNodeModification(rightSibling);

                // Adapt sibling keys of moved node.
                final TextNode movedNode =
                    (TextNode)getTransactionState().prepareNodeForModification(paramFromNode.getNodeKey());
                movedNode.setRightSiblingKey(rightSibling.getNodeKey());
                // Adapt left sibling key of moved node.
                movedNode.setLeftSiblingKey(((TextNode)getCurrentNode()).getLeftSiblingKey());
                getTransactionState().finishNodeModification(movedNode);
                
                // Merge text nodes.
                moveTo(movedNode.getNodeKey());
                builder.append(getValueOfCurrentNode());
                setValue(builder.toString());

                final AbsStructNode insertAnchor =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(paramToNode.getNodeKey());
                // Adapt right sibling key of node where the subtree has to be inserted.
                insertAnchor.setRightSiblingKey(paramFromNode.getNodeKey());
                getTransactionState().finishNodeModification(insertAnchor);
                
                // Remove first child.
                moveTo(paramToNode.getNodeKey());
                try {
                    remove();
                } catch (final AbsTTException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (hasMoved && paramFromNode.getKind() == ENodes.TEXT_KIND
                && getCurrentNode().getKind() == ENodes.TEXT_KIND) {
                final StringBuilder builder = new StringBuilder(getValueOfCurrentNode());

                // Adapt left sibling key of former right sibling of first child.
                final AbsStructNode rightSibling =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(
                        getCurrentNode().getNodeKey());
                rightSibling.setLeftSiblingKey(paramFromNode.getNodeKey());
                getTransactionState().finishNodeModification(rightSibling);

                // Adapt sibling keys of moved node.
                final TextNode movedNode =
                    (TextNode)getTransactionState().prepareNodeForModification(paramFromNode.getNodeKey());
                movedNode.setRightSiblingKey(rightSibling.getNodeKey());
                movedNode.setLeftSiblingKey(paramToNode.getNodeKey());
                getTransactionState().finishNodeModification(movedNode);
                
                // Merge text nodes.
                moveTo(movedNode.getNodeKey());
                builder.insert(0, getValueOfCurrentNode());
                setValue(builder.toString());

                // Remove right sibling.
                moveTo(paramToNode.getRightSiblingKey());
                try {
                    remove();
                } catch (final AbsTTException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                final AbsStructNode insertAnchor =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(paramToNode.getNodeKey());
                // Adapt right sibling key of node where the subtree has to be inserted.
                insertAnchor.setRightSiblingKey(paramFromNode.getNodeKey());
                getTransactionState().finishNodeModification(insertAnchor);
            } else {
                final AbsStructNode insertAnchor =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(paramToNode.getNodeKey());
                final long rightSiblKey = insertAnchor.getRightSiblingKey();
                // Adapt right sibling key of node where the subtree has to be inserted.
                insertAnchor.setRightSiblingKey(paramFromNode.getNodeKey());
                getTransactionState().finishNodeModification(insertAnchor);

                if (rightSiblKey > -1) {
                    // Adapt left sibling key of former right sibling.
                    final AbsStructNode oldRightSibling =
                        (AbsStructNode)getTransactionState().prepareNodeForModification(rightSiblKey);
                    oldRightSibling.setLeftSiblingKey(paramFromNode.getNodeKey());
                    getTransactionState().finishNodeModification(oldRightSibling);

                    // Adapt right sibling key of moved node.
                    final AbsStructNode movedNode =
                        (AbsStructNode)getTransactionState().prepareNodeForModification(
                            paramFromNode.getNodeKey());
                    movedNode.setRightSiblingKey(rightSiblKey);
                    getTransactionState().finishNodeModification(movedNode);
                }

                // Adapt left sibling key of moved node.
                final AbsStructNode movedNode =
                    (AbsStructNode)getTransactionState().prepareNodeForModification(
                        paramFromNode.getNodeKey());
                movedNode.setLeftSiblingKey(insertAnchor.getNodeKey());
                getTransactionState().finishNodeModification(movedNode);
            }
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsRightSibling(final QName paramQname) throws AbsTTException {
        if (getCurrentNode() instanceof AbsStructNode) {

            checkAccessAndCommit();

            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((AbsStructNode)getCurrentNode()).getRightSiblingKey();
            final ElementNode node =
                getTransactionState().createElementNode(parentKey, leftSibKey, rightSibKey, 0, paramQname);

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
        if (getCurrentNode() instanceof AbsStructNode) {
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
            throw new TTUsageException(
                "Insert is not allowed if current node is not an StructuralNode (either Text or Element)!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertAttribute(final QName paramQName, final String paramValueAsString)
        throws AbsTTException {
        if (paramQName == null || paramValueAsString == null) {
            throw new IllegalArgumentException("QName and Value of attribute may not be null!");
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
            throw new IllegalArgumentException("QName may not be null!");
        }
        if (getCurrentNode() instanceof ElementNode) {

            checkAccessAndCommit();

            final int uriKey = getTransactionState().createNameKey(paramQName.getNamespaceURI());
            final String name =
                paramQName.getPrefix().isEmpty() ? "xmlns" : "xmlns:" + paramQName.getPrefix();
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
    public synchronized void setQName(final QName paramName) throws TTIOException {
        if (paramName == null) {
            throw new IllegalStateException("Name may not be null!");
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
            throw new IllegalStateException("URI may not be null!");
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
            throw new IllegalStateException("Value may not be null!");
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
            throw new IllegalStateException("Value may not be null!");
        }
        setValue(getTransactionState().createNameKey("xs:untyped"), TypedValue.getBytes(paramValue));
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
     * @param paramInsert
     *            determines the position where to insert
     * @throws TTIOException
     *             if anything weird happens
     */
    private void adaptForInsert(final AbsNode paramNewNode, final EInsert paramInsert) throws TTIOException {
        assert paramNewNode != null;
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
}
