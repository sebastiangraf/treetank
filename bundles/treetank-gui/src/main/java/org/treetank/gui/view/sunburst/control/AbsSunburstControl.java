/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
/**
 * 
 */
package org.treetank.gui.view.sunburst.control;

import java.awt.event.MouseEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import javax.xml.stream.XMLStreamException;

import controlP5.ControlEvent;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Toggle;

import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.controls.AbsControl;
import org.treetank.gui.view.model.IModel;
import org.treetank.gui.view.sunburst.AbsSunburstGUI;
import org.treetank.gui.view.sunburst.EPruning;
import org.treetank.gui.view.sunburst.SunburstContainer;
import org.treetank.gui.view.sunburst.model.SunburstCompareModel;

import processing.core.PApplet;

/**
 * Abstract class to simplify the implementation of {@link ISunburstControl}.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public abstract class AbsSunburstControl extends AbsControl implements ISunburstControl {

    /** {@link IModel} implementation. */
    protected IModel mModel;

    /** {@link ReadDB} reference. */
    protected transient ReadDB mDb;

    /** The GUI. */
    protected final AbsSunburstGUI mGUI;

    /** Processing {@link PApplet}. */
    protected final PApplet mParent;
    
    /** Used for the hybrid comparsion. */
    public static CountDownLatch mLatch = new CountDownLatch(1);

    /**
     * Constructor.
     * 
     * @param paramParent
     *            {@link PApplet} reference
     * @param paramModel
     *            {@link IModel} implementation
     * @param paramDb
     *            {@link ReadDB} reference
     */
    public AbsSunburstControl(final PApplet paramParent, final IModel paramModel, final ReadDB paramDb) {
        assert paramParent != null;
        assert paramDb != null;
        assert paramModel != null;
        mParent = paramParent;
        mDb = paramDb;
        mGUI = getGUIInstance();
        mGUI.getControlP5().addListener(this);
        mModel = paramModel;
        mModel.addPropertyChangeListener(mGUI);
    }

    /**
     * Get GUI instance.
     */
    protected abstract AbsSunburstGUI getGUIInstance();

    /**
     * Implements processing mousePressed.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     * 
     * @see processing.core.PApplet#mousePressed
     */
    @Override
    public void mousePressed(final MouseEvent paramEvent) {
        mGUI.getControlP5().controlWindow.mouseEvent(paramEvent);
        mGUI.getZoomer().mouseEvent(paramEvent);
    }

    /**
     * Called on every change of the GUI.
     * 
     * @param paramControlEvent
     *            the {@link ControlEvent}
     */
    @Override
    public void controlEvent(final ControlEvent paramControlEvent) {
        assert paramControlEvent != null;
        if (paramControlEvent.isController()) {
            if (paramControlEvent.controller() instanceof Toggle) {
                final Toggle toggle = (Toggle)paramControlEvent.controller();
                switch (paramControlEvent.controller().id()) {
                case 0:
                    mGUI.setShowArcs(toggle.getState());
                    break;
                case 1:
                    mGUI.setShowLines(toggle.getState());
                    break;
                case 2:
                    mGUI.setUseBezierLine(toggle.getState());
                    break;
                }
            } else if (paramControlEvent.controller() instanceof Slider) {
                switch (paramControlEvent.controller().id()) {
                case 0:
                    mGUI.setInnerNodeArcScale(paramControlEvent.controller().value());
                    break;
                case 1:
                    mGUI.setLeafArcScale(paramControlEvent.controller().value());
                    break;
                case 2:
                    mGUI.setModificationWeight(paramControlEvent.controller().value());
                    break;
                case 3:
                    mGUI.setDotSize(paramControlEvent.controller().value());
                    break;
                case 4:
                    mGUI.setDotBrightness(paramControlEvent.controller().value());
                    break;
                case 5:
                    mGUI.setBackgroundBrightness(paramControlEvent.controller().value());
                    break;
                }
            } else if (paramControlEvent.controller() instanceof Range) {
                float[] f;
                switch (paramControlEvent.controller().id()) {
                case 0:
                    f = paramControlEvent.controller().arrayValue();
                    mGUI.setHueStart(f[0]);
                    mGUI.setHueEnd(f[1]);
                    break;
                case 1:
                    f = paramControlEvent.controller().arrayValue();
                    mGUI.setSaturationStart(f[0]);
                    mGUI.setSaturationEnd(f[1]);
                    break;
                case 2:
                    f = paramControlEvent.controller().arrayValue();
                    mGUI.setBrightnessStart(f[0]);
                    mGUI.setBrightnessEnd(f[1]);
                    break;
                case 3:
                    f = paramControlEvent.controller().arrayValue();
                    mGUI.setInnerNodeBrightnessStart(f[0]);
                    mGUI.setInnerNodeBrightnessEnd(f[1]);
                    break;
                case 4:
                    f = paramControlEvent.controller().arrayValue();
                    mGUI.setInnerNodeStrokeBrightnessStart(f[0]);
                    mGUI.setInnerNodeStrokeBrightnessEnd(f[1]);
                    break;
                case 5:
                    f = paramControlEvent.controller().arrayValue();
                    mGUI.setStrokeWeightStart(f[0]);
                    mGUI.setStrokeWeightEnd(f[1]);
                    break;
                }
            }

            mGUI.update();
        }
    }
    
    /**
     * Implements processing mouseEntered.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     * 
     * @see processing.core.PApplet#mouseEntered
     */
    @Override
    public void mouseEntered(final MouseEvent paramEvent) {
        // if (mSunburstGUI.mDone) {
        mGUI.getParent().loop();
        // }
    }

    /**
     * Implements processing mouseExited.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     * 
     * @see processing.core.PApplet#mouseExited
     */
    @Override
    public void mouseExited(final MouseEvent paramEvent) {
        mGUI.getParent().noLoop();
//        mGUI.draw();
    }

    /** {@inheritDoc} */
    @Override
    public void commit(final int paramValue) throws XMLStreamException {
    }

    /** {@inheritDoc} */
    @Override
    public void submit(final int paramValue) throws XMLStreamException {
    }

    /** {@inheritDoc} */
    @Override
    public void cancel(final int paramValue) {
    }
    
    /** {@inheritDoc} */
    @Override
    public IModel getModel() {
        return mModel;
    }
}
