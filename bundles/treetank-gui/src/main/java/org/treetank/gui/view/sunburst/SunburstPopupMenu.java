/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
