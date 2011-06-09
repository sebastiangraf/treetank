/**
 * 
 */
package org.treetank.gui.view.smallmultiples;

import org.treetank.gui.ReadDB;
import org.treetank.gui.controls.AbsControl;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.model.IModel;
import org.treetank.gui.view.smallmultiples.SmallMultiplesView.Embedded;
import org.treetank.gui.view.sunburst.AbsSunburstGUI;
import org.treetank.gui.view.sunburst.SunburstControl;
import org.treetank.gui.view.sunburst.control.AbsSunburstControl;

import processing.core.PApplet;

/**
 * Controller for the SmallMultiples view.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public class SmallMultiplesControl extends AbsSunburstControl {

    /**
     * @param paramParent
     * @param paramModel
     * @param paramDb
     */
    public SmallMultiplesControl(PApplet paramParent, IModel paramModel, ReadDB paramDb) {
        super(paramParent, paramModel, paramDb);
        // TODO Auto-generated constructor stub
    }

    IProcessingGUI mGUI;

    /**
     * @param embedded
     * @param mModel
     * @param mDB
     * @return
     */
    public static SmallMultiplesControl getInstance(Embedded embedded, SmallMultiplesModel mModel, ReadDB mDB) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    public void refreshUpdate() {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    @Override
    public IModel getModel() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.treetank.gui.view.sunburst.control.AbsSunburstControl#getGUIInstance()
     */
    @Override
    protected AbsSunburstGUI getGUIInstance() {
        // TODO Auto-generated method stub
        return null;
    }
}
