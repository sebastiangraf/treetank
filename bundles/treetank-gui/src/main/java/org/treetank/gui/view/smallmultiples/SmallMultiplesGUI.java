/**
 * 
 */
package org.treetank.gui.view.smallmultiples;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.gicentre.utils.move.ZoomPan;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.smallmultiples.SmallMultiplesView.Embedded;
import org.treetank.gui.view.sunburst.AbsSunburstGUI;
import org.treetank.gui.view.sunburst.EDraw;
import org.treetank.gui.view.sunburst.SunburstControl;
import org.treetank.gui.view.sunburst.SunburstGUI;
import org.treetank.gui.view.sunburst.control.AbsSunburstControl;
import org.treetank.gui.view.sunburst.control.ISunburstControl;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * GUI of the {@link SmallMultiplesView}.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SmallMultiplesGUI extends AbsSunburstGUI implements PropertyChangeListener {
    /** Instance of this class. */
    private static volatile SmallMultiplesGUI mGUI;

    /** {@link SmallMultiplesControl} reference. */
    private final SmallMultiplesControl mControl;

    /** {@link ReadDB} reference. */
    private transient ReadDB mDb;

    /** {@link SmallMultiplesControl} reference. */
    private final Embedded mParent;

    private final ZoomPan mZoomer;

    private transient int mDepthMax;

    private transient int mOldDepthMax;

    /** Instance of the {@link SunburstGUI} used for composition. */
    private final SunburstGUI mSunburstGUI;

    /**
     * Private constructor.
     * 
     * @param paramApplet
     *            parent processing applet
     * @param paramReadDB
     *            {@link ReadDB} instance
     */
    private SmallMultiplesGUI(final PApplet paramEmbedded, final ISunburstControl paramControl,
        final ReadDB paramReadDB) {
        super (paramEmbedded, paramControl, paramReadDB);
        mSunburstGUI = SunburstGUI.getInstance(paramEmbedded, paramControl, paramReadDB);
        mDb = paramReadDB;
        mControl = (SmallMultiplesControl)paramControl;
        mParent = (Embedded)paramEmbedded;
        mZoomer = new ZoomPan(paramEmbedded);
        mZoomer.setMouseMask(PConstants.CONTROL);
    }

    /**
     * Factory method (Singleton).
     * 
     * @param paramApplet
     *            parent processing applet
     * @param paramControl
     *            {@link ISunburstControl} implementation
     * @param paramReadDB
     *            {@link ReadDB} instance
     * @return a {@link SunburstGUI} singleton
     */
    public static SmallMultiplesGUI getInstance(final PApplet paramApplet, final ISunburstControl paramControl,
        final ReadDB paramReadDB) {
        if (mGUI == null) {
            synchronized (SmallMultiplesGUI.class) {
                if (mGUI == null) {
                    mGUI = new SmallMultiplesGUI(paramApplet, paramControl, paramReadDB);
                }
            }
        }
        return mGUI;
    }

    /** {@inheritDoc} */
    @Override
    public void draw() {

    }

    /** {@inheritDoc} */
    @Override
    public void update() {
        mSunburstGUI.update();
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        if (paramEvent.getPropertyName().equals("maxDepth")) {
            assert paramEvent.getNewValue() instanceof Integer;
            mDepthMax = (Integer)paramEvent.getNewValue();
            mDepthMax += 2;
        } else if (paramEvent.getPropertyName().equals("oldMaxDepth")) {
            assert paramEvent.getNewValue() instanceof Integer;
            mOldDepthMax = (Integer)paramEvent.getNewValue();
        } else if (paramEvent.getPropertyName().equals("done")) {
            update();
            assert paramEvent.getNewValue() instanceof Boolean;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void setup() {       
    }

    /** {@inheritDoc} */
    @Override
    protected void drawItems(final EDraw paramDraw) {
        
    }
}
