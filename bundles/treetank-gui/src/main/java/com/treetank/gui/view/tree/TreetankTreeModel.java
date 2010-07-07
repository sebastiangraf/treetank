package com.treetank.gui.view.tree;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;

/**
 * <h1>TreetankTreeModel</h1>
 * 
 * <p>
 * Extends an AbstractTreeModel and implements main methods, used to construct
 * the Tree representation with Treetank items.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 * 
 */
public final class TreetankTreeModel extends AbstractTreeModel {
    /** Logger. */
    private static final Log LOGGER = LogFactory
            .getLog(TreetankTreeModel.class);

    /** Treetank reading transaction. */
    private transient static IReadTransaction mRTX;

    /** Treetank database. */
    protected transient static IDatabase mDatabase;

    /**
     * Constructor.
     * 
     * @param database
     *            TreeTank database.
     */
    public TreetankTreeModel(final IDatabase database) {
        this(database, 0);
    }

    /**
     * Constructor.
     * 
     * @param database
     *            TreeTank database.
     * @param nodekeyToStart
     *            NodeKey to move to.
     */
    public TreetankTreeModel(final IDatabase database, final long nodekeyToStart) {
        try {
            if (mDatabase == null || mDatabase.getFile() == null
                    || !(mDatabase.getFile().equals(database.getFile()))) {
                mDatabase = database;

                if (mRTX != null && !mRTX.isClosed()) {
                    mRTX.close();
                }
            }

            if (mRTX == null || mRTX.isClosed()) {
                mRTX = mDatabase.getSession().beginReadTransaction();
            }
            mRTX.moveTo(nodekeyToStart);
        } catch (final TreetankException e) {
            LOGGER.error("TreetankException: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getChild(final Object parent, final int index) {
        // if (index >= 0 && index < getChildCount(parent)) {
        final IItem parentNode = (IItem) parent;
        final long parentNodeKey = parentNode.getNodeKey();
        mRTX.moveTo(parentNodeKey);

        switch (parentNode.getKind()) {
        case ROOT_KIND:
            mRTX.moveToFirstChild();
            return mRTX.getNode();
        case ELEMENT_KIND:
            // Namespaces.
            final int namespCount = ((ElementNode) parentNode)
                    .getNamespaceCount();
            for (int namespIndex = 0; namespIndex < namespCount; namespIndex++) {
                mRTX.moveToNamespace(namespIndex);
                if (namespIndex == index) {
                    return mRTX.getNode();
                }
                mRTX.moveTo(parentNodeKey);
            }

            // Attributes.
            final int attCount = ((ElementNode) parentNode).getAttributeCount();
            for (int attIndex = 0; attIndex < attCount; attIndex++) {
                mRTX.moveToAttribute(attIndex);
                if ((namespCount + attIndex) == index) {
                    return mRTX.getNode();
                }
                mRTX.moveTo(parentNodeKey);
            }

            // Children.
            for (long childIndex = 0, childCount = ((ElementNode) parentNode)
                    .getChildCount(); childIndex < childCount; childIndex++) {
                if (childIndex == 0) {
                    mRTX.moveToFirstChild();
                } else {
                    mRTX.moveToRightSibling();
                }
                if ((namespCount + attCount + childIndex) == index) {
                    return mRTX.getNode();
                }
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
    public int getChildCount(final Object parent) {
        mRTX.moveTo(((IItem) parent).getNodeKey());

        final IItem parentNode = mRTX.getNode();

        switch (parentNode.getKind()) {
        case ROOT_KIND:
            return 1;
        case ELEMENT_KIND:
            final int namespaces = ((ElementNode) parentNode)
                    .getNamespaceCount();
            final int attributes = ((ElementNode) parentNode)
                    .getAttributeCount();
            final long children = ((ElementNode) parentNode).getChildCount();

            // TODO: possibly unsafe cast.
            return (int) (namespaces + attributes + children);
        default:
            System.out.println("Value: " + mRTX.getValueOfCurrentNode());
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getIndexOfChild(final Object parent, final Object child) {
        // Parent node.
        mRTX.moveTo(((IItem) parent).getNodeKey());
        System.out.println(mRTX.getQNameOfCurrentNode());
        final IItem parentNode = mRTX.getNode();

        // Child node.
        final IItem childNode = (IItem) child;

        // Return value.
        int index = -1;

        // Values needed.
        final long nodeKey = parentNode.getNodeKey();
        int namespCount = 0;
        int attCount = 0;

        switch (childNode.getKind()) {
        case NAMESPACE_KIND:
            namespCount = ((ElementNode) parentNode).getNamespaceCount();
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
            namespCount = ((ElementNode) parentNode).getNamespaceCount();
            attCount = ((ElementNode) parentNode).getAttributeCount();
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
                namespCount = ((ElementNode) parentNode).getNamespaceCount();
                attCount = ((ElementNode) parentNode).getAttributeCount();
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
    public Object getRoot() {
        mRTX.moveToDocumentRoot();
        return mRTX.getNode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLeaf(final Object node) {
        mRTX.moveTo(((IItem) node).getNodeKey());
        final IItem currNode = mRTX.getNode();

        switch (currNode.getKind()) {
        case ROOT_KIND:
            return false;
        case ELEMENT_KIND:
            final ElementNode elemNode = (ElementNode) currNode;
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
        final long childCount = ((AbsStructNode) parentNode).getChildCount();

        int namespCount = 0;
        int attCount = 0;

        if (parentNode.getKind().getNodeIdentifier() == ENodes.ELEMENT_KIND
                .getNodeIdentifier()) {
            namespCount = ((ElementNode) parentNode).getNamespaceCount();
            attCount = ((ElementNode) parentNode).getAttributeCount();
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
