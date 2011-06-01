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
import org.treetank.gui.view.sunburst.model.AbsModel;
import org.treetank.gui.view.sunburst.model.IModel;
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
public class SunburstControl extends AbsControl implements ControlListener {

    /** Path to save visualization as a PDF or PNG file. */
    private static final String SAVEPATH = "target" + File.separator;

    final SunburstGUI mGUI;

    AbsModel mModel;

    private final ReadDB mDb;

    private static SunburstControl mControl;

    private SunburstControl(final PApplet paramParent, final AbsModel paramModel, final ReadDB paramDb) {
        assert paramModel != null;
        // Create GUI.
        mGUI = SunburstGUI.getInstance(paramParent, this, paramDb);
        mGUI.mControlP5.addListener(this);
        mModel = paramModel;
        mModel.addPropertyChangeListener(mGUI);
        mDb = paramDb;
        // Traverse model.
        mModel.traverseTree(new SunburstContainer().setKey(mDb.getNodeKey()).setPruning(EPruning.FALSE));
    }

    public static synchronized SunburstControl getInstance(final PApplet paramParent,
        final AbsModel paramModel, final ReadDB paramDb) {
        assert paramParent != null;
        assert paramModel != null;
        assert paramDb != null;

        if (mControl == null) {
            mControl = new SunburstControl(paramParent, paramModel, paramDb);
        }

        return mControl;
    }

