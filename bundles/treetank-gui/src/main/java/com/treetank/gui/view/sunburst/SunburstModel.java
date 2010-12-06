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
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.SunburstItem.StructType;
import com.treetank.gui.view.sunburst.SunburstView.Embedded;
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
final class SunburstModel extends AbsModel {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SunburstModel.class));

    /** {@link List} of {@link SunburstItem}s. */
    private transient List<SunburstItem> mItems;

    /** Treetank {@link IReadTransaction}. */
    private transient IReadTransaction mRtx;

    /** Treetank {@link ISession}. */
    private transient ISession mSession;

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

    /** Tracks modification of the current node. */
    private transient Modification mod = Modification.NONE;

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
     * Constructor.
     * 
     * @param paramApplet
     *            The processing {@link PApplet} core library.
     * @param paramDb
     *            {@link ReadDB} reference.
     */
    SunburstModel(final PApplet paramApplet, final ReadDB paramDb) {
        assert paramApplet != null && paramDb != null;
        mParent = paramApplet;
        try {
            final IReadTransaction rtx = paramDb.getRtx();
            mSession = paramDb.getSession();
            mRtx = mSession.beginReadTransaction(rtx.getRevisionNumber());
            mRtx.moveTo(rtx.getNode().getNodeKey());
        } catch (final TreetankIOException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final TreetankException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mItems = new ArrayList<SunburstItem>();
        mGUI = SunburstGUI.createGUI(mParent, this);
        mModel = this;
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
            new Thread(new XPathEvaluation(paramXPathExpression)).start();
        }
    }

    /**
     * Traverse and compare a tree.
     * 
     * @param paramRevision
     *            Revision to compare.
     * @param paramKey
     *            Node key to start from.
     * @param paramDepth
     *            Depth in the tree.
     * @param paramModificationWeight
     *            Weighting of modifications.
     * @param paramTextWeight
     *            Weighting of text length.
     */
    void traverseCompareTree(final long paramRevision, final long paramKey, final int paramDepth,
        final float paramModificationWeight, final float paramTextWeight) {
        final long nodeKey = initialize(paramKey);

        try {
            if (paramRevision < mRtx.getRevisionNumber()) {
                throw new IllegalArgumentException(
                    "paramRevision must be greater than the currently opened revision!");
            }
            final IReadTransaction rtx = mSession.beginReadTransaction(paramRevision);

            // Temporary list.
            final List<SunburstItem> itemList = new LinkedList<SunburstItem>(mItems);

            // Temporary max depth.
            // final int maxDepth = getDepthMax();

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

            final List<SunburstItem> items = new LinkedList<SunburstItem>();
            final List<Integer> modifications = new LinkedList<Integer>();
            final Stack<Integer> modCountPerSubtree = new Stack<Integer>();

            if (rtx.moveTo(nodeKey)) {
                final SunburstPostOrderAxis revision = new SunburstPostOrderAxis(mRtx);
                final SunburstPostOrderAxis secRevision = new SunburstPostOrderAxis(rtx);
                int modificationsPerNode = 0;
                final Future<Long> descCount =
                    getDescendantsOfNode(secRevision.getTransaction().getNode().getNodeKey());

                for (int index = 0; secRevision.hasNext(); index++) {
                    final Future<Long> childDescCount =
                        getDescendantsOfNode(secRevision.getTransaction().getNode().getNodeKey());

                    // Move cursors.
                    if (mod == Modification.DELETED) {
                        mod = Modification.NONE;
                    } else {
                        secRevision.next();
                    }
                    if (revision.hasNext()) {
                        revision.next();
                    }

                    modificationsPerNode = checkModification(rtx);

                    switch (secRevision.getMoved()) {
                    case CHILD:
                        if (modCountPerSubtree.empty()) {
                            modCountPerSubtree.push(modificationsPerNode);
                        } else {
                            throw new IllegalStateException("Stack should be empty!");
                        }

                        break;
                    case SIBL:
                        modCountPerSubtree.push(modCountPerSubtree.pop() + modificationsPerNode);
                        break;
                    case PARENT:
                        assert !modCountPerSubtree.empty();
                        modificationsPerNode += modCountPerSubtree.pop();
                        assert modCountPerSubtree.empty();
                        break;
                    default:
                        break;
                    }

                    childExtension = extension * descCount.get() / childDescCount.get();
                }
            } else {
                throw new IllegalArgumentException("Parameter nodeKey must exist in both revisions!");
            }

            firePropertyChange("list", itemList, Collections.unmodifiableList(mItems));
            // firePropertyChange("maxDepth", maxDepth, getDepthMax());
        } catch (final TreetankException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final InterruptedException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final ExecutionException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        mRtx.moveTo(nodeKey);
    }

    /**
     * Check for modification of the current node.
     * 
     * @param paramRtx
     *            The {@link IReadTransaction} to compare against.
     * @return modification count
     */
    private int checkModification(final IReadTransaction paramRtx) {
        assert paramRtx != null;
        // Check for modifications.
        int modificationsPerNode = 0;
        switch (paramRtx.getNode().getKind()) {
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (!paramRtx.getNode().equals(mRtx.getNode())) {
                modificationsPerNode++;
                boolean found = false;
                boolean isRightSibling = false;

                // See if one of the right sibling matches.
                final long key = mRtx.getNode().getNodeKey();
                do {
                    if (mRtx.getNode().equals(paramRtx.getNode())) {
                        found = true;
                    }

                    if (mRtx.getNode().getNodeKey() != key) {
                        isRightSibling = true;
                    }
                } while (((AbsStructNode)mRtx.getNode()).hasRightSibling() && mRtx.moveToRightSibling()
                    && !found);
                mRtx.moveTo(key);

                if (isRightSibling) {
                    // It has been deleted.
                    mod = Modification.DELETED;
                } else if (found) {
                    // Same.
                    mod = Modification.NONE;
                } else {
                    // It has been inserted.
                    mod = Modification.INSERTED;
                }
            }
            break;
        default:
            // Do nothing.
        }

        return modificationsPerNode;
    }

    /**
     * Traverse the tree and create sunburst items.
     * 
     * @param paramKey
     *            Node key to start from.
     * @param paramTextWeight
     *            Weighting of text length.
     */
    void traverseTree(final long paramKey, final float paramTextWeight) {
        // final ExecutorService executor = Executors.newSingleThreadExecutor();
        // executor.submit(new TraverseTree(paramKey, this));
        // executor.shutdown();
        new Thread(new TraverseTree(paramKey, this)).start();
    }

    /**
     * Initialize traversal.
     * 
     * @param paramKey
     *            Node key to start from.
     * @return node key
     */
    private long initialize(final long paramKey) {
        LOGWRAPPER.debug("Build sunburst items.");
        assert paramKey > -1 && mRtx != null && !mRtx.isClosed();
        final long nodeKey = mRtx.getNode().getNodeKey();
        mRtx.moveTo(paramKey);
        if (mRtx.getNode().getKind() == ENodes.ROOT_KIND) {
            mRtx.moveToFirstChild();
        }
        return nodeKey;
    }

    /**
     * Get minimum and maximum global text length.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction}.
     */
    private void getMinMaxTextLength(final IReadTransaction paramRtx) {
        assert paramRtx != null && !paramRtx.isClosed();
        for (final IAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
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
     * Get descendants of node.
     * 
     * @param paramNodeKey
     *            NodeKey of current node.
     * @return {@link Future} which has the descendant count
     * @throws ExecutionException
     *             if execution fails
     * @throws InterruptedException
     *             if task gets interrupted
     */
    private Future<Long> getDescendantsOfNode(final long paramNodeKey) throws InterruptedException,
        ExecutionException {
        final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final Future<Long> submit = executor.submit(new Descendants(paramNodeKey));
        executor.shutdown();
        return submit;
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
        // Get descendants for every node and save it to a list.
        final List<Future<Long>> descendants = new LinkedList<Future<Long>>();
        final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        boolean firstNode = true;
        for (final IAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
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
            assert paramKey > -1;
            mKey = paramKey;
            mQuery = paramQuery;
            try {
                mRTX = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                mRTX.moveTo(mKey);
                if (mRTX.getNode().getKind() == ENodes.ROOT_KIND) {
                    mRTX.moveToFirstChild();
                }
            } catch (final TreetankException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            final List<Long> nodeKeys = new LinkedList<Long>();
            final IAxis axis = new XPathAxis(mRTX, mQuery);

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
            for (int fromIndex = 0; fromIndex < mItems.size(); fromIndex += (int)(mItems.size() / processors)) {
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
            } catch (final TreetankIOException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            } catch (final TreetankException e) {
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
//            mModel.addPropertyChangeListener(mGUI);

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

                final Item item = new Item();

                for (final IAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
                    item.setAll(angle, extension, childCountPerDepth, indexToParent);
                    moved.processMove(mRtx, item, angleStack, extensionStack, childrenPerDepth, parentStack);
                    angle = item.mAngle;
                    extension = item.mExtension;
                    childCountPerDepth = item.mChildCountPerDepth;
                    indexToParent = item.mIndexToParent;
                    // switch (moved) {
                    // case START:
                    // break;
                    // case CHILD:
                    // assert !angleStack.empty();
                    // angle = angleStack.peek();
                    // assert !extensionStack.empty();
                    // extension = extensionStack.peek();
                    // assert !childrenPerDepth.empty();
                    // childCountPerDepth = childCountPerDepth();
                    // assert !parentStack.empty();
                    // indexToParent = parentStack.peek();
                    // break;
                    // case ANCHESTSIBL:
                    // assert !angleStack.empty();
                    // angle = angleStack.pop();
                    // assert !extensionStack.empty();
                    // angle += extensionStack.pop();
                    // assert !extensionStack.empty();
                    // extension = extensionStack.peek();
                    // assert !parentStack.empty();
                    // parentStack.pop();
                    // assert !parentStack.empty();
                    // indexToParent = parentStack.peek();
                    // assert !childrenPerDepth.empty();
                    // childCountPerDepth = childrenPerDepth.pop();
                    // break;
                    // default:
                    // // Do nothing.
                    // }

                    // Add a sunburst item.
                    final AbsStructNode node = (AbsStructNode)mRtx.getNode();
                    final StructType structKind =
                        node.hasFirstChild() ? StructType.ISINNERNODE : StructType.ISLEAF;
                    final long childCount = ((AbsStructNode)mRtx.getNode()).getChildCount();

                    // Calculate extension.
                    if (childCountPerDepth == 0) {
                        final long key = mRtx.getNode().getNodeKey();
                        mRtx.moveToParent();
                        childExtension = extension / (float)((AbsStructNode)mRtx.getNode()).getChildCount();
                        System.out.println("extension: " + childExtension);
                        System.out.println(((AbsStructNode)mRtx.getNode()).getChildCount());
                        mRtx.moveTo(key);
                        System.out.println(mRtx.getValueOfCurrentNode());
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
                    mItems.add(new SunburstItem.Builder(mParent, mModel, node, mRtx.getQNameOfCurrentNode(),
                        text, angle, childExtension, relations).build());
//                    mGUI.addPropertyChangeListener(mItems.get(index + 1));

                    // Set depth max.
                    mDepthMax = PApplet.max(depth, mDepthMax);

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
            } catch (final TreetankException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            // Copy list and fire changes with unmodifiable lists.
            mModel.firePropertyChange("items", Collections.unmodifiableList(itemList), Collections
                .unmodifiableList(new ArrayList<SunburstItem>(mItems)));
            mModel.firePropertyChange("maxDepth", maxDepth, mDepthMax);

            mRtx.moveTo(mKey);

            LOGWRAPPER.info(mItems.size() + " SunburstItems created!");
        }

//        /**
//         * Traverses all right siblings and sums up child count. Thus a precondition to invoke the method is
//         * that it must be called on the first child node.
//         * 
//         * @return child count per depth
//         */
//        private long childCountPerDepth() {
//            long retVal = 0;
//            final long key = mRtx.getNode().getNodeKey();
//            do {
//                retVal += ((AbsStructNode)mRtx.getNode()).getChildCount();
//            } while (((AbsStructNode)mRtx.getNode()).hasRightSibling() && mRtx.moveToRightSibling());
//            mRtx.moveTo(key);
//            return retVal;
//        }

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
            try {
                mRTX = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                mRTX.moveTo(paramNodeKey);
            } catch (final TreetankException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }

        @Override
        public Long call() throws Exception {
            long retVal = 0;

            for (final IAxis axis = new DescendantAxis(mRTX, false); axis.hasNext(); axis.next()) {
                retVal++;
            }

            return retVal;
        }

    }
}
