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

package org.treetank.node;

import org.treetank.node.interfaces.IStructNode;
import org.treetank.settings.EFixed;

/**
 * Abstact class to represent all structural nodes which are the base for a
 * tree.
 * 
 * Each AbsStructNode represents pointers to the parent, the left- and
 * rightsibling plus a pointer to the firstchild.
 * 
 * The pointers of a node is stored in a longArray while additional data is
 * stored in a intarray.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
@Deprecated
public abstract class AbsStructNode extends AbsNode implements IStructNode {

    /**
     * Pointer to the first child.
     */
    protected static final int FIRST_CHILD_KEY = 24;

    /**
     * Pointer to the left sib.
     */
    protected static final int LEFT_SIBLING_KEY = 32;

    /**
     * Pointer to the right sib.
     */
    protected static final int RIGHT_SIBLING_KEY = 40;

    /**
     * Pointer to children.
     */
    protected static final int CHILD_COUNT = 48;

    /**
     * Abstract constructor with datasetting arrays.
     * 
     * The concrete arrays are set by the concrete classes.
     * 
     * @param paramByteBuilder
     *            byte array to be set
     * @param paramPointerBuilder
     *            byte array to be set
     */
    protected AbsStructNode(final byte[] paramByteBuilder, final byte[] paramPointerBuilder) {
        super(paramByteBuilder, paramPointerBuilder);
    }

    /**
     * Setting the first child key.
     * 
     * @param mFirstChildKey
     *            the key for the first child.
     */
    public void setFirstChildKey(final long mFirstChildKey) {
        writeLongPointer(FIRST_CHILD_KEY, mFirstChildKey);
    }

    /**
     * Setting the left sibling key.
     * 
     * @param mLeftSiblingKey
     *            the key for the left sibling.
     */
    public void setLeftSiblingKey(final long mLeftSiblingKey) {
        writeLongPointer(LEFT_SIBLING_KEY, mLeftSiblingKey);
    }

    /**
     * Setting the right sibling key.
     * 
     * @param mRightSiblingKey
     *            the key for the right sibling.
     */
    public void setRightSiblingKey(final long mRightSiblingKey) {
        writeLongPointer(RIGHT_SIBLING_KEY, mRightSiblingKey);
    }

    /**
     * Gets key of the context item's first child.
     * 
     * @return first child's key
     */
    @Override
    public long getFirstChildKey() {
        return readLongPointer(FIRST_CHILD_KEY);
    }

    /**
     * Gets key of the context item's left sibling.
     * 
     * @return left sibling key
     */
    @Override
    public long getLeftSiblingKey() {
        return readLongPointer(LEFT_SIBLING_KEY);
    }

    /**
     * Gets key of the context item's right sibling.
     * 
     * @return right sibling key
     */
    @Override
    public long getRightSiblingKey() {
        return readLongPointer(RIGHT_SIBLING_KEY);
    }

    /**
     * Declares, whether the item has a first child.
     * 
     * @return true, if item has a first child
     */
    @Override
    public boolean hasFirstChild() {
        return readLongPointer(FIRST_CHILD_KEY) != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * Declares, whether the item has a left sibling.
     * 
     * @return true, if item has a left sibling
     */
    @Override
    public final boolean hasLeftSibling() {
        return readLongPointer(LEFT_SIBLING_KEY) != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * Declares, whether the item has a right sibling.
     * 
     * @return true, if item has a right sibling
     */
    @Override
    public final boolean hasRightSibling() {
        return readLongPointer(RIGHT_SIBLING_KEY) != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    /**
     * Gets the number of children of the item.
     * 
     * @return item's number of children
     */
    @Override
    public final long getChildCount() {
        return readLongPointer(CHILD_COUNT);
    }

    /**
     * Setting the child count.
     * 
     * @param paramChildCount
     *            to be set.
     */
    public void setChildCount(final long paramChildCount) {
        writeLongPointer(CHILD_COUNT, paramChildCount);
    }

    /**
     * Incrementing the child count.
     */
    public void incrementChildCount() {
        long curChildCount = readLongPointer(CHILD_COUNT);
        writeLongPointer(CHILD_COUNT, curChildCount + 1);
    }

    /**
     * Decrementing the child count.
     */
    public void decrementChildCount() {
        final long curChildCount = readLongPointer(CHILD_COUNT);
        writeLongPointer(CHILD_COUNT, curChildCount - 1);
    }

    @Override
    public String toString() {
        return new StringBuilder(super.toString()).append("\n\tfirst child: ").append(getFirstChildKey())
            .append("\n\tleft sib: ").append(getLeftSiblingKey()).append("\n\tright sib: ").append(
                getRightSiblingKey()).append("\n\tfirst child: ").append(getFirstChildKey()).append(
                "\n\tchild count: ").append(getChildCount()).toString();
    }

}
