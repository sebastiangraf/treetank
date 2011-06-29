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
package org.treetank.gui.view.model;

import java.lang.Thread.State;
import java.util.*;
import java.util.concurrent.*;

import javax.xml.namespace.QName;

import org.datanucleus.util.ViewUtils;
import org.slf4j.LoggerFactory;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IStructuralItem;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.diff.DiffDepth;
import org.treetank.diff.DiffFactory;
import org.treetank.diff.IDiffObserver;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.diff.DiffFactory.EDiffOptimized;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.AbsObservableComponent;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.sunburst.*;
import org.treetank.gui.view.sunburst.SunburstItem.EStructType;
import org.treetank.gui.view.sunburst.axis.SunburstCompareDescendantAxis;
import org.treetank.gui.view.sunburst.model.SunburstCompareModel;
import org.treetank.gui.view.sunburst.model.SunburstModel;
import org.treetank.node.ENodes;
import org.treetank.utils.LogWrapper;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Traverse and compare trees.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TraverseCompareTree extends AbsObservableComponent implements Callable<Void>,
    IDiffObserver, ITraverseModel {

    /** Locking changes which are fired. */
    private final Semaphore mLock;

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(TraverseCompareTree.class));

    /** Timeout for {@link CountDownLatch}. */
    private static final long TIMEOUT_S = 100000;

    /** {@link CountDownLatch} to wait until {@link List} of {@link EDiff}s has been created. */
    private final CountDownLatch mStart;

    /** Revision to compare. */
    private final long mRevision;

    /** Key from which to start traversal. */
    private final long mKey;

    /** {@link SunburstCompareModel}. */
    private final SunburstCompareModel mModel;

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
    private transient int mMaxDescendantCount;

    /** Parent processing frame. */
    private final PApplet mParent;

    /** {@link IReadTransaction} instance. */
    private transient IReadTransaction mRtx;

    /** Weighting of modifications. */
    private transient float mModWeight;

    /** Maximum depth in new revision. */
    private transient int mNewDepthMax;

    /** {@link IReadTransaction} on the revision to compare. */
    private transient IReadTransaction mNewRtx;

    /** {@link List} of {@link EDiff} constants. */
    private transient List<Diff> mDiffs;

    /** Start depth in the tree. */
    private transient int mDepth;

    /** Determines if tree should be pruned or not. */
    private transient EPruning mPrune;

    /** Determines if current item is pruned or not. */
    private transient boolean mIsPruned;

    /** {@link SunburstCompareDescendantAxis} instance. */
    private transient SunburstCompareDescendantAxis mAxis;

    /** GUI which extends {@link AbsSunburstGUI}. */
    private final AbsSunburstGUI mGUI;

    /**
     * Constructor.
     * 
     * @param paramContainer
     *            {@link SunburstContainer} reference
     * @param paramCurrRevision
     *            current revision number
     * @param paramModel
     *            the {@link SunburstModel}
     * @param paramGUI
     *            GUI which extends the {@link SunburstGUI}
     */
    public TraverseCompareTree(final SunburstContainer paramContainer, final long paramCurrRevision,
        final AbsSunburstGUI paramGUI, final AbsModel paramModel) {
        assert paramContainer != null;
        assert paramContainer.getRevision() >= 0;
        assert paramContainer.getStartKey() >= 0;
        assert paramContainer.getDepth() >= 0;
        assert paramContainer.getModWeight() >= 0;
        assert paramContainer.getPruning() != null;
        assert paramModel != null;
        assert paramGUI != null;

        if (paramContainer.getRevision() < paramCurrRevision) {
            throw new IllegalArgumentException(
                "paramNewRevision must be greater than the currently opened revision!");
        }

        mModel = (SunburstCompareModel)paramModel;
        addPropertyChangeListener(mModel);
        mDb = mModel.getDb();

        try {
            mNewRtx = mDb.getSession().beginReadTransaction(paramContainer.getRevision());
            mRtx = mModel.getDb().getSession().beginReadTransaction(paramCurrRevision);
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        mGUI = paramGUI;
        mRevision = paramContainer.getRevision();
        mKey =
            paramContainer.getStartKey() == 0 ? paramContainer.getStartKey() + 1 : paramContainer
                .getStartKey();
        mModWeight = paramContainer.getModWeight();
        mRelations = new NodeRelations();
        mDiffs = new LinkedList<Diff>();
        mStart = new CountDownLatch(1);
        mItems = new LinkedList<SunburstItem>();
        mParent = mModel.getParent();
        mDepth = paramContainer.getDepth();
        mRtx.moveTo(mKey);
        mNewRtx.moveTo(mKey);
        mPrune = paramContainer.getPruning();
        mLock = paramContainer.getLock();
    }

    @Override
    public Void call() {
        LOGWRAPPER.debug("Build sunburst items.");

        // Remove all elements from item list.
        mItems.clear();

        try {
            // Get min and max textLength of both revisions.
            final IReadTransaction rtx = mDb.getSession().beginReadTransaction(mRevision);
            getMinMaxTextLength(rtx);
            rtx.close();

            // Invoke diff.
            LOGWRAPPER.debug("CountDownLatch: " + mStart.getCount());
            final Set<IDiffObserver> observer = new HashSet<IDiffObserver>();
            observer.add(this);
            DiffFactory.invokeStructuralDiff(new DiffFactory.Builder(mDb.getDatabase(), mKey, mRevision, mRtx
                .getRevisionNumber(), EDiffOptimized.NO, observer).setNewDepth(mDepth).setOldDepth(mDepth));

            // Wait for diff list to complete.
            final boolean done = mStart.await(TIMEOUT_S, TimeUnit.SECONDS);
            if (!done) {
                LOGWRAPPER.error("Diff failed - Timeout occured after " + TimeUnit.SECONDS + " seconds!");
            }

            // for (final Diff diff : mDiffs) {
            // final EDiff diffEnum = diff.getDiff();
            //
            // LOGWRAPPER.debug("diff: " + diffEnum);
            // }

            // Maximum depth in old revision.
            mDepthMax = getDepthMax(mRtx);
//            ViewUtilities.stackTraces();

            mLock.acquireUninterruptibly();
            for (mAxis =
                new SunburstCompareDescendantAxis(true, this, mNewRtx, mRtx, mDiffs, mDepthMax, mDepth); mAxis
                .hasNext(); mAxis.next())
                ;
            mLock.release();
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        try {
            mRtx.close();
            mNewRtx.close();
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        LOGWRAPPER.info(mItems.size() + " SunburstItems created!");
        LOGWRAPPER.debug("oldMaxDepth: " + mDepthMax);

        mLock.acquireUninterruptibly();
        firePropertyChange("oldMaxDepth", null, mDepthMax);
        firePropertyChange("maxDepth", null, mNewDepthMax);
        firePropertyChange("items", null, mItems);
        firePropertyChange("done", null, true);

        LOGWRAPPER.debug("Property changes sent!");
        // Lock is released in the controller.

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void diffListener(final EDiff paramDiff, final IStructuralItem paramNewNode,
        final IStructuralItem paramOldNode, final DiffDepth paramDepth) {
        mDiffs.add(new Diff(paramDiff, paramNewNode, paramOldNode, paramDepth));
    }

    /** {@inheritDoc} */
    @Override
    public void diffDone() {
        mStart.countDown();
    }

    /**
     * Get the maximum depth in the tree.
     * 
     * @param paramRtx
     *            {@link IReadTransaction} on the tree
     * @return maximum depth
     */
    private int getDepthMax(final IReadTransaction paramRtx) {
        assert paramRtx != null;
        assert !paramRtx.isClosed();

        int depthMax = 0;
        int depth = 0;
        int index = -1;
        final long nodeKey = paramRtx.getNode().getNodeKey();
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            index++;
            while (index < mDiffs.size() && mDiffs.get(index).getDiff() == EDiff.INSERTED) {
                index++;
            }
            final IStructuralItem node = paramRtx.getStructuralNode();
            if (node.hasFirstChild()) {
                depth++;

                int tmpIndex = index + 1;
                while (tmpIndex < mDiffs.size() && mDiffs.get(tmpIndex).getDiff() == EDiff.INSERTED) {
                    tmpIndex++;
                }
                index = tmpIndex - 1;

                if (// index < mDiffs.size() && mDiffs.get(index).getDiff() == EDiff.SAME
                index + 1 < mDiffs.size() && mDiffs.get(index + 1).getDiff() == EDiff.SAME) {
                    // Set depth max.
                    depthMax = Math.max(depth, depthMax);
                }
            } else if (node.hasRightSibling()) {
                if (index + 1 < mDiffs.size() && mDiffs.get(index + 1).getDiff() == EDiff.SAME) {
                    // Set depth max.
                    depthMax = Math.max(depth, depthMax);
                }
            } else if (!node.hasRightSibling()) {
                // Next node will be a right sibling of an anchestor node or the traversal ends.
                final long currNodeKey = mRtx.getNode().getNodeKey();
                do {
                    if (mRtx.getStructuralNode().hasParent()) {
                        mRtx.moveToParent();
                        depth--;
                    } else {
                        break;
                    }
                } while (!mRtx.getStructuralNode().hasRightSibling());
                mRtx.moveTo(currNodeKey);

                if (index + 1 < mDiffs.size() && mDiffs.get(index + 1).getDiff() == EDiff.SAME) {
                    // Set depth max.
                    depthMax = Math.max(depth, depthMax);
                }
            }
        }
        paramRtx.moveTo(nodeKey);
        return depthMax;
    }

    /** {@inheritDoc} */
    @Override
    public void getMinMaxTextLength(final IReadTransaction paramRtx) {
        assert paramRtx != null;
        assert !paramRtx.isClosed();

        mMinTextLength = Integer.MAX_VALUE;
        mMaxTextLength = Integer.MIN_VALUE;
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            if (axis.getTransaction().getNode().getKind() == ENodes.TEXT_KIND) {
                final int length = axis.getTransaction().getValueOfCurrentNode().length();
                if (length < mMinTextLength) {
                    mMinTextLength = length;
                }

                if (length > mMaxTextLength) {
                    mMaxTextLength = length;
                }
            }
        }

        for (final AbsAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
            if (axis.getTransaction().getNode().getKind() == ENodes.TEXT_KIND) {
                final int length = axis.getTransaction().getValueOfCurrentNode().length();
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

    /** {@inheritDoc} */
    @Override
    public float createSunburstItem(final Item paramItem, final int paramDepth, final int paramIndex) {
        // Initialize variables.
        final float angle = paramItem.mAngle;
        final float parExtension = paramItem.mExtension;
        final int indexToParent = paramItem.mIndexToParent;
        final int descendantCount = paramItem.mDescendantCount;
        final int parentDescCount = paramItem.mParentDescendantCount;
        final int modificationCount = paramItem.mModificationCount;
        long parentModificationCount = paramItem.mParentModificationCount;
        final boolean subtract = paramItem.mSubtract;
        final EDiff diff = paramItem.mDiff;
        final int depth = paramDepth;

        // Calculate extension.
        float extension = 2 * PConstants.PI;
        if (indexToParent > -1) {
            if (mItems.get(indexToParent).getSubtract()) {
                parentModificationCount -= FACTOR;
            }
            extension =
                (1 - mModWeight) * (parExtension * (float)descendantCount / ((float)parentDescCount - 1f))
                    + mModWeight
                    // -1 because we add the descendant-or-self count to the
                    // modificationCount/parentModificationCount.
                    * (parExtension * (float)modificationCount / ((float)parentModificationCount - 1f));
        }

        LOGWRAPPER.debug("ITEM: " + paramIndex);
        LOGWRAPPER.debug("modificationCount: " + modificationCount);
        LOGWRAPPER.debug("parentModificationCount: " + parentModificationCount);
        LOGWRAPPER.debug("descendantCount: " + descendantCount);
        LOGWRAPPER.debug("parentDescCount: " + parentDescCount);
        LOGWRAPPER.debug("indexToParent: " + indexToParent);
        LOGWRAPPER.debug("extension: " + extension);
        LOGWRAPPER.debug("depth: " + depth);
        LOGWRAPPER.debug("angle: " + angle);

        if (mPrune == EPruning.TRUE && extension < 0.03f && modificationCount <= descendantCount) {
            mAxis.decrementIndex();
            mIsPruned = true;
        } else {
            // Add a sunburst item.
            mIsPruned = false;
            IStructuralItem node = null;
            if (diff == EDiff.DELETED) {
                node = mRtx.getStructuralNode();
            } else {
                node = mNewRtx.getStructuralNode();
            }
            final EStructType structKind =
                node.hasFirstChild() ? EStructType.ISINNERNODE : EStructType.ISLEAFNODE;

            // Set node relations.
            String text = null;
            if (node.getKind() == ENodes.TEXT_KIND) {
                if (diff == EDiff.DELETED) {
                    text = mRtx.getValueOfCurrentNode();
                } else {
                    text = mNewRtx.getValueOfCurrentNode();
                }
                mRelations.setSubtract(subtract).setAll(depth, structKind, text.length(), mMinTextLength,
                    mMaxTextLength, indexToParent);
            } else {
                mRelations.setSubtract(subtract).setAll(depth, structKind, descendantCount, 0,
                    mMaxDescendantCount, indexToParent);
            }

            // Build item.
            synchronized (mModel) {
                final SunburstItem.Builder builder =
                    new SunburstItem.Builder(mParent, mModel, angle, extension, mRelations, mDb, mGUI)
                        .setNode(node).setDiff(diff);

                if (modificationCount > descendantCount) {
                    final int diffCounts = (modificationCount - descendantCount) / ITraverseModel.FACTOR;
                    LOGWRAPPER.debug("modCount: " + diffCounts);
                    builder.setModifcations(diffCounts);
                }

                if (text != null) {
                    LOGWRAPPER.debug("text: " + text);
                    builder.setText(text);
                } else {
                    QName name = null;
                    if (diff == EDiff.DELETED) {
                        name = mRtx.getQNameOfCurrentNode();
                    } else {
                        name = mNewRtx.getQNameOfCurrentNode();
                    }
                    LOGWRAPPER.debug("name: " + name.getLocalPart());
                    builder.setQName(name);
                }
                updated(diff, builder);
                mItems.add(builder.build());
            }

            // Set depth max.
            mNewDepthMax = Math.max(depth, mNewDepthMax);
        }

        return extension;
    }

    /**
     * Add old node text or {@link QName} to the {@link SunburstItem.Builder}.
     * 
     * @param paramDiff
     *            determines if it's value is EDiff.UPDATED
     * @param builder
     *            {@link SunburstItem.Builder} instance
     */
    private void updated(final EDiff paramDiff, final SunburstItem.Builder builder) {
        if (paramDiff == EDiff.UPDATED) {
            final IStructuralItem oldNode = mRtx.getStructuralNode();
            if (oldNode.getKind() == ENodes.TEXT_KIND) {
                builder.setOldText(mRtx.getValueOfCurrentNode());
            } else {
                builder.setOldQName(mRtx.getQNameOfCurrentNode());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean getIsPruned() {
        return mIsPruned;
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
        int index = 0;
        final int depth = mDiffs.get(0).getDepth().getNewDepth();
        try {
            final IReadTransaction newRtx =
                mDb.getSession().beginReadTransaction(paramRtx.getRevisionNumber());
            newRtx.moveTo(paramRtx.getNode().getNodeKey());

            for (final AbsAxis axis = new DescendantAxis(newRtx, true); index < mDiffs.size()
                && mDiffs.get(index).getDiff() == EDiff.DELETED
                && depth < mDiffs.get(index).getDepth().getOldDepth() || axis.hasNext();) {
                if (axis.getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
                    if (mDiffs.get(index).getDiff() != EDiff.DELETED) {
                        axis.next();
                    }

                    Future<Integer> submit =
                        executor.submit(new Descendants(newRtx.getRevisionNumber(), mRtx.getRevisionNumber(),
                            axis.getTransaction().getNode().getNodeKey(), mDb.getSession(), index));
                    if (firstNode) {
                        firstNode = false;
                        mMaxDescendantCount = submit.get();
                    }

                    descendants.add(submit);
                    index++;
                }
            }

            newRtx.close();
        } catch (final TTIOException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        executor.shutdown();

        return descendants;
    }

    /** Counts descendants. */
    private final class Descendants implements Callable<Integer> {
        /** Treetank {@link IReadTransaction} on new revision. */
        private transient IReadTransaction mNewRtx;

        /** Treetank {@link IReadTransaction} on old revision. */
        private transient IReadTransaction mOldRtx;

        /** Index of current diff. */
        private int mIndex;

        /**
         * Constructor.
         * 
         * @param paramNewRevision
         *            new revision number
         * @param paramOldRevision
         *            old revision number
         * @param paramKey
         *            key of node
         * @param paramSession
         *            {@link ISession} instance
         * @param paramIndex
         *            current index in diff list
         * @param paramDiffs
         *            {@link List} of {@link Diff}s
         */
        Descendants(final long paramNewRevision, final long paramOldRevision, final long paramKey,
            final ISession paramSession, final int paramIndex) {
            assert paramNewRevision > 0;
            assert paramOldRevision >= 0;
            assert paramNewRevision > paramOldRevision;
            assert paramSession != null;
            assert paramIndex > -1;
            try {
                mNewRtx = paramSession.beginReadTransaction(paramNewRevision);
                mOldRtx = paramSession.beginReadTransaction(paramOldRevision);
            } catch (final TTIOException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
            mNewRtx.moveTo(paramKey);
            mIndex = paramIndex;
        }

        @Override
        public Integer call() throws AbsTTException {
            int retVal = 0;

            final Diff diff = mDiffs.get(mIndex);
            if (diff.getDiff() == EDiff.DELETED) {
                mOldRtx.moveTo(diff.getOldNode().getNodeKey());

                for (final AbsAxis axis = new DescendantAxis(mOldRtx, true); axis.hasNext(); axis.next()) {
                    retVal++;
                }
            } else {
                final int depth = diff.getDepth().getNewDepth();
                final long nodeKey = mNewRtx.getNode().getNodeKey();

                // Be sure we are on the root node.
                if (mNewRtx.getNode().getKind() == ENodes.ROOT_KIND) {
                    mNewRtx.moveToFirstChild();
                }

                // // Root node has been deleted.
                // if (retVal < mDiffs.size() && mDiffs.get(mIndex + retVal).getDiff() == EDiff.DELETED) {
                // retVal++;
                // }

                // Root node.
                retVal++;

                if (mNewRtx.getStructuralNode().getChildCount() > 0) {
                    // Current "root"-node of the subtree has children.
                    do {
                        if (mNewRtx.getStructuralNode().hasFirstChild()) {
                            retVal = countDeletes(retVal, depth);
                            mNewRtx.moveToFirstChild();
                            retVal++;
                        } else {
                            while (!mNewRtx.getStructuralNode().hasRightSibling()
                                && mNewRtx.getNode().hasParent() && mNewRtx.getNode().getNodeKey() != nodeKey) {
                                retVal = countDeletes(retVal, depth);
                                mNewRtx.moveToParent();
                            }
                            if (mNewRtx.getNode().getNodeKey() != nodeKey) {
                                retVal = countDeletes(retVal, depth);
                                mNewRtx.moveToRightSibling();
                                retVal++;
                            }
                        }
                    } while (mNewRtx.getNode().getNodeKey() != nodeKey);
                    mNewRtx.moveTo(nodeKey);
                } else
                // Remember deleted subtrees of the current "root" node of the subtree.
                if (mIndex + 1 < mDiffs.size()
                    && retVal == 1
                    && mDiffs.get(mIndex + 1).getDiff() == EDiff.DELETED
                    && mDiffs.get(mIndex + 1).getDepth().getOldDepth() > mDiffs.get(mIndex).getDepth()
                        .getNewDepth()) {
                    mOldRtx.moveTo(mDiffs.get(mIndex + 1).getOldNode().getNodeKey());

                    int diffIndex = mIndex;
                    do {
                        final long key = mOldRtx.getStructuralNode().getNodeKey();

                        if (diffIndex + 1 < mDiffs.size()
                            && mDiffs.get(mIndex).getDepth().getNewDepth() == (mDiffs.get(diffIndex + 1)
                                .getDepth().getOldDepth() - 1)) {
                            // For each child of the current "root" node of the subtree.
                            for (final AbsAxis axis = new DescendantAxis(mOldRtx, true); axis.hasNext(); axis
                                .next()) {
                                diffIndex++;
                                retVal++;
                            }
                        } else {
                            break;
                        }

                        mOldRtx.moveTo(key);
                    } while (mOldRtx.getStructuralNode().hasRightSibling() && mOldRtx.moveToRightSibling());

                    mIndex++;
                }
            }

            mNewRtx.close();
            mOldRtx.close();
            return retVal;
        }

        /**
         * Count subsequent deletes.
         * 
         * @param paramCount
         *            current count of descendants
         * @param paramDepth
         *            depth of node in new revision
         * @return counter
         */
        private int countDeletes(final int paramCount, final int paramDepth) {
            int retVal = paramCount;
            while (mIndex + retVal < mDiffs.size() && mDiffs.get(mIndex + retVal).getDiff() == EDiff.DELETED
                && paramDepth < mDiffs.get(mIndex + retVal).getDepth().getOldDepth()) {
                retVal++;
            }
            return retVal;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxDescendantCount() {
        return mMaxDescendantCount;
    }
}
