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
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ENode;
import org.treetank.node.TextNode;
import org.treetank.node.interfaces.IStructNode;
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
        void processMove(final IStructNode pFromNode, final IStructNode pToNode,
            final WriteTransaction paramWtx) throws AbsTTException {
            assert pFromNode != null;
            assert pToNode != null;
            assert paramWtx != null;
            if (pToNode.hasFirstChild()) {
                paramWtx.moveTo(pToNode.getFirstChildKey());

                if (paramWtx.getCurrentNode().getKind() == ENode.TEXT_KIND
                    && pFromNode.getKind() == ENode.TEXT_KIND) {
                    final StringBuilder builder = new StringBuilder(paramWtx.getValueOfCurrentNode());

                    // Adapt right sibling key of moved node.
                    paramWtx.moveTo(((TextNode)paramWtx.getCurrentNode()).getRightSiblingKey());
                    final TextNode moved =
                        (TextNode)paramWtx.getTransactionState().prepareNodeForModification(
                            pFromNode.getNodeKey());
                    moved.setRightSiblingKey(paramWtx.getCurrentNode().getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(moved);

                    // Merge text nodes.
                    paramWtx.moveTo(moved.getNodeKey());
                    builder.insert(0, paramWtx.getValueOfCurrentNode());
                    paramWtx.setValue(builder.toString());

                    // Remove first child.
                    paramWtx.moveTo(pToNode.getFirstChildKey());
                    paramWtx.remove();

                    // Adapt left sibling key of former right sibling of first
                    // child.
                    paramWtx.moveTo(moved.getRightSiblingKey());
                    final IStructNode rightSibling =
                        (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                            paramWtx.getCurrentNode().getNodeKey());
                    rightSibling.setLeftSiblingKey(pFromNode.getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(rightSibling);
                } else {
                    // Adapt left sibling key of former first child.
                    final IStructNode oldFirstChild =
                        (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                            pToNode.getFirstChildKey());
                    oldFirstChild.setLeftSiblingKey(pFromNode.getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(oldFirstChild);

                    // Adapt right sibling key of moved node.
                    final IStructNode moved =
                        (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                            pFromNode.getNodeKey());
                    moved.setRightSiblingKey(oldFirstChild.getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(moved);
                }
            } else {
                // Adapt right sibling key of moved node.
                final IStructNode moved =
                    (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        pFromNode.getNodeKey());
                moved.setRightSiblingKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
                paramWtx.getTransactionState().finishNodeModification(moved);
            }

            // Adapt first child key and childCount of parent where the subtree
            // has to be inserted.
            final IStructNode newParent =
                (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                    pToNode.getNodeKey());
            newParent.incrementChildCount();
            newParent.setFirstChildKey(pFromNode.getNodeKey());
            paramWtx.getTransactionState().finishNodeModification(newParent);

            // Adapt left sibling key and parent key of moved node.
            final IStructNode moved =
                (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                    pFromNode.getNodeKey());
            moved.setLeftSiblingKey((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
            moved.setParentKey(pToNode.getNodeKey());
            paramWtx.getTransactionState().finishNodeModification(moved);
        }

        /** {@inheritDoc} */
        @Override
        void insertNode(final IWriteTransaction paramWtx, final IReadTransaction paramRtx)
            throws AbsTTException {
            assert paramWtx != null;
            assert paramRtx != null;
            assert paramWtx.getNode().getKind() == ENode.ELEMENT_KIND;
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
        void processMove(final IStructNode pFromNode, final IStructNode pToNode,
            final WriteTransaction paramWtx) throws AbsTTException {
            assert pFromNode != null;
            assert pToNode != null;
            assert paramWtx != null;
            final boolean hasMoved = paramWtx.moveTo(pToNode.getRightSiblingKey());

            if (pFromNode.getKind() == ENode.TEXT_KIND && pToNode.getKind() == ENode.TEXT_KIND) {
                paramWtx.moveTo(pToNode.getNodeKey());
                final StringBuilder builder = new StringBuilder(paramWtx.getValueOfCurrentNode());

                // Adapt left sibling key of former right sibling of first
                // child.
                final IStructNode rightSibling =
                    (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        ((TextNode)paramWtx.getCurrentNode()).getRightSiblingKey());
                rightSibling.setLeftSiblingKey(pFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(rightSibling);

                // Adapt sibling keys of moved node.
                final TextNode movedNode =
                    (TextNode)paramWtx.getTransactionState().prepareNodeForModification(
                        pFromNode.getNodeKey());
                movedNode.setRightSiblingKey(rightSibling.getNodeKey());
                // Adapt left sibling key of moved node.
                movedNode.setLeftSiblingKey(((TextNode)paramWtx.getCurrentNode()).getLeftSiblingKey());
                paramWtx.getTransactionState().finishNodeModification(movedNode);

                // Merge text nodes.
                paramWtx.moveTo(movedNode.getNodeKey());
                builder.append(paramWtx.getValueOfCurrentNode());
                paramWtx.setValue(builder.toString());

                final IStructNode insertAnchor =
                    (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        pToNode.getNodeKey());
                // Adapt right sibling key of node where the subtree has to be
                // inserted.
                insertAnchor.setRightSiblingKey(pFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(insertAnchor);

                // Remove first child.
                paramWtx.moveTo(pToNode.getNodeKey());
                paramWtx.remove();
            } else if (hasMoved && pFromNode.getKind() == ENode.TEXT_KIND
                && paramWtx.getCurrentNode().getKind() == ENode.TEXT_KIND) {
                final StringBuilder builder = new StringBuilder(paramWtx.getValueOfCurrentNode());

                // Adapt left sibling key of former right sibling of first
                // child.
                final IStructNode rightSibling =
                    (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        paramWtx.getCurrentNode().getNodeKey());
                rightSibling.setLeftSiblingKey(pFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(rightSibling);

                // Adapt sibling keys of moved node.
                final TextNode movedNode =
                    (TextNode)paramWtx.getTransactionState().prepareNodeForModification(
                        pFromNode.getNodeKey());
                movedNode.setRightSiblingKey(rightSibling.getNodeKey());
                movedNode.setLeftSiblingKey(pToNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(movedNode);

                // Merge text nodes.
                paramWtx.moveTo(movedNode.getNodeKey());
                builder.insert(0, paramWtx.getValueOfCurrentNode());
                paramWtx.setValue(builder.toString());

                // Remove right sibling.
                paramWtx.moveTo(pToNode.getRightSiblingKey());
                paramWtx.remove();

                final IStructNode insertAnchor =
                    (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        pToNode.getNodeKey());
                // Adapt right sibling key of node where the subtree has to be
                // inserted.
                insertAnchor.setRightSiblingKey(pFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(insertAnchor);
            } else {
                final IStructNode insertAnchor =
                    (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        pToNode.getNodeKey());
                final long rightSiblKey = insertAnchor.getRightSiblingKey();
                // Adapt right sibling key of node where the subtree has to be
                // inserted.
                insertAnchor.setRightSiblingKey(pFromNode.getNodeKey());
                paramWtx.getTransactionState().finishNodeModification(insertAnchor);

                if (rightSiblKey > -1) {
                    // Adapt left sibling key of former right sibling.
                    final IStructNode oldRightSibling =
                        (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(rightSiblKey);
                    oldRightSibling.setLeftSiblingKey(pFromNode.getNodeKey());
                    paramWtx.getTransactionState().finishNodeModification(oldRightSibling);

                    // Adapt right sibling key of moved node.
                    final IStructNode movedNode =
                        (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                            pFromNode.getNodeKey());
                    movedNode.setRightSiblingKey(rightSiblKey);
                    paramWtx.getTransactionState().finishNodeModification(movedNode);
                }

                // Adapt left sibling key of moved node.
                final IStructNode movedNode =
                    (IStructNode)paramWtx.getTransactionState().prepareNodeForModification(
                        pFromNode.getNodeKey());
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
            assert paramWtx.getNode().getKind() == ENode.ELEMENT_KIND
                || paramWtx.getNode().getKind() == ENode.TEXT_KIND;
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
        void processMove(final IStructNode pFromNode, final IStructNode pToNode,
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
            assert paramWtx.getNode().getKind() == ENode.ELEMENT_KIND;
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
     * @param pFromNode
     *            root {of subtree to move
     * @param pToNode
     *            determines where the subtree has to be inserted
     * @param pWtx
     *            write-transaction which implements the {@link IWriteTransaction} interface
     * @throws AbsTTException
     *             if an I/O error occurs
     */
    abstract void processMove(final IStructNode pFromNode, final IStructNode pToNode,
        final WriteTransaction pWtx) throws AbsTTException;

    /**
     * Insert a node.
     * 
     * @param prtx
     *            read-transaction which implements the {@link IReadTransaction} interface
     * @param pWtx
     *            write-transaction which implements the {@link IWriteTransaction} interface
     * @throws AbsTTException
     *             if insertion of node fails
     */
    abstract void insertNode(final IWriteTransaction pWtx, final IReadTransaction prtx) throws AbsTTException;
}
