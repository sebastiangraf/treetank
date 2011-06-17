/**
 * 
 */
package org.treetank.gui.view.sunburst.control;

import java.awt.event.MouseEvent;

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
    private final AbsSunburstGUI mGUI;

    protected final PApplet mParent;

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
        getGUIInstance().getControlP5().controlWindow.mouseEvent(paramEvent);
        getGUIInstance().getZoomer().mouseEvent(paramEvent);
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
                    getGUIInstance().setShowArcs(toggle.getState());
                    break;
                case 1:
                    getGUIInstance().setShowLines(toggle.getState());
                    break;
                case 2:
                    getGUIInstance().setUseBezierLine(toggle.getState());
                    break;
                }
            } else if (paramControlEvent.controller() instanceof Slider) {
                switch (paramControlEvent.controller().id()) {
                case 0:
                    getGUIInstance().setInnerNodeArcScale(paramControlEvent.controller().value());
                    break;
                case 1:
                    getGUIInstance().setLeafArcScale(paramControlEvent.controller().value());
                    break;
                case 2:
                    getGUIInstance().setModificationWeight(paramControlEvent.controller().value());
                    break;
                case 3:
                    getGUIInstance().setDotSize(paramControlEvent.controller().value());
                    break;
                case 4:
                    getGUIInstance().setDotBrightness(paramControlEvent.controller().value());
                    break;
                case 5:
                    getGUIInstance().setBackgroundBrightness(paramControlEvent.controller().value());
                    break;
                }
            } else if (paramControlEvent.controller() instanceof Range) {
                float[] f;
                switch (paramControlEvent.controller().id()) {
                case 0:
                    f = paramControlEvent.controller().arrayValue();
                    getGUIInstance().setHueStart(f[0]);
                    getGUIInstance().setHueEnd(f[1]);
                    break;
                case 1:
                    f = paramControlEvent.controller().arrayValue();
                    getGUIInstance().setSaturationStart(f[0]);
                    getGUIInstance().setSaturationEnd(f[1]);
                    break;
                case 2:
                    f = paramControlEvent.controller().arrayValue();
                    getGUIInstance().setBrightnessStart(f[0]);
                    getGUIInstance().setBrightnessEnd(f[1]);
                    break;
                case 3:
                    f = paramControlEvent.controller().arrayValue();
                    getGUIInstance().setInnerNodeBrightnessStart(f[0]);
                    getGUIInstance().setInnerNodeBrightnessEnd(f[1]);
                    break;
                case 4:
                    f = paramControlEvent.controller().arrayValue();
                    getGUIInstance().setInnerNodeStrokeBrightnessStart(f[0]);
                    getGUIInstance().setInnerNodeStrokeBrightnessEnd(f[1]);
                    break;
                case 5:
                    f = paramControlEvent.controller().arrayValue();
                    getGUIInstance().setStrokeWeightStart(f[0]);
                    getGUIInstance().setStrokeWeightEnd(f[1]);
                    break;
                }
            }

            getGUIInstance().update();
        }
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
