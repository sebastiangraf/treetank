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
package org.treetank.gui.view.sunburst;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;
import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.gui.view.sunburst.Item.Builder;
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

    /** Children per depth. */
    private transient Stack<Long> mChildrenPerDepth;

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

    /** Child count per depth. */
    private transient long mChildCountPerDepth;

    /** Current item indes. */
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

    /** {@link List} of {@link Future}s which hold the number of descendants. */
    private transient List<Future<Integer>> mDescendants;

    /**
     * Constructor initializing internal state.
     * 
     * @param paramRtx
     *            exclusive (immutable) trx to iterate with
     * @param paramModel
     *            model which observes axis changes
     */
    public SunburstDescendantAxis(final IReadTransaction paramRtx, final AbsModel paramModel) {
        super(paramRtx);
        mModel = paramModel;
    }

    /**
     * Constructor initializing internal state.
     * 
     * @param paramRtx
     *            exclusive (immutable) trx to iterate with
     * @param mIncludeSelf
     *            determines if self is included
     * @param paramModel
     *            model which observes axis changes
     */
    public SunburstDescendantAxis(final IReadTransaction paramRtx, final boolean mIncludeSelf,
        final AbsModel paramModel) {
        super(paramRtx, mIncludeSelf);
        mModel = paramModel;
        try {
            mDescendants = mModel.getDescendants(getTransaction());
            mParDescendantCount = mDescendants.get(mIndex + 1).get();
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
        mChildrenPerDepth = new Stack<Long>();
        mAngleStack = new Stack<Float>();
        mParentStack = new Stack<Integer>();
        mDescendantsStack = new Stack<Integer>();
        mAngle = 0F;
        mDepth = 0;
        mChildCountPerDepth = ((AbsStructNode)getTransaction().getNode()).getChildCount();
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

            mAngleStack.push(mAngle);
            mExtensionStack.push(mChildExtension);
            mParentStack.push(mIndex);
            mChildrenPerDepth.push(mChildCountPerDepth);
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
                        mAngleStack.pop();
                        mExtensionStack.pop();
                        mChildrenPerDepth.pop();
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
        mChildExtension = mModel.createSunburstItem(mItem, mDepth, mIndex);
        return true;
    }

    /** Process movement. */
    private void processMove() {
        try {
            mBuilder.set(mAngle, mExtension, mIndexToParent).setChildCountPerDepth(mChildCountPerDepth)
                .setParentDescendantCount(mParDescendantCount)
                .setDescendantCount(mDescendants.get(mIndex + 1).get()).set();
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mMoved.processMove(getTransaction(), mItem, mAngleStack, mExtensionStack, mChildrenPerDepth,
            mParentStack, mDescendantsStack);
        mAngle = mItem.mAngle;
        mExtension = mItem.mExtension;
        mChildCountPerDepth = mItem.mChildCountPerDepth;
        mIndexToParent = mItem.mIndexToParent;
        mParDescendantCount = mItem.mParentDescendantCount;
        mDescendantCount = mItem.mDescendantCount;
        mIndex++;
    }
}
