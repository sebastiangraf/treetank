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
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import controlP5.ControlEvent;

import org.treetank.api.IReadTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.controls.AbsControl;
import org.treetank.gui.view.model.IModel;
import org.treetank.gui.view.smallmultiples.SmallMultiplesView.Embedded;
import org.treetank.gui.view.sunburst.AbsSunburstGUI;
import org.treetank.gui.view.sunburst.EPruning;
import org.treetank.gui.view.sunburst.SunburstContainer;
import org.treetank.gui.view.sunburst.SunburstControl;
import org.treetank.gui.view.sunburst.SunburstGUI;
import org.treetank.gui.view.sunburst.control.AbsSunburstControl;
import org.treetank.gui.view.sunburst.control.ISunburstControl;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Controller for the {@link SmallMultiplesView}.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SmallMultiplesControl extends AbsSunburstControl {

    /** {@link SmallMultiplesControl} singleton instance. */
    private static SmallMultiplesControl mControl;

    /** Container with settings for the model. */
    private transient SunburstContainer mContainer;

    /** Locking of observable changes. */
    private final Semaphore mLock = new Semaphore(1);

    /** {@link SmallMultiplesGUI} reference. */
    private final SmallMultiplesGUI mSmallMultiplesGUI;

    /**
     * Private constructor.
     * 
     * @param paramParent
     *            parent {@link Embedded} reference
     * @param paramModel
     *            model which implements the {@link IModel} interface
     * @param paramDb
     *            {@link ReadDB} reference
     */
    private SmallMultiplesControl(final Embedded paramParent, final IModel paramModel, final ReadDB paramDB) {
        super(paramParent, paramModel, paramDB);
        mSmallMultiplesGUI = (SmallMultiplesGUI)mGUI;
        refreshIncrementalTraversal();
    }

    /**
     * Refresh differential traversal of the model.
     */
    public void refreshDifferentialTraversal() {
        try {
            final long lastRevision = getLastRevision();
            for (long j = mDb.getRevisionNumber() + 1, i = mDb.getRevisionNumber() + 1; i < lastRevision
                && i < j + 4; i++) {
                mContainer = new SunburstContainer(mSmallMultiplesGUI, mModel);
                mModel.traverseTree(mContainer.setLock(mLock).setStartKey(mDb.getNodeKey())
                    .setPruning(EPruning.FALSE).setModWeight(mSmallMultiplesGUI.getModificationWeight())
                    .setRevision(i).setDepth(0).setCompare(ECompare.DIFFERENTIAL));
            }
        } catch (final AbsTTException e) {

        }
    }

    /**
     * Refresh incremental traversal of the model.
     */
    public void refreshIncrementalTraversal() {
        try {
            final long lastRevision = getLastRevision();
            for (long j = mDb.getRevisionNumber() + 1, i = mDb.getRevisionNumber() + 1; i < lastRevision
                && i < j + 4; i++) {
                mContainer = new SunburstContainer(mSmallMultiplesGUI, mModel);
                mModel.traverseTree(mContainer.setLock(mLock).setStartKey(mDb.getNodeKey())
                    .setPruning(EPruning.FALSE).setModWeight(mSmallMultiplesGUI.getModificationWeight())
                    .setOldRevision(i - 1).setRevision(i).setDepth(0).setCompare(ECompare.DIFFERENTIAL));
            }
        } catch (final AbsTTException e) {

        }
    }

    /**
     * Refresh hybrid traversal of the model.
     */
    public void refreshHybridTraversal() {
        try {
            final long lastRevision = getLastRevision();
            ECompare.HYBRID.setValue(true);
            mContainer = new SunburstContainer(mSmallMultiplesGUI, mModel);
            mContainer
                .setLock(mLock)
                .setStartKey(mDb.getNodeKey())
                .setPruning(EPruning.FALSE)
                .setModWeight(mSmallMultiplesGUI.getModificationWeight())
                .setOldRevision(mDb.getRevisionNumber())
//                .setRevision(2)
                .setRevision(
                    (lastRevision < mDb.getRevisionNumber() + 4) ? lastRevision : mDb.getRevisionNumber() + 4)
                .setDepth(0).setCompare(ECompare.HYBRID);
            mModel.traverseTree(mContainer);
            AbsSunburstControl.mLatch = new CountDownLatch(1);
            try {
                final boolean done = AbsSunburstControl.mLatch.await(30, TimeUnit.SECONDS);
                if (!done) {
                    throw new IllegalStateException("Hybrid traversal Failed!");
                }
            } catch (final InterruptedException e) {

            }

//            mContainer = new SunburstContainer(mSmallMultiplesGUI, mModel);
//            mModel.traverseTree(mContainer.setLock(mLock).setStartKey(mDb.getNodeKey())
//                .setPruning(EPruning.FALSE).setModWeight(mSmallMultiplesGUI.getModificationWeight())
//                .setOldRevision(0).setRevision(1).setDepth(0).setCompare(ECompare.HYBRID));
//            
//            mContainer = new SunburstContainer(mSmallMultiplesGUI, mModel);
//            mModel.traverseTree(mContainer.setLock(mLock).setStartKey(mDb.getNodeKey())
//                .setPruning(EPruning.FALSE).setModWeight(mSmallMultiplesGUI.getModificationWeight())
//                .setOldRevision(1).setRevision(2).setDepth(0).setCompare(ECompare.HYBRID));
            for (long j = mDb.getRevisionNumber() + 1, i = mDb.getRevisionNumber() + 1; i < lastRevision
                && i < j + 4; i++) {
                mContainer = new SunburstContainer(mSmallMultiplesGUI, mModel);
                mModel.traverseTree(mContainer.setLock(mLock).setStartKey(mDb.getNodeKey())
                    .setPruning(EPruning.FALSE).setModWeight(mSmallMultiplesGUI.getModificationWeight())
                    .setOldRevision(i - 1).setRevision(i).setDepth(0).setCompare(ECompare.HYBRID));
            }
        } catch (final AbsTTException e) {

        }

    }

    /**
     * Get last revision of currently opened resource
     * 
     * @return last revision
     */
    private long getLastRevision() throws AbsTTException {
        final IReadTransaction rtx = mDb.getSession().beginReadTransaction();
        final long lastRevision = rtx.getRevisionNumber();
        rtx.close();
        return lastRevision;
    }

    /**
     * Get singleton instance.
     * 
     * @param paramParent
     *            reference of class which extends {@link PApplet}
     * @param paramModel
     *            {@link IModel} reference
     * @param paramDB
     *            {@link ReadDB} reference
     * @return singelton instance of this class
     */
    public static synchronized SmallMultiplesControl getInstance(final Embedded paramParent,
        final SmallMultiplesModel paramModel, final ReadDB paramDB) {
        if (mControl == null) {
            mControl = new SmallMultiplesControl(paramParent, paramModel, paramDB);
        }
        return mControl;
    }

    /** {@inheritDoc} */
    public void refreshUpdate() {
    }

    /**
     * Is getting called from processings keyRealeased-method and implements it.
     * 
     * @see processing.core.PApplet#keyReleased()
     */
    @Override
    public void keyReleased() {
        switch (mSmallMultiplesGUI.getParent().key) {
        case 's':
        case 'S':
            // Save PNG.
            mSmallMultiplesGUI.getParent().saveFrame(
                ViewUtilities.SAVEPATH + ViewUtilities.timestamp() + "_##.png");
            break;
        case 'p':
        case 'P':
            // Save PDF.
            mSmallMultiplesGUI.setSavePDF(true);
            PApplet.println("\n" + "saving to pdf – starting");
            mSmallMultiplesGUI.getParent().beginRecord(PConstants.PDF,
                ViewUtilities.SAVEPATH + ViewUtilities.timestamp() + ".pdf");
            mSmallMultiplesGUI.getParent().textMode(PConstants.SHAPE);
            mSmallMultiplesGUI.getParent().textFont(
                mSmallMultiplesGUI.getParent().createFont(
                    "src" + File.separator + "main" + File.separator + "resources" + File.separator + "data"
                        + File.separator + "miso-regular.ttf", 15));
            break;
        }

        if (mSmallMultiplesGUI.isShowGUI()) {
            mSmallMultiplesGUI.getControlP5().group("menu").open();
        } else {
            mSmallMultiplesGUI.getControlP5().group("menu").close();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected AbsSunburstGUI getGUIInstance() {
        return SmallMultiplesGUI.getInstance(mParent, this, mDb);
    }

    /** Release lock. */
    public void releaseLock() {
        mLock.release();
    }

    /** {@inheritDoc} */
    @Override
    public void controlEvent(final ControlEvent paramControlEvent) {
        assert paramControlEvent != null;
        super.controlEvent(paramControlEvent);
    }
}
