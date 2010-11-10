/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.gui.view.sunburst;

import java.awt.Container;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import com.treetank.api.IReadTransaction;
import com.treetank.gui.GUIProp;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.IView;
import com.treetank.gui.view.ViewNotifier;

import processing.core.PApplet;

/**
 * <h1>SunburstView</h1>
 * 
 * <p>
 * Main sunburst class.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SunburstView extends JScrollPane implements IView {

    /** {@link SunburstView} instance. */
    private static SunburstView mView;

    /** {@link ViewNotifier} to notify views of changes. */
    private final ViewNotifier mNotifier;

    /** {@link ReadDB} instance to interact with Treetank. */
    private transient ReadDB mDB;

    /** Processing {@link PApplet} reference. */
    private transient Embedded mEmbed;

    /** This container. */
    private final JComponent mContainer = this;

    /**
     * Constructor.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} instance.
     */
    private SunburstView(final ViewNotifier paramNotifier) {
        mNotifier = paramNotifier;

        // Add view to notifier.
        mNotifier.add(this);

        // Simple scroll mode, because we are adding a heavyweight component (PApplet to the JScrollPane).
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        mEmbed = new Embedded();
    }

    /**
     * Singleton factory method.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} to notify views of changes etc.pp.
     * @return {@link SunburstView} instance.
     */
    public static SunburstView createInstance(final ViewNotifier paramNotifier) {
        if (mView == null) {
            mView = new SunburstView(paramNotifier);
        }

        return mView;
    }

    @Override
    public boolean isVisible() {
        return GUIProp.EShowViews.SHOWSUNBURST.getValue();
    }

    @Override
    public void refreshInit() {
        mDB = mNotifier.getGUI().getReadDB();
        setViewportView(mEmbed);

        /*
         * Important to call this whenever embedding a PApplet.
         * It ensures that the animation thread is started and
         * that other internal variables are properly set.
         */
        mEmbed.init();
    }

    @Override
    public void refreshUpdate() {
        mEmbed.refreshUpdate();
    }

    @Override
    public void dispose() {
        if (mEmbed != null) {
            mEmbed.noLoop();
        }
    }

    /** Embedded processing view. */
    private final class Embedded extends PApplet {
        /** {@link SunburstGUI} which represents the GUI interface of the Sunburst view. */
        private SunburstGUI mGUI;

        /** The Treetank {@link SunburstModel}. */
        private transient SunburstModel mModel;

        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRtx;

        /** {@link SunburstController} used for communication between models and views. */
        private transient SunburstController<SunburstModel, SunburstGUI> mController;

        @Override
        public void setup() {
            size(getSketchWidth(), getSketchHeight(), P2D);
            mController = new SunburstController<SunburstModel, SunburstGUI>();
            mModel = new SunburstModel(this, mDB, mController);

            // Create GUI.
            mGUI = SunburstGUI.createGUI(this, mController);

            // Add components to controller.
            mController.addView(mGUI);
            mController.addModel(mModel);

            // Setup GUI and draw initial sunburst items.
            mGUI.setupGUI();
            handleHLWeight();

            mRtx = mDB.getRtx();

            refreshUpdate();

            // Prevent thread from starving everything else.
            // noLoop();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void draw() {
            if (mGUI != null) {
                final List<SunburstItem> items = (List<SunburstItem>)mController.get("Items");
                for (final SunburstItem item : items) {
                    item.update(mGUI.getMappingMode());
                }
                mGUI.draw();
                handleHLWeight();
            }
        }

        @Override
        public void mouseEntered(final MouseEvent paramEvent) {
            if (mGUI != null) {
                mGUI.mouseEntered(paramEvent);
                handleHLWeight();
            }
        }

        @Override
        public void mouseExited(final MouseEvent paramEvent) {
            if (mGUI != null) {
                mGUI.mouseExited(paramEvent);
                handleHLWeight();
            }
        }

        @Override
        public void keyReleased() {
            if (mGUI != null) {
                mGUI.keyReleased();
                handleHLWeight();
            }
        }

        // @Override
        // public void mousePressed(final MouseEvent paramEvent) {
        // super.mousePressed(paramEvent);
        //
        // // Update the screen (run draw once).
        // redraw();
        // handleHLWeight();
        // }

        /** Refresh. */
        public void refreshUpdate() {
            if (mModel != null) {
                final List<SunburstItem> items = mModel.traverseTree(mRtx.getNode().getNodeKey());
                for (final SunburstItem item : items) {
                    item.update(mGUI.getMappingMode());
                }
            }
        }

        /** Handle mix of heavyweight ({@link PApplet}) and leightweight ({@link JMenuBar}) components. */
        private void handleHLWeight() {
            final Container parent = mContainer.getParent();
            if (parent instanceof JComponent) {
                ((JComponent)parent).revalidate();
            }
            final Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.validate();
            }
        }
    }
}
