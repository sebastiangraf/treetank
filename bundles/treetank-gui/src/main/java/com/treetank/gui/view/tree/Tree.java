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
package com.treetank.gui.view.tree;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * <h1>Tree</h1>
 * 
 * <p>
 * Provides highlighting functionality to highlight subtrees of selected nodes.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 * 
 */
public final class Tree extends JTree {

    /** Generated UID. */
    private static final long serialVersionUID = -4157303763028056619L;

    /** Color used to highlight selected subtrees. */
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 204);

    /** Highlight path. */
    private TreePath mSelectionPath;

    /**
     * Default Constructor.
     */
    public Tree() {
    }

    /**
     * Constructor with TreeModel parameter.
     * 
     * @param model
     *            TreeModel to use.
     */
    public Tree(final TreeModel model) {
        super(model);
    }

    /**
     * Set the selection path.
     * 
     * @param paramSelectionPath
     *            Selection path.
     */
    @Override
    public void setSelectionPath(final TreePath paramSelectionPath) {
        mSelectionPath = paramSelectionPath;
        getSelectionModel().setSelectionPath(paramSelectionPath);
        treeDidChange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TreePath getSelectionPath() {
        return mSelectionPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(final Graphics paramGraphics) {
        // Paint background ourself.
        paramGraphics.setColor(getBackground());
        paramGraphics.fillRect(0, 0, getWidth(), getHeight());

        // Paint the highlight if any.
        paramGraphics.setColor(HIGHLIGHT_COLOR);
        final int fromRow = getRowForPath(mSelectionPath);

        if (fromRow != -1) {
            int toRow = fromRow;

            while (toRow < getRowCount()) {
                final TreePath path = getPathForRow(toRow);
                if (mSelectionPath.isDescendant(path)) {
                    toRow++;
                } else {
                    break;
                }
            }

            // Paint a rectangle.
            final Rectangle fromBounds = getRowBounds(fromRow);
            final Rectangle toBounds = getRowBounds(toRow - 1);
            paramGraphics.fillRect(0, fromBounds.y, getWidth(), toBounds.y - fromBounds.y + toBounds.height);
        }

        setOpaque(false); // trick not to paint background
        super.paintComponent(paramGraphics);
        setOpaque(true);
    }
}
