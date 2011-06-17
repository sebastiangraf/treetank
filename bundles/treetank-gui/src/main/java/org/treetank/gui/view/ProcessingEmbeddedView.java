/**
 * 
 */
package org.treetank.gui.view;

import java.awt.Container;
import java.awt.Window;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import org.treetank.gui.GUI;
import org.treetank.gui.view.controls.IControl;
import org.treetank.gui.view.sunburst.model.SunburstModel;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Processing view base class.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class ProcessingEmbeddedView extends PApplet {
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    /** Main {@link GUI} reference. */
    private final GUI mGUI;

    /** {@link IProcessingGUI} reference. */
    private final IProcessingGUI mProcessingGUI;

    /** {@link ViewNotifier} reference. */
    private final ViewNotifier mNotifier;

    /** {@link IControl} implementation. */
    private final IControl mControl;

    /** {@link IView} implementation. */
    private final IView mView;

    private static ProcessingEmbeddedView mEmbeddedView;

    /**
     * Constructor.
     * 
     * @param paramGUI
     *            main {@link GUI} reference
     * @param paramProcessingGUI
     *            {@link IProcessingGUI} implementation
     * @param paramControl
     *            {@link IControl} implementation
     * @param paramViewNotifier
     *            {@link ViewNotifier} reference
     */
    private ProcessingEmbeddedView(final IView paramView, final GUI paramGUI,
        final IProcessingGUI paramProcessingGUI, final IControl paramControl,
        final ViewNotifier paramViewNotifier) {
        mView = paramView;
        mGUI = paramGUI;
        mProcessingGUI = paramProcessingGUI;
        mControl = paramControl;
        mNotifier = paramViewNotifier;
    }

    /**
     * Get singleton instance.
     * 
     * @param paramView
     *            the {@link IView} instance, which embeds the processing view
     * @param paramProcessingGUI
     *            {@link IProcessingGUI} implementation
     * @param paramControl
     *            {@link IControl} implementation
     * @param paramViewNotifier
     *            {@link ViewNotifier} reference
     * @return {@link ProcessingEmbeddedView} singleton
     */
    public static synchronized ProcessingEmbeddedView getInstance(final IView paramView,
        final IProcessingGUI paramProcessingGUI, final IControl paramControl,
        final ViewNotifier paramViewNotifier) {
        assert paramView != null;
        assert paramProcessingGUI != null;
        assert paramControl != null;
        assert paramViewNotifier != null;
        if (mEmbeddedView == null) {
            mEmbeddedView =
                new ProcessingEmbeddedView(paramView, paramViewNotifier.getGUI(), paramProcessingGUI,
                    paramControl, paramViewNotifier);
        }
        return mEmbeddedView;
    }

    /** {@inheritDoc} */
    @Override
    public void draw() {
        if (mProcessingGUI != null) {
            mProcessingGUI.draw();
            handleHLWeight();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(final MouseEvent paramEvent) {
        if (mControl != null) {
            mControl.mouseEntered(paramEvent);
            handleHLWeight();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(final MouseEvent paramEvent) {
        if (mControl != null) {
            mControl.mouseExited(paramEvent);
            handleHLWeight();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void keyReleased() {
        if (mControl != null) {
            mControl.keyReleased();
            handleHLWeight();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(final MouseEvent paramEvent) {
        if (mControl != null) {
            mControl.mousePressed(paramEvent);
            handleHLWeight();
        }
    }

    /** Refresh. Thus Treetank storage has been updated to a new revision. */
    public void refresh() {
        mNotifier.update();
    }

    /** Update the GUI. */
    public void updateGUI() {
        if (mProcessingGUI != null) {
            mProcessingGUI.update();
        }
    }

    /** Handle mix of heavyweight ({@link PApplet}) and leightweight ({@link JMenuBar}) components. */
    public void handleHLWeight() {
        final Container parent = mView.component().getParent();
        if (parent instanceof JComponent) {
            ((JComponent)parent).revalidate();
        }
        final Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.validate();
        }
    }
}
