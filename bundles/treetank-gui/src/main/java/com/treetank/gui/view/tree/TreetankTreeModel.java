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
package com.treetank.gui.view.tree;

import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>TreetankTreeModel</h1>
 * 
 * <p>
 * Extends an AbstractTreeModel and implements main methods, used to construct the Tree representation with
 * Treetank items.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 * 
 */
public final class TreetankTreeModel extends AbstractTreeModel {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TreetankTreeModel.class);

    /** Treetank database {@link IDatabase}. */
    protected transient IDatabase mDatabase;

    /** Treetank reading transaction {@link IReadTransaction}. */
    private transient IReadTransaction mRTX;

    /**
     * Constructor.
     * 
     * @param paramDatabase
     *            {@link IDatabase} on a Treetank file.
     */
    public TreetankTreeModel(final IDatabase paramDatabase) {
        this(paramDatabase, 0, -1);
    }

    /**
     * Constructor.
     * 
     * @param paramDatabase
     *            {@link IDatabase} on a Treetank file.
     * @param paramNodeKeyToStart
     *            NodeKey to move to.
     */
    public TreetankTreeModel(final IDatabase paramDatabase, final long paramNodeKeyToStart) {
        this(paramDatabase, paramNodeKeyToStart, -1);
    }

    /**
     * Constructor.
     * 
     * @param paramDatabase
     *            {@link IDatabase} on a Treetank file.
     * @param paramNodekeyToStart
     *            Starting point of transaction (node key).
     * @param paramRevision
     *            Revision to open.
     */
    public TreetankTreeModel(final IDatabase paramDatabase, final long paramNodekeyToStart,
        final long paramRevision) {
        try {
            if (mDatabase == null || mDatabase.getFile() == null
                || !(mDatabase.getFile().equals(paramDatabase.getFile()))) {
                mDatabase = paramDatabase;

                if (mRTX != null && !mRTX.isClosed()) {
                    mRTX.close();
                }
            }

            if (mRTX == null || mRTX.isClosed()) {
                if (paramRevision == -1) {
                    mRTX = mDatabase.getSession().beginReadTransaction();
                } else {
                    mRTX = mDatabase.getSession().beginReadTransaction(paramRevision);
                }
            }
            mRTX.moveTo(paramNodekeyToStart);
        } catch (final TreetankException e) {
            LOGGER.error("TreetankException: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getChild(final Object paramParent, final int paramIndex) {
        // if (index >= 0 && index < getChildCount(parent)) {
        final IItem parentNode = (IItem)paramParent;
        final long parentNodeKey = parentNode.getNodeKey();
        mRTX.moveTo(parentNodeKey);

        switch (parentNode.getKind()) {
        case ROOT_KIND:
            mRTX.moveToFirstChild();
            return mRTX.getNode();
        case ELEMENT_KIND:
            // Namespaces.
            final int namespCount = ((ElementNode)parentNode).getNamespaceCount();
            if (paramIndex < namespCount) {
                mRTX.moveToNamespace(paramIndex);
                return mRTX.getNode();
            }

            // Attributes.
            final int attCount = ((ElementNode)parentNode).getAttributeCount();
            if (paramIndex < (namespCount + attCount)) {
                mRTX.moveToAttribute(paramIndex - namespCount);
                return mRTX.getNode();
            }
            
            // Children.
            final long childCount = ((ElementNode)parentNode).getChildCount();
            if (paramIndex < (namespCount + attCount + childCount)) {
                mRTX.moveToFirstChild();
                final long upper = paramIndex - namespCount - attCount;
                for (long i = 0; i < upper; i++) {
                    mRTX.moveToRightSibling();
                }
                
                return mRTX.getNode();
            }
        default:
            return null;
        }
        // }
        //
        // return null;
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
        // Parent node.
        mRTX.moveTo(((IItem)paramParent).getNodeKey());
        System.out.println(mRTX.getQNameOfCurrentNode());
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
                if (mRTX.getNode().equals(childNode)) {
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
                if (mRTX.getNode().equals(childNode)) {
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
            switch (parentNode.getKind()) {
            case ROOT_KIND:
                namespCount = 0;
                attCount = 0;
                index = getChildIndex(parentNode, childNode);
                break;
            case ELEMENT_KIND:
                namespCount = ((ElementNode)parentNode).getNamespaceCount();
                attCount = ((ElementNode)parentNode).getAttributeCount();
                index = getChildIndex(parentNode, childNode);
                break;
            default:
                throw new IllegalStateException("Parent node kind not known!");
            }
            break;
        default:
            throw new IllegalStateException("Child node kind not known!");
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
    public boolean isLeaf(final Object node) {
        mRTX.moveTo(((IItem)node).getNodeKey());
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

    /**
     * Get child index.
     * 
     * @param parentNode
     *            Parent node.
     * @param childNode
     *            Child node.
     * @return Index of child node.
     */
    private int getChildIndex(final IItem parentNode, final IItem childNode) {
        int index = -1;
        final long childCount = ((AbsStructNode)parentNode).getChildCount();

        int namespCount = 0;
        int attCount = 0;

        if (parentNode.getKind().getNodeIdentifier() == ENodes.ELEMENT_KIND.getNodeIdentifier()) {
            namespCount = ((ElementNode)parentNode).getNamespaceCount();
            attCount = ((ElementNode)parentNode).getAttributeCount();
        }

        for (int i = 0; i < childCount; i++) {
            if (i == 0) {
                mRTX.moveToFirstChild();
            } else {
                mRTX.moveToRightSibling();
            }
            if (mRTX.getNode().equals(childNode)) {
                index = namespCount + attCount + i;
                break;
            }
        }
        return index;
    }
}
