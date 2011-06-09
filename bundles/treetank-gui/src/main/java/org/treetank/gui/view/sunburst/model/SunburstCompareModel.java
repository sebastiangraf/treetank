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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.Thread.State;
import java.util.*;
import java.util.concurrent.*;

import javax.xml.namespace.QName;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IStructuralItem;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.diff.DiffDepth;
import org.treetank.diff.DiffFactory;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.diff.DiffFactory.EDiffOptimized;
import org.treetank.diff.IDiffObserver;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.AbsNode;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.model.AbsModel;
import org.treetank.gui.view.model.IContainer;
import org.treetank.gui.view.model.IModel;
import org.treetank.gui.view.model.TraverseCompareTree;
import org.treetank.gui.view.sunburst.*;
import org.treetank.gui.view.sunburst.SunburstItem.Builder;
import org.treetank.gui.view.sunburst.SunburstItem.EStructType;
import org.treetank.gui.view.sunburst.axis.SunburstCompareDescendantAxis;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Model to compare revisions.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SunburstCompareModel extends AbsModel<SunburstItem> implements PropertyChangeListener {

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(SunburstCompareModel.class));

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            the processing {@link PApplet} core library
     * @param paramDb
     *            {@link ReadDB} reference
     */
    public SunburstCompareModel(final PApplet paramApplet, final ReadDB paramDb) {
        super(paramApplet, paramDb);
    }

    /** {@inheritDoc} */
    @Override
    public void update(final IContainer paramContainer) {
        mLastItems.push(new ArrayList<SunburstItem>(mItems));
        mLastDepths.push(mLastMaxDepth);
        mLastOldDepths.push(mLastOldMaxDepth);
        traverseTree(paramContainer);
    }

    /** {@inheritDoc} */
    @Override
    public void traverseTree(final IContainer paramContainer) {
        assert paramContainer != null;
        
        final SunburstContainer container = (SunburstContainer)paramContainer;
        assert container.mRevision >= 0;
        assert container.mKey >= 0;
        assert container.mDepth >= 0;
        assert container.mModWeight >= 0;

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new TraverseCompareTree(container.mRevision, getDb().getRevisionNumber(),
            container.mKey, container.mDepth, container.mModWeight, container.mPruning,
            this));
        shutdown(executor);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        if (paramEvent.getPropertyName().equals("oldMaxDepth")) {
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
