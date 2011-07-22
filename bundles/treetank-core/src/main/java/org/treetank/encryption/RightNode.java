package org.treetank.encryption;

/**
 * This class represents the model for a right node. Each right node
 * represents an entity at the binary right tree.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class RightNode {

    /**
     * Name of node.
     */
    private String mNodeName;

    /**
     * Left child of node.
     */
    private RightNode mLeft;

    /**
     * Right child of node.
     */
    private RightNode mRight;

    /**
     * Constructor for building a right node.
     * 
     * @param paramName
     *            node name.
     * @param paramLeft
     *            left child.
     * @param paramRight
     *            right child.
     */
    public RightNode(final String paramName, final RightNode paramLeft,
        final RightNode paramRight) {
        this.mNodeName = paramName;
        this.mLeft = paramLeft;
        this.mRight = paramRight;
    }

    /**
     * Returns node name.
     * 
     * @return
     *         node name.
     */
    public final String getNodeName() {
        return mNodeName;
    }

    /**
     * Sets new node name.
     * 
     * @param paramName
     *            new node name.
     */
    public final void setNodeName(final String paramName) {
        this.mNodeName = paramName;
    }

    /**
     * Deletes a node.
     */
    public final void delete() {
        this.mNodeName = null;
        this.mLeft = null;
        this.mRight = null;
    }

    /**
     * Returns left child of node.
     * 
     * @return
     *         left child.
     */
    public final RightNode getLeft() {
        return mLeft;
    }

    /**
     * Sets left child of node.
     * 
     * @param paramLeft
     *            new left child.
     */
    public final void setLeft(final RightNode paramLeft) {
        this.mLeft = paramLeft;
    }

    /**
     * Returns right child of node.
     * 
     * @return
     *         right child.
     */
    public final RightNode getRight() {
        return mRight;
    }

    /**
     * Sets right child of node.
     * 
     * @param paramRight
     *            new right child.
     */
    public final void setRight(final RightNode paramRight) {
        this.mRight = paramRight;
    }
}
