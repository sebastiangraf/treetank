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
    public SunburstView(final ViewNotifier paramNotifier) {
        mNotifier = paramNotifier;

        // Add view to notifier.
        mNotifier.add(this);

        // Simple scroll mode, because we are adding a heavyweight component (PApplet to the JScrollPane).
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    }

    @Override
    public boolean isVisible() {
        return GUIProp.EShowViews.SHOWSUNBURST.getValue();
    }

    @Override
    public void refreshInit() {
        mDB = mNotifier.getGUI().getReadDB();
        mEmbed = new Embedded();
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
        mEmbed.noLoop();
    }

    /** Embedded processing view. */
    private final class Embedded extends PApplet {
        /** {@link SunburstGUI} which represents the GUI interface of the Sunburst view. */
        private SunburstGUI mGUI;

        /** The Treetank {@link SunburstModel}. */
        private transient SunburstModel mModel;
        
        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRtx;

        @Override
        public void setup() {
            size(getSketchWidth(), getSketchHeight(), P2D);
            final SunburstController<SunburstModel, SunburstGUI> controller =
                new SunburstController<SunburstModel, SunburstGUI>();
            mModel = new SunburstModel(this, mDB, controller);

            // Create GUI.
            mGUI = SunburstGUI.createGUI(this, controller);

            // Add components to controller.
            controller.addView(mGUI);
            controller.addModel(mModel);

            // Setup GUI and draw initial sunburst items.
            mGUI.setupGUI();
            handleHLWeight();
            
            mRtx = mDB.getRtx();
            
            refreshUpdate();

            // Prevent thread from starving everything else.
            // noLoop();
        }

        @Override
        public void draw() {
            if (mGUI != null) {
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
