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
package org.treetank.gui.view.smallmultiples;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IVisualItem;
import org.treetank.gui.view.model.AbsModel;
import org.treetank.gui.view.model.IContainer;
import org.treetank.gui.view.model.TraverseCompareTree;
import org.treetank.gui.view.smallmultiples.SmallMultiplesView.Embedded;
import org.treetank.gui.view.sunburst.EGreyState;
import org.treetank.gui.view.sunburst.SunburstContainer;
import org.treetank.gui.view.sunburst.SunburstItem;
import org.treetank.gui.view.sunburst.control.AbsSunburstControl;
import org.treetank.gui.view.sunburst.control.ISunburstControl;
import org.treetank.gui.view.sunburst.model.SunburstCompareModel;

import processing.core.PApplet;

/**
 * Small multiples model. Can be easily extended through the usage of composition.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SmallMultiplesModel extends AbsModel<SunburstItem> implements PropertyChangeListener {

    /** {@link SunbburstCompareModel} instance. */
    private final SunburstCompareModel mModel;

    /** {@link SunburstContainer} reference. */
    private transient SunburstContainer mContainer;

    /** {@link List} of {@link SunburstItem}s. */
    private transient List<SunburstItem> mDiffItems;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     * @param paramControl
     *            {@link ISunburstControl} implementation
     */
    public SmallMultiplesModel(final PApplet paramApplet, final ReadDB paramDb) {
        super(paramApplet, paramDb);
        mModel = new SunburstCompareModel(paramApplet, paramDb);
        mModel.addPropertyChangeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void update(final IContainer paramContainer) {
        assert paramContainer != null;
        mModel.update(paramContainer);
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final IContainer paramContainer) {
        assert paramContainer != null;
        mContainer = (SunburstContainer)paramContainer;
        mModel.traverseTree(paramContainer);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        if (paramEvent.getPropertyName().equals("oldRev")) {
            firePropertyChange("oldRev", null, (Long)paramEvent.getNewValue());
        } else if (paramEvent.getPropertyName().equals("newRev")) {
            firePropertyChange("newRev", null, (Long)paramEvent.getNewValue());
        } else if (paramEvent.getPropertyName().equals("oldMaxDepth")) {
            mLastOldMaxDepth = (Integer)paramEvent.getNewValue();
            if (mContainer.getCompare() == ECompare.HYBRID) {
                if (ECompare.HYBRID.getValue()) {
                    firePropertyChange("oldMaxDepth", null, mLastOldMaxDepth);
                }
            } else {
                firePropertyChange("oldMaxDepth", null, mLastOldMaxDepth);
            }
        } else if (paramEvent.getPropertyName().equals("maxDepth")) {
            mLastMaxDepth = (Integer)paramEvent.getNewValue();

            if (mContainer.getCompare() == ECompare.HYBRID) {
                if (ECompare.HYBRID.getValue()) {
                    firePropertyChange("maxDepth", null, mLastMaxDepth);
                }
            } else {
                firePropertyChange("maxDepth", null, mLastMaxDepth);
            }
        } else if (paramEvent.getPropertyName().equals("done")) {
            switch (mContainer.getCompare()) {
            case DIFFERENTIAL:
            case INCREMENTAL:
                firePropertyChange("done", null, true);
                break;
            case HYBRID:
                if (ECompare.HYBRID.getValue()) {
                    ECompare.HYBRID.setValue(false);
                    mDiffItems = mItems;
                    mContainer.getLock().release();
                    AbsSunburstControl.mLatch.countDown();
                } else {
                    compareLists(mItems, mDiffItems);
                    mItems = mDiffItems;
                    firePropertyChange("done", null, true);
                }
                break;
            }

        } else if (paramEvent.getPropertyName().equals("items")) {
            mItems = (List<SunburstItem>)paramEvent.getNewValue();
        }
    }

    /**
     * Compare two {@link List}s.
     * 
     * @param paramFirst
     *            first list
     * @param paramSecond
     *            second list
     */
    private void compareLists(final List<SunburstItem> paramFirst, final List<SunburstItem> paramSecond) {
        assert paramFirst != null;
        assert paramSecond != null;
        for (final SunburstItem item : paramSecond) {
            item.setGreyState(EGreyState.NO);
        }
        final List<SunburstItem> secondList = new ArrayList<SunburstItem>(paramSecond);

        // secondList.addAll(paramSecond);
        // secondList.removeAll(paramFirst);
        //
        // for (final SunburstItem item: secondList) {
        // item.setGreyState(EGreyState.YES);
        // }

        Collections.sort(paramFirst);
        Collections.sort(secondList);
        // System.out.println("old rev: " + mContainer.getOldRevision());
        // System.out.println("new rev: " + mContainer.getRevision());
        System.out.println("first: " + paramFirst.size());
        System.out.println("second: " + secondList.size());

        int i = 0;
        int j = 0;
        while (i < paramFirst.size() && j < secondList.size()) {
            final IVisualItem firstItem = paramFirst.get(i);
            final IVisualItem secondItem = secondList.get(j);

            if (firstItem.equals(secondItem)) {
                i++;
                j++;    
            } else {
                // Set secondItem to grey.
                secondItem.setGreyState(EGreyState.YES);

                if (firstItem.getNodeKey() > secondItem.getNodeKey()) {
                    j++;
                } else {
                    i++;
                }
            }
        }

        while (j < paramSecond.size() - 1) {
            // Set secondItem to grey.
            paramSecond.get(j).setGreyState(EGreyState.YES);
            j++;
        }

        int k = 0;
        for (final SunburstItem item : secondList) {
            if (item.getGreyState() == EGreyState.YES) {
                System.out.println(k + "juhu");
                k++;
            }
        }
    }
}
