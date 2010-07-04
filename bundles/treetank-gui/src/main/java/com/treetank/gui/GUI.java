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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.gui.view.tree.TreetankTree;

/**
 * <h1>GUI</h>
 * 
 * <p>Main GUI frame.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public final class GUI extends JPanel {

  /** Serialization UID. */
  private static final long serialVersionUID = 7396552752125858796L;

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(GUI.class);

  /** Optionally set the look and feel. */
  private transient static boolean useSystemLookAndFeel = true;

  /** Minimum height of panes. */
  private static final int HEIGHT = 1000;

  /** Tree view. */
  protected transient JTree tree;

  /** XML pane. */
  protected transient JTextArea xmlPane;

  /** AdjustmentListener temporal value. */
  private static transient int tempValue = 0;

  /** XML view (scrollpane). */
  public static transient JScrollPane xmlView;

  /** Main GUI reference. */
  public static transient GUI gui;

  /**
   * Empty Constructor.
   */
  public GUI() {
    super(new GridLayout(1, 0));

    try {
      // Build tree view.
      tree = new TreetankTree(null);
      tree.setBackground(Color.WHITE);

      /*
       * Performance tweak to use FixedLayoutManager and only invoke 
       * getChild(..) for nodes inside view "bounding box". Avoids caching but
       * therefore more rendering calls.
       */
      tree.setRowHeight(20);
      tree.setLargeModel(true);

      // Selection Model.
      tree.getSelectionModel().setSelectionMode(
          TreeSelectionModel.SINGLE_TREE_SELECTION);

      // Create a scroll pane and add the tree to it. 
      final JScrollPane treeView = new JScrollPane(tree);
      treeView.setBackground(Color.WHITE);

      // Create a XML text area.
      xmlPane = new JTextArea();
      xmlPane.setEditable(false);
      xmlPane.setMinimumSize(new Dimension(370, 600));
      xmlPane.setColumns(80);
      xmlPane.setLineWrap(true);
      xmlPane.setCaretPosition(0);

      // Create a scroll pane and add the XML text area to it.
      xmlView = new JScrollPane(xmlPane);
      xmlView
          .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      xmlView.setMinimumSize(new Dimension(400, 600));
      final JScrollBar vertScrollBar = xmlView.getVerticalScrollBar();
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
              xmlPane.getFontMetrics(xmlPane.getFont()).getHeight();
          int value = evt.getValue();
          System.out.println("VALUE: " + value);
          int result = value - tempValue;
          GUICommands.lineChanges = result / lineHeight;
          System.out.println("Lines: " + GUICommands.lineChanges);
          if (GUICommands.lineChanges != 0) {
            GUICommands.text(gui, xmlPane, false);
          }
          
          tempValue = value;
        }
      });

      // Add the scroll panes to a split pane.
      final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      splitPane.setLeftComponent(treeView);
      splitPane.setRightComponent(xmlView);

      // Set sizes of components.
      xmlView.setMinimumSize(new Dimension(800, HEIGHT));
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
    if (useSystemLookAndFeel) {
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
    final JFrame frame = new JFrame("Treetank GUI");
    final Dimension frameSize = new Dimension(1000, 1100);
    frame.setSize(frameSize);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create gui with menubar.
    gui = new GUI();
    final JMenuBar menuBar = new TreetankMenuBar(gui);

    // Add menubar.
    frame.setJMenuBar(menuBar);

    // Add content to the window.
    frame.add(gui);

    // Screen size.
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // Compute position of JFrame.
    final int top = (screenSize.height - frameSize.height) / 2;
    final int left = (screenSize.width - frameSize.width) / 2;

    // Set frame position to center.
    frame.setLocation(left, top);

    // Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  // GETTER.
  // =======================================================

  /**
   * Get tree.
   * 
   * @return tree.
   */
  protected JTree getTree() {
    return tree;
  }

  /**
   * Get xmlPane.
   * 
   * @return xmlPane.
   */
  protected JTextArea getXMLPane() {
    return xmlPane;
  }

  /**
   * Main method.
   * 
   * @param args
   *              Not used.
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
