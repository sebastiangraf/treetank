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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.serialize.XMLSerializer;
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
public enum GUICommands implements IGUICommand {
    
    /**
     * Open a Treetank file.
     */
    OPEN("Open TNK-File", false) {
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
            panel.setLayout(box);
            final BorderLayout layout = (BorderLayout)fc.getLayout();
            final Component comp = layout.getLayoutComponent(BorderLayout.SOUTH);
            panel.add(comp);
            final JComboBox cb = new JComboBox();

            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    final JComboBox cb = (JComboBox)paramEvent.getSource();
                    if (cb.getSelectedItem() != null) {
                        mRevNumber = (Long)cb.getSelectedItem();
                        System.out.println(mRevNumber);
                    }
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
                            cb.removeAllItems();
                        }

                        if (!error) {
                            // Create items, which are used as available revisions.
                            for (long i = 0; i <= revNumber; i++) {
                                cb.addItem(i);
                            }
                        }
                    }
                }
            };
            fc.addPropertyChangeListener(changeListener);

            // Handle open button action.
            if (fc.showOpenDialog(paramGUI) == JFileChooser.APPROVE_OPTION) {
                final File file = fc.getSelectedFile();
                paramGUI.execute(file, mRevNumber);
            }
        }
    },

    /**
     * Shredder an XML-document.
     */
    SHREDDER("Shredder XML-document", false) {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            shredder(paramGUI, false);
        }
    },

    /**
     * Update a shreddered file.
     */
    SHREDDER_UPDATE("Update shreddered file", false) {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            shredder(paramGUI, true);
        }
    },

    /**
     * Serialize a Treetank storage.
     */
    SERIALIZE("Serialize", false) {
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
                    } catch (final TreetankException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    } catch (final IOException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    }
                }
            }
        }
    },

    /**
     * Close Treetank GUI.
     */
    QUIT("Quit", false) {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            paramGUI.dispose();
        }
    },

    /**
     * Show tree view.
     */
    TREE("Tree", true) {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            GUIProp.EShowViews.SHOWTREE.setValue(true);
        }
    },

    /**
     * Show text view.
     */
    TEXT("Text", true) {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            GUIProp.EShowViews.SHOWTEXT.setValue(true);
        }
    },

    /**
     * Show treemap view.
     */
    TREEMAP("Treemap", true) {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            GUIProp.EShowViews.SHOWTREE.setValue(true);
        }
    },
    
    /**
     * Show sunburst view.
     */
    SUNBURST("Sunburst", true) {
        @Override
        public void execute(final ActionEvent paramE, final GUI paramGUI) {
            GUIProp.EShowViews.SHOWSUNBURST.setValue(true);
        }
    };

    /** Logger. */
    private static final Logger LOGWRAPPER = LoggerFactory.getLogger(GUICommands.class);

    /** Description of command. */
    private final String mDesc;
    
    /** Determins if menu item is checked or not. */
    private final boolean mChecked;

    /**
     * Constructor.
     * 
     * @param paramDesc
     *            Description of command.
     * @param paramChecked
     *            Determines if menu item is checked or not.       
     */
    GUICommands(final String paramDesc, final boolean paramChecked) {
        mDesc = paramDesc;
        mChecked = paramChecked;
    }

    @Override
    public String desc() {
        return mDesc;
    }
    
    @Override
    public boolean checked() {
        return mChecked;
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
                } catch (final TreetankException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                } catch (final IOException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                } catch (final XMLStreamException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                }
            }
        }
    }
}
