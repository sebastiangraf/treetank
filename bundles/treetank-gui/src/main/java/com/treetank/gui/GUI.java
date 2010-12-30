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
package com.treetank.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import com.treetank.exception.TTIOException;
import com.treetank.gui.view.IView;
import com.treetank.gui.view.ViewContainer;
import com.treetank.gui.view.ViewNotifier;
import com.treetank.gui.view.sunburst.SunburstView;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * <h1>Treetank GUI</h1>
 * 
 * <p>
 * Main GUI frame.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class GUI extends JFrame {
    /** Serialization UID. */
    private static final long serialVersionUID = 7396552752125858796L;

    /** {@link LogWrapper} which wraps a Logger. */
    private static final LogWrapper LOGGER = new LogWrapper(LoggerFactory.getLogger(GUI.class));

    /** Optionally set the look and feel. */
    private static boolean mUseSystemLookAndFeel;

    /** Width of the frame. */
    private static final int WIDTH = 1000;

    /** Height of the frame. */
    private static final int HEIGHT = 900;

    // /** {@link GUIProp}. */
    // private final GUIProp mProp; // Will be used in future versions (more GUI properties).

    /** {@link ViewNotifier} to notify all views of changes in the underlying data structure. */
    private final ViewNotifier mNotifier;

    /** {@link ViewContainer} which contains all {@link IView} implementations available. */
    private final ViewContainer mContainer;

    /** {@link ReadDB}. */
    private transient ReadDB mReadDB;

    /**
     * Constructor.
     * 
     * @param paramProp
     *            {@link GUIProp}
     */
    public GUI(final GUIProp paramProp) {
        // mProp = paramProp;

        // ===== Setup GUI ======
        // Title of the frame.
        setTitle("Treetank GUI");

        // Component listener, to revalidate layout manager.
        addComponentListener(new ComponentAdapter() {
            /**
             * Relayout all components.
             * 
             * @param paramEvent
             *            {@link ComponentEvent} reference
             */
            @Override
            public void componentResized(final ComponentEvent paramEvent) {
                mContainer.revalidate();
            }
        });

        // Set default size and close operation.
        final Dimension frameSize = new Dimension(WIDTH, HEIGHT);
        setSize(frameSize);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Add menubar.
        final JMenuBar menuBar = new GUIMenuBar(this);
        setJMenuBar(menuBar);

        // Create Panels.
        final JPanel top = new JPanel();
        top.setLayout(new BorderLayout());

        // Create views.
        mNotifier = new ViewNotifier(this);
        mContainer = ViewContainer.getInstance(this, SunburstView.getInstance(mNotifier));
        mContainer.layoutViews();
        top.add(mContainer, BorderLayout.CENTER);
        getContentPane().add(top);

        // Center the frame.
        setLocationRelativeTo(null);

        // Size the frame.
        pack();

        // Display the window.
        setVisible(true);
    }

    /**
     * Execute command.
     * 
     * @param paramFile
     *            {@link File} to open.
     * @param paramRevision
     *            Determines the revision.
     */
    void execute(final File paramFile, final long paramRevision) {
        if (mReadDB == null || !paramFile.equals(mReadDB.getDatabase().getFile())
            || paramRevision != mReadDB.getRevisionNumber()) {
            mReadDB = new ReadDB(paramFile, paramRevision);
            mNotifier.init();
        }
        mNotifier.update();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        mNotifier.dispose();
        if (mReadDB != null) {
            mReadDB.close();
        }
        super.dispose();
        System.exit(0);
    }

    /**
     * Get the {@link ReadDB} instance.
     * 
     * @return the ReadDB instance
     */
    public ReadDB getReadDB() {
        return mReadDB;
    }

    /**
     * Get view container.
     * 
     * @return the Container
     */
    public ViewContainer getViewContainer() {
        return mContainer;
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event dispatch thread.
     */
    private static void createAndShowGUI() {
        if (mUseSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (final ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (final InstantiationException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (final IllegalAccessException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (final UnsupportedLookAndFeelException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        // Create GUI.
        new GUI(new GUIProp());
    }

    /**
     * Main method.
     * 
     * @param args
     *            Not used.
     */
    public static void main(final String[] args) {
        /*
         * Schedule a job for the event dispatch thread:
         * creating and showing this application's GUI.
         */
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
