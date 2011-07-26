/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.gui.view.smallmultiples;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import org.slf4j.LoggerFactory;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.GUI;
import org.treetank.gui.GUIProp;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IView;
import org.treetank.gui.view.IVisualItem;
import org.treetank.gui.view.ProcessingEmbeddedView;
import org.treetank.gui.view.ViewNotifier;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.sunburst.SunburstControl;
import org.treetank.gui.view.sunburst.model.SunburstModel;
import org.treetank.utils.LogWrapper;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * SmallMultiples view.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SmallMultiplesView extends JScrollPane implements IView {

    /**
     * SerialUID.
     */
    private static final long serialVersionUID = 1L;

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(
        LoggerFactory.getLogger(SmallMultiplesView.class));

    /** Name of the view. */
    private static final String NAME = "SmallMultiplesView";

    /** {@link SunburstView} instance. */
    private static SmallMultiplesView mView;

    /** {@link ViewNotifier} to notify views of changes. */
    private final ViewNotifier mNotifier;

    /** {@link ReadDB} instance to interact with Treetank. */
    private transient ReadDB mDB;

    /** {@link GUI} reference. */
    private final GUI mGUI;

    /** Embedded processing view. */
    private transient Embedded mEmbed;

    /**
     * Constructor.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} instance.
     */
    private SmallMultiplesView(final ViewNotifier paramNotifier) {
        assert paramNotifier != null;
        mNotifier = paramNotifier;

        // Add view to notifier.
        mNotifier.add(this);

        // Main GUI frame.
        mGUI = mNotifier.getGUI();

        // Simple scroll mode, because we are adding a heavyweight component (PApplet to the JScrollPane).
        getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Create instance of processing innerclass.
        mEmbed = new Embedded(this, mNotifier);

        mGUI.addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                updateWindowSize();
            }
        });

        mGUI.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(final ComponentEvent paramEvt) {
                updateWindowSize();
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
     * @return {@link SmallMultiplesView} instance
     */
    public static synchronized SmallMultiplesView getInstance(final ViewNotifier paramNotifier) {
        if (mView == null) {
            mView = new SmallMultiplesView(paramNotifier);
        }

        return mView;
    }

    /** Update window size. */
    void updateWindowSize() {
        assert mEmbed != null;
        assert mGUI != null;
        final Dimension dim = mGUI.getSize();
        getViewport().setSize(dim.width, dim.height - 42);
        if (mEmbed.focused) {
            mEmbed.size(dim.width, dim.height - 42, PConstants.JAVA2D);
            // mEmbed.mControl.refreshTraversal();
        }
        // mEmbed.update();
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
        return GUIProp.EShowViews.SHOWSMALLMULTIPLES.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return NAME;
    }

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
        mDB = mNotifier.getGUI().getReadDB();
        setViewportView(mEmbed);

        /*
         * Important to call this whenever embedding a PApplet.
         * It ensures that the animation thread is started and
         * that other internal variables are properly set.
         */
        mEmbed.init();
        mEmbed.refreshInit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshUpdate() {
        try {
            mDB = ViewUtilities.refreshResource(mDB);
        } catch (final AbsTTException e) {
            e.printStackTrace();
        }
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
        return new Dimension(parentFrame.width, parentFrame.height - 21);
    }

    /** Embedded processing view. */
    final class Embedded extends PApplet {

        /**
         * Serial UID.
         */
        private static final long serialVersionUID = 1L;

        /** The Treetank {@link SunburstModel}. */
        private transient SmallMultiplesModel mModel;

        /** {@link ProcessingEmbeddedView} reference. */
        private transient ProcessingEmbeddedView mEmbeddedView;

        /** Reference on {@link IView} implementation. */
        private final IView mView;

        /** {@link ViewNotifier} reference. */
        private final ViewNotifier mViewNotifier;

        /** {@link SmallMultiplesControl} reference. */
        private transient SmallMultiplesControl mControl;

        /**
         * Constructor.
         * 
         * @param paramView
         * @param paramViewNotifier
         */
        public Embedded(final IView paramView, final ViewNotifier paramViewNotifier) {
            assert paramView != null;
            assert paramViewNotifier != null;
            mView = paramView;
            mViewNotifier = paramViewNotifier;
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            size(mViewNotifier.getGUI().getSize().width, mViewNotifier.getGUI().getSize().height - 42,
                PConstants.JAVA2D);
        }

        /** Setup processing view. */
        public void refreshInit() {
            // Initialization with no draw() loop.
            noLoop();

            // Frame rate reduced to 5 per sec.
            frameRate(5);

            // Create Model.
            mModel = new SmallMultiplesModel(this, mDB);

            // Create Controller.
            mControl = SmallMultiplesControl.getInstance(this, mModel, mDB);

            // Use embedded view.
            mEmbeddedView =
                ProcessingEmbeddedView.getInstance(mView, mControl.getGUIInstance(), mControl, mViewNotifier);
        }

        /** {@inheritDoc} */
        @Override
        public void draw() {
            if (mEmbeddedView != null) {
                mEmbeddedView.draw();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mouseEntered(final MouseEvent paramEvent) {
            if (mEmbeddedView != null) {
                mEmbeddedView.mouseEntered(paramEvent);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mouseExited(final MouseEvent paramEvent) {
            if (mEmbeddedView != null) {
                mEmbeddedView.mouseExited(paramEvent);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void keyReleased() {
            if (mEmbeddedView != null) {
                mEmbeddedView.keyReleased();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void mousePressed(final MouseEvent paramEvent) {
            if (mEmbeddedView != null) {
                mEmbeddedView.mousePressed(paramEvent);
            }
        }

        /** Refresh. */
        void refreshUpdate() {
            mControl.refreshUpdate();
            mEmbeddedView.handleHLWeight();
        }

        /** Refresh. Thus Treetank storage has been updated to a new revision. */
        void refresh() {
            mNotifier.update();
        }

        /** Update Processing GUI. */
        void update() {
            if (mEmbeddedView != null) {
                mEmbeddedView.updateGUI();
            }
        }

        /**
         * Get controller.
         * 
         * @return {@link SmallMultiplesControl} reference
         */
        SmallMultiplesControl getController() {
            return mControl;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void hover(final IVisualItem paramItem) {
        mNotifier.hover(paramItem);
    }
}
