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
import java.awt.event.ComponentListener;
import java.io.File;

import javax.swing.*;

import com.treetank.exception.TreetankIOException;
import com.treetank.gui.view.ViewContainer;
import com.treetank.gui.view.ViewNotifier;
import com.treetank.gui.view.sunburst.SunburstView;
import com.treetank.gui.view.text.TextView;
import com.treetank.gui.view.tree.TreeView;
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
    private static final int WIDTH = 1280;

    /** Height of the frame. */
    private static final int HEIGHT = 1024;

    /** {@link GUIProp}. */
    private final GUIProp mProp; // Will be used in future versions (more GUI properties).

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
        mProp = paramProp;

        // ===== Setup GUI ======
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

        final Dimension frameSize = new Dimension(WIDTH, HEIGHT);
        setSize(frameSize);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Add menubar.
        final JMenuBar menuBar = new GUIMenuBar(this);
        setJMenuBar(menuBar);

        // Create Panels.
        final JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        // final JPanel treeText = new JPanel();
        // treeText.setLayout(new GridLayout(1, 0));

        // Create default views.
        mNotifier = new ViewNotifier(this);
        // final TreeView treeView = new TreeView(mNotifier);
        // final TextView textView = new TextView(mNotifier);
        //
        // // Add the scroll panes to a split pane.
        // final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        // splitPane.setLeftComponent(treeView);
        // splitPane.setRightComponent(textView);
        //
        // // Set sizes of components.
        // splitPane.setDividerLocation(DIVIDER_LOCATION);
        // splitPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        //
        // // Add the split pane.
        // treeText.add(splitPane);
        // top.add(treeText, BorderLayout.CENTER);
        // final JPanel sunburst = new JPanel();
        // sunburst.setLayout(new GridLayout(0, 1));
        // final SunburstView sunburstView = SunburstView.createInstance(mNotifier);
        // sunburstView.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // sunburst.add(sunburstView);
        mContainer =
            ViewContainer.getInstance(this, TreeView.getInstance(mNotifier),
                SunburstView.getInstance(mNotifier), TextView.getInstance(mNotifier));
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
        try {
            if (mReadDB == null || !paramFile.equals(mReadDB.getDatabase().getFile())
                || paramRevision != mReadDB.getRtx().getRevisionNumber()) {
                mReadDB = new ReadDB(paramFile, paramRevision);
                mNotifier.init();
            }
        } catch (final TreetankIOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        mNotifier.update();
    }

    @Override
    public void dispose() {
        if (mReadDB != null) {
            mReadDB.close();
        }
        mNotifier.dispose();
        super.dispose();
        System.exit(0);
    }

    /**
     * Get the {@link ReadDB} instance.
     * 
     * @return the ReadDB
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
