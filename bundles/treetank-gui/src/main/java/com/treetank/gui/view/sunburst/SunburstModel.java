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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TTException;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.Item.Builder;
import com.treetank.gui.view.sunburst.SunburstItem.StructType;
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
final class SunburstModel extends AbsModel implements IModel, Iterator<SunburstItem> {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SunburstModel.class));

    /** Maximum descendant count in tree. */
    private transient long mMaxDescendantCount;

    /** {@link List} of descendants of every node. */
    private transient List<Future<Long>> mDescendants;

    /** Node relations used for simplyfing the SunburstItem constructor. */
    private transient NodeRelations mRelations;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    SunburstModel(final PApplet paramApplet, final ReadDB paramDb) {
        super(paramApplet, paramDb);
    }

    /** {@inheritDoc} */
    @Override
    public void update(final SunburstContainer paramContainer) {
        long nodeKey = 0;
        try {
            mLock.acquire();
            mLastItems.add(new ArrayList<SunburstItem>(mItems));
            nodeKey = mItems.get(mGUI.mHitTestIndex).mNode.getNodeKey();
        } catch (final InterruptedException e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
        }
        traverseTree(paramContainer.setKey(nodeKey));
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final SunburstContainer paramContainer) {
        assert paramContainer.mKey >= 0;
        try {
            mLock.acquire();
            // final ExecutorService executor = Executors.newSingleThreadExecutor();
            // executor.submit(new TraverseTree(paramContainer.mKey, this));
            // executor.shutdown();
            new TraverseTree(paramContainer.mKey, this).run();
        } catch (final Exception e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
        }
    }

    /**
     * Get a list of descendants per node.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction} over which to iterate.
     * @return List of {@link Future}s.
     * @throws ExecutionException
     *             if execution fails
     * @throws InterruptedException
     *             if task gets interrupted
     */
    private List<Future<Long>> getDescendants(final IReadTransaction paramRtx) throws InterruptedException,
        ExecutionException {
        assert paramRtx != null;

        // Get descendants for every node and save it to a list.
        final List<Future<Long>> descendants = new LinkedList<Future<Long>>();
        final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        boolean firstNode = true;
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            final Future<Long> submit = executor.submit(new Descendants(paramRtx.getNode().getNodeKey()));

            if (firstNode) {
                firstNode = false;
                mMaxDescendantCount = submit.get();
            }
            descendants.add(submit);
        }
        executor.shutdown();

        return descendants;
    }

    /** Traverse a tree (single revision). */
    private final class TraverseTree implements Runnable {
        /** Key from which to start traversal. */
        private transient long mKey;

        /** {@link SunburstModel}. */
        private transient SunburstModel mModel;

        /**
         * Constructor.
         * 
         * @param paramKey
         *            Key from which to start traversal.
         * @param paramModel
         *            The {@link SunburstModel}.
         */
        private TraverseTree(final long paramKey, final SunburstModel paramModel) {
            assert paramKey > -1;
            assert mRtx != null;
            assert !mRtx.isClosed();
            mKey = paramKey;
            mModel = paramModel;
            mRelations = new NodeRelations();

            mRtx.moveTo(mKey);
            if (mRtx.getNode().getKind() == ENodes.ROOT_KIND) {
                mRtx.moveToFirstChild();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            LOGWRAPPER.debug("Build sunburst items.");

            // Remove all elements from item list.
            mItems.clear();

            try {
                // Get min and max textLength.
                getMinMaxTextLength(mRtx);

                // Get list of descendants per node.
                final IReadTransaction rtx = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                rtx.moveTo(mRtx.getNode().getNodeKey());
                mDescendants = getDescendants(rtx);

                // Iterate over nodes and perform appropriate stack actions internally.
                for (final AbsAxis axis = new SunburstDescendantAxis(mRtx, true, mModel); axis
                    .hasNext(); axis.next()) {
                }

                rtx.close();
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final ExecutionException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final TTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            firePropertyChange("maxDepth", null, mDepthMax);
            firePropertyChange("done", null, true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public float createSunburstItem(final Item paramItem, final int paramDepth, final int paramIndex) {
        // Initialize variables.
        final float angle = paramItem.mAngle;
        final long childCountPerDepth = paramItem.mChildCountPerDepth;
        final float extension = paramItem.mExtension;
        final int indexToParent = paramItem.mIndexToParent;
        final int depth = paramDepth;
        final int index = paramIndex;
        
        // Add a sunburst item.
        final AbsStructNode node = (AbsStructNode)mRtx.getNode();
        final StructType structKind = node.hasFirstChild() ? StructType.ISINNERNODE : StructType.ISLEAFNODE;
        long childCount = ((AbsStructNode)mRtx.getNode()).getChildCount();

        // Calculate extension.
        float childExtension = 0f;
        if (childCountPerDepth == 0) {
            final long key = mRtx.getNode().getNodeKey();
            mRtx.moveToParent();
            childCount = ((AbsStructNode)mRtx.getNode()).getChildCount();
            childExtension = extension / (float)childCount;
            mRtx.moveTo(key);
        } else {
            childExtension = extension * (float)childCount / (float)childCountPerDepth;
        }

        LOGWRAPPER.debug("indexToParent: " + indexToParent);

        // Set node relations.
        String text = null;
        if (mRtx.getNode().getKind() == ENodes.TEXT_KIND) {
            mRelations.setAll(depth, structKind, mRtx.getValueOfCurrentNode().length(), mMinTextLength,
                mMaxTextLength, indexToParent);
            text = mRtx.getValueOfCurrentNode();
        } else {
            try {
                mRelations.setAll(depth, structKind, mDescendants.get(index).get(), 0, mMaxDescendantCount,
                    indexToParent);
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final ExecutionException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }

        // Build item.
        if (text != null) {
            mItems.add(new SunburstItem.Builder(mParent, this, angle, childExtension, mRelations, mDb)
                .setNode(node).setText(text).build());
        } else {
            mItems.add(new SunburstItem.Builder(mParent, this, angle, childExtension, mRelations, mDb)
                .setNode(node).setQName(mRtx.getQNameOfCurrentNode()).build());
        }

        // Set depth max.
        mDepthMax = Math.max(depth, mDepthMax);
        
        return childExtension;
    }

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public void run() {
    // LOGWRAPPER.debug("Build sunburst items.");
    //
    // // Initialize variables. =============================
    // // final List<SunburstItem> itemList = new ArrayList<SunburstItem>(mItems);
    // // final int maxDepth = mDepthMax;
    //
    // // Remove all elements from item list.
    // mItems.clear();
    //
    // // Initial extension and childExtension.
    // float extension = PConstants.TWO_PI;
    // float childExtension = 0f;
    //
    // // Node relations used for simplyfing the SunburstItem constructor.
    // final NodeRelations relations = new NodeRelations();
    //
    // // Depth in the tree starting at 0.
    // int depth = 0;
    //
    // // Start angle.
    // float angle = 0f;
    //
    // // Child count per depth.
    // long childCountPerDepth = ((AbsStructNode)mRtx.getNode()).getChildCount();
    //
    // try {
    // // Get min and max textLength.
    // getMinMaxTextLength(mRtx);
    //
    // // Get list of descendants per node.
    // final IReadTransaction rtx = mSession.beginReadTransaction(mRtx.getRevisionNumber());
    // rtx.moveTo(mRtx.getNode().getNodeKey());
    // final List<Future<Long>> descendants = getDescendants(rtx);
    //
    // // Determines movement of transaction.
    // EMoved moved = EMoved.STARTRIGHTSIBL;
    //
    // // Index to parent node.
    // int indexToParent = -1;
    // int index = -1;
    //
    // // Setting up stacks.
    // final Stack<Float> extensionStack = new Stack<Float>();
    // final Stack<Long> childrenPerDepth = new Stack<Long>();
    // final Stack<Float> angleStack = new Stack<Float>();
    // final Stack<Integer> parentStack = new Stack<Integer>();
    //
    // final Item item = Item.ITEM;
    // final Builder builder = Item.BUILDER;
    //
    // for (final AbsAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
    // builder.set(angle, extension, indexToParent).setChildCountPerDepth(childCountPerDepth)
    // .set();
    // moved.processMove(mRtx, item, angleStack, extensionStack, childrenPerDepth, parentStack);
    // angle = item.mAngle;
    // extension = item.mExtension;
    // childCountPerDepth = item.mChildCountPerDepth;
    // indexToParent = item.mIndexToParent;
    //
    // // Add a sunburst item.
    // final AbsStructNode node = (AbsStructNode)mRtx.getNode();
    // // if (depth < 3) {
    // final StructType structKind =
    // node.hasFirstChild() ? StructType.ISINNERNODE : StructType.ISLEAFNODE;
    // long childCount = ((AbsStructNode)mRtx.getNode()).getChildCount();
    //
    // // Calculate extension.
    // if (childCountPerDepth == 0) {
    // final long key = mRtx.getNode().getNodeKey();
    // mRtx.moveToParent();
    // childCount = ((AbsStructNode)mRtx.getNode()).getChildCount();
    // childExtension = extension / (float)childCount;
    // mRtx.moveTo(key);
    // } else {
    // childExtension = extension * (float)childCount / (float)childCountPerDepth;
    // }
    //
    // LOGWRAPPER.debug("indexToParent: " + indexToParent);
    //
    // // Set node relations.
    // String text = null;
    // if (mRtx.getNode().getKind() == ENodes.TEXT_KIND) {
    // relations.setAll(depth, structKind, mRtx.getValueOfCurrentNode().length(),
    // mMinTextLength, mMaxTextLength, indexToParent);
    // text = mRtx.getValueOfCurrentNode();
    // } else {
    // relations.setAll(depth, structKind, descendants.get(index + 1).get(), 0,
    // mMaxDescendantCount, indexToParent);
    // }
    //
    // // Build item.
    // if (text != null) {
    // mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension,
    // relations, mDb).setNode(node).setText(text).build());
    // } else {
    // mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension,
    // relations, mDb).setNode(node).setQName(mRtx.getQNameOfCurrentNode()).build());
    // }
    // // }
    //
    // // Set depth max.
    // mDepthMax = Math.max(depth, mDepthMax);
    //
    // index++;
    //
    // if (node.hasFirstChild()) {
    // // Next node will be a child node.
    // angleStack.push(angle);
    // extensionStack.push(childExtension);
    // parentStack.push(index);
    // depth++;
    // moved = EMoved.CHILD;
    //
    // // Children per depth.
    // childrenPerDepth.push(childCountPerDepth);
    // } else if (node.hasRightSibling()) {
    // // Next node will be a right sibling node.
    // angle += childExtension;
    // moved = EMoved.STARTRIGHTSIBL;
    // } else if (!node.hasRightSibling()) {
    // // Next node will be a right sibling of an anchestor node or the traversal ends.
    // moved = EMoved.ANCHESTSIBL;
    // final long currNodeKey = mRtx.getNode().getNodeKey();
    // boolean first = true;
    // do {
    // if (((AbsStructNode)mRtx.getNode()).hasParent()
    // && mRtx.getNode().getNodeKey() != mKey) {
    // if (first) {
    // // Do not pop from stack if it's a leaf node.
    // first = false;
    // } else {
    // angleStack.pop();
    // extensionStack.pop();
    // childrenPerDepth.pop();
    // parentStack.pop();
    // }
    //
    // mRtx.moveToParent();
    // depth--;
    // } else {
    // break;
    // }
    // } while (!((AbsStructNode)mRtx.getNode()).hasRightSibling());
    // mRtx.moveTo(currNodeKey);
    // }
    // }
    //
    // rtx.close();
    // } catch (final InterruptedException e) {
    // LOGWRAPPER.error(e.getMessage(), e);
    // } catch (final ExecutionException e) {
    // LOGWRAPPER.error(e.getMessage(), e);
    // } catch (final TTException e) {
    // LOGWRAPPER.error(e.getMessage(), e);
    // }
    //
    // // Copy list and fire changes with unmodifiable lists.
    // // mModel.firePropertyChange("items", null,
    // // Collections.unmodifiableList(new ArrayList<SunburstItem>(mItems)));
    // mModel.firePropertyChange("maxDepth", null, mDepthMax);
    // mModel.firePropertyChange("done", null, true);
    //
    // mRtx.moveTo(mKey);
    //
    // LOGWRAPPER.info(mItems.size() + " SunburstItems created!");
    // }
    // }

    /** Counts descendants. */
    private final class Descendants implements Callable<Long> {

        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRTX;

        /**
         * Constructor.
         * 
         * @param paramNodeKey
         *            Node key to which the current transaction should move.
         */
        private Descendants(final long paramNodeKey) {
            assert mRtx != null;
            assert !mRtx.isClosed();
            assert paramNodeKey >= 0;
            try {
                mRTX = mSession.beginReadTransaction(mRtx.getRevisionNumber());
            } catch (final TTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
            mRTX.moveTo(paramNodeKey);
        }

        @Override
        public Long call() throws Exception {
            long retVal = 0;

            for (final AbsAxis axis = new DescendantAxis(mRTX, false); axis.hasNext(); axis.next()) {
                retVal++;
            }

            mRTX.close();
            return retVal;
        }

    }
}
