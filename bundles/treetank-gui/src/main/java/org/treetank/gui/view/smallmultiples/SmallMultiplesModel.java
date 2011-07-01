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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.treetank.gui.ReadDB;
import org.treetank.gui.view.model.AbsModel;
import org.treetank.gui.view.model.IContainer;
import org.treetank.gui.view.model.TraverseCompareTree;
import org.treetank.gui.view.smallmultiples.SmallMultiplesView.Embedded;
import org.treetank.gui.view.sunburst.SunburstContainer;
import org.treetank.gui.view.sunburst.SunburstItem;
import org.treetank.gui.view.sunburst.model.SunburstCompareModel;

import processing.core.PApplet;

/**
 * Small multiples model. Can be easily extended through the usage of composition.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SmallMultiplesModel extends AbsModel implements PropertyChangeListener {

    /** {@link SunbburstCompareModel} instance. */
    private final SunburstCompareModel model;
    
    /** {@link SunburstContainer} reference. */
    private transient SunburstContainer mContainer;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    public SmallMultiplesModel(final PApplet paramApplet, final ReadDB paramDb) {
        super(paramApplet, paramDb);
        model = new SunburstCompareModel(paramApplet, paramDb);
        model.addPropertyChangeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void update(final IContainer paramContainer) {
        model.update(paramContainer);
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final IContainer paramContainer) {
        assert paramContainer != null;
        mContainer = (SunburstContainer) paramContainer;
        model.traverseTree(paramContainer);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        if (paramEvent.getPropertyName().equals("newRev")) {
            firePropertyChange("newRev", null, (Long) paramEvent.getNewValue());
        } else if (paramEvent.getPropertyName().equals("oldMaxDepth")) {
            mLastOldMaxDepth = (Integer)paramEvent.getNewValue();
            firePropertyChange("oldMaxDepth", null, mLastOldMaxDepth);
        } else if (paramEvent.getPropertyName().equals("maxDepth")) {
            mLastMaxDepth = (Integer)paramEvent.getNewValue();
            firePropertyChange("maxDepth", null, mLastMaxDepth);
        } else if (paramEvent.getPropertyName().equals("done")) {
            firePropertyChange("done", null, true);
        } else if (paramEvent.getPropertyName().equals("items")) {
            mItems = (List<SunburstItem>)paramEvent.getNewValue();
        }
    }
}
