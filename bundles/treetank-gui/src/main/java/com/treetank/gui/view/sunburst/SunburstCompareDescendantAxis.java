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
package com.treetank.gui.view.sunburst;

import java.util.List;
import java.util.Stack;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.diff.EDiff;
import com.treetank.gui.view.sunburst.Item.Builder;
import com.treetank.node.AbsStructNode;
import com.treetank.settings.EFixed;
import com.treetank.utils.FastStack;

import processing.core.PConstants;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SunburstCompareDescendantAxis extends AbsAxis {
    /** Extension stack. */
    private transient Stack<Float> mExtensionStack;

    /** Children per depth. */
    private transient Stack<Integer> mDescendantsStack;

    /** Angle stack. */
    private transient Stack<Float> mAngleStack;

    /** Parent stack. */
    private transient Stack<Integer> mParentStack;

    /** Diff stack. */
    private transient Stack<Integer> mDiffStack;

    /** Determines movement of transaction. */
    private transient EMoved mMoved;

    /** Index to parent node. */
    private transient int mIndexToParent;

    /** Parent extension. */
    private transient float mExtension;

    /** Extension. */
    private transient float mChildExtension;

    /** Depth in the tree starting at 0. */
    private transient int mDepth;

    /** Start angle. */
    private transient float mAngle;

    /** Current item index. */
    private transient int mIndex;

    /** Current item. */
    private transient Item mItem;

    /** Builder for an item. */
    private transient Builder mBuilder;

    /** Stack for remembering next nodeKey in document order. */
    private transient FastStack<Long> mRightSiblingKeyStack;

    /** The nodeKey of the next node to visit. */
    private transient long mNextKey;

    /** Model which implements the method createSunburstItem(...) defined by {@link IModel}. */
    private transient IModel mModel;

    /** {@link List} of {@link EDiff}s. */
    private transient List<EDiff> mDiffs;

    /** Modification count. */
    private transient int mModificationCount;

    /** Parent modification count. */
    private transient int mParentModificationCount;

    /** Descendant count. */
    private transient int mDescendantCount;

    /** Parent descendant count. */
    private transient int mParentDescendantCount;

    /**
     * Constructor initializing internal state.
     * 
     * @param paramRtx
     *            exclusive (immutable) trx to iterate with
     * @param paramModel
     *            {@link IModel} implementation which observes axis changes
     * @param paramDiffs
     *            {@link List} of {@link EDiff}s
     */
    public SunburstCompareDescendantAxis(final IReadTransaction paramRtx, final IModel paramModel,
        final List<EDiff> paramDiffs) {
        super(paramRtx);
        mModel = paramModel;
        mDiffs = paramDiffs;
    }

    /**
     * Constructor initializing internal state.
     * 
     * @param paramRtx
     *            exclusive (immutable) trx to iterate with
     * @param mIncludeSelf
     *            determines if self is included
     * @param paramModel
     *            {@link IModel} implementation which observes axis changes
     * @param paramDiffs
     *            {@link List} of {@link EDiff}s
     */
    public SunburstCompareDescendantAxis(final IReadTransaction paramRtx, final boolean mIncludeSelf,
        final IModel paramModel, final List<EDiff> paramDiffs) {
        super(paramRtx, mIncludeSelf);
        mModel = paramModel;
        mDiffs = paramDiffs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long mNodeKey) {
        super.reset(mNodeKey);
        mRightSiblingKeyStack = new FastStack<Long>();
        if (isSelfIncluded()) {
            mNextKey = getTransaction().getNode().getNodeKey();
        } else {
            mNextKey = ((AbsStructNode)getTransaction().getNode()).getFirstChildKey();
        }
        mExtensionStack = new Stack<Float>();
        mDescendantsStack = new Stack<Integer>();
        mAngleStack = new Stack<Float>();
        mParentStack = new Stack<Integer>();
        mDiffStack = new Stack<Integer>();
        mAngle = 0F;
        mDepth = 0;
        mModificationCount = 0;
        mParentModificationCount = 0;
        mDescendantCount = 0;
        mParentDescendantCount = 0;
        mMoved = EMoved.STARTRIGHTSIBL;
        mIndexToParent = -1;
        mExtension = PConstants.TWO_PI;
        mChildExtension = PConstants.TWO_PI;
        mIndex = -1;
        mItem = Item.ITEM;
        mBuilder = Item.BUILDER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        resetToLastKey();

        // Check for deletions.
        final EDiff diff = mDiffs.remove(0);
        if (diff == EDiff.DELETED) {
            return true;
        }

        // Fail if there is no node anymore.
        if (mNextKey == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            resetToStartKey();
            return false;
        }

        getTransaction().moveTo(mNextKey);

        // Fail if the subtree is finished.
        if (((AbsStructNode)getTransaction().getNode()).getLeftSiblingKey() == getStartKey()) {
            resetToStartKey();
            return false;
        }

        // Always follow first child if there is one.
        if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
            mNextKey = ((AbsStructNode)getTransaction().getNode()).getFirstChildKey();
            if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                mRightSiblingKeyStack.push(((AbsStructNode)getTransaction().getNode()).getRightSiblingKey());
            }

            processMove();
            mChildExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            int index = 0;
            int diffCounts = 0;
            for (final AbsAxis axis = new DescendantAxis(getTransaction(), true); axis.hasNext(); axis.next()) {
                final EDiff intermDiff = mDiffs.get(index);
                if (intermDiff == EDiff.DELETED || intermDiff == EDiff.INSERTED
                    || intermDiff == EDiff.DELETED) {
                    diffCounts++;
                }
                index++;
            }

            mDiffStack.push(diffCounts);
            mAngleStack.push(mAngle);
            mExtensionStack.push(mChildExtension);
            mParentStack.push(mIndex);
            mDescendantsStack.push(mDescendantCount);

            mDepth++;
            mMoved = EMoved.CHILD;

            return true;
        }

        // Then follow right sibling if there is one.
        if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
            mNextKey = ((AbsStructNode)getTransaction().getNode()).getRightSiblingKey();

            processMove();
            mChildExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            mAngle += mChildExtension;
            mMoved = EMoved.STARTRIGHTSIBL;

            return true;
        }

        // Then follow right sibling on stack.
        if (mRightSiblingKeyStack.size() > 0) {
            mNextKey = mRightSiblingKeyStack.pop();

            processMove();
            mChildExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            // Next node will be a right sibling of an anchestor node or the traversal ends.
            mMoved = EMoved.ANCHESTSIBL;
            final long currNodeKey = getTransaction().getNode().getNodeKey();
            boolean first = true;
            do {
                if (((AbsStructNode)getTransaction().getNode()).hasParent()
                    && getTransaction().getNode().getNodeKey() != mNextKey) {
                    if (first) {
                        // Do not pop from stack if it's a leaf node.
                        first = false;
                    } else {
                        mDiffStack.pop();
                        mAngleStack.pop();
                        mExtensionStack.pop();
                        mParentStack.pop();
                        mDescendantsStack.pop();
                    }

                    getTransaction().moveToParent();
                    mDepth--;
                } else {
                    break;
                }
            } while (!((AbsStructNode)getTransaction().getNode()).hasRightSibling());
            getTransaction().moveTo(currNodeKey);

            return true;
        }

        // Then end.
        mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        return true;
    }

    /** Process movement. */
    private void processMove() {
        mBuilder.set(mAngle, mExtension, mIndexToParent).setDescendantCount(mDescendantCount)
            .setParentDescendantCount(mParentDescendantCount).setModificationCount(mModificationCount)
            .setParentModificationCount(mParentModificationCount).set();
        mMoved.processCompareMove(getTransaction(), mItem, mAngleStack, mExtensionStack, mParentStack,
            mParentStack, mDiffStack);
        mModificationCount = mItem.mModificationCount;
        mParentModificationCount = mItem.mParentModificationCount;
        mDescendantCount = mItem.mDescendantCount;
        mParentDescendantCount = mItem.mParentDescendantCount;
        mAngle = mItem.mAngle;
        mExtension = mItem.mExtension;
        mIndexToParent = mItem.mIndexToParent;
        mIndex++;
    }
}
