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
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TTException;
import com.treetank.exception.TTIOException;
import com.treetank.exception.TTXPathException;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.Item.Builder;
import com.treetank.gui.view.sunburst.SunburstItem.StructType;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.service.xml.xpath.XPathAxis;
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
final class SunburstModel extends AbsComponent {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SunburstModel.class));

    /** {@link List} of {@link SunburstItem}s. */
    private transient List<SunburstItem> mItems;

    /** Treetank {@link IReadTransaction}. */
    private transient IReadTransaction mRtx;

    /** Treetank {@link ISession}. */
    private transient ISession mSession;

    /** Semaphore to guarantee mutual exclusion for all methods. */
    private transient Semaphore mLock = new Semaphore(1);

    /** The processing {@link PApplet} core library. */
    private final PApplet mParent;

    /** Determines the modification. */
    enum Modification {
        /** Node has been deleted. */
        DELETED,

        /** Node has been inserted. */
        INSERTED,

        /** Node hasn't been modified. */
        NONE
    };

    /** Modification of current node. */
    private transient Modification mMod = Modification.NONE;

    /** Maximum descendant count in tree. */
    private transient long mMaxDescendantCount;

    /** Minimum text length. */
    private transient int mMinTextLength = Integer.MAX_VALUE;

    /** Maximum text length. */
    private transient int mMaxTextLength = Integer.MIN_VALUE;

    /** {@link SunburstGUI} interface. */
    private final SunburstGUI mGUI;

    /** This {@link SunburstMode}. */
    private final SunburstModel mModel;

    /** Get maximum depth in the (sub)tree. */
    private transient int mDepthMax;

    /**
     * Read database.
     * 
     * @see ReadDB
     */
    private final ReadDB mDb;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    SunburstModel(final PApplet paramApplet, final ReadDB paramDb) {
        assert paramApplet != null && paramDb != null;
        mParent = paramApplet;
        try {
            final IReadTransaction rtx = paramDb.getRtx();
            mSession = paramDb.getSession();
            mRtx = mSession.beginReadTransaction(rtx.getRevisionNumber());
            mRtx.moveTo(rtx.getNode().getNodeKey());
        } catch (final TTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mItems = new ArrayList<SunburstItem>();
        mGUI = SunburstGUI.getInstance(mParent, this, paramDb);
        mModel = this;
        mDb = paramDb;
        addPropertyChangeListener(mGUI);
    }

    /**
     * XPath evaluation.
     * 
     * @param paramXPathExpression
     *            XPath expression to evaluate.
     */
    void evaluateXPath(final String paramXPathExpression) {
        // Initialize all items to ISNOTFOUND.
        for (final SunburstItem item : mItems) {
            item.setXPathState(EXPathState.ISNOTFOUND);
        }

        if (!paramXPathExpression.isEmpty()) {
            try {
                mLock.acquire();
                new Thread(new XPathEvaluation(paramXPathExpression)).start();
            } catch (final Exception e) {
                LOGWRAPPER.warn(e.getMessage(), e);
            } finally {
                mLock.release();
            }
        }
    }

    /**
     * Traverse and compare a tree.
     * 
     * @param paramRevision
     *            revision to compare
     * @param paramKey
     *            node key to start from
     * @param paramDepth
     *            Depth in the tree
     * @param paramModificationWeight
     *            weighting of modifications
     * @param paramTextWeight
     *            weighting of text length
     */
    void traverseCompareTree(final long paramRevision, final long paramKey, final int paramDepth,
        final float paramModificationWeight, final float paramTextWeight) {
        assert paramRevision >= 0;
        assert paramKey >= 0;
        assert paramDepth >= 0;
        assert paramModificationWeight >= 0;
        assert paramTextWeight >= 0;

        try {
            mLock.acquire();
            new Thread(new TraverseCompareTree(paramRevision, paramKey, paramDepth, paramModificationWeight,
                paramTextWeight, this)).start();
        } catch (final Exception e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
        }
    }

    // final long nodeKey = initialize(paramKey);
    //
    // try {
    // if (paramRevision < mRtx.getRevisionNumber()) {
    // throw new IllegalArgumentException(
    // "paramRevision must be greater than the currently opened revision!");
    // }
    // final IReadTransaction rtx = mSession.beginReadTransaction(paramRevision);
    //
    // // Temporary list.
    // final List<SunburstItem> itemList = new LinkedList<SunburstItem>(mItems);
    //
    // // Temporary max depth.
    // // final int maxDepth = getDepthMax();
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
    // final List<SunburstItem> items = new LinkedList<SunburstItem>();
    // final List<Integer> modifications = new LinkedList<Integer>();
    // final Stack<Integer> modCountPerSubtree = new Stack<Integer>();
    //
    // if (rtx.moveTo(nodeKey)) {
    // final SunburstPostOrderAxis revision = new SunburstPostOrderAxis(mRtx);
    // final SunburstPostOrderAxis secRevision = new SunburstPostOrderAxis(rtx);
    // int modificationsPerNode = 0;
    // final Future<Long> descCount =
    // getDescendantsOfNode(secRevision.getTransaction().getNode().getNodeKey());
    //
    // for (int index = 0; secRevision.hasNext(); index++) {
    // final Future<Long> childDescCount =
    // getDescendantsOfNode(secRevision.getTransaction().getNode().getNodeKey());
    //
    // // Move cursors.
    // if (mod == Modification.DELETED) {
    // mod = Modification.NONE;
    // } else {
    // secRevision.next();
    // }
    // if (revision.hasNext()) {
    // revision.next();
    // }
    //
    // modificationsPerNode = checkModification(rtx);
    //
    // switch (secRevision.getMoved()) {
    // case CHILD:
    // if (modCountPerSubtree.empty()) {
    // modCountPerSubtree.push(modificationsPerNode);
    // } else {
    // throw new IllegalStateException("Stack should be empty!");
    // }
    //
    // break;
    // case SIBL:
    // modCountPerSubtree.push(modCountPerSubtree.pop() + modificationsPerNode);
    // break;
    // case PARENT:
    // assert !modCountPerSubtree.empty();
    // modificationsPerNode += modCountPerSubtree.pop();
    // assert modCountPerSubtree.empty();
    // break;
    // default:
    // break;
    // }
    //
    // childExtension = extension * descCount.get() / childDescCount.get();
    // }
    // } else {
    // throw new IllegalArgumentException("Parameter nodeKey must exist in both revisions!");
    // }
    //
    // firePropertyChange("list", itemList, Collections.unmodifiableList(mItems));
    // // firePropertyChange("maxDepth", maxDepth, getDepthMax());
    // } catch (final TreetankException e) {
    // LOGWRAPPER.error(e.getMessage(), e);
    // } catch (final InterruptedException e) {
    // LOGWRAPPER.error(e.getMessage(), e);
    // } catch (final ExecutionException e) {
    // LOGWRAPPER.error(e.getMessage(), e);
    // }
    //
    // mRtx.moveTo(nodeKey);
    // }

    /**
     * Check for modification of the current node.
     * 
     * @param paramNewRtx
     *            the {@link IReadTransaction} from the new revision
     * @param paramOldRtx
     *            the {@link IReadTransaction} from the old revision
     */
    private void checkModification(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;

        // Check for modifications.
        switch (paramNewRtx.getNode().getKind()) {
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (!paramNewRtx.getNode().equals(paramOldRtx.getNode())) {
                boolean found = false;
                boolean isRightSibling = false;

                // See if one of the right sibling matches.
                final long key = paramNewRtx.getNode().getNodeKey();
                do {
                    if (paramNewRtx.getNode().equals(paramOldRtx.getNode())) {
                        found = true;
                    }

                    if (paramNewRtx.getNode().getNodeKey() != key) {
                        isRightSibling = true;
                    }
                } while (((AbsStructNode)paramNewRtx.getNode()).hasRightSibling()
                    && paramNewRtx.moveToRightSibling() && !found);
                paramNewRtx.moveTo(key);

                if (isRightSibling) {
                    // It has been deleted.
                    mMod = Modification.DELETED;
                } else if (found) {
                    // Same.
                    mMod = Modification.NONE;
                } else {
                    // It has been inserted.
                    mMod = Modification.INSERTED;
                }
            }
            break;
        default:
            // Do nothing.
        }
    }

    /**
     * Traverse the tree and create sunburst items.
     * 
     * @param paramKey
     *            node key to start from
     * @param paramTextWeight
     *            weighting of text length
     */
    void traverseTree(final long paramKey, final float paramTextWeight) {
        // final ExecutorService executor = Executors.newSingleThreadExecutor();
        // executor.submit(new TraverseTree(paramKey, this));
        // executor.shutdown();
        assert paramKey >= 0;
        assert paramTextWeight >= 0;
        try {
            mLock.acquire();
            new Thread(new TraverseTree(paramKey, this)).start();
        } catch (final Exception e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
        }
    }

    /**
     * Get the maximum depth in the tree.
     * 
     * @param paramRtx
     *            {@link IReadTransaction} on the tree
     * @return maximum depth
     */
    private int getDepthMax(final IReadTransaction paramRtx) {
        assert paramRtx != null && !paramRtx.isClosed();
        int depthMax = 0;
        int depth = 0;
        final long nodeKey = paramRtx.getNode().getNodeKey();
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            final AbsStructNode node = (AbsStructNode)paramRtx.getNode();
            if (node.hasFirstChild()) {
                depth++;
                // Set depth max.
                depthMax = Math.max(depth, depthMax);
            } else if (!node.hasRightSibling()) {
                // Next node will be a right sibling of an anchestor node or the traversal ends.
                final long currNodeKey = mRtx.getNode().getNodeKey();
                do {
                    if (((AbsStructNode)mRtx.getNode()).hasParent()) {
                        mRtx.moveToParent();
                        depth--;
                    } else {
                        break;
                    }
                } while (!((AbsStructNode)mRtx.getNode()).hasRightSibling());
                mRtx.moveTo(currNodeKey);
            }
        }
        paramRtx.moveTo(nodeKey);
        return depthMax;
    }

    /**
     * Get minimum and maximum global text length.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction}
     */
    private void getMinMaxTextLength(final IReadTransaction paramRtx) {
        assert paramRtx != null && !paramRtx.isClosed();
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
        LOGWRAPPER.debug("MINIMUM text length: " + mMinTextLength);
        LOGWRAPPER.debug("MAXIMUM text length: " + mMaxTextLength);
    }

    /**
     * Get a list of descendants and modifications per node.
     * 
     * @param paramRevision
     *            revision to compare
     * @param paramRtx
     *            Treetank {@link IReadTransaction} over which to iterate.
     * @return List of {@link Future}s.
     * @throws ExecutionException
     *             if execution fails
     * @throws InterruptedException
     *             if task gets interrupted
     */
    private List<Future<List<?>>> get(final long paramRevision, final IReadTransaction paramRtx)
        throws InterruptedException, ExecutionException {
        assert paramRtx != null;

        // Get descendants for every node and save it to a list.
        final List<Future<List<? extends Object>>> list = new LinkedList<Future<List<?>>>();
        final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        boolean firstNode = true;
        for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            final Future<List<?>> submit =
                executor.submit(new TreeWalk(paramRevision, paramRtx.getNode().getNodeKey()));

            if (firstNode) {
                firstNode = false;
                mMaxDescendantCount = (Long)submit.get().get(0);
            }
            list.add(submit);
        }
        executor.shutdown();

        return list;
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
    private final class XPathEvaluation implements Runnable {
        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRTX;

        /** Key from which to start traversal. */
        private final long mKey;

        /** XPath query. */
        private final String mQuery;

        /**
         * Constructor.
         * 
         * @param paramQuery
         *            The XPath query.
         */
        private XPathEvaluation(final String paramQuery) {
            this(mRtx.getNode().getNodeKey(), paramQuery);
        }

        /**
         * Constructor.
         * 
         * @param paramKey
         *            Key from which to start traversal.
         * @param paramQuery
         *            The XPath query.
         */
        private XPathEvaluation(final long paramKey, final String paramQuery) {
            assert paramKey >= 0;
            assert paramQuery != null;
            mKey = paramKey;
            mQuery = paramQuery;
            try {
                mRTX = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                mRTX.moveTo(mKey);
                if (mRTX.getNode().getKind() == ENodes.ROOT_KIND) {
                    mRTX.moveToFirstChild();
                }
            } catch (final TTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            try {
                final List<Long> nodeKeys = new LinkedList<Long>();
                final AbsAxis axis = new XPathAxis(mRTX, mQuery);

                while (axis.hasNext()) {
                    axis.next();
                    nodeKeys.add(mRTX.getNode().getNodeKey());
                }

                // Old values for PropertyChangeListener.
                final List<SunburstItem> itemList = mItems;
                final int maxDepth = mDepthMax;

                // Do the work.
                final int processors = Runtime.getRuntime().availableProcessors();
                final ExecutorService executor = Executors.newFixedThreadPool(processors);
                for (int fromIndex = 0; fromIndex < mItems.size(); fromIndex +=
                    (int)(mItems.size() / processors)) {
                    int toIndex = fromIndex + (int)(mItems.size() / processors);
                    if (toIndex >= mItems.size()) {
                        toIndex = mItems.size() - 1;
                    }
                    executor.submit(new XPathSublistEvaluation(nodeKeys, mItems.subList(fromIndex, toIndex)));
                }

                executor.shutdown();
                try {
                    executor.awaitTermination(5, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                    return;
                }

                // Copy list and fire changes with unmodifiable lists.
                mModel.firePropertyChange("items", Collections.unmodifiableList(itemList), Collections
                    .unmodifiableList(new ArrayList<SunburstItem>(mItems)));
                mModel.firePropertyChange("maxDepth", maxDepth, mDepthMax);
            } catch (final TTXPathException exc) {
                LOGWRAPPER.error(exc);
            }
        }
    }

    /** XPath sublist evaluation. */
    private final class XPathSublistEvaluation implements Runnable {

        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRTX;

        /** {@link List} of {@link SunburstItem}s. */
        private final List<SunburstItem> mSunburstItems;

        /** {@link List} of node keys which are in the result. */
        private final List<Long> mKeys;

        /**
         * Constructor.
         * 
         * @param paramNodeKeys
         *            Keys of nodes, which are in the result of an XPath query
         * @param paramSublist
         *            Sublist which has to be searched for matches
         */
        private XPathSublistEvaluation(final List<Long> paramNodeKeys, final List<SunburstItem> paramSublist) {
            assert paramNodeKeys != null && paramSublist != null;
            mKeys = paramNodeKeys;
            mSunburstItems = paramSublist;
            try {
                mRTX = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                mRTX.moveTo(mRtx.getNode().getNodeKey());
            } catch (final TTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            for (final SunburstItem item : mSunburstItems) {
                for (final long key : mKeys) {
                    if (item.getNode().getNodeKey() == key) {
                        item.setXPathState(EXPathState.ISFOUND);
                    }
                }
            }
        }
    }

    /** Traverse and compare trees. */
    private final class TraverseCompareTree implements Runnable {

        /** Revision to compare. */
        private final long mRevision;

        /** Key from which to start traversal. */
        private final long mKey;

        /** Weighting of modifications. */
        private final float mModWeight;

        /** Weighting of textnode length. */
        private final float mTextWeight;

        /** {@link SunburstModel}. */
        private final SunburstModel mModel;

        /**
         * Constructor.
         * 
         * @param paramRevision
         *            the revision to compare
         * @param paramKey
         *            key from which to start traversal
         * @param paramDepth
         *            depth to prune
         * @param paramModificationWeight
         *            determines how much modifications are weighted to compute the extension angle of each
         *            {@link SunburstItem}
         * @param paramTextWeight
         *            determines how much text is weighted to compute the extension angle of each
         *            {@link SunburstItem}
         * @param paramModel
         *            the {@link SunburstModel}
         */
        private TraverseCompareTree(final long paramRevision, final long paramKey, final long paramDepth,
            final float paramModificationWeight, final float paramTextWeight, final SunburstModel paramModel) {
            assert paramRevision >= 0;
            assert paramKey > -1 && mRtx != null && !mRtx.isClosed();
            assert paramDepth >= 0;
            assert paramModificationWeight >= 0;
            assert paramTextWeight >= 0;
            assert paramModel != null;

            try {
                if (paramRevision < mRtx.getRevisionNumber()) {
                    throw new IllegalArgumentException(
                        "paramRevision must be greater than the currently opened revision!");
                }
            } catch (final TTIOException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            mRevision = paramRevision;
            mKey = paramKey;
            mModWeight = paramModificationWeight;
            mTextWeight = paramTextWeight;
            mModel = paramModel;

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

            // Initialize variables. =============================
            final List<SunburstItem> itemList = new ArrayList<SunburstItem>(mItems);
            final int maxDepth = mDepthMax;

            // Remove all elements from item list.
            mItems.clear();

            // Initial extension and childExtension.
            float extension = PConstants.TWO_PI;
            float childExtension = 0f;

            // Node relations used for simplyfing the SunburstItem constructor.
            final NodeRelations relations = new NodeRelations();

            // Depth in the tree starting at 0.
            int depth = 0;

            // Start angle.
            float angle = 0f;

            try {
                // Get min and max textLength of the new revision.
                getMinMaxTextLength(mRtx);

                // Get list of descendants per node on the new revision.
                final IReadTransaction firstRtx = mSession.beginReadTransaction(mRevision);
                firstRtx.moveTo(mRtx.getNode().getNodeKey());
                final List<Future<List<?>>> descsAndMods = get(mRevision, firstRtx);

                // Get maximum depth of the old revision.
                final IReadTransaction secondRtx = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                mDepthMax = getDepthMax(secondRtx);

                // Determines movement of transaction.
                EMoved moved = EMoved.START;

                // Initialize modification.
                mMod = Modification.NONE;

                // Index to parent node.
                int indexToParent = -1;

                int index = -1;
                long descendantCount;
                int modificationCount;

                // Setting up stacks.
                final Stack<Float> extensionStack = new Stack<Float>();
                final Stack<Long> descendantsStack = new Stack<Long>();
                final Stack<Float> angleStack = new Stack<Float>();
                final Stack<Integer> parentStack = new Stack<Integer>();
                final Stack<Integer> modificationStack = new Stack<Integer>();

                final Item item = Item.ITEM;
                final Builder builder = Item.BUILDER;

                for (final AbsAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
                    builder.set(angle, extension, indexToParent).setDescendantCount(
                        (Long)descsAndMods.get(indexToParent).get().get(0));
                    moved.processCompareMove(mRtx, item, angleStack, extensionStack, descendantsStack,
                        parentStack, modificationStack);
                    angle = item.mAngle;
                    extension = item.mExtension;
                    descendantCount = item.mDescendantCount;
                    indexToParent = item.mIndexToParent;
                    modificationCount = item.mModificationCount;

                    // Add a sunburst item.
                    final AbsStructNode node = (AbsStructNode)mRtx.getNode();
                    final StructType structKind =
                        node.hasFirstChild() ? StructType.ISINNERNODE : StructType.ISLEAF;

                    // Calculate extension.
                    if (descendantCount == 0) {
                        final long key = mRtx.getNode().getNodeKey();
                        mRtx.moveToParent();
                        childExtension = extension / ((AbsStructNode)mRtx.getNode()).getChildCount();
                        mRtx.moveTo(key);
                    } else {
                        long parentDescCount = 0;
                        int parentModCount = 0;

                        try {
                            parentDescCount = descendantsStack.peek();
                            parentModCount = modificationStack.peek();
                        } catch (final EmptyStackException e) {
                            parentDescCount = descendantCount;
                            parentModCount = modificationCount;
                        }

                        childExtension =
                            mModWeight * (extension * (float)descendantCount / (float)parentDescCount)
                                + (1 - mModWeight)
                                * (extension * (float)modificationCount / (float)parentModCount);
                    }

                    LOGWRAPPER.debug("indexToParent: " + indexToParent);

                    // Set node relations.
                    int actualDepth = depth;
                    if (mMod != Modification.NONE && depth < mDepthMax) {
                        actualDepth = mDepthMax + 1;
                    }
                    String text = null;
                    if (mRtx.getNode().getKind() == ENodes.TEXT_KIND) {
                        relations.setAll(actualDepth, structKind, mRtx.getValueOfCurrentNode().length(),
                            mMinTextLength, mMaxTextLength, indexToParent);
                        text = mRtx.getValueOfCurrentNode();
                    } else {
                        relations.setAll(actualDepth, structKind, descendantCount, 0, mMaxDescendantCount,
                            indexToParent);
                    }

                    // Build item.
                    if (text != null) {
                        mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension,
                            relations, mDb).setNode(node).setText(text).build());
                    } else {
                        mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension,
                            relations, mDb).setNode(node).setQName(mRtx.getQNameOfCurrentNode()).build());
                    }

                    // Set depth max.
                    mDepthMax = Math.max(depth, mDepthMax);

                    index++;

                    if (node.hasFirstChild()) {
                        // Next node will be a child node.
                        angleStack.push(angle);
                        extensionStack.push(childExtension);
                        parentStack.push(index);
                        depth++;
                        moved = EMoved.CHILD;

                        descendantsStack.push(descendantCount);
                        modificationStack.push(modificationCount);
                    } else if (node.hasRightSibling()) {
                        // Next node will be a right sibling node.
                        angle += childExtension;
                    } else if (!node.hasRightSibling()) {
                        // Next node will be a right sibling of an anchestor node or the traversal ends.
                        moved = EMoved.ANCHESTSIBL;
                        final long currNodeKey = mRtx.getNode().getNodeKey();
                        boolean first = true;
                        do {
                            if (((AbsStructNode)mRtx.getNode()).hasParent()
                                && mRtx.getNode().getNodeKey() != mKey) {
                                if (first) {
                                    // Do not pop from stack if it's a leaf node.
                                    first = false;
                                } else {
                                    angleStack.pop();
                                    extensionStack.pop();
                                    descendantsStack.pop();
                                    parentStack.pop();
                                    modificationStack.pop();
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

                firstRtx.close();
                secondRtx.close();
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final ExecutionException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final TTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            // Copy list and fire changes with unmodifiable lists.
            mModel.firePropertyChange("items", Collections.unmodifiableList(itemList), Collections
                .unmodifiableList(new ArrayList<SunburstItem>(mItems)));
            mModel.firePropertyChange("maxDepth", maxDepth, mDepthMax);

            mRtx.moveTo(mKey);

            LOGWRAPPER.info(mItems.size() + " SunburstItems created!");
        }
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
            assert paramKey > -1 && mRtx != null && !mRtx.isClosed();
            mKey = paramKey;
            mModel = paramModel;

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

            // Initialize variables. =============================
            final List<SunburstItem> itemList = new ArrayList<SunburstItem>(mItems);
            final int maxDepth = mDepthMax;

            // Remove all elements from item list.
            mItems.clear();

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
                // Get min and max textLength.
                getMinMaxTextLength(mRtx);

                // Get list of descendants per node.
                final IReadTransaction rtx = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                rtx.moveTo(mRtx.getNode().getNodeKey());
                final List<Future<Long>> descendants = getDescendants(rtx);

                // Determines movement of transaction.
                EMoved moved = EMoved.START;

                // Index to parent node.
                int indexToParent = -1;
                int index = -1;

                // Setting up stacks.
                final Stack<Float> extensionStack = new Stack<Float>();
                final Stack<Long> childrenPerDepth = new Stack<Long>();
                final Stack<Float> angleStack = new Stack<Float>();
                final Stack<Integer> parentStack = new Stack<Integer>();

                final Item item = Item.ITEM;
                final Builder builder = Item.BUILDER;

                for (final AbsAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
                    builder.set(angle, extension, indexToParent).setChildCountPerDepth(childCountPerDepth)
                        .set();
                    moved.processMove(mRtx, item, angleStack, extensionStack, childrenPerDepth, parentStack);
                    angle = item.mAngle;
                    extension = item.mExtension;
                    childCountPerDepth = item.mChildCountPerDepth;
                    indexToParent = item.mIndexToParent;

                    // Add a sunburst item.
                    final AbsStructNode node = (AbsStructNode)mRtx.getNode();
                    final StructType structKind =
                        node.hasFirstChild() ? StructType.ISINNERNODE : StructType.ISLEAF;
                    final long childCount = ((AbsStructNode)mRtx.getNode()).getChildCount();

                    // Calculate extension.
                    if (childCountPerDepth == 0) {
                        final long key = mRtx.getNode().getNodeKey();
                        mRtx.moveToParent();
                        childExtension = extension / (float)childCount;
                        mRtx.moveTo(key);
                    } else {
                        childExtension = extension * (float)childCount / (float)childCountPerDepth;
                    }

                    LOGWRAPPER.debug("indexToParent: " + indexToParent);

                    // Set node relations.
                    String text = null;
                    if (mRtx.getNode().getKind() == ENodes.TEXT_KIND) {
                        relations.setAll(depth, structKind, mRtx.getValueOfCurrentNode().length(),
                            mMinTextLength, mMaxTextLength, indexToParent);
                        text = mRtx.getValueOfCurrentNode();
                    } else {
                        relations.setAll(depth, structKind, descendants.get(index + 1).get(), 0,
                            mMaxDescendantCount, indexToParent);
                    }

                    // Build item.
                    if (text != null) {
                        mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension,
                            relations, mDb).setNode(node).setText(text).build());
                    } else {
                        mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension,
                            relations, mDb).setNode(node).setQName(mRtx.getQNameOfCurrentNode()).build());
                    }

                    // Set depth max.
                    mDepthMax = Math.max(depth, mDepthMax);

                    index++;

                    if (node.hasFirstChild()) {
                        // Next node will be a child node.
                        angleStack.push(angle);
                        extensionStack.push(childExtension);
                        parentStack.push(index);
                        depth++;
                        moved = EMoved.CHILD;

                        // Children per depth.
                        childrenPerDepth.push(childCountPerDepth);
                    } else if (node.hasRightSibling()) {
                        // Next node will be a right sibling node.
                        angle += childExtension;
                    } else if (!node.hasRightSibling()) {
                        // Next node will be a right sibling of an anchestor node or the traversal ends.
                        moved = EMoved.ANCHESTSIBL;
                        final long currNodeKey = mRtx.getNode().getNodeKey();
                        boolean first = true;
                        do {
                            if (((AbsStructNode)mRtx.getNode()).hasParent()
                                && mRtx.getNode().getNodeKey() != mKey) {
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

                rtx.close();
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final ExecutionException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final TTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            // Copy list and fire changes with unmodifiable lists.
            mModel.firePropertyChange("items", Collections.unmodifiableList(itemList), Collections
                .unmodifiableList(new ArrayList<SunburstItem>(mItems)));
            mModel.firePropertyChange("maxDepth", maxDepth, mDepthMax);

            mRtx.moveTo(mKey);

            LOGWRAPPER.info(mItems.size() + " SunburstItems created!");
        }
    }

    /** Counts descendants and modifications. */
    private final class TreeWalk implements Callable<List<?>> {

        /** Treetank {@link IReadTransaction} (on old revision). */
        private transient IReadTransaction mOldRev;

        /** Treetank {@link IReadTransaction} (on new revision). */
        private transient IReadTransaction mNewRev;

        /**
         * Constructor.
         * 
         * @param paramRevision
         *            revision to compare
         * @param paramNodeKey
         *            node key to which the current transaction should move
         */
        private TreeWalk(final long paramRevision, final long paramNodeKey) {
            assert mRtx != null && !mRtx.isClosed();
            assert paramNodeKey >= 0;
            try {
                assert paramRevision > mRtx.getRevisionNumber();
                mNewRev = mSession.beginReadTransaction(paramRevision);
                mOldRev = mSession.beginReadTransaction(mRtx.getRevisionNumber());
            } catch (final TTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
            mOldRev.moveTo(paramNodeKey);
        }

        @Override
        public List<?> call() throws Exception {
            final List<Object> retVal = new LinkedList<Object>();

            final AbsAxis revNew = new DescendantAxis(mNewRev, true);
            final AbsAxis revOld = new DescendantAxis(mOldRev, true);
            int descendants = 0;
            int modifications = 0;
            while (revNew.hasNext()) {
                descendants++;
                checkModification(revNew.getTransaction(), revOld.getTransaction());

                if (mMod != Modification.NONE) {
                    modifications++;
                }
            }

            retVal.add(descendants, modifications);
            mNewRev.close();
            mOldRev.close();
            return retVal;
        }

    }

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
            assert mRtx != null && !mRtx.isClosed();
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
