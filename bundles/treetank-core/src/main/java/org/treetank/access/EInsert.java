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

import org.treetank.api.IReadTransaction;
import org.treetank.api.IStructuralItem;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.node.TextNode;
import org.treetank.settings.EFixed;

/**
 * Determines the position of the insert of nodes and appropriate methods.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public enum EInsert {
    /** Insert as first child. */
    ASFIRSTCHILD {
        /** {@inheritDoc} */
        @Override
        void processMove(final IStructuralItem paramFromNode, final IStructuralItem paramToNode,
            final WriteTransaction paramWtx) throws AbsTTException {
            assert paramFromNode != null;
            assert paramToNode != null;
            assert paramWtx != null;
            if (paramToNode.hasFirstChild()) {
                paramWtx.moveTo(paramToNode.getFirstChildKey());

                if (paramWtx.getCurrentNode().getKind() == ENodes.TEXT_KIND
                    && paramFromNode.getKind() == ENodes.TEXT_KIND) {
                    final StringBuilder builder = new StringBuilder(paramWtx.getValueOfCurrentNode());

                    // Adapt right sibling key of moved node.
                    paramWtx.moveTo(((TextNode)paramWtx.getCurrentNode()).getRightSiblingKey());
                    final TextNode moved =
                        (TextNode)paramWtx.getTransactionState().prepareNodeForModification(
                            paramFromNode.getNodeKey());
                    moved.setRightSiblingKey(paramWtx.getCurrentNode().getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(moved);

                    // Merge text nodes.
                    paramWtx.moveTo(moved.getNodeKey());
                    builder.insert(0, paramWtx.getValueOfCurrentNode());
                    paramWtx.setValue(builder.toString());

                    // Remove first child.
                    paramWtx.moveTo(paramToNode.getFirstChildKey());
                    paramWtx.remove();

                    // Adapt left sibling key of former right sibling of first
                    // child.
                    paramWtx.moveTo(moved.getRightSiblingKey());
                    final IStructuralItem rightSibling =
                        (IStructuralItem)paramWtx.getTransactionState().prepareNodeForModification(
                            paramWtx.getCurrentNode().getNodeKey());
                    rightSibling.setLeftSiblingKey(paramFromNode.getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(rightSibling);
                } else {
                    // Adapt left sibling key of former first child.
                    final IStructuralItem oldFirstChild =
                        (IStructuralItem)paramWtx.getTransactionState().prepareNodeForModification(
                            paramToNode.getFirstChildKey());
                    oldFirstChild.setLeftSiblingKey(paramFromNode.getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(oldFirstChild);

                    // Adapt right sibling key of moved node.
                    final IStructuralItem moved =
                        (IStructuralItem)paramWtx.getTransactionState().prepareNodeForModification(
                            paramFromNode.getNodeKey());
                    moved.setRightSiblingKey(oldFirstChild.getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(moved);
                }
            } else {
                // Adapt right sibling key of moved node.
                final IStructuralItem moved =
                    (IStructuralItem)paramWtx.getTransactionState().prepareNodeForModification(
                        paramFromNode.getNodeKey());
                moved.setRightSiblingKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
                paramWtx.getTransactionState().finishNodeModification(moved);
            }

            // Adapt first child key and childCount of parent where the subtree
            // has to be inserted.
            final AbsStructNode newParent =
                (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                    paramToNode.getNodeKey());
            newParent.incrementChildCount();
            newParent.setFirstChildKey(paramFromNode.getNodeKey());
            paramWtx.getTransactionState().finishNodeModification(newParent);

            // Adapt left sibling key and parent key of moved node.
            final AbsStructNode moved =
                (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                    paramFromNode.getNodeKey());
            moved.setLeftSiblingKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
            moved.setParentKey(paramToNode.getNodeKey());
            paramWtx.getTransactionState().finishNodeModification(moved);
        }

        /** {@inheritDoc} */
        @Override
        void insertNode(final IWriteTransaction paramWtx, final IReadTransaction paramRtx)
            throws AbsTTException {
            assert paramWtx != null;
            assert paramRtx != null;
            assert paramWtx.getNode().getKind() == ENodes.ELEMENT_KIND;
            switch (paramRtx.getNode().getKind()) {
            case ELEMENT_KIND:
                paramWtx.insertElementAsFirstChild(paramRtx.getQNameOfCurrentNode());
                break;
            case TEXT_KIND:
                paramWtx.insertTextAsFirstChild(paramRtx.getValueOfCurrentNode());
                break;
            default:
                throw new IllegalStateException("Node type not known!");
            }

        }
    },
    /** Insert as right sibling. */
    ASRIGHTSIBLING {
        /** {@inheritDoc} */
        @Override
        void processMove(final IStructuralItem paramFromNode, final IStructuralItem paramToNode,
            final WriteTransaction paramWtx) throws AbsTTException {
            assert paramFromNode != null;
            assert paramToNode != null;
            assert paramWtx != null;
            final boolean hasMoved = paramWtx.moveTo(paramToNode.getRightSiblingKey());

            if (paramFromNode.getKind() == ENodes.TEXT_KIND && paramToNode.getKind() == ENodes.TEXT_KIND) {
                paramWtx.moveTo(paramToNode.getNodeKey());
                final StringBuilder builder = new StringBuilder(paramWtx.getValueOfCurrentNode());

                // Adapt left sibling key of former right sibling of first
                // child.
                final AbsStructNode rightSibling =
                    (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        ((TextNode)paramWtx.getCurrentNode()).getRightSiblingKey());
                rightSibling.setLeftSiblingKey(paramFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(rightSibling);

                // Adapt sibling keys of moved node.
                final TextNode movedNode =
                    (TextNode)paramWtx.getTransactionState().prepareNodeForModification(
                        paramFromNode.getNodeKey());
                movedNode.setRightSiblingKey(rightSibling.getNodeKey());
                // Adapt left sibling key of moved node.
                movedNode.setLeftSiblingKey(((TextNode)paramWtx.getCurrentNode()).getLeftSiblingKey());
                paramWtx.getTransactionState().finishNodeModification(movedNode);

                // Merge text nodes.
                paramWtx.moveTo(movedNode.getNodeKey());
                builder.append(paramWtx.getValueOfCurrentNode());
                paramWtx.setValue(builder.toString());

                final AbsStructNode insertAnchor =
                    (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        paramToNode.getNodeKey());
                // Adapt right sibling key of node where the subtree has to be
                // inserted.
                insertAnchor.setRightSiblingKey(paramFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(insertAnchor);

                // Remove first child.
                paramWtx.moveTo(paramToNode.getNodeKey());
                paramWtx.remove();
            } else if (hasMoved && paramFromNode.getKind() == ENodes.TEXT_KIND
                && paramWtx.getCurrentNode().getKind() == ENodes.TEXT_KIND) {
                final StringBuilder builder = new StringBuilder(paramWtx.getValueOfCurrentNode());

                // Adapt left sibling key of former right sibling of first
                // child.
                final AbsStructNode rightSibling =
                    (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        paramWtx.getCurrentNode().getNodeKey());
                rightSibling.setLeftSiblingKey(paramFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(rightSibling);

                // Adapt sibling keys of moved node.
                final TextNode movedNode =
                    (TextNode)paramWtx.getTransactionState().prepareNodeForModification(
                        paramFromNode.getNodeKey());
                movedNode.setRightSiblingKey(rightSibling.getNodeKey());
                movedNode.setLeftSiblingKey(paramToNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(movedNode);

                // Merge text nodes.
                paramWtx.moveTo(movedNode.getNodeKey());
                builder.insert(0, paramWtx.getValueOfCurrentNode());
                paramWtx.setValue(builder.toString());

                // Remove right sibling.
                paramWtx.moveTo(paramToNode.getRightSiblingKey());
                paramWtx.remove();

                final AbsStructNode insertAnchor =
                    (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        paramToNode.getNodeKey());
                // Adapt right sibling key of node where the subtree has to be
                // inserted.
                insertAnchor.setRightSiblingKey(paramFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(insertAnchor);
            } else {
                final AbsStructNode insertAnchor =
                    (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        paramToNode.getNodeKey());
                final long rightSiblKey = insertAnchor.getRightSiblingKey();
                // Adapt right sibling key of node where the subtree has to be
                // inserted.
                insertAnchor.setRightSiblingKey(paramFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(insertAnchor);

                if (rightSiblKey > -1) {
                    // Adapt left sibling key of former right sibling.
                    final AbsStructNode oldRightSibling =
                        (AbsStructNode)paramWtx.getTransactionState()
                            .prepareNodeForModification(rightSiblKey);
                    oldRightSibling.setLeftSiblingKey(paramFromNode.getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(oldRightSibling);

                    // Adapt right sibling key of moved node.
                    final AbsStructNode movedNode =
                        (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                            paramFromNode.getNodeKey());
                    movedNode.setRightSiblingKey(rightSiblKey);
                    paramWtx.getTransactionState().finishNodeModification(movedNode);
                }

                // Adapt left sibling key of moved node.
                final AbsStructNode movedNode =
                    (AbsStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        paramFromNode.getNodeKey());
                movedNode.setLeftSiblingKey(insertAnchor.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(movedNode);
            }

        }

        /** {@inheritDoc} */
        @Override
        void insertNode(final IWriteTransaction paramWtx, final IReadTransaction paramRtx)
            throws AbsTTException {
            assert paramWtx != null;
            assert paramRtx != null;
            // TODO: or instanceof AbsStructNode?
            assert paramWtx.getNode().getKind() == ENodes.ELEMENT_KIND
                || paramWtx.getNode().getKind() == ENodes.TEXT_KIND;
            switch (paramRtx.getNode().getKind()) {
            case ELEMENT_KIND:
                paramWtx.insertElementAsRightSibling(paramRtx.getQNameOfCurrentNode());
                break;
            case TEXT_KIND:
                paramWtx.insertTextAsRightSibling(paramRtx.getValueOfCurrentNode());
                break;
            default:
                throw new IllegalStateException("Node type not known!");
            }
        }
    },
    /** Insert as a non structural node. */
    ASNONSTRUCTURAL {
        /** {@inheritDoc} */
        @Override
        void processMove(final IStructuralItem paramFromNode, final IStructuralItem paramToNode,
            final WriteTransaction paramWtx) throws AbsTTException {
            // Not allowed.
            throw new AssertionError("May never be invoked!");
        }

        /** {@inheritDoc} */
        @Override
        void insertNode(final IWriteTransaction paramWtx, final IReadTransaction paramRtx)
            throws AbsTTException {
            assert paramWtx != null;
            assert paramRtx != null;
            assert paramWtx.getNode().getKind() == ENodes.ELEMENT_KIND;
            switch (paramRtx.getNode().getKind()) {
            case NAMESPACE_KIND:
                paramWtx.insertNamespace(paramRtx.getQNameOfCurrentNode());
                break;
            case ATTRIBUTE_KIND:
                paramWtx.insertAttribute(paramRtx.getQNameOfCurrentNode(), paramRtx.getValueOfCurrentNode());
                break;
            default:
                throw new IllegalStateException("Only namespace- and attribute-nodes are permitted!");
            }
        }
    };

    /**
     * Process movement of a subtree.
     * 
     * @param paramFromNode
     *            root {of subtree to move
     * @param paramToNode
     *            determines where the subtree has to be inserted
     * @param paramWtx
     *            write-transaction which implements the {@link IWriteTransaction} interface
     * @throws AbsTTException
     *             if an I/O error occurs
     */
    abstract void processMove(final IStructuralItem paramFromNode, final IStructuralItem paramToNode,
        final WriteTransaction paramWtx) throws AbsTTException;

    /**
     * Insert a node.
     * 
     * @param paramRtx
     *            read-transaction which implements the {@link IReadTransaction} interface
     * @param paramWtx
     *            write-transaction which implements the {@link IWriteTransaction} interface
     * @throws AbsTTException
     *             if insertion of node fails
     */
    abstract void insertNode(final IWriteTransaction paramWtx, final IReadTransaction paramRtx)
        throws AbsTTException;
}
