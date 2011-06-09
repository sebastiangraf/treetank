/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.gui.view.sunburst;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import controlP5.ControlGroup;

import org.treetank.gui.view.sunburst.SunburstView.Embedded;
import org.treetank.gui.view.model.AbsModel;
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
        void createMenuItem(final AbsModel paramModel, final JPopupMenu paramMenu,
            final IWriteTransaction paramWtx, final ControlGroup paramCtrl) {
            // Create and add a menu item
            final JMenuItem item = new JMenuItem("insert as first child");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    paramCtrl.setVisible(true);
                    paramCtrl.open();
                    paramModel.setInsert(EShredderInsert.ADDASFIRSTCHILD);
                }
            });
            paramMenu.add(item);

        }
    },

    /** Insert XML fragment as right sibling. */
    INSERT_FRAGMENT_AS_RIGHT_SIBLING {
        @Override
        void createMenuItem(final AbsModel paramModel, final JPopupMenu paramMenu,
            final IWriteTransaction paramWtx, final ControlGroup paramCtrl) {
            // Create and add a menu item
            final JMenuItem item = new JMenuItem("insert as right sibling");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    paramCtrl.setVisible(true);
                    paramCtrl.open();
                    paramModel.setInsert(EShredderInsert.ADDASRIGHTSIBLING);
                }
            });
            paramMenu.add(item);
        }
    },

    /** Delete node. */
    DELETE {
        @Override
        void createMenuItem(final AbsModel paramModel, final JPopupMenu paramMenu,
            final IWriteTransaction paramWtx, final ControlGroup paramCtrl) {
            // Create and add a menu item
            final JMenuItem item = new JMenuItem("delete node");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent paramEvent) {
                    delete(paramModel.getParent(), paramWtx);
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
                ((Embedded)paramParent).refresh();
            } catch (final AbsTTException e) {
                JOptionPane.showMessageDialog(paramParent, "Failed to delete node: " + e.getMessage());
            }
        }
    };

    /**
     * Create a menu item.
     * 
     * @param paramModel
     *            the model
     * @param paramMenu
     *            {@link JPopupMenu} reference
     * @param paramWtx
     *            {@link IWriteTransaction} reference to delete a subtree
     * @param paramCtrl
     *            {@link ControlGroup} to add XML fragments
     */
    abstract void createMenuItem(final AbsModel paramModel, final JPopupMenu paramMenu,
        final IWriteTransaction paramWtx, final ControlGroup paramCtrl);
}
