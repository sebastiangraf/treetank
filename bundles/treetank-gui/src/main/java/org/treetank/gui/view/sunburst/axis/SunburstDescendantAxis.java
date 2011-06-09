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

package org.treetank.gui.view.sunburst.axis;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;
import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.gui.view.model.ITraverseModel;
import org.treetank.gui.view.sunburst.Item.Builder;
import org.treetank.gui.view.sunburst.EMoved;
import org.treetank.gui.view.sunburst.EPruning;
import org.treetank.gui.view.sunburst.Item;
import org.treetank.node.AbsStructNode;
import org.treetank.settings.EFixed;
import org.treetank.utils.FastStack;
import org.treetank.utils.LogWrapper;

import processing.core.PConstants;

/**
 * Special Sunburst axis.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SunburstDescendantAxis extends AbsAxis {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(SunburstDescendantAxis.class));

    /** Extension stack. */
    private transient Stack<Float> mExtensionStack;

    /** Angle stack. */
    private transient Stack<Float> mAngleStack;

    /** Parent stack. */
    private transient Stack<Integer> mParentStack;

    /** Descendants stack. */
    private transient Stack<Integer> mDescendantsStack;

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

    /** Parent descendant count. */
    private transient int mParDescendantCount;

    /** Descendant count. */
    private transient int mDescendantCount;

    /** Start angle. */
    private transient float mAngle;

    /** Current item indes. */
    private transient int mIndex;

    /** Current item. */
    private transient Item mItem;

    /** Stack for remembering next nodeKey in document order. */
    private transient FastStack<Long> mRightSiblingKeyStack;

    /** The nodeKey of the next node to visit. */
    private transient long mNextKey;

    /** {@link ITraverseModel} which observes axis changes through a callback method. */
    private transient ITraverseModel mModel;

    /** {@link List} of {@link Future}s which hold the number of descendants. */
    private transient List<Future<Integer>> mDescendants;

    /** Determines if tree should be pruned or not. */
    private transient EPruning mPruning;

    /**
     * Constructor initializing internal state.
     * 
     * @param paramRtx
     *            exclusive (immutable) trx to iterate with
     * @param paramIncludeSelf
     *            determines if self is included
     * @param paramModel
     *            model which observes axis changes
     * @param paramTraverseModel
     *            model
     * @param paramPruning
     */
    public SunburstDescendantAxis(final IReadTransaction paramRtx, final boolean paramIncludeSelf,
        final ITraverseModel paramTraverseModel, final EPruning paramPruning) {
        super(paramRtx, paramIncludeSelf);
        assert paramRtx != null;
        assert paramTraverseModel != null;
        assert paramPruning != null;
        mPruning = paramPruning;
        mModel = paramTraverseModel;
        try {
            mDescendants = mModel.getDescendants(getTransaction());
            mParDescendantCount = mModel.getMaxDescendantCount();
            mDescendantCount = mParDescendantCount;
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
    public void reset(final long mNodeKey) {
        super.reset(mNodeKey);
        mRightSiblingKeyStack = new FastStack<Long>();
        if (isSelfIncluded()) {
            mNextKey = getTransaction().getNode().getNodeKey();
        } else {
            mNextKey = ((AbsStructNode)getTransaction().getNode()).getFirstChildKey();
        }
        mExtensionStack = new Stack<Float>();
        mAngleStack = new Stack<Float>();
        mParentStack = new Stack<Integer>();
        mDescendantsStack = new Stack<Integer>();
        mAngle = 0F;
        mDepth = 0;
        mMoved = EMoved.STARTRIGHTSIBL;
        mIndexToParent = -1;
        mExtension = PConstants.TWO_PI;
        mChildExtension = PConstants.TWO_PI;
        mIndex = -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        resetToLastKey();

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
            if (mPruning == EPruning.TRUE && mDepth > 3) {
                processPruned();
            } else {
                if (mIndex + 1 < mDescendants.size()) {
                    mNextKey = ((AbsStructNode)getTransaction().getNode()).getFirstChildKey();
                    boolean hasRightSibling = false;
                    if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                        mRightSiblingKeyStack.push(((AbsStructNode)getTransaction().getNode())
                            .getRightSiblingKey());
                        hasRightSibling = true;
                    }
                    processMove();
                    mChildExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

                    mAngleStack.push(mAngle);
                    mExtensionStack.push(mChildExtension);
                    mParentStack.push(mIndex);
                    mDescendantsStack.push(mDescendantCount);
                    mDepth++;

                    if (mPruning == EPruning.TRUE && mDepth > 3 && !mRightSiblingKeyStack.empty()
                        && hasRightSibling) {
                        mRightSiblingKeyStack.pop();
                    }
                    mMoved = EMoved.CHILD;
                } else {
                    return false;
                }
            }

            return true;
        }

        // Then follow right sibling if there is one.
        if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
            if (mPruning == EPruning.TRUE && mDepth > 3) {
                processPruned();
            } else {
                if (mIndex + 1 < mDescendants.size()) {
                    mNextKey = ((AbsStructNode)getTransaction().getNode()).getRightSiblingKey();
                    processMove();
                    mChildExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);

                    mAngle += mChildExtension;
                    mMoved = EMoved.STARTRIGHTSIBL;
                } else {
                    return false;
                }
            }

            return true;
        }

        // Then follow right sibling on stack.
        if (mRightSiblingKeyStack.size() > 0) {
            if (mPruning == EPruning.TRUE && mDepth > 3) {
                processPruned();
            } else {
                if (mIndex + 1 < mDescendants.size()) {
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
                } else {
                    return false;
                }
            }

            return true;
        }

        // Then end.
        mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        if (mIndex + 1 < mDescendants.size()) {
            processMove();
            mChildExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Process pruned node.
     */
    private void processPruned() {
        if (mIndex + 1 < mDescendants.size()) {
            processMove();
            if (getTransaction().getStructuralNode().hasRightSibling()) {
                getTransaction().moveToRightSibling();
                mMoved = EMoved.STARTRIGHTSIBL;
            } else {
                boolean first = true;
                boolean movedToNextFollowing = false;
                while (!getTransaction().getStructuralNode().hasRightSibling()) {
                    if (getTransaction().getStructuralNode().hasParent()) {
                        if (first) {
                            // Do not pop from stack if it's a leaf node.
                            first = false;
                        } else {
                            mAngleStack.pop();
                            mExtensionStack.pop();
                            mParentStack.pop();
                            mDescendantsStack.pop();
                        }

                        getTransaction().moveToParent();
                        mDepth--;
                        movedToNextFollowing = true;
                    } else {
                        mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
                        return;
                    }
                }
                getTransaction().moveToRightSibling();
                if (movedToNextFollowing) {
                    mMoved = EMoved.ANCHESTSIBL;
                } else {
                    mMoved = EMoved.STARTRIGHTSIBL;
                }
            }
            mIndex--;
            mNextKey = getTransaction().getNode().getNodeKey();
        } else {
            mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        }
    }

    /** Process movement. */
    private void processMove() {
        try {
            mItem = Item.BUILDER.set(mAngle, mExtension, mIndexToParent).setParentDescendantCount(mParDescendantCount)
                .setDescendantCount(mDescendants.get(mIndex + 1).get()).build();
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mMoved.processMove(getTransaction(), mItem, mAngleStack, mExtensionStack, mParentStack,
            mDescendantsStack);
        mAngle = mItem.mAngle;
        mExtension = mItem.mExtension;
        mIndexToParent = mItem.mIndexToParent;
        mParDescendantCount = mItem.mParentDescendantCount;
        mDescendantCount = mItem.mDescendantCount;
        mIndex++;
    }
}
