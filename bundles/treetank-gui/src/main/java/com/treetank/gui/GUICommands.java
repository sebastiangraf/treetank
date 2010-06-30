package com.treetank.gui;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.gui.view.tree.TreetankTreeCellRenderer;
import com.treetank.gui.view.tree.TreetankTreeModel;
import com.treetank.node.ElementNode;
import com.treetank.service.xml.XMLSerializer;
import com.treetank.service.xml.XMLShredder;

/**
 * <h1>GUICommands</h1>
 * 
 * <p>All available GUI commands.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public enum GUICommands implements GUICommand {
  OPEN("Open TNK-File") {
    @Override
    public void execute(final ActionEvent e, final GUI gui) {
      // Create a file chooser.
      final JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setAcceptAllFileFilterUsed(false);

      // Handle open button action.
      if (fc.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
        final File file = fc.getSelectedFile();
        setViews(gui, file);
      }
    }
  },

  SHREDDER("Shredder XML-File") {
    @Override
    public void execute(final ActionEvent e, final GUI gui) {
      shredder(gui, false);
    }
  },

  SHREDDER_INTO("Shredder into TNK-File") {
    @Override
    public void execute(final ActionEvent e, final GUI gui) {
      shredder(gui, true);
    }
  },

  SERIALIZE("Serialize") {
    @Override
    public void execute(final ActionEvent e, final GUI gui) {
      // Create a file chooser.
      final JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.setAcceptAllFileFilterUsed(false);

      if (fc.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
        final File source = fc.getSelectedFile();
        
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(true);
        if (fc.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
          final File target = fc.getSelectedFile();
          target.delete();
          try {
          final FileOutputStream outputStream = new FileOutputStream(target);

          final IDatabase db = Database.openDatabase(source);
          final ISession session = db.getSession();
          final IReadTransaction rtx = session.beginReadTransaction();
          final XMLSerializer serializer =
              new XMLSerializer(rtx, outputStream, true, false, false, true);
          serializer.call();

          rtx.close();
          session.close();
          db.close();
          outputStream.close();
          } catch(final Exception e1) {
            LOGGER.error(e1.getMessage(), e1);
          }
        }
      }
    }
  },

  QUIT("Quit") {
    @Override
    public void execute(final ActionEvent e, final GUI gui) {
      System.exit(0);
    }
  },

  TREE("Tree") {
    @Override
    public void execute(final ActionEvent e, final GUI gui) {

    }
  },

  TEXT("Text") {
    @Override
    public void execute(final ActionEvent e, final GUI gui) {

    }
  },

  TREEMAP("Treemap") {
    @Override
    public void execute(final ActionEvent e, final GUI gui) {

    }
  };

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(GUICommands.class);

  /** Description of command. */
  private transient String mDesc;

  /**
   * Constructor
   * 
   * @param desc
   *              Description of command.
   */
  GUICommands(final String desc) {
    mDesc = desc;
  }

  /**
   * Get description.
   */
  public String desc() {
    return mDesc;
  }

  /**
   * Sets Tree and XML views.
   * 
   * @param gui
   *              Main GUI frame.
   * @param file
   *              File to open and display.
   */
  private static void setViews(final GUI gui, final File file) {
    try {
      // Initialize database.
      final IDatabase database = Database.openDatabase(file);
      final ISession session = database.getSession();

      // Tree.
      final JTree tree = gui.getTree();

      for (final TreeSelectionListener listener : tree
          .getTreeSelectionListeners()) {
        tree.removeTreeSelectionListener(listener);
      }

      // XML.
      final JTextArea xmlPane = gui.getXMLPane();

      // Use our Treetank model and renderer.
      tree.setModel(new TreetankTreeModel(database));
      tree.setCellRenderer(new TreetankTreeCellRenderer(database));

      // Serialize file into XML view if it is empty.
      final IReadTransaction rtx = session.beginReadTransaction();
      final OutputStream out = new ByteArrayOutputStream();
      new XMLSerializer(rtx, out, true, false, true, true).call();
      xmlPane.setText(out.toString().substring(0, 2000));

      // Listen for when the selection changes.
      tree.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(final TreeSelectionEvent e) {
          /*
           * Returns the last path element of the selection. This method is 
           * useful only when the selection model allows a single selection.
           */
          final IItem node = (IItem) tree.getLastSelectedPathComponent();
          final OutputStream out = new ByteArrayOutputStream();
          IReadTransaction rtx = null;
          try {
            rtx = session.beginReadTransaction();
            final long nodeKey = node.getNodeKey();

            switch (node.getKind()) {
            case ROOT_KIND:
              rtx.moveTo(nodeKey);
              new XMLSerializer(rtx, out, true, false, false, true).call();
              break;
            case ELEMENT_KIND:
              rtx.moveTo(nodeKey);
              System.out.println(rtx.getQNameOfCurrentNode());
              new XMLSerializer(rtx, out, false, false, false, true).call();
              break;
            case TEXT_KIND:
              rtx.moveTo(nodeKey);
              out.write(rtx.getNode().getRawValue());
              break;
            case ATTRIBUTE_KIND:
              // Move transaction to parent of given attribute node.
              rtx.moveTo(node.getParentKey());
              final long aNodeKey = node.getNodeKey();
              for (int i = 0, attsCount =
                  ((ElementNode) rtx.getNode()).getAttributeCount(); i < attsCount; i++) {
                rtx.moveToAttribute(i);
                if (rtx.getNode().equals(node)) {
                  break;
                }
                rtx.moveTo(aNodeKey);
              }

              // Display value.
              final String attPrefix = rtx.getQNameOfCurrentNode().getPrefix();
              final QName attQName = rtx.getQNameOfCurrentNode();

              if (attPrefix == null || attPrefix == "") {
                out.write((attQName.getLocalPart()
                    + "='"
                    + rtx.getValueOfCurrentNode() + "'").getBytes());
              } else {
                out.write((attPrefix
                    + ":"
                    + attQName.getLocalPart()
                    + "='"
                    + rtx.getValueOfCurrentNode() + "'").getBytes());
              }
            default:

            }
          } catch (final Exception e1) {
            LOGGER.error(e1.getMessage(), e1);
          } finally {
            try {
              rtx.close();
            } catch (final TreetankException e1) {
              LOGGER.error(e1.getMessage(), e1);
            }
          }

          xmlPane.setText(out.toString());
        }
      });
    } catch (final Exception e1) {
      LOGGER.error(e1.getMessage(), e1);
    }
  }

  /**
   * Shredder or shredder into.
   * 
   * @param gui
   *              Main GUI frame.
   * @param updateOnly
   *              Shredder into an existing file or not.
   */
  private static void shredder(final GUI gui, final boolean updateOnly) {
    // Create a file chooser.
    final JFileChooser fc = new JFileChooser();
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fc.setAcceptAllFileFilterUsed(false);

    if (fc.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
      final File source = fc.getSelectedFile();

      if (fc.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
        final File target = fc.getSelectedFile();

        try {
          if (!updateOnly) {
            Database.truncateDatabase(target);
            Database.createDatabase(new DatabaseConfiguration(target));
          }
          final IDatabase database = Database.openDatabase(target);
          final ISession session = database.getSession();
          final IWriteTransaction wtx = session.beginWriteTransaction();
          final XMLEventReader reader = XMLShredder.createReader(source);
          final XMLShredder shredder =
              new XMLShredder(wtx, reader, true, updateOnly);
          shredder.call();
          wtx.close();
          session.close();
          database.close();

          setViews(gui, target);
        } catch (final Exception e1) {
          LOGGER.error(e1.getMessage(), e1);
        }
      }
    }
  }
}
