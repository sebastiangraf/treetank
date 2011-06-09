/**
 * 
 */
package org.treetank.gui.view.sunburst;

import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import controlP5.ControlEvent;
import controlP5.ControlListener;

import org.datanucleus.util.ViewUtils;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.controls.AbsControl;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.model.AbsModel;
import org.treetank.gui.view.model.IModel;
import org.treetank.gui.view.sunburst.SunburstView.Embedded;
import org.treetank.gui.view.sunburst.control.AbsSunburstControl;
import org.treetank.gui.view.sunburst.model.SunburstCompareModel;
import org.treetank.gui.view.sunburst.model.SunburstModel;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Controller for the {@link SunburstView}.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SunburstControl extends AbsSunburstControl {

    /** Path to save visualization as a PDF or PNG file. */
    private static final String SAVEPATH = "target" + File.separator;

    /** {@link SunburstControl} instance. */
    private static SunburstControl mControl;

    /** {@link SunburstGUI} instance. */
    private final SunburstGUI mSunburstGUI;

    /**
     * Constructor.
     * 
     * @param paramParent
     *            parent processing {@link PApplet}
     * @param paramModel
     *            an {@link IModel} implementation
     * @param paramDb
     *            {@link ReadDB} instance
     */
    private SunburstControl(final PApplet paramParent, final IModel paramModel, final ReadDB paramDb) {
        super(paramParent, paramModel, paramDb);
        assert paramParent != null;
        mSunburstGUI = (SunburstGUI)getGUI();
    }

    /**
     * Get instance.
     * 
     * @param paramParent
     *            parent processing {@link PApplet}
     * @param paramModel
     *            an {@link IModel} implementation
     * @param paramDb
     *            {@link ReadDB} instance
     * @return {@link SunburstControl} instance
     */
    public static synchronized SunburstControl getInstance(final PApplet paramParent,
        final IModel paramModel, final ReadDB paramDb) {
        assert paramParent != null;
        assert paramModel != null;
        assert paramDb != null;

        if (mControl == null) {
            mControl = new SunburstControl(paramParent, paramModel, paramDb);
        }

        return mControl;
    }

    /** {@inheritDoc} */
    @Override
    protected AbsSunburstGUI getGUIInstance() {
        return SunburstGUI.getInstance(mParent, this, mDb);
    }

    /** {@inheritDoc} */
    @Override
    public void controlEvent(final ControlEvent paramControlEvent) {
        super.controlEvent(paramControlEvent);
        if (paramControlEvent.isGroup()) {
            if (paramControlEvent.group().name().equals("Compare revision")) {
                mSunburstGUI.mParent.noLoop();
                mSunburstGUI.mSelectedRev = (int)paramControlEvent.group().value();
                mModel = new SunburstCompareModel(mSunburstGUI.mParent, mSunburstGUI.mDb);
                mModel.addPropertyChangeListener(mSunburstGUI);
                final SunburstContainer container = new SunburstContainer();
                if (mSunburstGUI.mUsePruning) {
                    container.setPruning(EPruning.TRUE);
                } else {
                    container.setPruning(EPruning.FALSE);
                }
                mModel.traverseTree(container.setRevision(mSunburstGUI.mSelectedRev).setModWeight(
                    mSunburstGUI.mModificationWeight));
            }
        }
    }

    /**
     * Is getting called from processings keyRealeased-method and implements it.
     * 
     * @see processing.core.PApplet#keyReleased()
     */
    @Override
    public void keyReleased() {
        if (!mSunburstGUI.mXPathField.isFocus() && !mSunburstGUI.mCtrl.isOpen()) {
            switch (mSunburstGUI.mParent.key) {
            case 'r':
            case 'R':
                mSunburstGUI.getZoomer().reset();
                mSunburstGUI.mZoomPanReset = true;
                break;
            case 's':
            case 'S':
                // Save PNG.
                mSunburstGUI.mParent.saveFrame(SAVEPATH + ViewUtilities.timestamp() + "_##.png");
                break;
            case 'p':
            case 'P':
                // Save PDF.
                mSunburstGUI.mSavePDF = true;
                PApplet.println("\n" + "saving to pdf – starting");
                mSunburstGUI.mParent.beginRecord(PConstants.PDF, SAVEPATH + ViewUtilities.timestamp() + ".pdf");
                mSunburstGUI.mParent.textMode(PConstants.SHAPE);
                mSunburstGUI.mParent.textFont(mSunburstGUI.mParent.createFont("src" + File.separator + "main" + File.separator
                    + "resources" + File.separator + "data" + File.separator + "miso-regular.ttf", 15));
                break;
            case '\b':
                // Backspace.
                mModel.undo();
                mSunburstGUI.update();
                break;
            case '1':
                mSunburstGUI.mMappingMode = 1;
                break;
            case '2':
                mSunburstGUI.mMappingMode = 2;
                break;
            case '3':
                mSunburstGUI.mMappingMode = 3;
                break;
            case 'o':
            case 'O':
                if (!mSunburstGUI.mUseDiffView) {
                    mSunburstGUI.mRevisions =
                        mSunburstGUI.getControlP5().addDropdownList("Compare revision", mSunburstGUI.mParent.width - 250, 100,
                            100, 120);
                    assert mSunburstGUI.mDb != null;
                    try {
                        for (long i = mSunburstGUI.mDb.getRevisionNumber() + 1, newestRev =
                            mSunburstGUI.mDb.getSession().beginReadTransaction().getRevisionNumber(); i <= newestRev; i++) {
                            mSunburstGUI.mRevisions.addItem("Revision " + i, (int)i);
                        }
                    } catch (final AbsTTException exc) {
                        exc.printStackTrace();
                    }
                }
                break;
            default:
                // Do nothing.
            }

            switch (mSunburstGUI.mParent.key) {
            case '1':
            case '2':
            case '3':
                mSunburstGUI.update();
                break;
            case 'm':
            case 'M':
                mSunburstGUI.mShowGUI = mSunburstGUI.getControlP5().group("menu").isOpen();
                mSunburstGUI.mShowGUI = !mSunburstGUI.mShowGUI;
                break;
            default:
                // No action.
            }

            if (mSunburstGUI.mShowGUI) {
                mSunburstGUI.getControlP5().group("menu").open();
            } else {
                mSunburstGUI.getControlP5().group("menu").close();
            }

            if (mSunburstGUI.mParent.keyCode == PConstants.RIGHT) {
                mSunburstGUI.mRad += 5;
                mSunburstGUI.mRadChanged = true;
            } else if (mSunburstGUI.mParent.keyCode == PConstants.LEFT) {
                mSunburstGUI.mRad -= 5;
                mSunburstGUI.mRadChanged = true;
            }
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
        if (mSunburstGUI.mDone) {
            mSunburstGUI.mParent.loop();
        }
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
        mSunburstGUI.mParent.noLoop();
    }

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
        mSunburstGUI.getControlP5().controlWindow.mouseEvent(paramEvent);
        mSunburstGUI.getZoomer().mouseEvent(paramEvent);

        mSunburstGUI.mShowGUI = mSunburstGUI.getControlP5().group("menu").isOpen();

        if (!mSunburstGUI.mShowGUI) {
            boolean doMouseOver = true;
            if (mSunburstGUI.mRevisions != null && mSunburstGUI.mRevisions.isOpen()) {
                doMouseOver = false;
            }

            if (doMouseOver) {
                // Mouse rollover.
                if (!mSunburstGUI.mParent.keyPressed) {
                    mSunburstGUI.rollover();

                    if (mSunburstGUI.mHitTestIndex != -1) {
                        // Bug in processing's mousbotton, thus used SwingUtilities.
                        if (SwingUtilities.isLeftMouseButton(paramEvent) && !mSunburstGUI.mCtrl.isOpen()) {
                            final SunburstContainer container = new SunburstContainer();
                            if (mSunburstGUI.mUsePruning) {
                                container.setPruning(EPruning.TRUE);
                            } else {
                                container.setPruning(EPruning.FALSE);
                            }
                            if (mSunburstGUI.mUseDiffView) {
                                final SunburstItem item = (SunburstItem)mModel.getItem(mSunburstGUI.mHitTestIndex);
                                if (item.mDiff == EDiff.SAME) {
                                    mSunburstGUI.mDone = false;
                                    mModel.update(container.setAll(mSunburstGUI.mSelectedRev, item.getDepth(),
                                        mSunburstGUI.mModificationWeight).setStartKey(item.getNode().getNodeKey()));
                                }
                            } else {
                                mSunburstGUI.mDone = false;
                                final SunburstItem item = (SunburstItem)mModel.getItem(mSunburstGUI.mHitTestIndex);
                                mModel.update(container.setStartKey(item.getNode().getNodeKey()));
                            }
                        } else if (SwingUtilities.isRightMouseButton(paramEvent)) {
                            if (!mSunburstGUI.mUseDiffView) {
                                try {
                                    ((SunburstModel)mModel).popupMenu(paramEvent, mSunburstGUI.mCtrl,
                                        mSunburstGUI.mHitTestIndex);
                                } catch (final AbsTTException exc) {
                                    exc.printStackTrace();
                                    JOptionPane.showMessageDialog(mSunburstGUI.mParent, "Failed to commit change: "
                                        + exc.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * XPath expression.
     * 
     * @param paramXPath
     *            the XPath expression
     */
    public void xpath(final String paramXPath) {
        mModel.evaluateXPath(paramXPath);
    }

    /**
     * Method to process event for cancel-button.
     * 
     * @param paramValue
     *            change value
     */
    @Override
    public void cancel(final int paramValue) {
        mSunburstGUI.mTextArea.clear();
        mSunburstGUI.mCtrl.setVisible(false);
    }

    /**
     * Method to process event for submit-button.
     * 
     * @param paramValue
     *            change value
     * @throws FactoryConfigurationError
     *             if something odd happens
     * @throws XMLStreamException
     *             if the XML fragment isn't well formed
     */
    @Override
    public void submit(final int paramValue) throws XMLStreamException {
        try {
            assert mModel instanceof SunburstModel;
            mSunburstGUI.mCtrl.setVisible(false);
            mSunburstGUI.mCtrl.setOpen(false);
            ((SunburstModel)mModel).addXMLFragment(mSunburstGUI.mTextArea.getText());
            mSunburstGUI.mTextArea.clear();
        } catch (final FactoryConfigurationError exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(mSunburstGUI.mParent, "Failed to commit change: " + exc.getMessage());
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(mSunburstGUI.mParent, "Failed to commit change: " + exc.getMessage());
        }
    }

    /**
     * Method to process event for commit-button.
     * 
     * @param paramValue
     *            change value
     * @throws XMLStreamException
     *             if the XML fragment isn't well formed
     */
    @Override
    public void commit(final int paramValue) throws XMLStreamException {
        try {
            assert mModel instanceof SunburstModel;
            mSunburstGUI.mCtrl.setVisible(false);
            mSunburstGUI.mCtrl.setOpen(false);
            ((SunburstModel)mModel).addXMLFragment(mSunburstGUI.mTextArea.getText());
            ((SunburstModel)mModel).commit();
            mSunburstGUI.mTextArea.clear();
        } catch (final FactoryConfigurationError exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(mSunburstGUI.mParent, "Failed to commit change: " + exc.getMessage());
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(mSunburstGUI.mParent, "Failed to commit change: " + exc.getMessage());
        }
        ((Embedded)mSunburstGUI.mParent).refresh();
    }

    /**
     * Refresh storage after an update.
     * 
     * @param paramDB
     *            {@link ReadDB} instance
     */
    public void refreshUpdate(final ReadDB paramDB) {
        assert paramDB != null;
        mDb = paramDB;
        mSunburstGUI.mDone = false;
        mSunburstGUI.mUseDiffView = false;
        final SunburstContainer container = new SunburstContainer().setStartKey(mDb.getNodeKey());
        if (mSunburstGUI.mUsePruning) {
            container.setPruning(EPruning.TRUE);
        } else {
            container.setPruning(EPruning.FALSE);
        }
        mModel.updateDb(mDb, container);
        mSunburstGUI.updateDb(mDb);
    }

    /** {@inheritDoc} */
    @Override
    public IModel getModel() {
        return mModel;
    }
}