    /**
     * Is getting called from processings keyRealeased-method and implements it.
     * 
     * @see processing.core.PApplet#keyReleased()
     */
    @Override
    public void keyReleased() {
        if (!mGUI.mXPathField.isFocus() && !mGUI.mCtrl.isOpen()) {
            switch (mGUI.mParent.key) {
            case 'r':
            case 'R':
                mGUI.mZoomer.reset();
                mGUI.mZoomPanReset = true;
                break;
            case 's':
            case 'S':
                // Save PNG.
                mGUI.mParent.saveFrame(SAVEPATH + ViewUtilities.timestamp() + "_##.png");
                break;
            case 'p':
            case 'P':
                // Save PDF.
                mGUI.mSavePDF = true;
                PApplet.println("\n" + "saving to pdf – starting");
                mGUI.mParent.beginRecord(PConstants.PDF, SAVEPATH + ViewUtilities.timestamp() + ".pdf");
                mGUI.mParent.textMode(PConstants.SHAPE);
                mGUI.mParent.textFont(mGUI.mParent.createFont("src" + File.separator + "main"
                    + File.separator + "resources" + File.separator + "data" + File.separator
                    + "miso-regular.ttf", 15));
                break;
            case '\b':
                // Backspace.
                mModel.undo();
                mGUI.update();
                break;
            case '1':
                mGUI.mMappingMode = 1;
                break;
            case '2':
                mGUI.mMappingMode = 2;
                break;
            case '3':
                mGUI.mMappingMode = 3;
                break;
            case 'o':
            case 'O':
                if (!mGUI.mUseDiffView) {
                    mGUI.mRevisions =
                        mGUI.mControlP5.addDropdownList("Compare revision", mGUI.mParent.width - 250, 100,
                            100, 120);
                    assert mGUI.mDb != null;
                    try {
                        for (long i = mGUI.mDb.getRevisionNumber() + 1, newestRev =
                            mGUI.mDb.getSession().beginReadTransaction().getRevisionNumber(); i <= newestRev; i++) {
                            mGUI.mRevisions.addItem("Revision " + i, (int)i);
                        }
                    } catch (final AbsTTException exc) {
                        exc.printStackTrace();
                    }
                }
                break;
            default:
                // Do nothing.
            }

            switch (mGUI.mParent.key) {
            case '1':
            case '2':
            case '3':
                mGUI.update();
                break;
            case 'm':
            case 'M':
                mGUI.mShowGUI = mGUI.mControlP5.group("menu").isOpen();
                mGUI.mShowGUI = !mGUI.mShowGUI;
                break;
            default:
                // No action.
            }

            if (mGUI.mShowGUI) {
                mGUI.mControlP5.group("menu").open();
            } else {
                mGUI.mControlP5.group("menu").close();
            }

            if (mGUI.mParent.keyCode == PConstants.RIGHT) {
                mGUI.mRad += 5;
                mGUI.mRadChanged = true;
            } else if (mGUI.mParent.keyCode == PConstants.LEFT) {
                mGUI.mRad -= 5;
                mGUI.mRadChanged = true;
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
        if (mGUI.mDone) {
            mGUI.mParent.loop();
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
        mGUI.mParent.noLoop();
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
        mGUI.mControlP5.controlWindow.mouseEvent(paramEvent);
        mGUI.mZoomer.mouseEvent(paramEvent);

        mGUI.mShowGUI = mGUI.mControlP5.group("menu").isOpen();

        if (!mGUI.mShowGUI) {
            boolean doMouseOver = true;
            if (mGUI.mRevisions != null && mGUI.mRevisions.isOpen()) {
                doMouseOver = false;
            }

            if (doMouseOver) {
                // Mouse rollover.
                if (!mGUI.mParent.keyPressed) {
                    mGUI.rollover();

                    if (mGUI.mHitTestIndex != -1) {
                        // Bug in processing's mousbotton, thus used SwingUtilities.
                        if (SwingUtilities.isLeftMouseButton(paramEvent) && !mGUI.mCtrl.isOpen()) {
                            final SunburstContainer container = new SunburstContainer();
                            if (mGUI.mUsePruning) {
                                container.setPruning(EPruning.TRUE);
                            } else {
                                container.setPruning(EPruning.FALSE);
                            }
                            if (mGUI.mUseDiffView) {
                                final SunburstItem item = mModel.getItem(mGUI.mHitTestIndex);
                                if (item.mDiff == EDiff.SAME) {
                                    mGUI.mDone = false;
                                    mModel.update(container.setAll(mGUI.mSelectedRev, item.getDepth(),
                                        mGUI.mModificationWeight).setKey(item.getNode().getNodeKey()));
                                }
                            } else {
                                mGUI.mDone = false;
                                final SunburstItem item = mModel.getItem(mGUI.mHitTestIndex);
                                mModel.update(container.setKey(item.getNode().getNodeKey()));
                            }
                        } else if (SwingUtilities.isRightMouseButton(paramEvent)) {
                            if (!mGUI.mUseDiffView) {
                                try {
                                    ((SunburstModel)mModel).popupMenu(paramEvent, mGUI.mCtrl,
                                        mGUI.mHitTestIndex);
                                } catch (final AbsTTException exc) {
                                    exc.printStackTrace();
                                    JOptionPane.showMessageDialog(mGUI.mParent, "Failed to commit change: "
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
     * Called on every change of the GUI.
     * 
     * @param paramControlEvent
     *            the {@link ControlEvent}
     */
    @Override
    public void controlEvent(final ControlEvent paramControlEvent) {
        if (paramControlEvent.isController()) {
            if (paramControlEvent.controller().name().equals("leaf node hue range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                mGUI.mHueStart = f[0];
                mGUI.mHueEnd = f[1];
            }
            if (paramControlEvent.controller().name().equals("leaf node saturation range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                mGUI.mSaturationStart = f[0];
                mGUI.mSaturationEnd = f[1];
            }
            if (paramControlEvent.controller().name().equals("leaf node brightness range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                mGUI.mBrightnessStart = f[0];
                mGUI.mBrightnessEnd = f[1];
            }
            if (paramControlEvent.controller().name().equals("inner node brightness range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                mGUI.mInnerNodeBrightnessStart = f[0];
                mGUI.mInnerNodeBrightnessEnd = f[1];
            }
            if (paramControlEvent.controller().name().equals("inner node stroke brightness range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                mGUI.mInnerNodeStrokeBrightnessStart = f[0];
                mGUI.mInnerNodeStrokeBrightnessEnd = f[1];
            }
            if (paramControlEvent.controller().name().equals("stroke weight range")) {
                final float[] f = paramControlEvent.controller().arrayValue();
                mGUI.mStrokeWeightStart = f[0];
                mGUI.mStrokeWeightEnd = f[1];
            }

            mGUI.update();
        } else if (paramControlEvent.isGroup()) {
            if (paramControlEvent.group().name().equals("Compare revision")) {
                mGUI.mParent.noLoop();
                mGUI.mSelectedRev = (int)paramControlEvent.group().value();
                mModel = new SunburstCompareModel(mGUI.mParent, mGUI.mDb);
                mModel.addPropertyChangeListener(mGUI);
                final SunburstContainer container = new SunburstContainer();
                if (mGUI.mUsePruning) {
                    container.setPruning(EPruning.TRUE);
                } else {
                    container.setPruning(EPruning.FALSE);
                }
                mModel.traverseTree(container.setRevision(mGUI.mSelectedRev).setModWeight(
                    mGUI.mModificationWeight));
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
    public void cancel(final int paramValue) {
        mGUI.mTextArea.clear();
        mGUI.mCtrl.setVisible(false);
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
    public void submit(final int paramValue) throws XMLStreamException {
        try {
            assert mModel instanceof SunburstModel;
            mGUI.mCtrl.setVisible(false);
            mGUI.mCtrl.setOpen(false);
            ((SunburstModel)mModel).shredder(mGUI.mTextArea.getText());
            mGUI.mTextArea.clear();
        } catch (final FactoryConfigurationError exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(mGUI.mParent, "Failed to commit change: " + exc.getMessage());
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(mGUI.mParent, "Failed to commit change: " + exc.getMessage());
        }
    }

    /**
     * Method to process event for submit-button.
     * 
     * @param paramValue
     *            change value
     * @throws XMLStreamException
     *             if the XML fragment isn't well formed
     */
    public void commit(final int paramValue) throws XMLStreamException {
        try {
            assert mModel instanceof SunburstModel;
            mGUI.mCtrl.setVisible(false);
            mGUI.mCtrl.setOpen(false);
            ((SunburstModel)mModel).shredder(mGUI.mTextArea.getText());
            ((SunburstModel)mModel).commit();
            mGUI.mTextArea.clear();
        } catch (final FactoryConfigurationError exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(mGUI.mParent, "Failed to commit change: " + exc.getMessage());
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(mGUI.mParent, "Failed to commit change: " + exc.getMessage());
        }
        mGUI.mParent.refresh();
    }

    /**
     * Refresh storage after an update.
     */
    public void refreshUpdate() {
        // Database change.
        mGUI.mDone = false;
        mGUI.mUseDiffView = false;
        final SunburstContainer container = new SunburstContainer().setKey(mDb.getNodeKey());
        if (mGUI.mUsePruning) {
            container.setPruning(EPruning.TRUE);
        } else {
            container.setPruning(EPruning.FALSE);
        }
        mModel.updateDb(mDb, container);
        mGUI.updateDb(mDb);
    }
}
