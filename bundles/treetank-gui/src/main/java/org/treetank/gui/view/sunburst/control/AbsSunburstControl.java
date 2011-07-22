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
