package com.treetank.gui.view.tree;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * <h1>TreetankTree</h1>
 * 
 * <p>
 * Provides highlighting functionality to highlight subtrees of selected nodes.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 * 
 */
public class TreetankTree extends JTree {

    /**
     * Generated UID.
     */
    private static final long serialVersionUID = -4157303763028056619L;

    /** Highlight path. */
    private TreePath mSelectionPath = null;

    /** Color used to highlight selected subtrees. */
    private Color highlightColor = new Color(255, 255, 204);

    /**
     * {@inheritDoc}
     */
    public TreetankTree() {
        super();
    }

    /**
     * Constructor with TreeModel parameter.
     * 
     * @param model
     *            TreeModel to use.
     */
    public TreetankTree(final TreeModel model) {
        super(model);
    }

    /**
     * Set the selection path.
     * 
     * @param selectionPath
     *            Selection path.
     */
    @Override
    public void setSelectionPath(final TreePath selectionPath) {
        mSelectionPath = selectionPath;
        getSelectionModel().setSelectionPath(selectionPath);
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
    protected void paintComponent(final Graphics g) {
        // Paint background ourself.
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        // Paint the highlight if any.
        g.setColor(highlightColor);
        int fromRow = getRowForPath(mSelectionPath);

        if (fromRow != -1) {
            int toRow = fromRow;

            while(toRow < getRowCount()) {
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
            g.fillRect(0, fromBounds.y, getWidth(), toBounds.y - fromBounds.y + toBounds.height);
        }

        setOpaque(false); // trick not to paint background
        super.paintComponent(g);
        setOpaque(true);
    }
}
