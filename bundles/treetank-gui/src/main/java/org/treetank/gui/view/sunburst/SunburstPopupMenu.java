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

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.xml.namespace.QName;

import controlP5.ControlGroup;
import controlP5.Textarea;

import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.sunburst.SunburstView.Embedded;

import processing.core.PApplet;

/**
 * Sunburst PopupMenu to insert and delete nodes.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstPopupMenu extends JPopupMenu {

    /** {@link SunburstGUI} instance. */
    private final SunburstGUI mGUI;

    /** Treetank {@link IWriteTransaction}. */
    private final IWriteTransaction mWtx;

    /** Textarea for XML fragment input. */
    private final ControlGroup mCtrl;

    /**
     * Constructor.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramWtx
     *            Treetank {@link IWriteTransaction}
     * @param paramCtrl
     *            control group for XML input
     */
    SunburstPopupMenu(final SunburstGUI paramGUI, final IWriteTransaction paramWtx,
        final ControlGroup paramCtrl) {
        mGUI = paramGUI;
        mWtx = paramWtx;
        mCtrl = paramCtrl;

        switch (mWtx.getNode().getKind()) {
        case ELEMENT_KIND:
            createMenu();
            break;
        case TEXT_KIND:
            EMenu.DELETE.createMenuItem(mGUI, this, mWtx, mCtrl);
            break;
        }
    }

    /**
     * Create all menu items.
     */
    private void createMenu() {
        for (EMenu menu : EMenu.values()) {
            // Create and add a menu item
            menu.createMenuItem(mGUI, this, mWtx, mCtrl);
        }
    }
}
