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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.diff.EDiff;
import com.treetank.gui.view.sunburst.Item.Builder;
import com.treetank.node.AbsStructNode;
import com.treetank.settings.EFixed;
import com.treetank.utils.FastStack;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

import processing.core.PConstants;

/**
 * Compare descendant axis.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SunburstCompareDescendantAxis extends AbsAxis {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(SunburstCompareDescendantAxis.class));

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
    private transient float mParExtension;

    /** Extension. */
    private transient float mExtension;

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

    /** {@link IReadTransaction} on old revision. */
    private transient IReadTransaction mOldRtx;

    /** {@link List} of {@link Future}s which hold the number of descendants. */
    private transient List<Future<Integer>> mDescendants;

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
     * @param mIncludeSelf
     *            determines if self is included
     * @param paramModel
     *            {@link IModel} implementation which observes axis changes
     * @param paramNewRtx
     *            {@link IReadTransaction} on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} on old revision
     * @param paramDiffs
     *            {@link List} of {@link EDiff}s
     */
    public SunburstCompareDescendantAxis(final boolean mIncludeSelf, final IModel paramModel,
        final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx, final List<EDiff> paramDiffs) {
        super(paramNewRtx, mIncludeSelf);
        mModel = paramModel;
        mDiffs = paramDiffs;
        mOldRtx = paramOldRtx;
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
        mParExtension = PConstants.TWO_PI;
        mExtension = PConstants.TWO_PI;
        mIndex = -1;
        mItem = Item.ITEM;
        mBuilder = Item.BUILDER;
        try {
            mDescendants = mModel.getDescendants(getTransaction());
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        resetToLastKey();

        // Check for deletions.
        if (!mDiffs.isEmpty()) {
            final EDiff diff = mDiffs.remove(0);
            if (diff == EDiff.DELETED) {
                // FIXME
                mOldRtx.moveTo(diff.getNode().getNodeKey());
                if (getTransaction().getNode().getNodeKey() == mOldRtx.getNode().getParentKey()) {
                    mModificationCount = countDiffs();
                    mParentModificationCount = mDiffStack.peek();
                    try {
                        mDescendantCount = mModel.getDescendants(mOldRtx).get(0).get();
                    } catch (final InterruptedException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    } catch (final ExecutionException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    }
                    mParentDescendantCount = mDescendantsStack.peek();
                    mAngle = mAngleStack.peek();
                    mParExtension = mExtensionStack.peek();
                    mIndexToParent = mParentStack.peek();
                    mIndex++;
                } else if (getTransaction().getNode().getNodeKey() == ((AbsStructNode)mOldRtx.getNode())
                    .getLeftSiblingKey()) {
                    
                }
                mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);
                return true;
            }
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
            mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            final int diffCounts = countDiffs();
            mDiffStack.push(diffCounts);
            mAngleStack.push(mAngle);
            mExtensionStack.push(mExtension);
            mParentStack.push(mIndex);
            try {
                mDescendantsStack.push(mDescendants.get(mIndex).get());
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final ExecutionException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            mDepth++;
            mMoved = EMoved.CHILD;

            return true;
        }

        // Then follow right sibling if there is one.
        if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
            mNextKey = ((AbsStructNode)getTransaction().getNode()).getRightSiblingKey();

            processMove();
            mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            mAngle += mExtension;
            mMoved = EMoved.STARTRIGHTSIBL;

            return true;
        }

        // Then follow right sibling on stack.
        if (mRightSiblingKeyStack.size() > 0) {
            mNextKey = mRightSiblingKeyStack.pop();

            processMove();
            mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

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
        mBuilder.set(mAngle, mParExtension, mIndexToParent).setDescendantCount(mDescendantCount)
            .setParentDescendantCount(mParentDescendantCount).setModificationCount(mModificationCount)
            .setParentModificationCount(mParentModificationCount).set();
        mMoved.processCompareMove(getTransaction(), mItem, mAngleStack, mExtensionStack, mParentStack,
            mParentStack, mDiffStack);
        mModificationCount = mItem.mModificationCount;
        mParentModificationCount = mItem.mParentModificationCount;
        mDescendantCount = mItem.mDescendantCount;
        mParentDescendantCount = mItem.mParentDescendantCount;
        mAngle = mItem.mAngle;
        mParExtension = mItem.mExtension;
        mIndexToParent = mItem.mIndexToParent;
        mIndex++;
    }

    /**
     * Increment diff counter if it's a modified diff.
     * 
     * @param paramIndex
     *            index of diff to get
     * @param paramDiffCounts
     *            diff counter
     * @return modified diff counter
     */
    private int incrDiffCounter(final int paramIndex, int paramDiffCounts) {
        final EDiff intermDiff = mDiffs.get(paramIndex);
        if (intermDiff != EDiff.SAME && intermDiff != EDiff.DONE) {
            paramDiffCounts++;
        }
        return paramDiffCounts;
    }

    /**
     * Count how many differences in the subtree exists.
     * 
     * @return number of differences
     */
    private int countDiffs() {
        int index = 0;
        int diffCounts = 0;
        final long nodeKey = getTransaction().getNode().getNodeKey();
        do {
            boolean done = false;
            diffCounts = incrDiffCounter(index++, diffCounts);
            if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
                getTransaction().moveToFirstChild();
                diffCounts = incrDiffCounter(index++, diffCounts);
            } else if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
                getTransaction().moveToRightSibling();
                diffCounts = incrDiffCounter(index++, diffCounts);
            } else {
                do {
                    if (((AbsStructNode)getTransaction().getNode()).hasParent()
                        && getTransaction().getNode().getNodeKey() != nodeKey) {
                        getTransaction().moveToParent();
                    } else {
                        done = true;
                        break;
                    }
                } while (!((AbsStructNode)getTransaction().getNode()).hasRightSibling());
                if (!done) {
                    getTransaction().moveToRightSibling();
                    diffCounts = incrDiffCounter(index++, diffCounts);
                }
            }
        } while (getTransaction().getNode().getNodeKey() != nodeKey);
        getTransaction().moveTo(nodeKey);
        return diffCounts;
    }
}
