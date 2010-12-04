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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import com.treetank.api.IReadTransaction;
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
    
    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(Embedded.class));

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

        // Create instance of processing innerclass.
        mEmbed = new Embedded();

        mNotifier.getGUI().addComponentListener(new ComponentListener() {
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
     * @return {@link SunburstView} instance.
     */
    public static SunburstView createInstance(final ViewNotifier paramNotifier) {
        if (mView == null) {
            mView = new SunburstView(paramNotifier);
        }

        return mView;
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

    /** Embedded processing view. */
    final class Embedded extends PApplet implements PropertyChangeListener {
        /** {@link SunburstGUI} which represents the GUI interface of the Sunburst view. */
        private SunburstGUI mGUI;

        /** The Treetank {@link SunburstModel}. */
        private transient SunburstModel mModel;

        /** Treetank {@link IReadTransaction}. */
        private transient IReadTransaction mRtx;

        /** {@link List} of {@link SunburstItem}s. */
        private transient List<SunburstItem> mItems;

        /** Flag to indicate that it's not drawing while updating. */
        private transient Lock mLock = new ReentrantLock();

        /** {@inheritDoc} */
        @Override
        public void setup() {
            // Create Model.
            mModel = new SunburstModel(this, mDB);

            // Create GUI.
            mGUI = SunburstGUI.createGUI(this, mModel);

            // Setup GUI and draw initial sunburst items.
            mGUI.setupGUI();
            handleHLWeight();

            mRtx = mDB.getRtx();

            mModel.traverseTree(mRtx.getNode().getNodeKey(), mGUI.mTextWeight);
            //            
            // mModel.traverseCompareTree(mRtx.getRevisionNumber() + 1, mRtx.getNode().getNodeKey(), 1000,
            // 0.5f,
            // mGUI.mTextWeight);
        }

        /** {@inheritDoc} */
        @Override
        public void draw() {
            if (mGUI != null && mItems != null) {
                mLock.lock();
                LOGWRAPPER.debug("drawing");
                for (final SunburstItem item : mItems) {
                    item.update(mGUI.getMappingMode());
                }
                mGUI.draw();
                handleHLWeight();
                mLock.unlock();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mouseEntered(final MouseEvent paramEvent) {
            if (mGUI != null) {
                mGUI.mouseEntered(paramEvent);
                handleHLWeight();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mouseExited(final MouseEvent paramEvent) {
            if (mGUI != null) {
                mGUI.mouseExited(paramEvent);
                handleHLWeight();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void keyReleased() {
            if (mGUI != null) {
                mGUI.keyReleased();
                handleHLWeight();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mousePressed(final MouseEvent paramEvent) {
            if (mGUI != null) {
                mGUI.mousePressed(paramEvent);
                handleHLWeight();
            }
        }

        /** Refresh. */
        public void refreshUpdate() {
            if (mGUI != null && mModel != null && mItems != null) {
                noLoop();
                mLock.lock();
                LOGWRAPPER.info("refreshUpdate");
                mModel = new SunburstModel(this, mDB);
                mGUI = mGUI.refresh(this, mModel);
                mGUI.setupGUI();
                handleHLWeight();
                mRtx = mDB.getRtx();
                mModel.traverseTree(mRtx.getNode().getNodeKey(), mGUI.mTextWeight);
                mLock.unlock();
                loop();
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

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        @Override
        public void propertyChange(final PropertyChangeEvent paramEvent) {
            noLoop();
            mLock.lock();
            if (paramEvent.getPropertyName().equals("items")) {
                mItems = (List<SunburstItem>)paramEvent.getNewValue();
            }
            mLock.unlock();
            loop();
        }
    }
}
