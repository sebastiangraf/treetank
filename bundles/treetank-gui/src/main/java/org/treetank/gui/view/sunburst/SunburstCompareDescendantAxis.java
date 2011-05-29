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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.treetank.api.IReadTransaction;
import org.treetank.api.IStructuralItem;
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

    /** The nodeKey of the right sibling of the INSERTED, DELETED or UPDATED node. */
    private transient long mTempKey;

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

    /** Current diff. */
    private transient EDiff mDiff;

    /** Last diff. */
    private transient EDiff mLastDiff;

    /** Determines if {@link ITraverseModel#FACTOR} must be subtracted in the model for the parent item. */
    private transient boolean mSubtract;

    /** {@link Diff} instance. */
    private transient Diff mDiffCont;

    /** Last {@link Diff}. */
    private transient Diff mLastDiffCont;

    /** Initial depth. */
    private transient int mInitDepth;

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
     * @param paramInitDepth
     *            initial depth
     */
    public SunburstCompareDescendantAxis(final boolean paramIncludeSelf,
        final ITraverseModel paramCallableModel, final IReadTransaction paramNewRtx,
        final IReadTransaction paramOldRtx, final List<Diff> paramDiffs, final int paramMaxDepth,
        final int paramInitDepth) {
        super(paramNewRtx, paramIncludeSelf);
        mModel = paramCallableModel;
        mDiffs = paramDiffs;
        mNewRtx = paramNewRtx;
        mOldRtx = paramOldRtx;
        try {
            mDescendants = mModel.getDescendants(getTransaction());
            mParentDescendantCount = mModel.getMaxDescendantCount();
            mDescendantCount = mParentDescendantCount;
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        mParentModificationCount = 1;
        mModificationCount = mParentModificationCount;
        mMaxDepth = paramMaxDepth;
        mInitDepth = paramInitDepth;
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
        mLastDiff = EDiff.SAME;
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
                int tmpDepth = mLastDiffCont.getDepth().getNewDepth();

                if (mMoved == EMoved.ANCHESTSIBL) {
                    if (tmpDepth - mInitDepth > mDiffCont.getDepth().getOldDepth() - mInitDepth) {
                        // Must be done on the transaction which is bound to the new revision.
                        final long currNodeKey = getTransaction().getNode().getNodeKey();
                        boolean first = true;
                        do {
                            if (getTransaction().getStructuralNode().hasParent()
                                && tmpDepth - mInitDepth > mDiffCont.getDepth().getOldDepth() - mInitDepth) {
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

                                tmpDepth--;
                                mDepth--;
                                getTransaction().moveToParent();
                            } else {
                                break;
                            }
                        } while (!getTransaction().getStructuralNode().hasRightSibling());
                        getTransaction().moveTo(currNodeKey);
                    } else {
                        mMoved = EMoved.STARTRIGHTSIBL;
                        mAngle += mExtension;
                    }
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
                mLastDiffCont = mDiffCont;
                return true;
            } else if (mDiff != EDiff.DELETED && mLastDiff == EDiff.DELETED) {
                mRightSiblingKeyStack = mTempRightSiblingKeyStack;
                mNextKey = mTempNextKey;

                if (mMoved == EMoved.ANCHESTSIBL) {
                    boolean first = true;
                    long oldDepth = mLastDiffCont.getDepth().getOldDepth();
                    do {
                        if (getTransaction().getStructuralNode().hasParent()
                            && getTransaction().getNode().getNodeKey() != mNextKey
                            && oldDepth - mInitDepth > mDiffCont.getDepth().getNewDepth() - mInitDepth) {
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
                            oldDepth--;
                        } else {
                            break;
                        }
                    } while (!getTransaction().getStructuralNode().hasRightSibling());
                }

                setTransaction(mNewRtx);
            } else if (mDiff == EDiff.DELETED) {
                mNextKey = mDiffCont.getOldNode().getNodeKey();

                if (mMoved != EMoved.ANCHESTSIBL) {
                    getTransaction().moveTo(mNextKey);
                }

                // Modify stacks.
                moveToNextSibling();
            }
        }

        // Fail if there is no node anymore.
        if (mNextKey == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            resetToStartKey();
            return false;
        }

        getTransaction().moveTo(mNextKey);

        // Fail if the subtree is finished.
        if (getTransaction().getStructuralNode().getLeftSiblingKey() == getStartKey()) {
            resetToStartKey();
            return false;
        }

        if (mDiff == EDiff.UPDATED) {
            // For EDiff.UPDATE the transaction needs to be on the right node.
            mOldRtx.moveTo(mDiffCont.getOldNode().getNodeKey());
        }

        /*
         * Always follow first child if there is one.
         * If not check if a DELETE rooted at the current node follows or if current diff is an UPDATE and
         * DELETES follow because then current values have to be pushed onto the stacks and the movement has
         * to be EMoved.CHILD as well.
         */
        if (getTransaction().getStructuralNode().hasFirstChild()
            || (0 < mDiffs.size() && mDiffs.get(0).getDiff() == EDiff.DELETED
                && mOldRtx.moveTo(mDiffs.get(0).getOldNode().getNodeKey()) && mOldRtx.getStructuralNode()
                .getParentKey() == getTransaction().getNode().getNodeKey())
            || (mDiff == EDiff.UPDATED && mDiffs.get(0).getDiff() == EDiff.DELETED && mDiffs.get(0)
                .getDepth().getOldDepth() == mDiffCont.getDepth().getNewDepth() + 1)) {
            if (getTransaction().getStructuralNode().hasFirstChild()) {
                mNextKey = getTransaction().getStructuralNode().getFirstChildKey();
            } else if (getTransaction().getStructuralNode().hasRightSibling()) {
                mNextKey = getTransaction().getStructuralNode().getRightSiblingKey();
            } else if (mRightSiblingKeyStack.size() > 0) {
                mNextKey = mRightSiblingKeyStack.pop();
            } else {
                mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            }

            if (getTransaction().getStructuralNode().hasFirstChild()
                && getTransaction().getStructuralNode().hasRightSibling()) {
                mRightSiblingKeyStack.push(getTransaction().getStructuralNode().getRightSiblingKey());
            }

            if (getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
                processMove();
                calculateDepth();
                mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

                if (mModel.getIsPruned()) {
                    for (final org.treetank.axis.AbsAxis axis = new DescendantAxis(getTransaction()); axis
                        .hasNext(); axis.next()) {
                        mDiffs.remove(0);
                        mDescendants.remove(0);
                    }

                    if (getTransaction().getStructuralNode().hasRightSibling()) {
                        if (getTransaction().getStructuralNode().hasFirstChild()) {
                            mRightSiblingKeyStack.pop();
                        }

                        mNextKey = getTransaction().getStructuralNode().getRightSiblingKey();
                        mAngle += mExtension;
                        mMoved = EMoved.STARTRIGHTSIBL;
                    } else {
                        if (mLastDiff == EDiff.DELETED && mDiff != EDiff.DELETED && mMoved == EMoved.CHILD) {
                            mRightSiblingKeyStack.pop();
                        }
                        if (!mRightSiblingKeyStack.empty()) {
                            mNextKey = mRightSiblingKeyStack.pop();

                            if (mDiff != EDiff.DELETED && 0 < mDiffs.size()
                                && mDiffs.get(0).getDiff() != EDiff.DELETED) {
                                /*
                                 * Only move to next node if next diff is not a delete, because in deletes it
                                 * moves itself to
                                 * the next node. This has been done because deletes can occur some
                                 * depths/levels
                                 * above but
                                 * between the next node and the current node.
                                 */
                                moveToNextSibling();
                            }
                        } else {
                            mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
                        }
                        mMoved = EMoved.ANCHESTSIBL;
                    }
                } else {
                    mDiffStack.push(mModificationCount);
                    mAngleStack.push(mAngle);
                    mExtensionStack.push(mExtension);
                    mParentStack.push(mIndex);
                    mDescendantsStack.push(mDescendantCount);

                    mDepth++;
                    mMoved = EMoved.CHILD;

                    if (0 >= mDiffs.size()) {
                        mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
                    }
                }
            }

            mLastDiff = mDiff;
            mLastDiffCont = mDiffCont;
            return true;
        }

        getTransaction().moveTo(mNextKey);

        // Then follow right sibling if there is one.
        if (getTransaction().getStructuralNode().hasRightSibling()) {
            mNextKey = getTransaction().getStructuralNode().getRightSiblingKey();

            processMove();
            calculateDepth();
            mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            mAngle += mExtension;
            mMoved = EMoved.STARTRIGHTSIBL;

            mLastDiff = mDiff;
            mLastDiffCont = mDiffCont;
            if (0 >= mDiffs.size()) {
                mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            }

            return true;
        }

        // Then follow right sibling on stack.
        if (mRightSiblingKeyStack.size() > 0) {
            if (mLastDiff == EDiff.DELETED && mDiff != EDiff.DELETED && mMoved == EMoved.CHILD) {
                mRightSiblingKeyStack.pop();
            }

            if (!mRightSiblingKeyStack.empty()) {
                mNextKey = mRightSiblingKeyStack.pop();
            }

            processMove();
            calculateDepth();
            mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

            // Next node will be a right sibling of an anchestor node or the traversal ends.
            mMoved = EMoved.ANCHESTSIBL;
            if (mDiff != EDiff.DELETED && 0 < mDiffs.size() && mDiffs.get(0).getDiff() != EDiff.DELETED) {
                /*
                 * Only move to next node if next diff is not a delete, because in deletes it moves itself to
                 * the next node. This has been done because deletes can occur some depths/levels above but
                 * between the next node and the current node.
                 */
                moveToNextSibling();
            }

            mLastDiff = mDiff;
            mLastDiffCont = mDiffCont;
            if (0 >= mDiffs.size()) {
                mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            }
            return true;
        }

        /*
         * Then end (might be the end of DELETES or the end of all other kinds, such that other nodes still
         * might follow.
         */
        mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        processMove();
        calculateDepth();
        if (mDiff == EDiff.DELETED && 0 < mDiffs.size() && mDepth > mDiffs.get(0).getDepth().getNewDepth()) {
            mMoved = EMoved.ANCHESTSIBL;
        }
        mExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);
        if (0 < mDiffs.size() && mDiffs.get(0).getDiff() == EDiff.DELETED
            && mDepth > mDiffs.get(0).getDepth().getNewDepth()) {
            // Must be anchestsibl movement to pop in the delete step from the stacks.
            mMoved = EMoved.ANCHESTSIBL;
        }
        mLastDiff = mDiff;
        mLastDiffCont = mDiffCont;
        return true;
    }

    /**
     * Simulate move to next sibling node to adapt stacks.
     */
    private void moveToNextSibling() {
        final long currNodeKey = getTransaction().getNode().getNodeKey();
        boolean first = true;
        while (!getTransaction().getStructuralNode().hasRightSibling()
            && getTransaction().getStructuralNode().hasParent()
            && getTransaction().getNode().getNodeKey() != mNextKey &&
            getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
            getTransaction().moveToParent();
            mMoved = EMoved.ANCHESTSIBL;
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
            mDepth--;
        }
        getTransaction().moveTo(currNodeKey);
    }

    /**
     * Calculates new depth for modifications and back to "normal" depth.
     */
    private void calculateDepth() {
        if (mDiff != EDiff.SAME && mDepth < mMaxDepth + 2) {
            mDepth = mMaxDepth + 2;

            if (mDiff == EDiff.DELETED) {
                final IStructuralItem node = mDiffCont.getOldNode();
                setTempKey(node);
            } else {
                final IStructuralItem node = mDiffCont.getNewNode();
                setTempKey(node);
            }
        } else if (mDiff == EDiff.SAME// && mLastDiff != EDiff.SAME
            && mDiffCont.getNewNode().getNodeKey() == mTempKey) {
            mDepth = mDiffCont.getDepth().getNewDepth() - mInitDepth;
        }
    }

    /**
     * Set temporal key which is the next sibling of an INSERTED, DELETED or UPDATED node.
     */
    private void setTempKey(final IStructuralItem paramNode) {
        if (paramNode.hasRightSibling()) {
            mTempKey = paramNode.getRightSiblingKey();
        } else {
            final long key = paramNode.getNodeKey();
            while (!getTransaction().getStructuralNode().hasRightSibling()
                && getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
                getTransaction().moveToParent();
            }
            mTempKey = getTransaction().getStructuralNode().getRightSiblingKey();
            getTransaction().moveTo(key);
        }
    }

    /** Process movement. */
    private void processMove() {
        try {
            mDescendantCount = mDescendants.remove(0).get();
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
        assert paramIndex >= 0;
        assert paramDiffCounts >= 0;
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
     * Count how many differences in the subtree exists and add descendant-or-self count.
     * 
     * @return number of differences plus descendants
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
        index++;

        if (diffCounts == 1 && getTransaction().getStructuralNode().hasFirstChild()) {
            mSubtract = true;
        }

        if (mDiff != null && mDiff == EDiff.DELETED) {
            // Current node has been deleted which means simply count all descendants in old transaction.
            for (final org.treetank.axis.AbsAxis axis = new DescendantAxis(getTransaction()); axis.hasNext(); axis
                .next()) {
                diffCounts++;
            }
        } else if (getTransaction().getStructuralNode().getChildCount() > 0) {
            /*
             * Current node has children, thus traverse tree, count EDiff != SAME nodes and remember to count
             * deleted nodes as well.
             */
            do {
                if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
                    int[] retVal = checkDeletes(index, diffCounts);
                    index = retVal[0];
                    diffCounts = retVal[1];
                    getTransaction().moveToFirstChild();
                    diffCounts = incrDiffCounter(index, diffCounts);
                    index++;
                    int[] ret = checkDeletes(index, diffCounts);
                    index = ret[0];
                    diffCounts = ret[1];
                } else {
                    while (!getTransaction().getStructuralNode().hasRightSibling()
                        && getTransaction().getStructuralNode().hasParent()
                        && getTransaction().getNode().getNodeKey() != nodeKey) {
                        getTransaction().moveToParent();
                        int[] retVal = checkDeletes(index, diffCounts);
                        index = retVal[0];
                        diffCounts = retVal[1];
                    }
                    if (getTransaction().getNode().getNodeKey() != nodeKey) {
                        getTransaction().moveToRightSibling();
                        diffCounts = incrDiffCounter(index, diffCounts);
                        index++;
                        int[] retVal = checkDeletes(index, diffCounts);
                        index = retVal[0];
                        diffCounts = retVal[1];
                    }
                }
            } while (getTransaction().getNode().getNodeKey() != nodeKey);
            getTransaction().moveTo(nodeKey);
        } else if (0 < mDiffs.size() && mDiffs.get(0).getDiff() == EDiff.DELETED) {
            // Current node has no children but might have several deleted children with subtrees.
            mOldRtx.moveTo(mDiffs.get(0).getOldNode().getNodeKey());

            int diffIndex = 0;
            do {
                final long key = mOldRtx.getNode().getNodeKey();

                if (mDiffCont.getDepth().getNewDepth() == (mDiffs.get(diffIndex).getDepth().getOldDepth() - 1)) {
                    // Make sure that subtract is true if current node has changed.
                    if (diffCounts == 1 && mDiff != EDiff.SAME) {
                        mSubtract = true;
                    }
                    for (final org.treetank.axis.AbsAxis axis = new DescendantAxis(mOldRtx, true); axis
                        .hasNext(); axis.next()) {
                        diffCounts++;
                        diffIndex++;
                    }
                } else {
                    break;
                }

                mOldRtx.moveTo(key);
            } while (mOldRtx.getStructuralNode().hasRightSibling() && mOldRtx.moveToRightSibling());
        }

        // Add a factor to add some weighting to the diffCounts.
        return ITraverseModel.FACTOR * diffCounts + mDescendantCount;
    }

    /**
     * Check for deletes and increment diffCount if applicable.
     * 
     * @param paramIndex
     *            index in {@link List} of {@link EDiff}s
     * @param paramDiffCounts
     *            determines how many diffs have been counted (diff != EDiff.SAME)
     * @return integer array with two entries: arr[0] = index; arr[1] = diffCounts
     */
    private int[] checkDeletes(final int paramIndex, final int paramDiffCounts) {
        int index = paramIndex;
        int diffCounts = paramDiffCounts;
        if (index - 1 < mDiffs.size()) {
            final Diff diffCont = mDiffs.get(index - 1);
            while (index - 1 < mDiffs.size() && mDiffs.get(index - 1).getDiff() == EDiff.DELETED) {
                mCurrDiff = EDiff.DELETED;
                if (diffCont.getDepth().getNewDepth() <= mDiffs.get(index - 1).getDepth().getOldDepth()
                    && mDiffCont.getDepth().getNewDepth() < mDiffs.get(index - 1).getDepth().getOldDepth()) {
                    diffCounts++;
                } else {
                    break;
                }
                index++;
            }
        }
        return new int[] {
            index, diffCounts
        };
    }

    /** Decrement item index used for pruning. */
    public void decrementIndex() {
        mIndex--;
    }
}
