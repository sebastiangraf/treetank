/**
 * 
 */
package org.treetank.gui.view.smallmultiples;

import java.io.File;
import java.util.concurrent.Semaphore;

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
        refreshTraversal();
    }

    /**
     * Refresh traversal of the model.
     */
    public void refreshTraversal() {
        try {
            mSmallMultiplesGUI.getBufferedImages().clear();
            final IReadTransaction rtx = mDb.getSession().beginReadTransaction();
            final long lastRevision = rtx.getRevisionNumber();
            rtx.close();
            
            for (long j = mDb.getRevisionNumber() + 1, i = mDb.getRevisionNumber() + 1; i < lastRevision && i < j + 4; i++) {
                mContainer = new SunburstContainer(getGUIInstance());
                mModel.traverseTree(mContainer.setLock(mLock).setStartKey(mDb.getNodeKey())
                    .setPruning(EPruning.FALSE).setModWeight(getGUIInstance().getModificationWeight())
                    .setRevision(i).setDepth(0));
            }
        } catch (final AbsTTException e) {

        }
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
            mSmallMultiplesGUI.getParent().saveFrame(ViewUtilities.SAVEPATH + ViewUtilities.timestamp()
                + "_##.png");
            break;
        case 'p':
        case 'P':
            // Save PDF.
            mSmallMultiplesGUI.setSavePDF(true);
            PApplet.println("\n" + "saving to pdf – starting");
            mSmallMultiplesGUI.getParent().beginRecord(PConstants.PDF,
                ViewUtilities.SAVEPATH + ViewUtilities.timestamp() + ".pdf");
            mSmallMultiplesGUI.getParent().textMode(PConstants.SHAPE);
            mSmallMultiplesGUI.getParent().textFont(mSmallMultiplesGUI.getParent().createFont("src" + File.separator
                + "main" + File.separator + "resources" + File.separator + "data" + File.separator
                + "miso-regular.ttf", 15));
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
}
