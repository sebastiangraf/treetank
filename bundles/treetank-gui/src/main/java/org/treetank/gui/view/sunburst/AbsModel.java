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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTXPathException;
import org.treetank.gui.ReadDB;
import org.treetank.node.ENodes;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.utils.LogWrapper;

import processing.core.PApplet;

/**
 * Abstract model, to simplify implementation of the {@link IModel} interface and share common methods among
 * implementations.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsModel extends AbsComponent implements IModel, Iterator<SunburstItem> {
    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(AbsModel.class));

    /** Semaphore to guarantee mutual exclusion for all methods. */
    // transient Semaphore mLock = new Semaphore(1);

    /** {@link List} of {@link SunburstItem}s. */
    transient List<SunburstItem> mItems;

    /**
     * Temporary {@link List} of {@link List}s of {@link SunburstItem}s.
     */
//    final List<List<SunburstItem>> mLastItems;

    /** {@link SunburstGUI} interface. */
    final SunburstGUI mGUI;

    /** The processing {@link PApplet} core library. */
    final PApplet mParent;

    /** Treetank {@link IReadTransaction}. */
    transient IReadTransaction mRtx;

    /** Treetank {@link ISession}. */
    transient ISession mSession;

    /**
     * Read database.
     * 
     * @see ReadDB
     */
    transient ReadDB mDb;

    /** Index of the current {@link SunburstItem} for the iterator. */
    private transient int mIndex;
    
    /** {@link Stack} with {@link List}s of {@link SunburstItem}s for undo operation. */
    transient Stack<List<SunburstItem>> mLastItems;
    
    /** {@link Stack} with depths for undo operation. */
    transient Stack<Integer> mLastDepths;
    
    /** Last maximum depth in the tree. */
    transient int mLastMaxDepth;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    AbsModel(final PApplet paramApplet, final ReadDB paramDb) {
        assert paramApplet != null;
        assert paramDb != null;
        mParent = paramApplet;
        try {
            mSession = paramDb.getSession();
            mRtx = mSession.beginReadTransaction(paramDb.getRevisionNumber());
            mRtx.moveTo(paramDb.getNodeKey());
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mItems = new ArrayList<SunburstItem>();
        mLastItems = new Stack<List<SunburstItem>>();
        mLastDepths = new Stack<Integer>();
        mDb = paramDb;
        mGUI = SunburstGUI.getInstance(mParent, this, mDb);
        addPropertyChangeListener(mGUI);
    }

    /**
     * Update {@link IDatabase} instance.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     */
    void updateDb(final ReadDB paramDb) {
        mDb = paramDb;
        try {
            mSession = paramDb.getSession();
            mRtx = mSession.beginReadTransaction(paramDb.getRevisionNumber());
            mRtx.moveTo(paramDb.getNodeKey());
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mLastItems = new Stack<List<SunburstItem>>();
        mLastDepths = new Stack<Integer>();
        traverseTree(new SunburstContainer().setKey(mDb.getNodeKey()));
    }

    /** {@inheritDoc} */
    @Override
    public void evaluateXPath(final String paramXPathExpression) {
        assert paramXPathExpression != null;

        // Initialize all items to ISNOTFOUND.
        for (final SunburstItem item : mItems) {
            item.setXPathState(EXPathState.ISNOTFOUND);
        }

        if (!paramXPathExpression.isEmpty()) {
            new Thread(new XPathEvaluation(paramXPathExpression)).start();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void undo() {
        if (!mLastItems.empty()) {
            // Go back one index in history list.
            mItems = mLastItems.pop();
            mLastMaxDepth = mLastDepths.peek();
            firePropertyChange("maxDepth", null, mLastDepths.pop());
            firePropertyChange("done", null, true);
        }
    }

    /**
     * Get {@link SunburstItem} at the specified index.
     * 
     * @param paramIndex
     *            index of the item
     * @return the {@link SunburstItem} found at the index
     * @throws IndexOutOfBoundsException
     *             if index > mItems.size() - 1 or < 0
     */
    @Override
    public SunburstItem getItem(final int paramIndex) throws IndexOutOfBoundsException {
        return mItems.get(paramIndex);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        boolean retVal = false;
        if (mIndex < mItems.size() - 1) {
            retVal = true;
        }
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public SunburstItem next() {
        SunburstItem item = null;
        if (mIndex > mItems.size()) {
            throw new NoSuchElementException();
        }
        item = mItems.get(mIndex);
        mIndex++;
        assert item != null;
        return item;
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove operation not supported!");

    }

    /** {@inheritDoc} */
    @Override
    public Iterator<SunburstItem> iterator() {
        return mItems.iterator();
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
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            try {
                final Set<Long> nodeKeys = new HashSet<Long>();
                final XPathAxis axis = new XPathAxis(mRTX, mQuery);

                // Save found node keys with descendants.
                while (axis.hasNext()) {
                    axis.next();
                    final long key = axis.getTransaction().getNode().getNodeKey();
                    nodeKeys.add(key);
                    for (final AbsAxis desc = new DescendantAxis(axis.getTransaction()); desc.hasNext(); desc
                        .next()) {
                        nodeKeys.add(desc.getTransaction().getNode().getNodeKey());
                    }
                    axis.getTransaction().moveTo(key);
                }

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

                firePropertyChange("done", null, true);
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
        private final Set<Long> mKeys;

        /**
         * Constructor.
         * 
         * @param paramNodeKeys
         *            Keys of nodes, which are in the result of an XPath query
         * @param paramSublist
         *            Sublist which has to be searched for matches
         */
        private XPathSublistEvaluation(final Set<Long> paramNodeKeys, final List<SunburstItem> paramSublist) {
            assert paramNodeKeys != null && paramSublist != null;
            mKeys = paramNodeKeys;
            mSunburstItems = paramSublist;
            try {
                mRTX = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                mRTX.moveTo(mRtx.getNode().getNodeKey());
            } catch (final AbsTTException e) {
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
}
