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
package com.treetank.gui.view.sunburst;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.xml.namespace.QName;

import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.AbsTTException;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.SunburstView.Embedded;

import processing.core.PApplet;

/**
 * Sunburst PopupMenu to insert and delete nodes.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstPopupMenu extends JPopupMenu {

    /** Parent processing {@link PApplet}. */
    private final Embedded mParent;

    /** Treetank {@link IWriteTransaction}. */
    private final IWriteTransaction mWtx;

    /** {@link ReadDB} reference. */
    private transient ReadDB mDb;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            parent processing {@link PApplet}
     * @param paramWtx
     *            Treetank {@link IWriteTransaction}
     * @param paramDb
     *            read Treetank database
     */
    SunburstPopupMenu(final Embedded paramApplet, final IWriteTransaction paramWtx, final ReadDB paramDb) {
        mParent = paramApplet;
        mWtx = paramWtx;
        mDb = paramDb;

        for (EMenu menu : EMenu.values()) {
            // Create and add a menu item
            final JMenuItem item = new JMenuItem(menu.toString());

            switch (menu) {
            case INSERT_ELEMENT_AS_FIRST_CHILD:
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent paramEvent) {
                        // TODO Auto-generated method stub

                    }
                });
                break;
            case INSERT_ELEMENT_AS_RIGHT_SIBLING:
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent paramEvent) {
                        // TODO Auto-generated method stub

                    }
                });
                break;
            case INSERT_TEXT_AS_FIRST_CHILD:
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent paramEvent) {
                        // TODO Auto-generated method stub

                    }
                });
                break;
            case INSERT_TEXT_AS_RIGHT_SIBLING:
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent paramEvent) {
                        // TODO Auto-generated method stub

                    }
                });
                break;
            case DELETE:
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent paramEvent) {
                        delete();
                    }
                });
                break;
            default:
                throw new AssertionError("Enum value not known!");
            }

            add(item);
        }
    }

    /**
     * Insert element as first child of the current node.
     * 
     * @param paramName
     *            {@link QName} of the element node to insert.
     */
    private void insertElementAsFirstChild(final QName paramName) {
        try {
            mWtx.insertElementAsFirstChild(paramName);
        } catch (final AbsTTException e) {
            JOptionPane.showMessageDialog(mParent, "Failed to insert node: " + e.getMessage());
        }

        refresh();
    }

    /**
     * Insert element as right sibling of the current node.
     * 
     * @param paramName
     *            {@link QName} of the element node to insert.
     */
    private void insertElementAsRightSibling(final QName paramName) {
        try {
            mWtx.insertElementAsRightSibling(paramName);
        } catch (final AbsTTException e) {
            JOptionPane.showMessageDialog(mParent, "Failed to insert node: " + e.getMessage());
        }

        refresh();
    }

    /**
     * Insert text as right sibling of the current node.
     * 
     * @param paramText
     *            Text to insert.
     */
    private void insertTextAsFirstChild(final String paramText) {
        try {
            mWtx.insertTextAsFirstChild(paramText);
        } catch (final AbsTTException e) {
            JOptionPane.showMessageDialog(mParent, "Failed to insert node: " + e.getMessage());
        }

        refresh();
    }

    /**
     * Insert text as right sibling of the current node.
     * 
     * @param paramText
     *            Text to insert.
     */
    private void insertTextAsRightSibling(final String paramText) {
        try {
            mWtx.insertTextAsRightSibling(paramText);
        } catch (final AbsTTException e) {
            JOptionPane.showMessageDialog(mParent, "Failed to insert node: " + e.getMessage());
        }

        refresh();
    }

    /** Delete the current node, and it's subtree. */
    private void delete() {
        try {
            mWtx.remove();
        } catch (final AbsTTException e) {
            JOptionPane.showMessageDialog(mParent, "Failed to delete node: " + e.getMessage());
        }

        refresh();
    }

    /**
     * Commit and refresh.
     */
    private void refresh() {
        try {
            mWtx.commit();
            mWtx.close();
            final IReadTransaction rtx = mDb.getSession().beginReadTransaction();
            mDb = new ReadDB(mDb.getDatabase().getFile(), rtx.getRevisionNumber());
            rtx.close();
        } catch (final AbsTTException e) {
            JOptionPane.showMessageDialog(mParent, "Failed to commit change: " + e.getMessage());
        }
        mParent.refreshUpdate();
    }
}
