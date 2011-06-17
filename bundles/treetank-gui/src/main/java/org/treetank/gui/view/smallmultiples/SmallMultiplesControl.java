/**
 * 
 */
package org.treetank.gui.view.smallmultiples;

import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
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

/**
 * Controller for the SmallMultiples view.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SmallMultiplesControl extends AbsSunburstControl {

    /** {@link SmallMultiplesControl} singleton instance. */
    private static SmallMultiplesControl mControl;

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
        
        // Initial traversal of tree.
        mModel.traverseTree(new SunburstContainer(getGUIInstance()).setStartKey(mDb.getNodeKey())
            .setPruning(EPruning.FALSE).setModWeight(getGUIInstance().getModificationWeight()).setRevision(1)
            .setDepth(0));
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
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    protected AbsSunburstGUI getGUIInstance() {
        return SmallMultiplesGUI.getInstance(mParent, this, mDb);
    }
}
