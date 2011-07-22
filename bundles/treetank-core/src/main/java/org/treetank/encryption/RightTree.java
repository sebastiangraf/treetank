package org.treetank.encryption;

/**
 * This class represents a right tree and provides operations on it.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class RightTree {

    /**
     * Root node of tree.
     */
    private RightNode mRoot = null;

    /**
     * Name of group node.
     */
    private String mGroup = null;

    /**
     * Name of user node.
     */
    private String mUser = null;

    /**
     * Constructor for building a new tree.
     * 
     * @param paramRoot
     *            root node.
     */
    public RightTree(final RightNode paramRoot) {
        this.mRoot = paramRoot;
    }

    /**
     * Traverses the tree recursively by a given node.
     * 
     * @param branch
     *            start point of recursion.
     */
    public final void traverseRec(final RightNode branch) {
        if (branch != null) {
            traverseRec(branch.getLeft());
            traverseRec(branch.getRight());
        }
    }

    /**
     * Traverses the tree recursively starting at root node.
     */
    public final void traverse() {
        if (mRoot != null) {
            traverseRec(mRoot.getLeft());
            traverseRec(mRoot.getRight());
        }
    }

    // public void addRec(final RightNode branch) throws TTUsageException {
    // if (branch != null) {
    // if (branch.getNodeName().equals(mGroup)) {
    // if (branch.getLeft() == null) {
    // branch.setLeft(mNewNode);
    // } else if (branch.getRight() == null) {
    // branch.setRight(mNewNode);
    // } else {
    // throw new TTEncryptionException("Only two entities per node are allowed!");
    // }
    // } else {
    // addRec(branch.getLeft());
    // addRec(branch.getRight());
    // }
    // }
    // }
    //
    // public void add(final String groupname, final RightNode node) throws TTUsageException {
    // this.mGroup = groupname;
    // this.mNewNode = node;
    // if (mRoot != null) {
    // addRec(mRoot.getLeft());
    // addRec(mRoot.getRight());
    // }
    // }
    //
    // public void leave(final String groupname, final String username) throws TTUsageException {
    // this.mGroup = groupname;
    // this.mUser = username;
    // if (mRoot != null) {
    // leaveRec(mRoot.getLeft());
    // leaveRec(mRoot.getRight());
    // }
    // }
    //
    // public void leaveRec(final RightNode branch) throws TTUsageException {
    // if (branch != null) {
    // if (branch.getNodeName().equals(mGroup)) {
    // if (branch.getLeft().getNodeName().equals(mUser)) {
    // branch.setLeft(null);
    // } else if (branch.getRight().getNodeName().equals(mUser)) {
    // branch.setRight(null);
    // } else {
    // throw new TTEncryptionException("User does not exist in this group. Nothing has changed!");
    // }
    // } else {
    // leaveRec(branch.getLeft());
    // leaveRec(branch.getRight());
    // }
    // }
    // }

}
