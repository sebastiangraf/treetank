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
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.concurrent.Semaphore;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import com.treetank.gui.GUI;
import com.treetank.gui.GUIProp;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.IView;
import com.treetank.gui.view.ViewNotifier;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

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

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SunburstView.class));

    /** Name of the sunburst view. */
    private static final String NAME = "SunburstView";

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

    /** {@link GUI} reference. */
    private final GUI mGUI;

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

        // Main GUI frame.
        mGUI = mNotifier.getGUI();

        // Simple scroll mode, because we are adding a heavyweight component (PApplet to the JScrollPane).
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        // Create instance of processing innerclass.
        mEmbed = new Embedded();

        mGUI.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(final ComponentEvent paramEvt) {
                mEmbed.loop();
            }

            @Override
            public void componentHidden(final ComponentEvent paramEvt) {

            }

            @Override
            public void componentMoved(final ComponentEvent paramEvt) {

            }

            @Override
            public void componentShown(final ComponentEvent paramEvt) {

            }
        });
    }

    /**
     * Singleton factory method.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} to notify views of changes etc.pp.
     * @return {@link SunburstView} instance
     */
    public static synchronized SunburstView getInstance(final ViewNotifier paramNotifier) {
        if (mView == null) {
            mView = new SunburstView(paramNotifier);
        }

        return mView;
    }

    /**
     * Not supported.
     * 
     * @see Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisible() {
        return GUIProp.EShowViews.SHOWSUNBURST.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return NAME;
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent component() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshInit() {
        setViewportView(mEmbed);

        /*
         * Important to call this whenever embedding a PApplet.
         * It ensures that the animation thread is started and
         * that other internal variables are properly set.
         */
        mEmbed.init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshUpdate() {
        mDB = mNotifier.getGUI().getReadDB();
        mEmbed.refreshUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (mEmbed != null) {
            mEmbed.noLoop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        final Dimension parentFrame = mGUI.getSize();
        return new Dimension(1280, 900);
        // return new Dimension((int)(parentFrame.width - 300), parentFrame.height - 200);
    }

    /** Embedded processing view. */
    final class Embedded extends PApplet {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        /** {@link SunburstGUI} which represents the GUI interface of the Sunburst view. */
        private transient SunburstGUI mGUI;

        /** The Treetank {@link SunburstModel}. */
        private transient SunburstModel mModel;

        /** Lock while initially querying model, thus draw() doesn't have to be invoked. */
        private transient Semaphore mLock = new Semaphore(1);

        /** {@inheritDoc} */
        @Override
        public void setup() {
            if (mGUI != null) {
//                mGUI.setupGUI();
                mGUI.mDone = false;
            }
            size(1280, 900);
            noLoop();
        }

        /** {@inheritDoc} */
        @Override
        public void draw() {
            if (mGUI != null && mGUI.mDone) {
                mLock.tryAcquire();
                LOGWRAPPER.debug("drawing");
                mGUI.draw();
                handleHLWeight();
                mLock.release();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mouseEntered(final MouseEvent paramEvent) {
            if (mGUI != null && mGUI.mDone) {
                mLock.tryAcquire();
                mGUI.mouseEntered(paramEvent);
                handleHLWeight();
                mLock.release();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mouseExited(final MouseEvent paramEvent) {
            if (mGUI != null && mGUI.mDone) {
                mLock.tryAcquire();
                mGUI.mouseExited(paramEvent);
                handleHLWeight();
                mLock.release();
            }
        }

        // /** {@inheritDoc} */
        // @Override
        // public void mouseMoved(final MouseEvent paramEvent) {
        // // draw();
        // if (mGUI != null && mDone) {
        // mLock.tryAcquire();
        // mGUI.mouseMoved(paramEvent);
        // handleHLWeight();
        // mLock.release();
        // }
        // }

        /** {@inheritDoc} */
        @Override
        public void keyReleased() {
            if (mGUI != null && mGUI.mDone) {
                mLock.tryAcquire();
                mGUI.keyReleased();
                handleHLWeight();
                mLock.release();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mousePressed(final MouseEvent paramEvent) {
            if (mGUI != null && mGUI.mDone) {
                mLock.tryAcquire();
                mGUI.mousePressed(paramEvent);
                handleHLWeight();
                mLock.release();
            }
        }

        /** Refresh. */
        void refreshUpdate() {
            try {
                noLoop();
                mLock.acquire();

                frameRate(30);

                // Create Model.
                mModel = new SunburstModel(this, mDB);

                // Create GUI.
                mGUI = SunburstGUI.getInstance(this, mModel, mDB);
                mGUI.mDone = false;
                mGUI.mUseDiffView = false;

                // Traverse.
                handleHLWeight();
                mModel.traverseTree(new SunburstContainer().setKey(mDB.getNodeKey()));
            } catch (final InterruptedException e) {
                LOGWRAPPER.warn(e.getMessage(), e);
            } finally {
                mLock.release();
                loop();
            }
        }

        /** Refresh initialization. Thus Treetank storage has been updated to a new revision. */
        void refreshInit() {
            mNotifier.init();
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
