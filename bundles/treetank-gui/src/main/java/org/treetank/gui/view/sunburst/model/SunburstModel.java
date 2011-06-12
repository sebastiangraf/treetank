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

package org.treetank.gui.view.sunburst.model;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import controlP5.ControlGroup;

import org.slf4j.LoggerFactory;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.AbsObservableComponent;
import org.treetank.gui.view.model.AbsModel;
import org.treetank.gui.view.model.IChangeModel;
import org.treetank.gui.view.model.IContainer;
import org.treetank.gui.view.model.ITraverseModel;
import org.treetank.gui.view.sunburst.*;
import org.treetank.gui.view.sunburst.SunburstItem.Builder;
import org.treetank.gui.view.sunburst.SunburstItem.EStructType;
import org.treetank.gui.view.sunburst.axis.SunburstDescendantAxis;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.service.xml.shredder.EShredderCommit;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.settings.EFixed;
import org.treetank.utils.LogWrapper;

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
public final class SunburstModel extends AbsModel<SunburstItem> implements IChangeModel {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SunburstModel.class));

    /** {@link IWriteTransaction} instance. */
    private transient IWriteTransaction mWtx;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    public SunburstModel(final PApplet paramApplet, final ReadDB paramDb) {
        super(paramApplet, paramDb);
    }

    /** {@inheritDoc} */
    @Override
    public void update(final IContainer paramContainer) {
        mLastItems.push(new ArrayList<SunburstItem>(mItems));
        mLastDepths.push(mLastMaxDepth);
        traverseTree(paramContainer);
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final IContainer paramContainer) {
        assert paramContainer != null;
        final SunburstContainer container = (SunburstContainer)paramContainer;
        assert container.getStartKey() >= 0;
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(new TraverseTree(container.getStartKey(), container.getPruning(), this));
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        shutdown(executor);
    }

    /** Traverse a tree (single revision). */
    private static final class TraverseTree extends AbsObservableComponent implements Callable<Void>,
        ITraverseModel {
        /** Key from which to start traversal. */
        private transient long mKey;

        /** {@link SunburstModel} instance. */
        private final SunburstModel mModel;

        /** {@link IReadTransaction} instance. */
        private final IReadTransaction mRtx;

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
        private transient PApplet mParent;

        /** Depth in the tree. */
        private transient int mDepth;

        /** Determines if tree should be pruned or not. */
        private transient EPruning mPruning;

        /** Determines if current item has been pruned or not. */
        private transient boolean mPruned;

        /**
         * Constructor.
         * 
         * @param paramKey
         *            Key from which to start traversal.
         * @param paramPruning
         *            Pruning of nodes.
         * @param paramModel
         *            The {@link SunburstModel}.
         */
        private TraverseTree(final long paramKey, final EPruning paramPruning, final SunburstModel paramModel)
            throws AbsTTException {
            assert paramKey >= 0;
            assert paramModel != null;
            mKey = paramKey == 0 ? paramKey + 1 : paramKey;
            mModel = paramModel;
            addPropertyChangeListener(mModel);
            mPruning = paramPruning;
            mDb = mModel.getDb();

            mRtx = mModel.getDb().getSession().beginReadTransaction(mModel.getDb().getRevisionNumber());
            mRtx.moveTo(mKey);
            mParent = mModel.getParent();
            mRelations = new NodeRelations();
            mItems = new LinkedList<SunburstItem>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Void call() {
            LOGWRAPPER.debug("Build sunburst items.");

            // Get min and max textLength.
            getMinMaxTextLength(mRtx);

            // Iterate over nodes and perform appropriate stack actions internally.
            for (final AbsAxis axis = new SunburstDescendantAxis(mRtx, true, this, mPruning); axis.hasNext(); axis
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
            firePropertyChange("items", null, mItems);
            firePropertyChange("done", null, true);

            return null;
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
            float childExtension = 2 * PConstants.PI;
            if (indexToParent > -1) {
                childExtension = extension * (float)descendantCount / ((float)parDescendantCount - 1f);
            }
            LOGWRAPPER.debug("ITEM: " + paramIndex);
            LOGWRAPPER.debug("descendantCount: " + descendantCount);
            LOGWRAPPER.debug("parentDescCount: " + parDescendantCount);
            LOGWRAPPER.debug("indexToParent: " + indexToParent);
            LOGWRAPPER.debug("extension: " + childExtension);
            LOGWRAPPER.debug("depth: " + depth);
            LOGWRAPPER.debug("angle: " + angle);

            // Set node relations.
            String text = null;
            if (mRtx.getNode().getKind() == ENodes.TEXT_KIND) {
                mRelations.setAll(depth, structKind, mRtx.getValueOfCurrentNode().length(), mMinTextLength,
                    mMaxTextLength, indexToParent);
                text = mRtx.getValueOfCurrentNode();
                // LOGWRAPPER.debug("text: " + text);
            } else {
                mRelations.setAll(depth, structKind, descendantCount, 0, mMaxDescendantCount, indexToParent);
            }

            // Build item.
            if (text != null) {
                mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension, mRelations, mDb)
                    .setNode(node).setText(text).build());
            } else {
                // LOGWRAPPER.debug("QName: " + mRtx.getQNameOfCurrentNode());
                mItems.add(new SunburstItem.Builder(mParent, mModel, angle, childExtension, mRelations, mDb)
                    .setNode(node).setQName(mRtx.getQNameOfCurrentNode()).build());
            }

            // Set depth max.
            mDepthMax = Math.max(depth, mDepthMax);

            return childExtension;
        }

        /** {@inheritDoc} */
        @Override
        public int getMaxDescendantCount() {
            return mMaxDescendantCount;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getIsPruned() {
            return mPruned;
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
            assert mPruning != null;
            final List<Future<Integer>> descendants = new LinkedList<Future<Integer>>();

            try {
                final IReadTransaction rtx = mDb.getSession().beginReadTransaction(mDb.getRevisionNumber());
                rtx.moveTo(paramRtx.getNode().getNodeKey());

                switch (mPruning) {
                case TRUE:
                    // mDepth = 0;
                    // boolean first = true;
                    //
                    // if (paramRtx.getNode().getKind() == ENodes.ROOT_KIND) {
                    // paramRtx.moveToFirstChild();
                    // }
                    // final long key = paramRtx.getNode().getNodeKey();
                    // boolean hasNoChild = true;
                    //
                    // if (paramRtx.getStructuralNode().hasFirstChild()) {
                    // while (first || paramRtx.getNode().getNodeKey() != key) {
                    // if (paramRtx.getStructuralNode().hasFirstChild()) {
                    // hasNoChild = true;
                    // if (first) {
                    // first = false;
                    // final int descCount = countDescendants(paramRtx);
                    // descendants.add(descCount);
                    // mMaxDescendantCount = descCount;
                    // paramRtx.moveToFirstChild();
                    // mDepth++;
                    // } else {
                    // if (mDepth > 3) {
                    // while (!paramRtx.getStructuralNode().hasRightSibling()
                    // && paramRtx.getNode().getNodeKey() != key) {
                    // paramRtx.moveToParent();
                    // mDepth--;
                    // }
                    // paramRtx.moveToRightSibling();
                    // } else {
                    // final int descCount = countDescendants(paramRtx);
                    // descendants.add(descCount);
                    // paramRtx.moveToFirstChild();
                    // mDepth++;
                    // }
                    // }
                    // } else {
                    // boolean movedToNextFollowing = false;
                    // while (!paramRtx.getStructuralNode().hasRightSibling()
                    // && paramRtx.getNode().getNodeKey() != key) {
                    // if (hasNoChild && !movedToNextFollowing && mDepth < 4) {
                    // descendants.add(countDescendants(paramRtx));
                    // }
                    // paramRtx.moveToParent();
                    // mDepth--;
                    // movedToNextFollowing = true;
                    // }
                    // if (paramRtx.getNode().getNodeKey() != key) {
                    // hasNoChild = true;
                    // if (movedToNextFollowing) {
                    // paramRtx.moveToRightSibling();
                    //
                    // if (!hasNoChild && mDepth < 4) {
                    // descendants.add(countDescendants(paramRtx));
                    // }
                    // } else {
                    // if (mDepth < 4) {
                    // descendants.add(countDescendants(paramRtx));
                    // }
                    // paramRtx.moveToRightSibling();
                    // }
                    // hasNoChild = true;
                    // }
                    // }
                    // }
                    //
                    // paramRtx.moveTo(mKey);
                    // } else {
                    // descendants.add(1);
                    // }
                    // break;
                case FALSE:
                    // Get descendants for every node and save it to a list.
                    final ExecutorService executor =
                        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                    boolean firstNode = true;
                    for (final AbsAxis axis = new DescendantAxis(rtx, true); axis.hasNext(); axis.next()) {
                        if (axis.getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
                            try {
                                final Future<Integer> submit =
                                    executor.submit(new Descendants(mDb.getSession(),
                                        rtx.getRevisionNumber(), rtx.getNode().getNodeKey()));
                                if (firstNode) {
                                    firstNode = false;
                                    mMaxDescendantCount = submit.get();
                                }
                                System.out.println(submit.isDone());
                                descendants.add(submit);
                            } catch (TTIOException e) {
                                LOGWRAPPER.error(e.getMessage(), e);
                            }
                        }
                    }
                    mModel.shutdown(executor);
                    // for (Future<Integer> future : descendants) {
                    // System.out.println(future.isDone());
                    // }
                    break;
                }
                rtx.close();
            } catch (final AbsTTException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return descendants;
        }

        /**
         * Count descendants.
         * 
         * @param paramRtx
         *            {@link IReadTransaction} instance
         */
        int countDescendants(final IReadTransaction paramRtx) {
            assert paramRtx != null;
            int retVal = 0;

            // for (final AbsAxis axis = new DescendantAxis(paramRtx, true); axis.hasNext(); axis.next()) {
            // retVal++;
            // }
            retVal++;

            final long key = paramRtx.getNode().getNodeKey();
            boolean first = true;
            while (first || paramRtx.getNode().getNodeKey() != key) {
                first = false;
                if (paramRtx.getStructuralNode().hasFirstChild()) {
                    mDepth++;
                    if (mDepth < 4) {
                        paramRtx.moveToFirstChild();
                        retVal++;
                    } else {
                        mDepth--;
                        retVal += nextFollowingNode(paramRtx, key);
                    }
                } else {
                    retVal += nextFollowingNode(paramRtx, key);
                }
            }
            paramRtx.moveTo(key);

            return retVal;
        }

        /**
         * Move transaction to the next following node.
         */
        private int nextFollowingNode(final IReadTransaction paramRtx, final long paramKey) {
            int retVal = 0;
            while (!paramRtx.getStructuralNode().hasRightSibling()) {
                if (paramRtx.getNode().getNodeKey() == paramKey) {
                    break;
                }
                paramRtx.moveToParent();
                mDepth--;
            }
            if (paramRtx.getNode().getNodeKey() != paramKey) {
                paramRtx.moveToRightSibling();
                if (mDepth < 4) {
                    retVal++;
                }
            }
            return retVal;
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
                    // synchronized (paramSession) {
                    mRtx = paramSession.beginReadTransaction(paramRevision);
                    // }
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

    /**
     * Shredder XML fragment input.
     * 
     * @param paramFragment
     *            XML fragment to shredder (might be text as well)
     * @throws AbsTTException
     *             if shredding in treetank fails
     * @throws XMLStreamException
     *             if parser can't parse the XML fragment
     */
    public void addXMLFragment(final String paramFragment) throws AbsTTException, XMLStreamException {
        if (!paramFragment.isEmpty()) {
            try {
                if (paramFragment.startsWith("<")) {
                    final XMLEventReader reader =
                        XMLInputFactory.newInstance().createXMLEventReader(
                            new ByteArrayInputStream(paramFragment.getBytes()));
                    final ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(new XMLShredder(mWtx, reader, mInsert, EShredderCommit.NOCOMMIT));
                    service.shutdown();
                    service.awaitTermination(60, TimeUnit.SECONDS);
                } else {
                    switch (mInsert) {
                    case ADDASFIRSTCHILD:
                        mWtx.insertTextAsFirstChild(paramFragment);
                        break;
                    case ADDASRIGHTSIBLING:
                        mWtx.insertTextAsRightSibling(paramFragment);
                    }
                }
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Commit changes.
     * 
     * @throws AbsTTException
     *             if commiting or closeing transaction fails
     */
    @Override
    public void commit() throws AbsTTException {
        mWtx.commit();
        mWtx.close();
    }

    /**
     * Create a popup menu for modifying nodes.
     * 
     * @param paramEvent
     *            the current {@link MouseEvent}
     * @param paramCtrl
     *            {@link ControlGroup} to insert XML fragment
     * @param paramHitTestIndex
     *            the index of the {@link SunburstItem} which is currently hovered
     * @throws AbsTTException
     */
    public void popupMenu(final MouseEvent paramEvent, final ControlGroup paramCtrl,
        final int paramHitTestIndex) throws AbsTTException {
        if (mWtx == null || mWtx.isClosed()) {
            mWtx = getDb().getSession().beginWriteTransaction();
            mWtx.revertTo(getDb().getRevisionNumber());
        }
        mWtx.moveTo(((SunburstItem)getItem(paramHitTestIndex)).getNode().getNodeKey());
        final SunburstPopupMenu menu = SunburstPopupMenu.getInstance(this, mWtx, paramCtrl);
        menu.show(paramEvent.getComponent(), paramEvent.getX(), paramEvent.getY());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        if (paramEvent.getPropertyName().equals("maxDepth")) {
            mLastMaxDepth = (Integer)paramEvent.getNewValue();
            firePropertyChange("maxDepth", null, mLastMaxDepth);
        } else if (paramEvent.getPropertyName().equals("done")) {
            firePropertyChange("done", null, true);
        } else if (paramEvent.getPropertyName().equals("items")) {
            mItems = (List<SunburstItem>)paramEvent.getNewValue();
        }
    }
}
