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
package org.treetank.gui.view.sunburst;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import controlP5.ControlGroup;

import org.treetank.gui.view.sunburst.SunburstView.Embedded;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.EShredderInsert;

import processing.core.PApplet;

/**
 * Menu options.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
enum EMenu {
    /** Insert XML fragment as first child. */
    INSERT_FRAGMENT_AS_FIRST_CHILD {
        @Override
        void createMenuItem(final SunburstGUI paramGUI, final JPopupMenu paramMenu,
            final IWriteTransaction paramWtx, final ControlGroup paramCtrl) {
            // Create and add a menu item
            final JMenuItem item = new JMenuItem("insert as first child");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    paramCtrl.setVisible(true);
                    paramCtrl.open();
                    paramGUI.setInsert(EShredderInsert.ADDASFIRSTCHILD);
                }
            });
            paramMenu.add(item);

        }
    },

    /** Insert XML fragment as right sibling. */
    INSERT_FRAGMENT_AS_RIGHT_SIBLING {
        @Override
        void createMenuItem(final SunburstGUI paramGUI, final JPopupMenu paramMenu,
            final IWriteTransaction paramWtx, final ControlGroup paramCtrl) {
            // Create and add a menu item
            final JMenuItem item = new JMenuItem("insert as right sibling");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    paramCtrl.setVisible(true);
                    paramCtrl.open();
                    paramGUI.setInsert(EShredderInsert.ADDASRIGHTSIBLING);
                }
            });
            paramMenu.add(item);
        }
    },

    /** Delete node. */
    DELETE {
        @Override
        void createMenuItem(final SunburstGUI paramGUI, final JPopupMenu paramMenu,
            final IWriteTransaction paramWtx, final ControlGroup paramCtrl) {
            // Create and add a menu item
            final JMenuItem item = new JMenuItem("delete node");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    delete(paramGUI.mParent, paramWtx);
                }
            });
            paramMenu.add(item);
        }

        /**
         * Delete the current node, and it's subtree.
         * 
         * @param paramParent
         *            {@link PApplet} instance
         * @param param
         *            {@link IWriteTransaction} instance
         */
        private void delete(final PApplet paramParent, final IWriteTransaction paramWtx) {
            try {
                paramWtx.remove();
                paramWtx.commit();
                paramWtx.close();
                ((Embedded) paramParent).refresh();
            } catch (final AbsTTException e) {
                JOptionPane.showMessageDialog(paramParent, "Failed to delete node: " + e.getMessage());
            }
        }
    };

    abstract void createMenuItem(final SunburstGUI paramGUI, final JPopupMenu paramMenu,
        final IWriteTransaction paramWtx, final ControlGroup paramCtrl);
}
