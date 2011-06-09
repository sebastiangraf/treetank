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
import org.treetank.gui.view.sunburst.SunburstControl;
import org.treetank.gui.view.sunburst.SunburstGUI;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SmallMultiplesGUI implements IProcessingGUI, PropertyChangeListener {
    private static SmallMultiplesGUI mGUI;

    private final SmallMultiplesControl mControl;

    private transient ReadDB mDb;

    private final Embedded mParent;

    private final ZoomPan mZoomer;

    private transient int mDepthMax;

    private transient int mOldDepthMax;
    
    private final SunburstGUI mSunburstGUI;

    /**
     * Private constructor.
     * 
     * @param paramEmbedded
     *            parent processing applet
     * @param paramReadDB
     *            {@link ReadDB} instance
     */
    private SmallMultiplesGUI(final Embedded paramEmbedded, final SmallMultiplesControl paramControl,
        final ReadDB paramReadDB) {
        mSunburstGUI = SunburstGUI.getInstance(paramEmbedded, paramControl, paramReadDB);
        mDb = paramReadDB;
        mControl = paramControl;
        mParent = paramEmbedded;
        mZoomer = new ZoomPan(paramEmbedded);
        mZoomer.setMouseMask(PConstants.CONTROL);
    }

    /**
     * Factory method (Singleton). Note that it's always called from the animation thread, thus it doesn't
     * need to be synchronized.
     * 
     * @param paramEmbedded
     *            parent processing applet
     * @param paramReadDB
     *            {@link ReadDB} instance
     * @return a {@link SunburstGUI} singleton
     */
    static SmallMultiplesGUI getInstance(final Embedded paramEmbedded, final SmallMultiplesControl paramControl,
        final ReadDB paramReadDB) {
        if (mGUI == null) {
            synchronized (SmallMultiplesGUI.class) {
                if (mGUI == null) {
                    mGUI = new SmallMultiplesGUI(paramEmbedded, paramControl, paramReadDB);
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
}
