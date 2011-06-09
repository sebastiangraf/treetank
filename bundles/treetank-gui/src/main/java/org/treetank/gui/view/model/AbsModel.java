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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTXPathException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.AbsObservableComponent;
import org.treetank.gui.view.IVisualItem;
import org.treetank.gui.view.sunburst.EXPathState;
import org.treetank.gui.view.sunburst.SunburstContainer;
import org.treetank.gui.view.sunburst.SunburstItem;
import org.treetank.node.ENodes;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.xpath.XPathAxis;

import processing.core.PApplet;

/**
 * Abstract model, to simplify implementation of the {@link IModel} interface and share common methods among
 * implementations.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * @param <T>
 * 
 */
public abstract class AbsModel<T extends IVisualItem> extends AbsObservableComponent implements IModel<T> {

    /** {@link List} of {@link SunburstItem}s. */
    protected transient List<T> mItems;

    /** The processing {@link PApplet} core library. */
    private final PApplet mParent;

    /** Treetank {@link IReadTransaction}. */
    protected transient IReadTransaction mRtx;

    /** Treetank {@link ISession}. */
    protected transient ISession mSession;

    /** {@link ReadDB} instance. */
    private transient ReadDB mDb;

    /** Index of the current {@link SunburstItem} for the iterator. */
    private transient int mIndex;

    /** {@link Stack} with {@link List}s of a {@link IVisualItem} implementation for undo operation. */
    protected transient Stack<List<T>> mLastItems;

    /** {@link Stack} with depths for undo operation. */
    protected transient Stack<Integer> mLastDepths;

    /** {@link Stack} with depths for undo operation. */
    protected transient Stack<Integer> mLastOldDepths;

    /** The last maximum depth. */
    protected transient int mLastMaxDepth;

    /** The last maximum depth in the old revision. */
    protected transient int mLastOldMaxDepth;

    /** Determines if XML fragments should be inserted as first child or as right sibling of the current node. */
    protected transient EShredderInsert mInsert;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    protected AbsModel(final PApplet paramApplet, final ReadDB paramDb) {
        assert paramApplet != null;
        assert paramDb != null;
        mParent = paramApplet;
        try {
            mSession = paramDb.getSession();
            mRtx = mSession.beginReadTransaction(paramDb.getRevisionNumber());
            mRtx.moveTo(paramDb.getNodeKey());
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
        }
        mItems = new ArrayList<T>();
        mLastItems = new Stack<List<T>>();
        mLastDepths = new Stack<Integer>();
        mLastOldDepths = new Stack<Integer>();
        mDb = paramDb;
    }

    /**
     * Shutdown {@link ExecutorService}.
     * 
     * @param paramPool
     *            thread pool; {@link ExecutorService} instance
     */
    public void shutdown(final ExecutorService paramPool) {
        paramPool.shutdown(); // Disable new tasks from being submitted.
    }

    /** {@inheritDoc} */
    @Override
    public void updateDb(final ReadDB paramDb, final IContainer paramContainer) {
        assert paramDb != null;
        assert paramContainer != null;
        setDb(paramDb);
        try {
            mSession = paramDb.getSession();
            mRtx = mSession.beginReadTransaction(paramDb.getRevisionNumber());
            mRtx.moveTo(paramDb.getNodeKey());
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
        }
        mItems = new ArrayList<T>();
        mLastItems = new Stack<List<T>>();
        mLastDepths = new Stack<Integer>();
        traverseTree(paramContainer);
    }

    /** {@inheritDoc} */
    @Override
    public void evaluateXPath(final String paramXPathExpression) {
        assert paramXPathExpression != null;

        // Initialize all items to ISNOTFOUND.
        for (final T item : mItems) {
            ((SunburstItem)item).setXPathState(EXPathState.ISNOTFOUND);
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
            mLastMaxDepth = mLastDepths.pop();
            if (!mLastOldDepths.empty()) {
                mLastOldMaxDepth = mLastOldDepths.pop();
            }
            firePropertyChange("maxDepth", null, mLastMaxDepth);
            if (mLastOldMaxDepth != 0) {
                firePropertyChange("oldMaxDepth", null, mLastOldMaxDepth);
            }
            firePropertyChange("done", null, true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public T getItem(final int paramIndex) throws IndexOutOfBoundsException {
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
    public T next() {
        T item = null;
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
    public Iterator<T> iterator() {
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
            } catch (final AbsTTException exc) {
                exc.printStackTrace();
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

                shutdown(executor);
                firePropertyChange("done", null, true);
            } catch (final TTXPathException exc) {
                exc.printStackTrace();
            }
        }
    }

    /** XPath sublist evaluation. */
    private final class XPathSublistEvaluation implements Runnable {

        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRTX;

        /** {@link List} of a {@link IVisualItem} implementation. */
        private final List<T> mItems;

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
        private XPathSublistEvaluation(final Set<Long> paramNodeKeys, final List<T> paramSublist) {
            assert paramNodeKeys != null && paramSublist != null;
            mKeys = paramNodeKeys;
            mItems = paramSublist;
            try {
                mRTX = mSession.beginReadTransaction(mRtx.getRevisionNumber());
                mRTX.moveTo(mRtx.getNode().getNodeKey());
            } catch (final AbsTTException exc) {
                exc.printStackTrace();
            }
        }

        @Override
        public void run() {
            for (final T item : mItems) {
                for (final long key : mKeys) {
                    if (((SunburstItem)item).getNode().getNodeKey() == key) {
                        ((SunburstItem)item).setXPathState(EXPathState.ISFOUND);
                    }
                }
            }
        }
    }

    /**
     * Set insert for shredding.
     * 
     * @param paramInsert
     *            determines how to insert an XML fragment
     */
    @Override
    public void setInsert(final EShredderInsert paramInsert) {
        mInsert = paramInsert;
    }

    /**
     * Get the parent.
     * 
     * @return the parent
     */
    public PApplet getParent() {
        return mParent;
    }

    /**
     * Set new {@link ReadDB} instance.
     * 
     * @param paramDb
     *            the {@link ReadDB} instance to set
     */
    public void setDb(final ReadDB paramDb) {
        mDb.close();
        mDb = paramDb;
    }

    /**
     * Get database handle.
     * 
     * @return the database access
     */
    public ReadDB getDb() {
        return mDb;
    }
}
