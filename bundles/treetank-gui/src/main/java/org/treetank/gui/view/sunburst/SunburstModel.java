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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import org.slf4j.LoggerFactory;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
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
        mLastItems.push(new ArrayList<SunburstItem>(mItems));
        mLastDepths.push(mLastMaxDepth);
        nodeKey = mItems.get(mGUI.mHitTestIndex).mNode.getNodeKey();
        traverseTree(paramContainer.setKey(nodeKey));
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final SunburstContainer paramContainer) {
        assert paramContainer.mKey >= 0;
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<SunburstFireContainer> future =
            executor.submit(new TraverseTree(paramContainer.mKey, this));
        mGUI.mDone = false;
        try {
            mItems = future.get().mItems;
            mLastMaxDepth = future.get().mDepthMax;
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        shutdownAndAwaitTermination(executor);

        firePropertyChange("maxDepth", null, mLastMaxDepth);
        firePropertyChange("done", null, true);
    }

    /** Traverse a tree (single revision). */
    private static final class TraverseTree extends AbsComponent implements Callable<SunburstFireContainer>,
        ITraverseModel {
        /** Key from which to start traversal. */
        private transient long mKey;

        /** {@link SunburstModel} instance. */
        private final SunburstModel mModel;

        /** {@link IReadTransaction} instance. */
        private transient IReadTransaction mRtx;

        /** {@link List} of {@link SunburstItem}s. */
        private final List<SunburstItem> mItems;

        /** {@link NodeRelations} instance. */
        private final NodeRelations mRelations;

        /** Maximum depth in the tree. */
        private transient int mDepthMax;

        /** Minimum text length. */
        private transient int mMinTextLength;

        /** Maximum text length. */
        private transient int mMaxTextLength;

        /** {@link ReadDb} instance. */
        private final ReadDB mDb;

        /** Maximum descendant count in tree. */
        private transient long mMaxDescendantCount;

        /** Parent processing frame. */
        private transient PApplet mParent;

        /**
         * Constructor.
         * 
         * @param paramKey
         *            Key from which to start traversal.
         * @param paramModel
         *            The {@link SunburstModel}.
         */
        private TraverseTree(final long paramKey, final SunburstModel paramModel) {
            assert paramKey >= (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
            assert paramKey >= 0;
            assert paramModel != null;
            mKey = paramKey;
            mModel = paramModel;
            mDb = mModel.mDb;
            try {
                mRtx = mModel.mDb.getSession().beginReadTransaction();
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
            mParent = mModel.mParent;
            addPropertyChangeListener(mModel.mGUI);
            mRelations = new NodeRelations();
            mItems = new LinkedList<SunburstItem>();

            mRtx.moveTo(mKey);
            if (mRtx.getNode().getKind() == ENodes.ROOT_KIND) {
                mRtx.moveToFirstChild();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SunburstFireContainer call() {
            LOGWRAPPER.debug("Build sunburst items.");

            // Get min and max textLength.
            getMinMaxTextLength(mRtx);

            // Iterate over nodes and perform appropriate stack actions internally.
            for (final AbsAxis axis = new SunburstDescendantAxis(mRtx, true, mModel, this); axis.hasNext(); axis
                .next()) {
            }

            LOGWRAPPER.debug("Built " + mItems.size() + " SunburstItems!");

            try {
                mRtx.close();
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            // Fire property changes.
            firePropertyChange("maxDepth", null, mDepthMax);

            return new SunburstFireContainer(mItems, mDepthMax);
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
            LOGWRAPPER.debug("ITEM: " + paramIndex);
            LOGWRAPPER.debug("descendantCount: " + descendantCount);
            LOGWRAPPER.debug("parentDescCount: " + parDescendantCount);
            LOGWRAPPER.debug("indexToParent: " + indexToParent);
            LOGWRAPPER.debug("extension: " + extension);
            LOGWRAPPER.debug("depth: " + depth);
            LOGWRAPPER.debug("angle: " + angle);

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
                mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension, mRelations, mDb)
                    .setNode(node).setText(text).build());
            } else {
                mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension, mRelations, mDb)
                    .setNode(node).setQName(mRtx.getQNameOfCurrentNode()).build());
            }

            // Set depth max.
            mDepthMax = Math.max(depth, mDepthMax);

            return childExtension;
        }

        /** {@inheritDoc} */
        @Override
        public void getMinMaxTextLength(final IReadTransaction paramRtx) {
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
        public List<Future<Integer>> getDescendants(final IReadTransaction paramRtx)
            throws InterruptedException, ExecutionException {
            assert paramRtx != null;

            // Get descendants for every node and save it to a list.
            final List<Future<Integer>> descendants = new LinkedList<Future<Integer>>();
            final ExecutorService executor =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            boolean firstNode = true;
            for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
                if (axis.getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
                    Future<Integer> submit = null;
                    try {
                        submit =
                            executor.submit(new Descendants(mDb.getSession(), paramRtx.getRevisionNumber(),
                                axis.getTransaction().getNode().getNodeKey()));
                    } catch (TTIOException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    }

                    assert submit != null;
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
            Descendants(final ISession paramSession, final long paramRevision, final long paramNodeKey) {
                assert paramSession != null;
                assert !paramSession.isClosed();
                assert paramRevision >= 0;
                try {
                    synchronized (paramSession) {
                        mRtx = paramSession.beginReadTransaction(paramRevision);
                    }
                } catch (final TTIOException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                } catch (final AbsTTException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                }
                mRtx.moveTo(paramNodeKey);
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
}
