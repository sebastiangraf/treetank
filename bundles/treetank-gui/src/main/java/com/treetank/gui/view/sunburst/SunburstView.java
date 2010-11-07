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

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

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
    private transient PApplet mEmbed;

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
        // revalidate();
        // final Window window = SwingUtilities.getWindowAncestor(this);
        // if (window != null) {
        // window.validate();
        // }
    }

    @Override
    public void refreshUpdate() {
        mEmbed.draw();
        // revalidate();
        // final Window window = SwingUtilities.getWindowAncestor(this);
        // if (window != null) {
        // window.validate();
        // }
    }

    @Override
    public void dispose() {
    }

    /** Embedded processing view. */
    private final class Embedded extends PApplet {
        /** {@link SunburstGUI} which represents the GUI interface of the Sunburst view. */
        private SunburstGUI mGUI;

        /** {@link List} of {@link SunburstItem}s. */
        private transient List<SunburstItem> mItems;

        @Override
        public void setup() {
            final SunburstController<SunburstModel, SunburstGUI> controller =
                new SunburstController<SunburstModel, SunburstGUI>();
            final SunburstModel model = new SunburstModel(this, mDB, controller);
            mItems = model.traverseTree();

            // Create GUI.
            mGUI = SunburstGUI.createGUI(this, controller);

            // Add components to controller.
            controller.addView(mGUI);
            controller.addModel(model);

            // Setup GUI and draw initial sunburst items.
            mGUI.setupGUI();

            // Prevent thread from starving everything else.
            // noLoop();
        }

        @Override
        public void draw() {
            if (mGUI != null) {
                mGUI.draw();

                for (final SunburstItem item : mItems) {
                    item.update(mGUI.getMappingMode());
                }
            }
        }

        @Override
        public void mouseEntered(final MouseEvent paramEvent) {
            if (mGUI != null) {
                mGUI.mouseEntered(paramEvent);
            }
        }

        @Override
        public void mouseExited(final MouseEvent paramEvent) {
            if (mGUI != null) {
                mGUI.mouseExited(paramEvent);
            }
        }

        @Override
        public void keyReleased() {
            if (mGUI != null) {
                mGUI.keyReleased();
            }
        }

        @Override
        public void mousePressed(final MouseEvent paramEvent) {
            super.mousePressed(paramEvent);

            // Update the screen (run draw once).
            redraw();
        }
    }
}
