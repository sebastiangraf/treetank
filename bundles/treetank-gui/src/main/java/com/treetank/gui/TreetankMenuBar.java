package com.treetank.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public final class TreetankMenuBar extends JMenuBar {

    /**
     * Constructor.
     * 
     * @param gui
     *            Main GUI frame.
     */
    public TreetankMenuBar(final GUI gui) {
        // Loop through all menu entries
        for (int i = 0; i < MENUBAR.length; i++) {
            final JMenu menu = new JMenu(MENUBAR[i]);
            menu.setMnemonic((int)MENUBAR[i].charAt(0));

            for (int j = 0; j < MENUITEMS[i].length; j++) {
                final GUICommand cmd = (GUICommand)MENUITEMS[i][j];
                final JMenuItem item = new JMenuItem(cmd.desc());
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        cmd.execute(e, gui);
                    }
                });
                item.setMnemonic(cmd.desc().charAt(0));
                menu.add(item);
            }

            add(menu);
        }
    }
}
