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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;
import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.sunburst.SunburstItem.EStructType;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.settings.EFixed;
import org.treetank.utils.LogWrapper;

import processing.core.PApplet;

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
final class SunburstModel extends AbsModel implements Iterator<SunburstItem> {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SunburstModel.class));

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
            mLastItems.add(new ArrayList<SunburstItem>(mItems));
            nodeKey = mItems.get(mGUI.mHitTestIndex).mNode.getNodeKey();
        traverseTree(paramContainer.setKey(nodeKey));
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final SunburstContainer paramContainer) {
        assert paramContainer.mKey >= 0;
//             final ExecutorService executor = Executors.newSingleThreadExecutor();
//             executor.submit(new TraverseTree(paramContainer.mKey, this));
//             executor.shutdown();
            new TraverseTree(paramContainer.mKey, this).run();
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
            assert paramKey >= (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
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

            // try {
            // Get min and max textLength.
            getMinMaxTextLength(mRtx);

            // // Get list of descendants per node.
            // final IReadTransaction rtx = mSession.beginReadTransaction(mRtx.getRevisionNumber());
            // rtx.moveTo(mRtx.getNode().getNodeKey());
            // mDescendants = getDescendants(rtx);

            // Iterate over nodes and perform appropriate stack actions internally.
            for (final AbsAxis axis = new SunburstDescendantAxis(mRtx, true, mModel); axis.hasNext(); axis
                .next()) {
            }

            // rtx.close();
            // } catch (final InterruptedException e) {
            // LOGWRAPPER.error(e.getMessage(), e);
            // } catch (final ExecutionException e) {
            // LOGWRAPPER.error(e.getMessage(), e);
            // } catch (final TTException e) {
            // LOGWRAPPER.error(e.getMessage(), e);
            // }

            firePropertyChange("maxDepth", null, mDepthMax);
            firePropertyChange("done", null, true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public float createSunburstItem(final Item paramItem, final int paramDepth, final int paramIndex) {
        // Initialize variables.
        final float angle = paramItem.mAngle;
        final float extension = paramItem.mExtension;
        final int indexToParent = paramItem.mIndexToParent;
        final int descendantCount = paramItem.mDescendantCount;
        final int parDescendantCount = paramItem.mParentDescendantCount;
        final int depth = paramDepth;

        // Add a sunburst item.
        final AbsStructNode node = (AbsStructNode)mRtx.getNode();
        final EStructType structKind =
            node.hasFirstChild() ? EStructType.ISINNERNODE : EStructType.ISLEAFNODE;
        
        // Calculate extension. 
        float childExtension = 0f;
        if (depth > 0) {
            childExtension = extension * (float)descendantCount / ((float)parDescendantCount - 1f);
        } else {
            childExtension = extension * (float)descendantCount / (float)parDescendantCount;
        }
        LOGWRAPPER.debug("indexToParent: " + indexToParent);

        // Set node relations.
        String text = null;
        if (mRtx.getNode().getKind() == ENodes.TEXT_KIND) {
            mRelations.setAll(depth, structKind, mRtx.getValueOfCurrentNode().length(), mMinTextLength,
                mMaxTextLength, indexToParent);
            text = mRtx.getValueOfCurrentNode();
        } else {
            mRelations.setAll(depth, structKind, descendantCount, 0, mMaxDescendantCount, indexToParent);
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
    
    /**
     * Get minimum and maximum global text length.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction}
     */
    void getMinMaxTextLength(final IReadTransaction paramRtx) {
        assert paramRtx != null;
        assert !paramRtx.isClosed();
    
        mMinTextLength = Integer.MAX_VALUE;
        mMaxTextLength = Integer.MIN_VALUE;
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            if (paramRtx.getNode().getKind() == ENodes.TEXT_KIND) {
                final int length = paramRtx.getValueOfCurrentNode().length();
                if (length < mMinTextLength) {
                    mMinTextLength = length;
                }

                if (length > mMaxTextLength) {
                    mMaxTextLength = length;
                }
            }
        }
        if (mMinTextLength == Integer.MAX_VALUE) {
            mMinTextLength = 0;
        }
        if (mMaxTextLength == Integer.MIN_VALUE) {
            mMaxTextLength = 0;
        }
        
        LOGWRAPPER.debug("MINIMUM text length: " + mMinTextLength);
        LOGWRAPPER.debug("MAXIMUM text length: " + mMaxTextLength);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Future<Integer>> getDescendants(final IReadTransaction paramRtx) throws InterruptedException,
        ExecutionException {
        assert paramRtx != null;

        // Get descendants for every node and save it to a list.
        final List<Future<Integer>> descendants = new LinkedList<Future<Integer>>();
        final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        boolean firstNode = true;
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            if (axis.getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
                final Future<Integer> submit = executor.submit(new Descendants(paramRtx));

                if (firstNode) {
                    firstNode = false;
                    mMaxDescendantCount = submit.get();
                }
                descendants.add(submit);
            }
        }
        executor.shutdown();

        return descendants;
    }
    
    /** Counts descendants. */
    final class Descendants implements Callable<Integer> {
        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRtx;

        /**
         * Constructor.
         * 
         * @param paramRtx
         *            {@link IReadTransaction} over which to iterate
         */
        Descendants(final IReadTransaction paramRtx) {
            assert paramRtx != null;
            assert !paramRtx.isClosed();
            try {
                mRtx = mSession.beginReadTransaction(paramRtx.getRevisionNumber());
            } catch (final TTIOException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
            mRtx.moveTo(paramRtx.getNode().getNodeKey());
        }

        @Override
        public Integer call() throws Exception {
            int retVal = 0;

            for (final AbsAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
                retVal++;
            }

            mRtx.close();
            return retVal;
        }
    }
}
