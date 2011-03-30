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

package org.treetank.gui.view.sunburst;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.settings.EFixed;
import org.treetank.utils.FastStack;
import org.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;
import org.treetank.gui.view.sunburst.Item.Builder;

import processing.core.PConstants;

/**
 * Special sunburst compare descendant axis.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SunburstCompareDescendantAxis extends AbsAxis {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(SunburstCompareDescendantAxis.class));

    /** Current diff value. */
    private transient EDiff mCurrDiff;

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

    /** Temporal stack for remembering next nodeKey in document order. */
    private transient FastStack<Long> mTempRightSiblingKeyStack;

    /** The nodeKey of the next node to visit. */
    private transient long mNextKey;

    /** The nodeKey of the next node to visit. */
    private transient long mTempNextKey;

    /** Model which implements the method createSunburstItem(...) defined by {@link IModel}. */
    private final ITraverseModel mModel;

    /** {@link List} of {@link EDiff}s. */
    private final List<Diff> mDiffs;

    /** Modification count. */
    private transient int mModificationCount;

    /** Parent modification count. */
    private transient int mParentModificationCount;

    /** Descendant count. */
    private transient int mDescendantCount;

    /** Parent descendant count. */
    private transient int mParentDescendantCount;

    /** {@link IReadTransaction} on new revision. */
    private transient IReadTransaction mNewRtx;

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

    /** {@link Diff} instance. */
    private transient Diff mDiffCont;

    /**
     * Constructor initializing internal state.
     * 
     * @param paramIncludeSelf
     *            determines if self is included
     * @param paramCallableModel
     *            model which implements {@link ITraverseModel} interface and "observes" axis changes
     * @param paramNewRtx
     *            {@link IReadTransaction} on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} on old revision
     * @param paramDiffs
     *            {@link List} of {@link EDiff}s
     * @param paramMaxDepth
     *            maximum depth in old revision
     */
    public SunburstCompareDescendantAxis(final boolean paramIncludeSelf,
        final ITraverseModel paramCallableModel, final IReadTransaction paramNewRtx,
        final IReadTransaction paramOldRtx, final List<Diff> paramDiffs, final int paramMaxDepth) {
        super(paramNewRtx, paramIncludeSelf);
        mModel = paramCallableModel;
        mDiffs = paramDiffs;
        mNewRtx = paramNewRtx;
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

        mParentModificationCount = 1;
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
            mDiffCont = mDiffs.remove(0);
            mDiff = mDiffCont.getDiff();
            if (mDiff == EDiff.DELETED && mLastDiff != EDiff.DELETED) {
                mNewRtx = getTransaction();
                mOldRtx.moveTo(mDiffCont.getOldNode().getNodeKey());

                if (mMoved == EMoved.ANCHESTSIBL) {
                    final long currNodeKey = getTransaction().getNode().getNodeKey();
                    boolean first = true;
                    do {
                        if (((AbsStructNode)getTransaction().getNode()).hasParent()
                            && getTransaction().getNode().getNodeKey() != mNextKey
                            && mDepth <= mDiffCont.getDepth().getOldDepth()) {
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
                }

                setTransaction(mOldRtx);
                mTempNextKey = mNextKey;
                mTempRightSiblingKeyStack = mRightSiblingKeyStack;
                mRightSiblingKeyStack = new FastStack<Long>();

                processMove();
                calculateDepth();

                mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

                if (getTransaction().getStructuralNode().hasFirstChild()) {
                    mDiffStack.push(mModificationCount);
                    mAngleStack.push(mAngle);
                    mExtensionStack.push(mExtension);
                    mParentStack.push(mIndex);
                    mDescendantsStack.push(mDescendantCount);
                    mMoved = EMoved.CHILD;
                    mDepth++;
                } else if (getTransaction().getStructuralNode().hasRightSibling()) {
                    mAngle += mExtension;
                    mMoved = EMoved.STARTRIGHTSIBL;
                } else {
                    mMoved = EMoved.ANCHESTSIBL;
                }

                mLastDiff = mDiff;
                return true;
            } else if (mDiff != EDiff.DELETED && mLastDiff == EDiff.DELETED) {
                mRightSiblingKeyStack = mTempRightSiblingKeyStack;
                mNextKey = mTempNextKey;

                if (mMoved == EMoved.ANCHESTSIBL) {
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
                }

                setTransaction(mNewRtx);
            } else if (mDiff == EDiff.DELETED) {
                mNextKey = mDiffCont.getOldNode().getNodeKey();

                if (mMoved != EMoved.ANCHESTSIBL) {
                    getTransaction().moveTo(mNextKey);
                }

                // Modify stacks.
                boolean movedToParent = false;
                boolean first = true;
                while (!getTransaction().getStructuralNode().hasRightSibling()) {
                    if (getTransaction().getStructuralNode().hasParent()
                        && getTransaction().getNode().getNodeKey() != mNextKey) {
                        getTransaction().moveToParent();
                        if (!first) {
                            mDiffStack.pop();
                            mAngleStack.pop();
                            mExtensionStack.pop();
                            mParentStack.pop();
                            mDescendantsStack.pop();
                        }
                        first = false;
                        movedToParent = true;
                        mDepth--;
                    } else {
                        break;
                    }
                }
                if (movedToParent) {
                    mMoved = EMoved.ANCHESTSIBL;
                }
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
        if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()
            || (0 < mDiffs.size() && mDiffs.get(0).getDiff() == EDiff.DELETED
                && mOldRtx.moveTo(mDiffs.get(0).getOldNode().getNodeKey()) && mOldRtx.getStructuralNode()
                .getParentKey() == getTransaction().getNode().getNodeKey())) {
            if (getTransaction().getStructuralNode().hasFirstChild()) {
                mNextKey = getTransaction().getStructuralNode().getFirstChildKey();
            } else if (getTransaction().getStructuralNode().hasRightSibling()) {
                mNextKey = getTransaction().getStructuralNode().getRightSiblingKey();
            } else if (mRightSiblingKeyStack.size() > 0) {
                mNextKey = mRightSiblingKeyStack.pop();
            } else {
                mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            }

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

            mLastDiff = mDiff;
            return true;
        }

        getTransaction().moveTo(mNextKey);

        // Then follow right sibling if there is one.
        if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
            mNextKey = ((AbsStructNode)getTransaction().getNode()).getRightSiblingKey();

            processMove();
            calculateDepth();
            mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            mAngle += mExtension;
            mMoved = EMoved.STARTRIGHTSIBL;

            mLastDiff = mDiff;
            return true;
        }

        // Then follow right sibling on stack.
        if (mRightSiblingKeyStack.size() > 0) {
            if (mLastDiff == EDiff.DELETED && mDiff != EDiff.DELETED) {
                mRightSiblingKeyStack.pop();
            }

            if (mRightSiblingKeyStack.size() > 0) {
                mNextKey = mRightSiblingKeyStack.pop();
            } else {
                mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            }

            processMove();
            calculateDepth();
            mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            // Next node will be a right sibling of an anchestor node or the traversal ends.
            mMoved = EMoved.ANCHESTSIBL;
            if (mDiffs.get(0).getDiff() != EDiff.DELETED) {
                moveToNextNode();
            }

            mLastDiff = mDiff;
            return true;
        }

        // Then end.
        mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        processMove();
        calculateDepth();
        if (mDiff == EDiff.DELETED && 0 < mDiffs.size() && mDepth > mDiffs.get(0).getDepth().getNewDepth()) {
            mMoved = EMoved.ANCHESTSIBL;
        }
        mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);
        mLastDiff = mDiff;
        return true;
    }

    /**
     * Move to next "anchestor right sibling node.
     */
    private void moveToNextNode() {
        final long currNodeKey = getTransaction().getNode().getNodeKey();
        boolean first = true;
        do {
            if (((AbsStructNode)getTransaction().getNode()).hasParent()
                && getTransaction().getNode().getNodeKey() != mTempNextKey) {
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
    }

    /**
     * Calculates new depth when same nodes have been encountered before and now an insert or rename occurs.
     */
    private void calculateDepth() {
        if (mDiff != EDiff.SAME && mLastDiff == EDiff.SAME) {
            mTempDepth = mDepth;
            mDepth = mMaxDepth + 2;
        } else if (mDiff == EDiff.SAME && mLastDiff != EDiff.SAME) {
            mDepth = mDiffCont.getDepth().getNewDepth();
        } else if (mDiff == EDiff.DELETED && mDepth < mMaxDepth + 2) {
            mDepth = mMaxDepth + 2;
        }
    }

    /** Process movement. */
    private void processMove() {
        try {
            mDescendantCount = mDescendants.get(mIndex + 1).get();
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mBuilder.set(mAngle, mParExtension, mIndexToParent).setDescendantCount(mDescendantCount)
            .setParentDescendantCount(mParentDescendantCount).setModificationCount(countDiffs())
            .setParentModificationCount(mParentModificationCount).setSubtract(mSubtract).setDiff(mDiff).set();
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
    private int incrDiffCounter(final int paramIndex, final int paramDiffCounts) {
        int diffCounts = paramDiffCounts;
        if (paramIndex == 0) {
            mCurrDiff = mDiff;
        } else {
            assert paramIndex - 1 < mDiffs.size();
            mCurrDiff = mDiffs.get(paramIndex - 1).getDiff();
        }

        if (mCurrDiff != EDiff.SAME) {
            diffCounts++;
        }
        return diffCounts;
    }

    /**
     * Count how many differences in the subtree exists.
     * 
     * @return number of differences plus one
     */
    private int countDiffs() {
        int index = 0;
        int diffCounts = 0;
        Diff diffCont = null;
        mSubtract = false;
        final long nodeKey = getTransaction().getNode().getNodeKey();
        if (getTransaction().getNode().getKind() == ENodes.ROOT_KIND) {
            getTransaction().moveToFirstChild();
        }

        diffCounts = incrDiffCounter(index, diffCounts);
        index++;

        if (diffCounts == 1 && getTransaction().getStructuralNode().hasFirstChild()) {
            mSubtract = true;
        }

        if (mDiff != null && mDiff == EDiff.DELETED) {
            for (final AbsAxis axis = new DescendantAxis(getTransaction()); axis.hasNext(); axis.next()) {
                diffCounts++;
            }
        } else {
            do {
                if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
                    getTransaction().moveToFirstChild();
                    diffCounts = incrDiffCounter(index, diffCounts);
                    index++;
                    if (index - 1 < mDiffs.size()) {
                        diffCont = mDiffs.get(index - 1);
                    }
                    while (index - 1 < mDiffs.size() && mDiffs.get(index - 1).getDiff() == EDiff.DELETED) {
                        mCurrDiff = EDiff.DELETED;
                        if (diffCont.getDepth().getNewDepth() <= mDiffs.get(index - 1).getDepth()
                            .getOldDepth()
                            && mDiffCont.getDepth().getNewDepth() < mDiffs.get(index - 1).getDepth()
                                .getOldDepth()) {
                            diffCounts++;
                        } else {
                            break;
                        }
                        index++;
                    }

                } else {
                    while (!((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                        if (((AbsStructNode)getTransaction().getNode()).hasParent()
                            && getTransaction().getNode().getNodeKey() != nodeKey) {
                            getTransaction().moveToParent();

                            if (index - 1 < mDiffs.size()) {
                                diffCont = mDiffs.get(index - 1);
                            }
                            while (index - 1 < mDiffs.size()
                                && mDiffs.get(index - 1).getDiff() == EDiff.DELETED) {
                                mCurrDiff = EDiff.DELETED;
                                if (diffCont.getDepth().getNewDepth() <= mDiffs.get(index - 1).getDepth()
                                    .getOldDepth()
                                    && mDiffCont.getDepth().getNewDepth() < mDiffs.get(index - 1).getDepth()
                                        .getOldDepth()) {
                                    diffCounts++;
                                } else {
                                    break;
                                }
                                index++;
                            }
                        } else {
                            break;
                        }
                    }
                    if (getTransaction().getNode().getNodeKey() != nodeKey) {
                        getTransaction().moveToRightSibling();
                        diffCounts = incrDiffCounter(index, diffCounts);
                        index++;
                        if (index - 1 < mDiffs.size()) {
                            diffCont = mDiffs.get(index - 1);
                        }
                        while (index - 1 < mDiffs.size() && mDiffs.get(index - 1).getDiff() == EDiff.DELETED) {
                            mCurrDiff = EDiff.DELETED;
                            if (diffCont.getDepth().getNewDepth() <= mDiffs.get(index - 1).getDepth()
                                .getOldDepth()
                                && mDiffCont.getDepth().getNewDepth() < mDiffs.get(index - 1).getDepth()
                                    .getOldDepth()) {
                                diffCounts++;
                            } else {
                                break;
                            }
                            index++;
                        }
                    }
                }
            } while (getTransaction().getNode().getNodeKey() != nodeKey);
            getTransaction().moveTo(nodeKey);
        }

        if (diffCounts == 0 && 0 < mDiffs.size() && mDiffs.get(0).getDiff() == EDiff.DELETED) {
            mOldRtx.moveTo(mDiffs.get(0).getOldNode().getNodeKey());

            int diffIndex = 0;
            do {
                final long key = mOldRtx.getStructuralNode().getNodeKey();

                if (mDiffCont.getDepth().getNewDepth() == (mDiffs.get(diffIndex).getDepth().getOldDepth() - 1)) {
                    for (final AbsAxis axis = new DescendantAxis(mOldRtx, true); axis.hasNext(); axis.next()) {
                        diffCounts++;
                        diffIndex++;
                    }
                } else {
                    break;
                }

                mOldRtx.moveTo(key);
            } while (mOldRtx.getStructuralNode().hasRightSibling() && mOldRtx.moveToRightSibling());
        }

        boolean moved = false;
        if (getTransaction().getNode().getKind() == ENodes.ROOT_KIND
            && ((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
            moved = getTransaction().moveToFirstChild();
        }

        final int retVal = diffCounts + mDescendantCount;

        if (moved) {
            getTransaction().moveToParent();
        }
        return retVal;
    }
}
