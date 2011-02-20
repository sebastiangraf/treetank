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
import com.treetank.node.ENodes;
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

    /** Maximum depth in old revision. */
    private transient int mMaxDepth;

    /** Temporal depth. */
    private transient int mTempDepth;

    /** Current diff. */
    private transient EDiff mDiff;

    /** Last diff. */
    private transient EDiff mLastDiff;

    /** Determines if one must be subtracted. */
    private transient boolean mSubtract;

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
     * @param paramMaxDepth
     *            maximum depth in old revision
     */
    public SunburstCompareDescendantAxis(final boolean mIncludeSelf, final IModel paramModel,
        final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx, final List<EDiff> paramDiffs,
        final int paramMaxDepth) {
        super(paramNewRtx, mIncludeSelf);
        mModel = paramModel;
        mDiffs = paramDiffs;
        mOldRtx = paramOldRtx;
        try {
            mDescendants = mModel.getDescendants(getTransaction());
            mParentDescendantCount = mDescendants.get(mIndex + 1).get();
            mDescendantCount = mParentDescendantCount;
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        mParentModificationCount = countDiffs();
        mModificationCount = mParentModificationCount;
        mMaxDepth = paramMaxDepth;
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
        mDescendantCount = 0;
        mParentDescendantCount = 0;
        mMoved = EMoved.STARTRIGHTSIBL;
        mIndexToParent = -1;
        mParExtension = PConstants.TWO_PI;
        mExtension = PConstants.TWO_PI;
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
        if (!mDiffs.isEmpty() && mNextKey != 0) {
            mDiff = mDiffs.remove(0);
            if (mDiff == EDiff.DELETED) {
                if (mLastDiff == EDiff.SAME) {
                    mTempDepth = mDepth;
                    mDepth = mMaxDepth + 2;
                }
                // FIXME
                mOldRtx.moveTo(mDiff.getNode().getNodeKey());
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

            if (getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
                processMove();
                calculateDepth();
                mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

                mDiffStack.push(mModificationCount);
                mAngleStack.push(mAngle);
                mExtensionStack.push(mExtension);
                mParentStack.push(mIndex);
                mDescendantsStack.push(mDescendantCount);

                mDepth++;
                mMoved = EMoved.CHILD;
            }

            return true;
        }

        // Then follow right sibling if there is one.
        if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
            mNextKey = ((AbsStructNode)getTransaction().getNode()).getRightSiblingKey();

            processMove();
            calculateDepth();
            mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            mAngle += mExtension;
            mMoved = EMoved.STARTRIGHTSIBL;

            return true;
        }

        // Then follow right sibling on stack.
        if (mRightSiblingKeyStack.size() > 0) {
            mNextKey = mRightSiblingKeyStack.pop();

            processMove();
            calculateDepth();
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
        processMove();
        calculateDepth();
        mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);
        return true;
    }

    /**
     * Calculates new depth when same nodes have been encountered before and now an insert or rename occurs.
     */
    private void calculateDepth() {
        if ((mDiff == EDiff.INSERTED || mDiff == EDiff.RENAMED) && mLastDiff == EDiff.SAME) {
            mTempDepth = mDepth;
            mDepth = mMaxDepth + 2;
        } else if (mDiff == EDiff.SAME
            && (mLastDiff == EDiff.INSERTED || mLastDiff == EDiff.RENAMED || mLastDiff == EDiff.DELETED)) {
            mDepth = mTempDepth;
        }
    }

    /** Process movement. */
    private void processMove() {
        try {
            mBuilder.set(mAngle, mParExtension, mIndexToParent)
                .setDescendantCount(mDescendants.get(mIndex + 1).get())
                .setParentDescendantCount(mParentDescendantCount).setModificationCount(countDiffs())
                .setParentModificationCount(mParentModificationCount).setSubtract(mSubtract).setDiff(mDiff)
                .set();
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mMoved.processCompareMove(getTransaction(), mItem, mAngleStack, mExtensionStack, mDescendantsStack,
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
        final EDiff intermDiff =
            paramIndex == 0 ? (mDiff == null ? mDiffs.get(paramIndex) : mDiff) : mDiffs.get(paramIndex);
        if (intermDiff != EDiff.SAME && intermDiff != EDiff.DONE) {
            paramDiffCounts++;
        }
        return paramDiffCounts;
    }

    /**
     * Count how many differences in the subtree exists.
     * 
     * @return number of differences plus one
     */
    private int countDiffs() {
        int index = 0;
        int diffCounts = 0;
        mSubtract = false;
        final long nodeKey = getTransaction().getNode().getNodeKey();
        if (getTransaction().getNode().getKind() == ENodes.ROOT_KIND) {
            getTransaction().moveToFirstChild();
        }

        diffCounts = incrDiffCounter(index, diffCounts);
        if (mDiff == null) {
            index++;
        }
        if (diffCounts == 1) {
            mSubtract = true;
        }

        if (index < mDiffs.size()) {
            do {
                if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
                    getTransaction().moveToFirstChild();
                    diffCounts = incrDiffCounter(index, diffCounts);
                    index++;
                } else if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
                    getTransaction().moveToRightSibling();
                    diffCounts = incrDiffCounter(index, diffCounts);
                    index++;
                } else {
                    while (!((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                        if (((AbsStructNode)getTransaction().getNode()).hasParent()
                            && getTransaction().getNode().getNodeKey() != nodeKey) {
                            getTransaction().moveToParent();
                        } else {
                            break;
                        }
                    }
                    if (getTransaction().getNode().getNodeKey() != nodeKey) {
                        getTransaction().moveToRightSibling();
                        diffCounts = incrDiffCounter(index, diffCounts);
                        index++;
                    }
                }
            } while (getTransaction().getNode().getNodeKey() != nodeKey);
            getTransaction().moveTo(nodeKey);
        }

        boolean moved = false;
        if (getTransaction().getNode().getKind() == ENodes.ROOT_KIND
            && ((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
            moved = getTransaction().moveToFirstChild();
        }

        final int retVal =
            ((AbsStructNode)getTransaction().getNode()).hasFirstChild() ? diffCounts
                + childCount(getTransaction()) : diffCounts + 1;

        if (moved) {
            getTransaction().moveToParent();
        }
        return retVal;
    }

    /**
     * Count children which have no first child.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction}
     * @return children which have no first child
     */
    private int childCount(final IReadTransaction paramRtx) {
        int retVal = 0;
        if (((AbsStructNode)paramRtx.getNode()).hasFirstChild()) {
            final long key = paramRtx.getNode().getNodeKey();
            paramRtx.moveToFirstChild();
            do {
                // final AbsStructNode node = (AbsStructNode)paramRtx.getNode();
                // retVal += node.hasFirstChild() ? 0 : 1;
                retVal += 1;
            } while (((AbsStructNode)paramRtx.getNode()).hasRightSibling() && paramRtx.moveToRightSibling());
            paramRtx.moveTo(key);
        }
        return retVal;
    }
}
