/**
 * 
 */
package org.treetank.gui.view.sunburst.control;

import java.awt.event.MouseEvent;

import javax.xml.stream.XMLStreamException;

import controlP5.ControlEvent;

import org.treetank.gui.ReadDB;
import org.treetank.gui.controls.AbsControl;
import org.treetank.gui.view.IProcessingGUI;
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
        getGUI().getControlP5().addListener(this);
        mModel = paramModel;
        mModel.addPropertyChangeListener(getGUI());
        mModel.traverseTree(new SunburstContainer().setStartKey(mDb.getNodeKey()).setPruning(EPruning.FALSE));
 
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
        getGUI().getControlP5().controlWindow.mouseEvent(paramEvent);
        getGUI().getZoomer().mouseEvent(paramEvent);
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
            if (paramControlEvent.controller().name().equals("leaf node hue range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                getGUI().setHueStart(f[0]);
                getGUI().setHueEnd(f[1]);
            }
            if (paramControlEvent.controller().name().equals("leaf node saturation range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                getGUI().setSaturationStart(f[0]);
                getGUI().setSaturationEnd(f[1]);
            }
            if (paramControlEvent.controller().name().equals("leaf node brightness range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                getGUI().setBrightnessStart(f[0]);
                getGUI().setBrightnessEnd(f[1]);
            }
            if (paramControlEvent.controller().name().equals("inner node brightness range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                getGUI().setInnerNodeBrightnessStart(f[0]);
                getGUI().setInnerNodeBrightnessEnd(f[1]);
            }
            if (paramControlEvent.controller().name().equals("inner node stroke brightness range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                getGUI().setInnerNodeStrokeBrightnessStart(f[0]);
                getGUI().setInnerNodeStrokeBrightnessEnd(f[1]);
            }
            if (paramControlEvent.controller().name().equals("stroke weight range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                getGUI().setStrokeWeightStart(f[0]);
                getGUI().setStrokeWeightEnd(f[1]);
            }

            getGUI().update();
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

    /**
     * @return the mGUI
     */
    public AbsSunburstGUI getGUI() {
        return mGUI;
    }
}
