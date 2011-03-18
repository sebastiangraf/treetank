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
package org.treetank.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.serialize.XMLSerializer;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    OPEN("Open TNK-File", EMenu.MENU) {
        /** Revision number. */
        private long mRevNumber;

        @Override
        public void execute(final GUI paramGUI) {
            // Create a file chooser.
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);

            // Create new panel etc.pp. for choosing the revision at the bottom of the frame.
            final JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            // final BoxLayout box = new BoxLayout(panel, BoxLayout.Y_AXIS);
            // panel.setLayout(box);
            // final BorderLayout layout = (BorderLayout)fc.getLayout();
            // final Component comp = layout.getLayoutComponent(BorderLayout.SOUTH);

            // panel.add(fc);
            final JComboBox cb = new JComboBox();

            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    final JComboBox cb = (JComboBox)paramEvent.getSource();
                    if (cb.getSelectedItem() != null) {
                        mRevNumber = (Long)cb.getSelectedItem();
                    }
                };
            });

            // panel.add(fc, BorderLayout.CENTER);
            panel.add(cb, BorderLayout.SOUTH);
            fc.setAccessory(panel);
            // fc.add(panel, BorderLayout.SOUTH);

            final PropertyChangeListener changeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(final PropertyChangeEvent paramEvent) {
                    // Remove items first.
                    cb.removeAllItems();

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
                        } catch (final AbsTTException e) {
                            // Selected directory is not a Treetank storage.
                            error = true;
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
    SHREDDER("Shredder XML-document", EMenu.MENU) {
        @Override
        public void execute(final GUI paramGUI) {
            shredder(paramGUI, EShredder.NORMAL);
        }
    },

    /**
     * Update a shreddered file.
     */
    SHREDDER_UPDATE("Update shreddered file", EMenu.MENU) {
        @Override
        public void execute(final GUI paramGUI) {
            shredder(paramGUI, EShredder.UPDATEONLY);
        }
    },

    /**
     * Serialize a Treetank storage.
     */
    SERIALIZE("Serialize", EMenu.MENU) {
        @Override
        public void execute(final GUI paramGUI) {
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
                    if (target.delete()) {
                        try {
                            final FileOutputStream outputStream = new FileOutputStream(target);

                            final IDatabase db = Database.openDatabase(source);
                            final ISession session = db.getSession();

                            final ExecutorService executor = Executors.newSingleThreadExecutor();
                            final XMLSerializer serializer =
                                new XMLSerializerBuilder(session, outputStream).build();
                            executor.submit(serializer);
                            executor.shutdown();
                            try {
                                executor.awaitTermination(5, TimeUnit.SECONDS);
                            } catch (final InterruptedException e) {
                                LOGWRAPPER.error(e.getMessage(), e);
                                return;
                            }

                            session.close();
                            db.close();
                            outputStream.close();
                        } catch (final AbsTTException e) {
                            LOGWRAPPER.error(e.getMessage(), e);
                        } catch (final IOException e) {
                            LOGWRAPPER.error(e.getMessage(), e);
                        }
                    } else {
                        // FIXME ERROR
                    }
                }
            }
        }
    },

    /**
     * Separator.
     */
    SEPARATOR("", EMenu.SEPARATOR) {
        @Override
        public void execute(final GUI paramGUI) {

        }
    },

    /**
     * Close Treetank GUI.
     */
    QUIT("Quit", EMenu.MENU) {
        @Override
        public void execute(final GUI paramGUI) {
            paramGUI.dispose();
        }
    },

    /**
     * Show tree view.
     */
    TREE("Tree", EMenu.CHECKBOXITEM) {
        @Override
        public boolean selected() {
            return GUIProp.EShowViews.SHOWTREE.getValue();
        }

        @Override
        public void execute(final GUI paramGUI) {
            GUIProp.EShowViews.SHOWTREE.invert();
            paramGUI.getViewContainer().layoutViews();
        }
    },

    /**
     * Show text view.
     */
    TEXT("Text", EMenu.CHECKBOXITEM) {
        @Override
        public boolean selected() {
            return GUIProp.EShowViews.SHOWTEXT.getValue();
        }

        @Override
        public void execute(final GUI paramGUI) {
            GUIProp.EShowViews.SHOWTEXT.invert();
            paramGUI.getViewContainer().layoutViews();
        }
    },

    /**
     * Show treemap view.
     */
    TREEMAP("Treemap", EMenu.CHECKBOXITEM) {
        @Override
        public boolean selected() {
            return GUIProp.EShowViews.SHOWTREEMAP.getValue();
        }

        @Override
        public void execute(final GUI paramGUI) {
            GUIProp.EShowViews.SHOWTREE.invert();
            paramGUI.getViewContainer().layoutViews();
        }
    },

    /**
     * Show sunburst view.
     */
    SUNBURST("Sunburst", EMenu.CHECKBOXITEM) {
        @Override
        public boolean selected() {
            return GUIProp.EShowViews.SHOWSUNBURST.getValue();
        }

        @Override
        public void execute(final GUI paramGUI) {
            GUIProp.EShowViews.SHOWSUNBURST.invert();
            paramGUI.getViewContainer().layoutViews();
        }
    };

    /** Logger. */
    private static final Logger LOGWRAPPER = LoggerFactory.getLogger(GUICommands.class);

    /** Description of command. */
    private final String mDesc;

    /** Determines menu entry type. */
    private final EMenu mType;

    /**
     * Constructor.
     * 
     * @param paramDesc
     *            Description of command
     * @param paramType
     *            Determines if menu item is checked or not
     */
    GUICommands(final String paramDesc, final EMenu paramType) {
        mDesc = paramDesc;
        mType = paramType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String desc() {
        return mDesc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EMenu type() {
        return mType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean selected() {
        throw new IllegalStateException("May not be invoked on this command!");
    }

    /**
     * Shredder or shredder into.
     * 
     * @param paramGUI
     *            Main GUI frame
     * @param paramShredding
     *            Determines which shredder to use
     */
    private static void shredder(final GUI paramGUI, final EShredder paramShredding) {
        // Create a file chooser.
        final JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showOpenDialog(paramGUI) == JFileChooser.APPROVE_OPTION) {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            final File source = fc.getSelectedFile();

            if (fc.showSaveDialog(paramGUI) == JFileChooser.APPROVE_OPTION) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                final File target = fc.getSelectedFile();

                paramShredding.shred(source, target);

                try {
                    final IDatabase database = Database.openDatabase(target);
                    final ISession session = database.getSession();
                    final IReadTransaction rtx = session.beginReadTransaction();
                    final long rev = rtx.getRevisionNumber();
                    rtx.close();
                    session.close();
                    database.close();
                    paramGUI.execute(target, rev);
                } catch (final AbsTTException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                }
            }
        }
    }
}
