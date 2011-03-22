/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.gui.view.tree;

import org.slf4j.LoggerFactory;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.node.AbsStructNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.utils.LogWrapper;

/**
 * <h1>TreeModel</h1>
 * 
 * <p>
 * Extends an AbstractTreeModel and implements main methods, used to construct the Tree representation with
 * Treetank items.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 * 
 */
public final class TreeModel extends AbsTreeModel {

    /** Logger. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(TreeModel.class));
    
    /** Treetank {@link IReadTransaction}. */
    private transient IReadTransaction mRTX;

    /**
     * Constructor.
     * 
     * @param paramDB
     *            {@link ReadDB}.
     */
    public TreeModel(final ReadDB paramDB) {
        try {
            mRTX = paramDB.getSession().beginReadTransaction(paramDB.getRevisionNumber());
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChild(final Object paramParent, final int paramIndex) {
        final IItem parentNode = (IItem)paramParent;
        final long parentNodeKey = parentNode.getNodeKey();
        mRTX.moveTo(parentNodeKey);

        switch (parentNode.getKind()) {
        case ROOT_KIND:
            assert paramIndex == 0;
            mRTX.moveToFirstChild();
            return mRTX.getNode();
        case ELEMENT_KIND:
            // Namespaces.
            final int namespCount = ((ElementNode)parentNode).getNamespaceCount();
            if (paramIndex < namespCount) {
                if (!mRTX.moveToNamespace(paramIndex)) {
                    throw new IllegalStateException("No namespace with index " + paramIndex + " found!");
                }
                return mRTX.getNode();
            }

            // Attributes.
            final int attCount = ((ElementNode)parentNode).getAttributeCount();
            if (paramIndex < (namespCount + attCount)) {
                if (!mRTX.moveToAttribute(paramIndex - namespCount)) {
                    throw new IllegalStateException("No attribute with index " + paramIndex + " found!");
                }
                return mRTX.getNode();
            }

            // Children.
            final long childCount = ((ElementNode)parentNode).getChildCount();
            if (paramIndex < (namespCount + attCount + childCount)) {
                if (!mRTX.moveToFirstChild()) {
                    throw new IllegalStateException("No node with index " + paramIndex + " found!");
                }
                final long upper = paramIndex - namespCount - attCount;
                for (long i = 0; i < upper; i++) {
                    if (!mRTX.moveToRightSibling()) {
                        throw new IllegalStateException("No node with index " + paramIndex + " found!");
                    }
                }
                // for (int i = 0; i < childCount; i++) {
                // if (i == 0) {
                // mRTX.moveToFirstChild();
                // } else {
                // mRTX.moveToRightSibling();
                // }
                // if (paramIndex == namespCount + attCount + i) {
                // break;
                // }
                // }

                return mRTX.getNode();
            } else {
                throw new IllegalStateException("May not happen: node with " + paramIndex + " not found!");
            }
        default:
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount(final Object parent) {
        mRTX.moveTo(((IItem)parent).getNodeKey());

        final IItem parentNode = mRTX.getNode();

        switch (parentNode.getKind()) {
        case ROOT_KIND:
            assert ((DocumentRootNode)mRTX.getNode()).hasFirstChild();
            return 1;
        case ELEMENT_KIND:
            final int namespaces = ((ElementNode)parentNode).getNamespaceCount();
            final int attributes = ((ElementNode)parentNode).getAttributeCount();
            final long children = ((ElementNode)parentNode).getChildCount();

            // TODO: possibly unsafe cast.
            return (int)(namespaces + attributes + children);
        default:
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexOfChild(final Object paramParent, final Object paramChild) {
        if (paramParent == null || paramChild == null) {
            return -1;
        }

        // Parent node.
        mRTX.moveTo(((IItem)paramParent).getNodeKey());
        final IItem parentNode = mRTX.getNode();

        // Child node.
        final IItem childNode = (IItem)paramChild;

        // Return value.
        int index = -1;

        // Values needed.
        final long nodeKey = parentNode.getNodeKey();
        int namespCount = 0;
        int attCount = 0;

        switch (childNode.getKind()) {
        case NAMESPACE_KIND:
            namespCount = ((ElementNode)parentNode).getNamespaceCount();
            for (int i = 0; i < namespCount; i++) {
                mRTX.moveToNamespace(i);
                if (mRTX.getNode().getNodeKey() == childNode.getNodeKey()) {
                    index = i;
                    break;
                }
                mRTX.moveTo(nodeKey);
            }
            break;
        case ATTRIBUTE_KIND:
            namespCount = ((ElementNode)parentNode).getNamespaceCount();
            attCount = ((ElementNode)parentNode).getAttributeCount();
            for (int i = 0; i < attCount; i++) {
                mRTX.moveToAttribute(i);
                if (mRTX.getNode().getNodeKey() == childNode.getNodeKey()) {
                    index = namespCount + i;
                    break;
                }
                mRTX.moveTo(nodeKey);
            }
            break;
        case WHITESPACE_KIND:
            break;
        case ELEMENT_KIND:
        case COMMENT_KIND:
        case PROCESSING_KIND:
        case TEXT_KIND:
            final AbsStructNode parent = (AbsStructNode)parentNode;
            if (parent.getKind() == ENodes.ELEMENT_KIND) {
                namespCount = ((ElementNode)parent).getNamespaceCount();
                attCount = ((ElementNode)parent).getAttributeCount();
            }
            final long childCount = parent.getChildCount();

            if (childCount == 0) {
                throw new IllegalStateException("May not happen!");
            }

            for (int i = 0; i < childCount; i++) {
                System.out.println(i);
                if (i == 0) {
                    mRTX.moveToFirstChild();
                } else {
                    mRTX.moveToRightSibling();
                }
                System.out.println("node key: " + mRTX.getNode().getNodeKey());
                if (mRTX.getNode().getNodeKey() == childNode.getNodeKey()) {
                    index = namespCount + attCount + i;
                    System.out.println("LALALA");
                    break;
                }
            }

            System.out.println("ChildCount: " + childCount);
            System.out.println("Child node: " + childNode);

            break;
        default:
            throw new IllegalStateException("Child node kind not known! ");
        }

        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getRoot() {
        mRTX.moveToDocumentRoot();
        return mRTX.getNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaf(final Object paramNode) {
        mRTX.moveTo(((IItem)paramNode).getNodeKey());
        final IItem currNode = mRTX.getNode();

        switch (currNode.getKind()) {
        case ROOT_KIND:
            return false;
        case ELEMENT_KIND:
            final ElementNode elemNode = (ElementNode)currNode;
            if (elemNode.getNamespaceCount() > 0) {
                return false;
            }
            if (elemNode.getAttributeCount() > 0) {
                return false;
            }
            if (elemNode.getChildCount() > 0) {
                return false;
            }
        default:
            // If it's not document root or element node it must be a leaf node.
            return true;
        }
    }
}
