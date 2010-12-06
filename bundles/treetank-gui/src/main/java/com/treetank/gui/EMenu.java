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

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

/**
 * Determines and creates the appropriate menu item type.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 */
enum EMenu {
    /** Menu item. */
    MENU {
        @Override
        JComponent construct(final GUI paramGUI, final IGUICommand paramCommand) {
            final JMenuItem item = new JMenuItem(paramCommand.desc());
            setupItem(item, paramGUI, paramCommand);
            return item;
        }
    },

    /** Separator item. */
    SEPARATOR {
        @Override
        JComponent construct(final GUI paramGUI, final IGUICommand paramCommand) {
            return new JSeparator();
        }
    },

    /** Checkbox item. */
    CHECKBOXITEM {
        @Override
        JComponent construct(final GUI paramGUI, final IGUICommand paramCommand) {
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem(paramCommand.desc());
            setupItem(item, paramGUI, paramCommand);
            return item;
        }
    },

    /** Radio button item. */
    RADIOBUTTONITEM {
        @Override
        JComponent construct(final GUI paramGUI, final IGUICommand paramCommand) {
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(paramCommand.desc());
            setupItem(item, paramGUI, paramCommand);
            return item;
        }
    };

    /**
     * Construct menu item.
     * 
     * @param paramGUI
     *            reference to main GUI frame
     * @param paramCommand
     *            the {@link IGUICommand}
     * @return component reference
     */
    abstract JComponent construct(final GUI paramGUI, final IGUICommand paramCommand);

    /**
     * Setup a menu item.
     * 
     * @param paramItem
     *            the item to set up
     * @param paramGUI
     *            reference to main GUI frame
     * @param paramCommand
     *            the menu command
     */
    void setupItem(final JMenuItem paramItem, final GUI paramGUI, final IGUICommand paramCommand) {
        paramItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent paramEvent) {
                paramCommand.execute(paramGUI);
            }
        });

        paramItem.setMnemonic(paramCommand.desc().charAt(0));
    }
}
