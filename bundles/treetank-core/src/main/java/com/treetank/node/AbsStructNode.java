package com.treetank.node;

import com.treetank.settings.EFixed;

public abstract class AbsStructNode extends AbsNode {

    protected static final int FIRST_CHILD_KEY = 2;

    protected static final int LEFT_SIBLING_KEY = 3;

    protected static final int RIGHT_SIBLING_KEY = 4;

    protected static final int CHILD_COUNT = 5;

    protected AbsStructNode(final long[] longBuilder, final int[] intBuilder) {
        super(longBuilder, intBuilder);
    }

    /**
     * Setting the first child key.
     * 
     * @param firstChildKey
     *            the key for the first child.
     */
    public void setFirstChildKey(final long firstChildKey) {
        mLongData[FIRST_CHILD_KEY] = firstChildKey;
    }

    /**
     * Setting the left sibling key.
     * 
     * @param leftSiblingKey
     *            the key for the left sibling.
     */
    public void setLeftSiblingKey(final long leftSiblingKey) {
        mLongData[LEFT_SIBLING_KEY] = leftSiblingKey;
    }

    /**
     * Setting the right sibling key.
     * 
     * @param rightSiblingKey
     *            the key for the right sibling.
     */
    public void setRightSiblingKey(final long rightSiblingKey) {
        mLongData[RIGHT_SIBLING_KEY] = rightSiblingKey;
    }

    /**
     * Gets key of the context item's first child.
     * 
     * @return first child's key
     */
    public long getFirstChildKey() {
        return mLongData[FIRST_CHILD_KEY];
    }

    /**
     * Gets key of the context item's left sibling.
     * 
     * @return left sibling key
     */
    public long getLeftSiblingKey() {
        return mLongData[LEFT_SIBLING_KEY];
    }

    /**
     * Gets key of the context item's right sibling.
     * 
     * @return right sibling key
     */
    public long getRightSiblingKey() {
        return mLongData[RIGHT_SIBLING_KEY];
    }

    /**
     * Declares, whether the item has a first child.
     * 
     * @return true, if item has a first child
     */
    public boolean hasFirstChild() {
        return mLongData[FIRST_CHILD_KEY] != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * Declares, whether the item has a left sibling.
     * 
     * @return true, if item has a left sibling
     */
    public boolean hasLeftSibling() {
        return mLongData[LEFT_SIBLING_KEY] != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * Declares, whether the item has a right sibling.
     * 
     * @return true, if item has a right sibling
     */
    public boolean hasRightSibling() {
        return mLongData[RIGHT_SIBLING_KEY] != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * Gets the number of children of the item.
     * 
     * @return item's number of children
     */
    public long getChildCount() {
        return mLongData[CHILD_COUNT];
    }

    /**
     * Setting the child count.
     * 
     * @param childCount
     *            to be set.
     */
    public void setChildCount(final long childCount) {
        mLongData[CHILD_COUNT] = childCount;
    }

    /**
     * Incrementing the child count.
     */
    public void incrementChildCount() {
        mLongData[CHILD_COUNT]++;
    }

    /**
     * Decrementing the child count.
     */
    public void decrementChildCount() {
        mLongData[CHILD_COUNT]--;
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append("\n\tfirst child: ").append(getFirstChildKey())
            .append("\n\tleft sib: ").append(getLeftSiblingKey()).append("\n\tright sib: ").append(
                getRightSiblingKey()).append("\n\tfirst child: ").append(getFirstChildKey()).append(
                "\n\tchild count: ").append(getChildCount()).toString();
    }

}
