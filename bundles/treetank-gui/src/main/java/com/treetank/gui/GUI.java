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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.TreeSelectionModel;

import com.treetank.gui.view.tree.TreetankTree;

import org.slf4j.Logger;
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
public final class GUI extends JPanel {

    /** Serialization UID. */
    private static final long serialVersionUID =
        7396552752125858796L;

    /** Logger. */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(GUI.class);

    /** Optionally set the look and feel. */
    private static transient boolean mUseSystemLookAndFeel;

    /** Minimum height of panes. */
    private static final int HEIGHT =
        1000;

    /** Tree view. */
    protected transient JTree mTree;

    /** XML pane. */
    protected transient JTextArea mXMLPane;

    /** AdjustmentListener temporal value. */
    private transient int mTempValue;

    /** XML view (scrollpane). */
    public static transient JScrollPane mXMLView;

    /** Main GUI reference. */
    public static transient GUI mGUI;

    /**
     * Empty Constructor.
     */
    public GUI() {
        super(new GridLayout(1, 0));

        try {
            // Build tree view.
            mTree =
                new TreetankTree(null);
            mTree.setBackground(Color.WHITE);

            /*
             * Performance tweak to use FixedLayoutManager and only invoke
             * getChild(..) for nodes inside view "bounding box". Avoids caching but
             * therefore more rendering calls.
             */
            mTree.setRowHeight(20);
            mTree.setLargeModel(true);

            // Selection Model.
            mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

            // Create a scroll pane and add the tree to it.
            final JScrollPane treeView =
                new JScrollPane(mTree);
            treeView.setBackground(Color.WHITE);

            // Create a XML text area.
            mXMLPane =
                new JTextArea();
            mXMLPane.setEditable(false);
            mXMLPane.setMinimumSize(new Dimension(370, 600));
            mXMLPane.setColumns(80);
            mXMLPane.setLineWrap(true);
            mXMLPane.setCaretPosition(0);

            // Create a scroll pane and add the XML text area to it.
            mXMLView =
                new JScrollPane(mXMLPane);
            mXMLView.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            mXMLView.setMinimumSize(new Dimension(400, 600));
            final JScrollBar vertScrollBar =
                mXMLView.getVerticalScrollBar();
            vertScrollBar.setValue(vertScrollBar.getMinimum());
            vertScrollBar.addAdjustmentListener(new AdjustmentListener() {
                @Override
                public void adjustmentValueChanged(final AdjustmentEvent evt) {
                    /*
                     * getValueIsAdjusting() returns true if the user is currently dragging
                     * the scrollbar's knob and has not picked a final value.
                     */
                    if (evt.getValueIsAdjusting()) {
                        // The user is dragging the knob.
                        return;
                    }

                    final int lineHeight =
                        mXMLPane.getFontMetrics(mXMLPane.getFont()).getHeight();
                    int value =
                        evt.getValue();
                    System.out.println("VALUE: " + value);
                    int result =
                        value - mTempValue;
                    GUICommands.lineChanges =
                        result / lineHeight;
                    System.out.println("Lines: " + GUICommands.lineChanges);
                    if (GUICommands.lineChanges != 0) {
                        GUICommands.text(mGUI, mXMLPane, false);
                    }

                    mTempValue =
                        value;
                }
            });

            // Add the scroll panes to a split pane.
            final JSplitPane splitPane =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setLeftComponent(treeView);
            splitPane.setRightComponent(mXMLView);

            // Set sizes of components.
            mXMLView.setMinimumSize(new Dimension(800, HEIGHT));
            treeView.setMinimumSize(new Dimension(220, HEIGHT));
            splitPane.setDividerLocation(220);
            splitPane.setPreferredSize(new Dimension(1020, HEIGHT));

            // Add the split pane to this panel.
            add(splitPane);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
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

        // Create and set up the window.
        final JFrame frame =
            new JFrame("Treetank GUI");
        final Dimension frameSize =
            new Dimension(1000, 1100);
        frame.setSize(frameSize);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create gui with menubar.
        mGUI =
            new GUI();
        final JMenuBar menuBar =
            new TreetankMenuBar(mGUI);

        // Add menubar.
        frame.setJMenuBar(menuBar);

        // Add content to the window.
        frame.add(mGUI);

        // Screen size.
        final Dimension screenSize =
            Toolkit.getDefaultToolkit().getScreenSize();

        // Compute position of JFrame.
        final int top =
            (screenSize.height - frameSize.height) / 2;
        final int left =
            (screenSize.width - frameSize.width) / 2;

        // Set frame position to center.
        frame.setLocation(left, top);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
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

    // GETTER.
    // =======================================================

    /**
     * Get tree.
     * 
     * @return tree.
     */
    protected JTree getTree() {
        return mTree;
    }

    /**
     * Get xmlPane.
     * 
     * @return xmlPane.
     */
    protected JTextArea getXMLPane() {
        return mXMLPane;
    }
}
