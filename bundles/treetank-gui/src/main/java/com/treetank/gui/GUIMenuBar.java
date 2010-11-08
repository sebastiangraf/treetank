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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import static com.treetank.gui.GUIConstants.*;

/**
 * This is the menu bar of the main window.
 * The menu structure is defined in {@link GUIConstants#MENUBAR} and {@link GUIConstants#MENUITEMS}. Based on
 * BaseX version.
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 */
@SuppressWarnings("serial")
public final class GUIMenuBar extends JMenuBar {

    /**
     * Constructor.
     * 
     * @param paramGUI
     *            Main GUI frame.
     */
    public GUIMenuBar(final GUI paramGUI) {
        // Loop through all menu entries
        for (int i = 0; i < MENUBAR.length; i++) {
            final JMenu menu = new JMenu(MENUBAR[i]);
            menu.setMnemonic((int)MENUBAR[i].charAt(0));

            for (int j = 0; j < MENUITEMS[i].length; j++) {
                final IGUICommand cmd = (IGUICommand)MENUITEMS[i][j];
                final JMenuItem item =
                    cmd.checked() ? new JCheckBoxMenuItem(cmd.desc()) : new JMenuItem(cmd.desc());
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent paramEvent) {
                        cmd.execute(paramGUI);
                    }
                });

                item.setMnemonic(cmd.desc().charAt(0));
                menu.add(item);
            }

            add(menu);
        }
    }
}
