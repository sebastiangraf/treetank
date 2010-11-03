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

import com.treetank.gui.view.ViewNotifier;
import com.treetank.gui.view.text.TextView;
import com.treetank.gui.view.tree.TreeView;
import com.treetank.utils.LogWrapper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.LoggerFactory;

/**
 * <h1>Treetank GUI</h1>
 * 
 * <p>Main GUI frame.</p>
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

    /** Minimum height of panes. */
    private static final int HEIGHT = 1000;
    
    /** {@link GUIProp}. */
    private final GUIProp mProp;
    
    /** {@link ViewNotifier}. */
    private final ViewNotifier mNotifier;
    
    /** {@link ReadDB}. */
    private transient ReadDB mReadDB;

    /**
     * Constructor.
     * 
     * @param paramProp
     *                {@link GUIProp}
     */
    public GUI(final GUIProp paramProp) {
        mProp = paramProp;
        setTitle("Treetank GUI");
        
        final Dimension frameSize = new Dimension(1000, 1100);
        setSize(frameSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final JMenuBar menuBar = new TreetankMenuBar(this);
        
        // Add menubar.
        setJMenuBar(menuBar);

        // Screen size.
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Compute position of JFrame.
        final int topPos = (screenSize.height - frameSize.height) / 2;
        final int leftPos = (screenSize.width - frameSize.width) / 2;

        // Set frame position to center.
        setLocation(leftPos, topPos);
        
        // Create Panels.
        final JPanel top = new JPanel();
        top.setLayout(new BorderLayout());       
        final JPanel treeText = new JPanel();
        treeText.setLayout(new GridLayout(1, 0));
        
        // Create default views.
        mNotifier = new ViewNotifier(this);
        final TreeView treeView = new TreeView(mNotifier);
        final TextView textView = new TextView(mNotifier);

        // Add the scroll panes to a split pane.
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(treeView);
        splitPane.setRightComponent(textView);

        // Set sizes of components.
        treeView.setMinimumSize(new Dimension(220, HEIGHT));
        textView.setMinimumSize(new Dimension(800, HEIGHT));
        splitPane.setDividerLocation(220);
        splitPane.setPreferredSize(new Dimension(1020, HEIGHT));

        // Add the split pane.
        treeText.add(splitPane);
        top.add(treeText, BorderLayout.CENTER);
        add(top);
        
        // Display the window.
        pack();
        setVisible(true);
    }
    
    /**
     * Execute.
     * 
     * @param paramFile
     *                  {@link File} to open.
     * @param paramRevision
     *                  Determines the revision.
     */
    public void execute(final File paramFile, final long paramRevision) {
        if (mReadDB == null) {
            mReadDB = new ReadDB(paramFile, paramRevision);
        }
        mNotifier.update();
        validate();
        repaint();
    }
    
    @Override
    public void dispose() {
        mReadDB.close();
        super.dispose();
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
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
