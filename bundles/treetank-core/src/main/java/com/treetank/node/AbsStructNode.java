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

package com.treetank.node;

import com.treetank.api.IStructuralItem;
import com.treetank.settings.EFixed;

/**
 * Abstact class to represent all structural nodes which are the base for a tree.
 * 
 * Each AbsStructNode represents pointers to the parent, the left- and rightsibling plus a pointer to
 * the firstchild.
 * 
 * The pointers of a node is stored in a longArray while additional data is stored in a intarray.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public abstract class AbsStructNode extends AbsNode implements IStructuralItem {

    /**
     * Pointer to the first child.
     */
    protected static final int FIRST_CHILD_KEY = 3;

    /**
     * Pointer to the left sib.
     */
    protected static final int LEFT_SIBLING_KEY = 4;

    /**
     * Pointer to the right sib.
     */
    protected static final int RIGHT_SIBLING_KEY = 5;

    /**
     * Pointer to children.
     */
    protected static final int CHILD_COUNT = 6;

    /**
     * Abstract constructor with datasetting arrays.
     * 
     * The concrete arrays are set by the concrete classes.
     * 
     * @param paramLongBuilder
     *            long array to be set
     * @param paramIntBuilder
     *            int array to be set
     */
    protected AbsStructNode(final long[] paramLongBuilder, final int[] paramIntBuilder) {
        super(paramLongBuilder, paramIntBuilder);
    }

    /**
     * Setting the first child key.
     * 
     * @param mFirstChildKey
     *            the key for the first child.
     */
    public void setFirstChildKey(final long mFirstChildKey) {
        mLongData[FIRST_CHILD_KEY] = mFirstChildKey;
    }

    /**
     * Setting the left sibling key.
     * 
     * @param mLeftSiblingKey
     *            the key for the left sibling.
     */
    public void setLeftSiblingKey(final long mLeftSiblingKey) {
        mLongData[LEFT_SIBLING_KEY] = mLeftSiblingKey;
    }

    /**
     * Setting the right sibling key.
     * 
     * @param mRightSiblingKey
     *            the key for the right sibling.
     */
    public void setRightSiblingKey(final long mRightSiblingKey) {
        mLongData[RIGHT_SIBLING_KEY] = mRightSiblingKey;
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
     * @param paramChildCount
     *            to be set.
     */
    public void setChildCount(final long paramChildCount) {
        mLongData[CHILD_COUNT] = paramChildCount;
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
