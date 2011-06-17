/**
 * 
 */
package org.treetank.gui.view.smallmultiples;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.gicentre.utils.move.ZoomPan;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.smallmultiples.SmallMultiplesView.Embedded;
import org.treetank.gui.view.sunburst.AbsSunburstGUI;
import org.treetank.gui.view.sunburst.EDraw;
import org.treetank.gui.view.sunburst.SunburstControl;
import org.treetank.gui.view.sunburst.SunburstGUI;
import org.treetank.gui.view.sunburst.control.AbsSunburstControl;
import org.treetank.gui.view.sunburst.control.ISunburstControl;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * GUI of the {@link SmallMultiplesView}.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SmallMultiplesGUI extends AbsSunburstGUI implements PropertyChangeListener {
    /** Instance of this class. */
    private static volatile SmallMultiplesGUI mGUI;

    private final SunburstGUI mSunburstGUI;

    /** {@link SmallMultiplesControl} reference. */
    private final ISunburstControl mControl;

    /** {@link ReadDB} reference. */
    private transient ReadDB mDb;

    /** {@link SmallMultiplesControl} reference. */
    private final Embedded mParent;

    /** {@link Set} of {@link PGraphics} to buffer {@link SunburstItem}s. */
    private final Set<PGraphics> mBufferedImages = new HashSet<PGraphics>();

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
        super(paramEmbedded, paramControl, paramReadDB);
        mSunburstGUI = SunburstGUI.getInstance(paramEmbedded, paramControl, paramReadDB);
        mDb = paramReadDB;
        mControl = paramControl;
        mParent = (Embedded)paramEmbedded;
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
    public static SmallMultiplesGUI getInstance(final PApplet paramApplet,
        final ISunburstControl paramControl, final ReadDB paramReadDB) {
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
        System.out.println("DRAW!");
        mParent.pushMatrix();
        mParent.colorMode(PConstants.HSB, 360, 100, 100, 100);
        mParent.noFill();
        mParent.ellipseMode(PConstants.RADIUS);
        mParent.strokeCap(PConstants.SQUARE);
        mParent.textLeading(14);
        mParent.textAlign(PConstants.LEFT, PConstants.TOP);
        mParent.smooth();

        for (final PGraphics buffer : mBufferedImages) {
            try {
                mLock.acquire();
                mParent.image(buffer, 0, 0);
            } catch (final InterruptedException exc) {
                exc.printStackTrace();
            } finally {
                mLock.release();
                // LOGWRAPPER.debug("[draw()]: Available permits: " + mLock.availablePermits());
            }
            mParent.translate((float)mParent.width / 2f, (float)mParent.height / 2f);
        }

        mParent.popMatrix();
        ViewUtilities.drawGUI(getControlP5());
    }

    /** {@inheritDoc} */
    @Override
    public void update() {
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        mSunburstGUI.propertyChange(paramEvent);
        if (paramEvent.getPropertyName().equals("maxDepth")) {
            assert paramEvent.getNewValue() instanceof Integer;
            mDepthMax = (Integer)paramEvent.getNewValue();
            mDepthMax += 2;
        } else if (paramEvent.getPropertyName().equals("done")) {
            // FIXME: quick hack
            while (mSunburstGUI.getBuffer() == null)
                ;
            mBufferedImages.add(mSunburstGUI.getBuffer());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void setup() {
    }

    /** {@inheritDoc} */
    @Override
    protected void drawItems(final EDraw paramDraw) {
        mSunburstGUI.drawItems(paramDraw);
    }
}
