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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TreetankException;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.SunburstItem.StructKind;
import com.treetank.gui.view.tree.TreeModel;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * <h1>SunburstModel</h1>
 * 
 * <p>
 * The model, which interacts with Treetank.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstModel extends AbsModel {
    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(TreeModel.class));

    /** {@link List} of sunburst items. */
    private transient List<SunburstItem> mItems;

    /** Treetank {@link IReadTransaction}. */
    private final IReadTransaction mRtx;

    /** Treetank {@link ISession}. */
    private final ISession mSession;

    /** The controller. */
    private final SunburstController<? extends AbsModel, ? extends AbsView> mController;

    /** The processing {@link PApplet} core library. */
    private final PApplet mParent;

    /** Determines if cursor moved to child of parent node. */
    private enum Moved {
        /** Start of traversal. */
        START,

        /** Next node is a child of the current node. */
        CHILD,

        /** Next node is a sibling of the current node. */
        // SIBLING,

            /** Next node is the rightsibling of the first anchestor node which has one. */
            ANCHESTSIBL
    };

    /** Maximum descendant count in tree. */
    private transient long mMaxDescendantCount;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            The processing {@link PApplet} core library.
     * @param paramDb
     *            {@link ReadDB} instance.
     * @param paramController
     *            The {@link SunburstController}.
     */
    SunburstModel(final PApplet paramApplet, final ReadDB paramDb,
        final SunburstController<? extends AbsModel, ? extends AbsView> paramController) {
        mParent = paramApplet;
        mRtx = paramDb.getRtx();
        mSession = paramDb.getSession();
        mController = paramController;
    }

    /**
     * Get maximum depth in the tree.
     * 
     * @return depthMax.
     */
    Integer getDepthMax() {
        int depthMax = 0;

        for (final SunburstItem item : mItems) {
            depthMax = PApplet.max(item.getDepth(), depthMax);
        }

        return depthMax;
    }

    /**
     * Get sunburst items.
     * 
     * @return List of {@link SunburstItem}s.
     */
    List<SunburstItem> getItems() {
        return mItems;
    }

    /**
     * Traverse the tree and create sunburst items.
     * 
     * @param paramKey
     *            Node key to start from.
     * @param paramTextWeight
     *            Weighting of text length.
     * @return {@link List} of {@link SunburstItem}s.
     */
    List<SunburstItem> traverseTree(final long paramKey, final float paramTextWeight) {
        LOGWRAPPER.debug("Build sunburst items.");
        assert mRtx != null;
        final long nodeKey = mRtx.getNode().getNodeKey();
        mRtx.moveTo(paramKey);
        if (mRtx.getNode().getKind() == ENodes.ROOT_KIND) {
            mRtx.moveToFirstChild();
        }

        // Initialize variables. =============================
        // Sunburst Item list.
        mItems = new LinkedList<SunburstItem>();

        // Initial extension and childExtension.
        float extension = PConstants.TWO_PI;
        float childExtension = 0f;

        // Node relations used for simplyfing the SunburstItem constructor.
        final NodeRelations relations = new NodeRelations();

        // Depth in the tree starting at 0.
        int depth = 0;

        // Start angle.
        float angle = 0f;

        // Child count per depth.
        long childCountPerDepth = ((AbsStructNode)mRtx.getNode()).getChildCount();

        try {
            // Get list of descendants per node.
            final List<Future<Long>> descendants = getDescendants();

            // Determines movement of transaction.
            Moved moved = Moved.START;

            // Index to parent node.
            int indexToParent = -1;
            int index = -1;

            // Setting up stacks.
            final Stack<Float> extensionStack = new Stack<Float>();
            final Stack<Long> childrenPerDepth = new Stack<Long>();
            final Stack<Float> angleStack = new Stack<Float>();
            final Stack<Integer> parentStack = new Stack<Integer>();

            for (final IAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
                switch (moved) {
                case START:
                    break;
                case CHILD:
                    assert !angleStack.empty();
                    angle = angleStack.peek();
                    assert !extensionStack.empty();
                    extension = extensionStack.peek();
                    assert !childrenPerDepth.empty();
                    childCountPerDepth = childCountPerDepth();
                    assert !parentStack.empty();
                    indexToParent = parentStack.peek();
                    break;
                case ANCHESTSIBL:
                    assert !angleStack.empty();
                    angle = angleStack.pop();
                    assert !extensionStack.empty();
                    angle += extensionStack.pop();
                    assert !extensionStack.empty();
                    extension = extensionStack.peek();
                    assert !parentStack.empty();
                    parentStack.pop();
                    assert !parentStack.empty();
                    indexToParent = parentStack.peek();
                    assert !childrenPerDepth.empty();
                    childCountPerDepth = childrenPerDepth.pop();
                    break;
                default:
                    // Do nothing.
                }

                // Add a sunburst item.
                final AbsStructNode node = (AbsStructNode)mRtx.getNode();
                final StructKind structKind =
                    node.hasFirstChild() ? StructKind.ISINNERNODE : StructKind.ISLEAF;
                final long childCount = ((AbsStructNode)mRtx.getNode()).getChildCount();

                if (childCountPerDepth == 0) {
                    final long key = mRtx.getNode().getNodeKey();
                    mRtx.moveToParent();
                    childExtension = extension / (float)((AbsStructNode)mRtx.getNode()).getChildCount();
                    mRtx.moveTo(key);
                } else {
                    childExtension = extension * (float)childCount / (float)childCountPerDepth;
                }

                LOGWRAPPER.debug("indexToParent: " + indexToParent);
                
                if (structKind == StructKind.ISINNERNODE || mRtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                    relations.setAll(depth, structKind, descendants.get(index + 1).get(), 0,
                        mMaxDescendantCount, indexToParent);
                } else {
                    LOGWRAPPER.debug("Text LEAF");
                    final long tmpDescCount = mMaxDescendantCount;
                    mMaxDescendantCount =
                        (long)(mMaxDescendantCount + paramTextWeight * mRtx.getValueOfCurrentNode().length());
                    LOGWRAPPER.debug("max descendant count: " + mMaxDescendantCount);
                    final long descendantCount =
                        (long)(descendants.get(index + 1).get() + paramTextWeight * mRtx
                            .getValueOfCurrentNode().length());
                    LOGWRAPPER.debug("descendant count: " + descendantCount);
                    relations
                        .setAll(depth, structKind, descendantCount, 0, mMaxDescendantCount, indexToParent);
                    mMaxDescendantCount = tmpDescCount;
                }
                mItems.add(new SunburstItem.Builder(mParent, mController, node, mRtx.getQNameOfCurrentNode(),
                    angle, childExtension, relations).build());

                index++;

                if (node.hasFirstChild()) {
                    // Next node will be a child node.
                    angleStack.push(angle);
                    extensionStack.push(childExtension);
                    parentStack.push(index);
                    depth++;
                    moved = Moved.CHILD;

                    // Children per depth.
                    childrenPerDepth.push(childCountPerDepth);
                } else if (node.hasRightSibling()) {
                    // Next node will be a right sibling node.
                    angle += childExtension;
                } else if (!node.hasRightSibling()) {
                    // Next node will be a right sibling of an anchestor node or the traversal ends.
                    moved = Moved.ANCHESTSIBL;
                    final long currNodeKey = mRtx.getNode().getNodeKey();
                    boolean first = true;
                    do {
                        if (((AbsStructNode)mRtx.getNode()).hasParent()) {
                            if (first) {
                                // Do not pop from stack if it's a leaf node.
                                first = false;
                            } else {
                                angleStack.pop();
                                extensionStack.pop();
                                childrenPerDepth.pop();
                                parentStack.pop();
                            }

                            mRtx.moveToParent();
                            depth--;
                        } else {
                            break;
                        }
                    } while (!((AbsStructNode)mRtx.getNode()).hasRightSibling());
                    mRtx.moveTo(currNodeKey);
                }
            }
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        mRtx.moveTo(nodeKey);

        LOGWRAPPER.info(mItems.size() + " SunburstItems created!");

        return mItems;
    }

    /**
     * Traverses all right siblings and sums up child count. Thus a precondition to invoke the method is
     * that it must be called on the first child node.
     * 
     * @return child count per depth
     */
    private long childCountPerDepth() {
        long retVal = 0;
        final long key = mRtx.getNode().getNodeKey();
        do {
            retVal += ((AbsStructNode)mRtx.getNode()).getChildCount();
        } while (((AbsStructNode)mRtx.getNode()).hasRightSibling() && mRtx.moveToRightSibling());
        mRtx.moveTo(key);
        return retVal;
    }

    /**
     * Get a list of descendants per node.
     * 
     * @return List of {@link Future}s.
     * @throws ExecutionException
     *             if execution fails
     * @throws InterruptedException
     *             if task gets interrupted
     */
    private List<Future<Long>> getDescendants() throws InterruptedException, ExecutionException {
        // Get descendants for every node and save it to a list.
        final List<Future<Long>> descendants = new LinkedList<Future<Long>>();
        final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        boolean firstNode = true;
        for (final IAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
            final Future<Long> submit = executor.submit(new Descendants());

            if (firstNode) {
                firstNode = false;
                mMaxDescendantCount = submit.get();
            }
            descendants.add(submit);
        }
        executor.shutdown();

        return descendants;
    }

    /** Counts descendants. */
    private class Descendants implements Callable<Long> {

        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRTX;

        /**
         * Constructor.
         */
        Descendants() {
            try {
                mRTX = mSession.beginReadTransaction();
                mRTX.moveTo(mRtx.getNode().getNodeKey());
            } catch (final TreetankException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }

        @Override
        public Long call() throws Exception {
            long retVal = 0;

            for (final IAxis axis = new DescendantAxis(mRTX, true); axis.hasNext(); axis.next()) {
                retVal += ((AbsStructNode)mRTX.getNode()).getChildCount();
            }

            return retVal;
        }

    }
}
