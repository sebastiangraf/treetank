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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.treetank.settings.ECharsForSerializing;

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
public enum GUICommands implements IGUICommand {
    /**
     * Open a Treetank file.
     */
    OPEN("Open TNK-File") {
        /** Revision number. */
        private long mRevNumber;

        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            // Create a file chooser.
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);

            // Create new panel etc.pp. for choosing the revision at the bottom of the frame.
            final JPanel panel = new JPanel();
            final BoxLayout box = new BoxLayout(panel, BoxLayout.Y_AXIS);
            final BorderLayout layout = (BorderLayout)fc.getLayout();
            panel.setLayout(box);
            final Component comp = layout.getLayoutComponent(BorderLayout.SOUTH);
            panel.add(comp);
            final JComboBox cb = new JComboBox();

            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    final JComboBox cb = (JComboBox)paramEvent.getSource();
                    mRevNumber = (Long)cb.getSelectedItem();
                };
            });

            panel.add(cb);
            fc.add(panel, BorderLayout.SOUTH);

            final PropertyChangeListener changeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(final PropertyChangeEvent paramEvent) {
                    // Get last revision number from TT-storage.
                    final JFileChooser fileChooser = (JFileChooser)paramEvent.getSource();
                    final File tmpDir = fileChooser.getSelectedFile();
                    long revNumber = 0;

                    if (tmpDir != null) {
                        // A directory is in focus.
                        boolean error = false;

                        try {
                            final IDatabase db = Database.openDatabase(tmpDir);
                            final IReadTransaction rtx = db.getSession().beginReadTransaction();
                            revNumber = rtx.getRevisionNumber();
                            rtx.close();
                            db.close();
                        } catch (final TreetankException e) {
                            // Selected directory is not a Treetank storage.
                            error = true;
                        }

                        if (!error) {
                            // Create items, which are used as available revisions.
                            cb.removeAll();
                            for (long i = 0; i <= revNumber; i++) {
                                cb.addItem(i);
                            }

                            // Repaint components.
                            panel.repaint();
                            fc.repaint();
                        }
                    }

                }

            };
            fc.addPropertyChangeListener(changeListener);

            // Handle open button action.
            if (fc.showOpenDialog(paramGUI) == JFileChooser.APPROVE_OPTION) {
                final File file = fc.getSelectedFile();
                setViews(paramGUI, file, mRevNumber);
            }
        }
    },

    /**
     * Shredder an XML-document.
     */
    SHREDDER("Shredder XML-document") {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            shredder(paramGUI, false);
        }
    },

    /**
     * Update a shreddered file.
     */
    SHREDDER_UPDATE("Update shreddered file") {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            shredder(paramGUI, true);
        }
    },

    /**
     * Serialize a Treetank storage.
     */
    SERIALIZE("Serialize") {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            // Create a file chooser.
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);

            if (fc.showOpenDialog(paramGUI) == JFileChooser.APPROVE_OPTION) {
                final File source = fc.getSelectedFile();

                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(true);
                if (fc.showSaveDialog(paramGUI) == JFileChooser.APPROVE_OPTION) {
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
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
    },

    /**
     * Close Treetank GUI.
     */
    QUIT("Quit") {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            System.exit(0);
        }
    },

    /**
     * Show tree view.
     */
    TREE("Tree") {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {

        }
    },

    /**
     * Show text view.
     */
    TEXT("Text") {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {

        }
    },

    /**
     * Show treemap view.
     */
    TREEMAP("Treemap") {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {

        }
    };

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GUICommands.class);

    /** Description of command. */
    private final transient String mDesc;

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
     * @param paramDesc
     *            Description of command.
     */
    GUICommands(final String paramDesc) {
        mDesc = paramDesc;
    }

    /**
     * Get description.
     * 
     * @return description
     */
    public String desc() {
        return mDesc;
    }

    /**
     * Sets Tree and XML views.
     * 
     * @param paramGUI
     *            Main GUI frame.
     * @param paramFile
     *            File to open and display.
     * @param paramRevision
     *            Revision to open.
     */
    private static void setViews(final GUI paramGUI, final File paramFile, final long paramRevision) {
        try {
            System.out.println(paramRevision);
            // Initialize database.
            final IDatabase database = Database.openDatabase(paramFile);
            final ISession session = database.getSession();

            // Tree.
            final JTree tree = paramGUI.getTree();

            /*
             * Remove a listener/listeners, which might already exist from
             * another call to setViews(...).
             */
            for (final TreeSelectionListener listener : tree.getTreeSelectionListeners()) {
                tree.removeTreeSelectionListener(listener);
            }
            final JScrollBar bar = GUI.mXMLView.getVerticalScrollBar();
            for (final AdjustmentListener listener : bar.getAdjustmentListeners()) {
                bar.removeAdjustmentListener(listener);
            }

            // XML.
            final JTextArea xmlPane = paramGUI.getXMLPane();

            // Use our Treetank model and renderer.
            tree.setModel(new TreetankTreeModel(database, 0, paramRevision));
            tree.setCellRenderer(new TreetankTreeCellRenderer(database, 0, paramRevision));

            // Serialize file into XML view if it is empty.
            out = new ByteArrayOutputStream();
            final XMLSerializerProperties properties = new XMLSerializerProperties();
            final XMLSerializer serializer = new XMLSerializerBuilder(session, 0, out, properties).build();
            serializer.call();
            text(paramGUI, xmlPane, true);

            // Listen for when the selection changes.
            tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(final TreeSelectionEvent paramE) {
                    if (paramE.getNewLeadSelectionPath() != null
                        && paramE.getNewLeadSelectionPath() != paramE.getOldLeadSelectionPath()) {
                        /*
                         * Returns the last path element of the selection. This
                         * method is useful only when the selection model allows
                         * a single selection.
                         */
                        final IItem node = (IItem)paramE.getNewLeadSelectionPath().getLastPathComponent(); // tree.getLastSelectedPathComponent();
                        out = new ByteArrayOutputStream();
                        IReadTransaction rtx = null;
                        try {
                            rtx = session.beginReadTransaction(paramRevision);
                            final long nodeKey = node.getNodeKey();

                            switch (node.getKind()) {
                            case ROOT_KIND:
                                new XMLSerializerBuilder(session, nodeKey, out, properties).build().call();
                                break;
                            case ELEMENT_KIND:
                                new XMLSerializerBuilder(session, nodeKey, out, properties).build().call();
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
                                break;
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

                                if (attPrefix == null || attPrefix.equals("")) {
                                    out
                                        .write((attQName.getLocalPart() + "='" + rtx.getValueOfCurrentNode() + "'")
                                            .getBytes());
                                } else {
                                    out.write((attPrefix + ":" + attQName.getLocalPart() + "='"
                                        + rtx.getValueOfCurrentNode() + "'").getBytes());
                                }
                                break;
                            default:
                                throw new IllegalStateException("Node kind not known!");
                            }
                        } catch (final Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        } finally {
                            try {
                                if (rtx != null) {
                                    rtx.close();
                                }
                            } catch (final TreetankException e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }

                        text(paramGUI, xmlPane, true);
                    }
                }
            });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Display text.
     * 
     * @param paramGUI
     *            Main GUI frame.
     * @param paramXMLPane
     *            XML panel.
     * @param paramInit
     *            Determines if it's the initial invocation.
     */
    public static void text(final GUI paramGUI, final JTextArea paramXMLPane, final boolean paramInit) {
        // Remove adjustmnet listeners temporarily.
        final JScrollBar bar = GUI.mXMLView.getVerticalScrollBar();
        for (final AdjustmentListener listener : bar.getAdjustmentListeners()) {
            adjListener = listener;
            bar.removeAdjustmentListener(listener);
        }

        // Initialize variables.
        final char[] text = out.toString().toCharArray();
        final int lineHeight = paramXMLPane.getFontMetrics(paramXMLPane.getFont()).getHeight();
        final int frameHeight = paramXMLPane.getHeight() + lineChanges * lineHeight;
        int rowsSize = 0;
        final StringBuilder sBuilder = new StringBuilder();
        int indexSepChar = 0;
        final String NL = ECharsForSerializing.NEWLINE.toString();
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
        if (paramInit) {
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
            if (paramInit) {
                paramXMLPane.setText(sBuilder.toString());
                paramXMLPane.setCaretPosition(0);
            } else {
                final int caretPos = paramXMLPane.getCaretPosition();
                paramXMLPane.setCaretPosition(paramXMLPane.getDocument().getLength());
                paramXMLPane.append(sBuilder.toString());
                // Check and update caret position.
                final int newCaretPos = caretPos + lineChanges * paramXMLPane.getColumns();
                final int documentLength = paramXMLPane.getDocument().getLength();
                if (newCaretPos < documentLength) {
                    paramXMLPane.setCaretPosition(newCaretPos);
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
     * @param paramGUI
     *            Main GUI frame.
     * @param paramUpdateOnly
     *            Shredder into an existing file or not.
     */
    private static void shredder(final GUI paramGUI, final boolean paramUpdateOnly) {
        // Create a file chooser.
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showOpenDialog(paramGUI) == JFileChooser.APPROVE_OPTION) {
            final File source = fc.getSelectedFile();

            if (fc.showSaveDialog(paramGUI) == JFileChooser.APPROVE_OPTION) {
                final File target = fc.getSelectedFile();

                try {
                    if (!paramUpdateOnly) {
                        Database.truncateDatabase(target);
                        Database.createDatabase(new DatabaseConfiguration(target));
                    }
                    final IDatabase database = Database.openDatabase(target);
                    final ISession session = database.getSession();
                    final IWriteTransaction wtx = session.beginWriteTransaction();
                    final XMLEventReader reader = XMLShredder.createReader(source);
                    if (paramUpdateOnly) {
                        final XMLShredder shredder = new XMLUpdateShredder(wtx, reader, true, source, true);
                        shredder.call();
                    } else {
                        final XMLShredder shredder = new XMLShredder(wtx, reader, true);
                        shredder.call();
                    }
                    wtx.close();
                    session.close();
                    database.close();

                    // setViews(paramGUI, target);
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}
