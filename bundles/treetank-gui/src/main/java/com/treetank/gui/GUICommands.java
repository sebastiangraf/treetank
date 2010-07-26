package com.treetank.gui;

import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.JScrollBar;
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
import com.treetank.service.xml.serialize.XMLSerializer;
import com.treetank.service.xml.serialize.XMLSerializerProperties;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.shredder.XMLUpdateShredder;

/**
 * <h1>GUICommands</h1>
 * 
 * <p>
 * All available GUI commands.
 * </p>
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
                        final XMLSerializer serializer =
                            new XMLSerializerBuilder(session, outputStream).build();
                        serializer.call();

                        session.close();
                        db.close();
                        outputStream.close();
                    } catch (final Exception e1) {
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
    private transient final String mDesc;

    /** Line number to append or remove from the text field. */
    public static transient int lineChanges = 0;

    /** Start position of char array for text insertion. */
    private static transient int startPos = 0;

    /** Text output stream. */
    public static transient OutputStream out;

    /** Adjustment Listener for textArea. */
    private static transient AdjustmentListener adjListener;

    /**
     * Constructor
     * 
     * @param desc
     *            Description of command.
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
     *            Main GUI frame.
     * @param file
     *            File to open and display.
     */
    private static void setViews(final GUI gui, final File file) {
        try {
            // Initialize database.
            final IDatabase database = Database.openDatabase(file);
            final ISession session = database.getSession();

            // Tree.
            final JTree tree = gui.getTree();

            /*
             * Remove a listener/listeners, which might already exist from
             * another call to setViews(...).
             */
            for (final TreeSelectionListener listener : tree.getTreeSelectionListeners()) {
                tree.removeTreeSelectionListener(listener);
            }
            final JScrollBar bar = GUI.xmlView.getVerticalScrollBar();
            for (final AdjustmentListener listener : bar.getAdjustmentListeners()) {
                bar.removeAdjustmentListener(listener);
            }

            // XML.
            final JTextArea xmlPane = gui.getXMLPane();

            // Use our Treetank model and renderer.
            tree.setModel(new TreetankTreeModel(database));
            tree.setCellRenderer(new TreetankTreeCellRenderer(database));

            // Serialize file into XML view if it is empty.
            out = new ByteArrayOutputStream();
            final XMLSerializerProperties properties = new XMLSerializerProperties();
            final XMLSerializer serializer = new XMLSerializerBuilder(session, out, properties).build();
            serializer.call();
            text(gui, xmlPane, true);

            // Listen for when the selection changes.
            tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(final TreeSelectionEvent e) {
                    if (e.getNewLeadSelectionPath() != null
                    && e.getNewLeadSelectionPath() != e.getOldLeadSelectionPath()) {
                        /*
                         * Returns the last path element of the selection. This
                         * method is useful only when the selection model allows
                         * a single selection.
                         */
                        final IItem node = (IItem)e.getNewLeadSelectionPath().getLastPathComponent(); // tree.getLastSelectedPathComponent();
                        out = new ByteArrayOutputStream();
                        IReadTransaction rtx = null;
                        try {
                            rtx = session.beginReadTransaction();
                            final long nodeKey = node.getNodeKey();

                            switch (node.getKind()) {
                            case ROOT_KIND:
                                rtx.moveTo(nodeKey);
                                new XMLSerializerBuilder(session, out, properties).build().call();
                                break;
                            case ELEMENT_KIND:
                                rtx.moveTo(nodeKey);
                                new XMLSerializerBuilder(session, out, properties).build().call();
                                break;
                            case TEXT_KIND:
                                rtx.moveTo(nodeKey);
                                out.write(rtx.getNode().getRawValue());
                                break;
                            case NAMESPACE_KIND:
                                // Move transaction to parent of given namespace node.
                                rtx.moveTo(node.getParentKey());

                                final long nNodeKey = node.getNodeKey();
                                for (int i = 0, namespCount =
                                    ((ElementNode)rtx.getNode()).getNamespaceCount(); i < namespCount; i++) {
                                    rtx.moveToNamespace(i);
                                    if (rtx.getNode().equals(node)) {
                                        break;
                                    }
                                    rtx.moveTo(nNodeKey);
                                }

                                if (rtx.nameForKey(rtx.getNode().getNameKey()).length() == 0) {
                                    out.write(("xmlns='" + rtx.nameForKey(rtx.getNode().getURIKey()) + "'")
                                        .getBytes());
                                } else {
                                    out.write(("xmlns:" + rtx.nameForKey(rtx.getNode().getNameKey()) + "='"
                                    + rtx.nameForKey(rtx.getNode().getURIKey()) + "'").getBytes());
                                }
                            case ATTRIBUTE_KIND:
                                // Move transaction to parent of given attribute node.
                                rtx.moveTo(node.getParentKey());
                                final long aNodeKey = node.getNodeKey();
                                for (int i = 0, attsCount = ((ElementNode)rtx.getNode()).getAttributeCount(); i < attsCount; i++) {
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
                                    out.write((attQName.getLocalPart() + "='" + rtx.getValueOfCurrentNode() + "'")
                                        .getBytes());
                                } else {
                                    out.write((attPrefix + ":" + attQName.getLocalPart() + "='"
                                    + rtx.getValueOfCurrentNode() + "'").getBytes());
                                }
                            default:

                            }
                        } catch (final Exception e1) {
                            LOGGER.error(e1.getMessage(), e1);
                        } finally {
                            try {
                                if (rtx != null) {
                                    rtx.close();
                                }
                            } catch (final TreetankException e1) {
                                LOGGER.error(e1.getMessage(), e1);
                            }
                        }

                        text(gui, xmlPane, true);
                    }
                }
            });
        } catch (final Exception e1) {
            LOGGER.error(e1.getMessage(), e1);
        }
    }

    public static void text(final GUI gui, final JTextArea xmlPane, final boolean init) {
        // Remove adjustmnet listeners temporarily.
        final JScrollBar bar = GUI.xmlView.getVerticalScrollBar();
        for (final AdjustmentListener listener : bar.getAdjustmentListeners()) {
            adjListener = listener;
            bar.removeAdjustmentListener(listener);
        }

        // Initialize variables.
        final char[] text = out.toString().toCharArray();
        final int lineHeight = xmlPane.getFontMetrics(xmlPane.getFont()).getHeight();
        final int frameHeight = xmlPane.getHeight() + lineChanges * lineHeight;
        int rowsSize = 0;
        final StringBuilder sBuilder = new StringBuilder();
        int indexSepChar = 0;
        final String NL = System.getProperty("line.separator");
        // int countNewlines = 0;
        // final StringBuilder insertAtFirstPos = new StringBuilder();
        //
        // if (changeColumns > 0) {
        // // Get start index.
        // for (int i = 0; i < text.length; i++) {
        // final char character = text[i];
        //
        // // Increment rowSize?
        // if (indexSepChar < NL.length() && character ==
        // NL.charAt(indexSepChar)) {
        // if (indexSepChar == NL.length() - 1) {
        // countNewlines++;
        // }
        // }
        //
        // insertAtFirstPos.append(character);
        //
        // if (countNewlines == changeColumns) {
        // startPos = i + 1;
        // break;
        // }
        // }
        //
        // xmlPane.replaceRange("", 0, startPos - 1);
        // } else if (changeColumns < 0) {
        // xmlPane.insert(insertAtFirstPos.toString(), 0);
        // }

        // Build text.
        rowsSize = 0;
        if (init) {
            startPos = 0;
        }
        for (int i = startPos == 0 ? startPos : startPos + 1; i < text.length && lineChanges >= 0
        && startPos + 1 != text.length; i++) {
            final char character = text[i];

            // Increment rowsSize?
            if (indexSepChar < NL.length() && character == NL.charAt(indexSepChar)) {
                if (indexSepChar == NL.length() - 1) {
                    rowsSize += lineHeight;
                } else {
                    indexSepChar++;
                }
            }

            if (rowsSize < frameHeight) {
                sBuilder.append(character);
                startPos = i;
            } else {
                startPos = i;
                System.out.println("START: " + startPos);
                break;
            }
        }

        if (lineChanges >= 0 && startPos + 1 <= text.length) {
            if (init) {
                xmlPane.setText(sBuilder.toString());
                xmlPane.setCaretPosition(0);
            } else {
                final int caretPos = xmlPane.getCaretPosition();
                xmlPane.setCaretPosition(xmlPane.getDocument().getLength());
                xmlPane.append(sBuilder.toString());
                // Check and update caret position.
                final int newCaretPos = caretPos + lineChanges * xmlPane.getColumns();
                final int documentLength = xmlPane.getDocument().getLength();
                if (newCaretPos < documentLength) {
                    xmlPane.setCaretPosition(newCaretPos);
                }
            }
        }

        /*
         * Schedule a job for the event dispatch thread: (Re)adding the
         * adjustment listener.
         */
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                bar.addAdjustmentListener(adjListener);
            }
        });
    }

    /**
     * Shredder or shredder into.
     * 
     * @param gui
     *            Main GUI frame.
     * @param updateOnly
     *            Shredder into an existing file or not.
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
                    if (updateOnly) {
                        final XMLShredder shredder = new XMLUpdateShredder(wtx, reader, true);
                        shredder.call();
                    } else {
                        final XMLShredder shredder = new XMLShredder(wtx, reader, true);
                        shredder.call();
                    }
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
