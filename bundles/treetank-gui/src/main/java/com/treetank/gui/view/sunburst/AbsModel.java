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
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TTException;
import com.treetank.exception.TTXPathException;
import com.treetank.gui.ReadDB;
import com.treetank.node.ENodes;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

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
    transient Semaphore mLock = new Semaphore(1);

    /** {@link List} of {@link SunburstItem}s. */
    transient List<SunburstItem> mItems;

    /**
     * Temporary {@link List} of {@link List}s of {@link SunburstItem}s.
     */
    final List<List<SunburstItem>> mLastItems;

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
    final ReadDB mDb;

    /** Get maximum depth in the (sub)tree. */
    transient int mDepthMax;
    
    /** Minimum text length. */
    transient int mMinTextLength = Integer.MAX_VALUE;

    /** Maximum text length. */
    transient int mMaxTextLength = Integer.MIN_VALUE;
    
    /** Index of the current {@link SunburstItem} for the iterator. */
    private transient int mIndex;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    AbsModel(final PApplet paramApplet, final ReadDB paramDb) {
        assert paramApplet != null && paramDb != null;
        mParent = paramApplet;
        try {
            mSession = paramDb.getSession();
            mRtx = mSession.beginReadTransaction(paramDb.getRevisionNumber());
            mRtx.moveTo(paramDb.getNodeKey());
        } catch (final TTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mItems = new ArrayList<SunburstItem>();
        mLastItems = new ArrayList<List<SunburstItem>>();
        mGUI = SunburstGUI.getInstance(mParent, this, paramDb);
        mDb = paramDb;
        addPropertyChangeListener(mGUI);
    }

    /** {@inheritDoc} */
    @Override
    public void evaluateXPath(final String paramXPathExpression) {
        assert paramXPathExpression != null;
        assert mDepthMax != 0;

        // Initialize all items to ISNOTFOUND.
        for (final SunburstItem item : mItems) {
            item.setXPathState(EXPathState.ISNOTFOUND);
        }

        if (!paramXPathExpression.isEmpty()) {
            try {
                mLock.acquire();
                new Thread(new XPathEvaluation(paramXPathExpression)).start();
            } catch (final InterruptedException e) {
                LOGWRAPPER.warn(e.getMessage(), e);
            } finally {
                mLock.release();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void undo() {
        if (!mLastItems.isEmpty()) {
            // Go back one index in history list.
            final int lastItemIndex = mLastItems.size() - 1;
            try {
                mLock.acquire();
                mItems = mLastItems.get(lastItemIndex);
                mLastItems.remove(lastItemIndex);
            } catch (final InterruptedException e) {
                LOGWRAPPER.warn(e.getMessage(), e);
            } finally {
                mLock.release();
            }
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
        try {
            mLock.acquire();
            if (mIndex < mItems.size() - 1) {
                retVal = true;
            }
        } catch (final InterruptedException e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
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
        try {
            mLock.acquire();
            item = mItems.get(mIndex);
            mIndex++;
        } catch (final InterruptedException e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
        }
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
    
    /**
     * Get minimum and maximum global text length.
     * 
     * @param paramRtx
     *            Treetank {@link IReadTransaction}
     */
    void getMinMaxTextLength(final IReadTransaction paramRtx) {
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
    
    /** Counts descendants. */
    final class Descendants implements Callable<Long> {
        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRTX;

        /**
         * Constructor.
         * 
         * @param paramNodeKey
         *            Node key to which the current transaction should move.
         */
        Descendants(final long paramNodeKey) {
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
