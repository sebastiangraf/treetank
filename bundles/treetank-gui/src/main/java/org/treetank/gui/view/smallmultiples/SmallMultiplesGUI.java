/**
 * 
 */
package org.treetank.gui.view.smallmultiples;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import org.gicentre.utils.move.ZoomPan;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.smallmultiples.SmallMultiplesView.Embedded;
import org.treetank.gui.view.sunburst.AbsSunburstGUI;
import org.treetank.gui.view.sunburst.EDraw;
import org.treetank.gui.view.sunburst.SunburstControl;
import org.treetank.gui.view.sunburst.SunburstGUI;
import org.treetank.gui.view.sunburst.SunburstItem;
import org.treetank.gui.view.sunburst.EDraw.EDrawSunburst;
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

    /** {@link SmallMultiplesControl} reference. */
    private final ISunburstControl mControl;

    /** {@link ReadDB} reference. */
    private transient ReadDB mDb;

    /** X value of left upper coordinate. */
    private transient int mX;

    /** Y value of left upper coordinate. */
    private transient int mY;

    /** {@link List} of {@link PGraphics} to buffer {@link SunburstItem}s. */
    private final List<ImageStore> mBufferedImages;

    /** {@link List} of revisions. */
    private final List<Long> mRevisions;

    /** {@link ImageStore} reference. */
    private transient ImageStore mImage;

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
        mDb = paramReadDB;
        mControl = paramControl;
        mUseDiffView = true;
        // ArrayLists because of sorting.
        mBufferedImages = new ArrayList<ImageStore>();
        mRevisions = new ArrayList<Long>();
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
        mParent.pushMatrix();
        mParent.colorMode(PConstants.HSB, 360, 100, 100, 100);
        mParent.noFill();
        mParent.ellipseMode(PConstants.RADIUS);
        mParent.strokeCap(PConstants.SQUARE);
        mParent.textLeading(14);
        mParent.textAlign(PConstants.LEFT, PConstants.TOP);
        mParent.smooth();

        int i = 1;
        mX = 0;
        mY = 0;

        try {
            mLock.acquire();
            for (final ImageStore imageStore : mBufferedImages) {
                final PGraphics buffer = imageStore.mBufferedImage;
                mParent.image(buffer, mX, mY, buffer.width / 2, buffer.height / 2);
                mX += buffer.width / 2;
                if (i % 2 == 0) {
                    mX = 0;
                    mY = buffer.height / 2 + 1;
                }
                i++;
            }
        } catch (final InterruptedException exc) {
            exc.printStackTrace();
        } finally {
            mLock.release();
            // LOGWRAPPER.debug("[draw()]: Available permits: " + mLock.availablePermits());
        }

        if (isSavePDF()) {
            setSavePDF(false);
            mParent.endRecord();
            PApplet.println("saving to pdf – done");
        }

        mParent.popMatrix();
        ViewUtilities.drawGUI(getControlP5());
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
            mLock.acquireUninterruptibly();
            mImage = new ImageStore(mBuffer, mSelectedRev);
            mBufferedImages.add(mImage);
            Collections.sort(mBufferedImages, mImage);
            mLock.release();
            ((SmallMultiplesControl)mControl).releaseLock();
            // LOGWRAPPER.debug("[draw()]: Available permits: " + mLock.availablePermits());
        } else if (paramEvent.getPropertyName().equals("newRev")) {
            assert paramEvent.getNewValue() instanceof Long;
            mSelectedRev = (Long)paramEvent.getNewValue();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void setup() {
    }

    /** {@inheritDoc} */
    @Override
    public void relocate() {
    }

    /** Stores an image buffer with it's revision for sorting. */
    private static final class ImageStore implements Comparator {

        /** {@link PGraphics} to buffer {@link SunburstItem}. */
        final PGraphics mBufferedImage;

        /** Revision. */
        final long mRevision;

        /**
         * Constructor.
         * 
         * @param paramBuffer
         *              {@link PGraphics} reference
         * @param paramRevision
         *              current revision
         */
        ImageStore(final PGraphics paramBuffer, final long paramRevision) {
            assert paramBuffer != null;
            assert paramRevision >= 0;
            mBufferedImage = paramBuffer;
            mRevision = paramRevision;
        }

        /** {@inheritDoc} */
        @Override
        public int compare(final Object paramFirstObject, final Object paramSecondObject) {
            if (((ImageStore)paramFirstObject).mRevision < ((ImageStore)paramSecondObject).mRevision) {
                return -1;
            } else if (((ImageStore)paramFirstObject).mRevision > ((ImageStore)paramSecondObject).mRevision) {
                return 1;
            } else {
                return 0;
            }
        }

    }
}
