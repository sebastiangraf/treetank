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
import java.awt.Dimension;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.gui.GUI;
import com.treetank.gui.GUIProp;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.IView;
import com.treetank.gui.view.ViewNotifier;

/**
 * <h1>TreeView</h1>
 * 
 * <p>
 * Tree view on a Treetank storage.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TreeView extends JScrollPane implements IView {
    
    /**
     * 
     */
    private static final long serialVersionUID = 5191158290313970043L;

    /** Name of the view. */
    private static final String NAME = "TreeView";
    
    /** Row height. */
    private static final int ROW_HEIGHT = 20;

    /** {@link TreeView} instance. */
    private static TreeView mView;
    
    /** A {@link JTree} instance. */
    private final JTree mTree;

    /** {@link ViewNotifier}, which notifies views of changes. */
    private final ViewNotifier mNotifier;

    /** Main {@link GUI} window. */
    private final GUI mGUI;

    /** Treetank {@link IReadTransaction}. */
    private transient IReadTransaction mRtx;

    /**
     * Private Constructor, called from singleton factory method.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} to notify views of changes etc.pp.
     */
    private TreeView(final ViewNotifier paramNotifier) {
        mNotifier = paramNotifier;
        mGUI = paramNotifier.getGUI();

        // Add view to notifier.
        mNotifier.add(this);

        // Build tree view.
        mTree = new Tree(null);
        mTree.setBackground(Color.WHITE);

        /*
         * Performance tweak to use FixedLayoutManager and only invoke
         * getChild(..) for nodes inside view "bounding box". Avoids caching but
         * therefore more rendering calls.
         */
        mTree.setRowHeight(ROW_HEIGHT);
        mTree.setLargeModel(true);

        // Selection Model.
        mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Add the tree to the scroll pane.
        setViewportView(mTree);
        setBackground(Color.WHITE);
    }
    
    /**
     * Singleton factory.
     * 
     * @param paramNotifier
     *            {@link ViewNotifier} to notify views of changes etc.pp.
     * @return {@link TreeView} instance.
     */
    public static TreeView getInstance(final ViewNotifier paramNotifier) {
        if (mView == null) {
            mView = new TreeView(paramNotifier);
        }
        
        return mView;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isVisible() {
        return GUIProp.EShowViews.SHOWTREE.getValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return NAME;
    };
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent component() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void refreshUpdate() {
        // Use our Treetank model and renderer.
        final ReadDB db = mGUI.getReadDB();
        mRtx = db.getRtx();

        if (mTree.getTreeSelectionListeners().length == 0) {
            // Listen for when the selection changes.
            mTree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(final TreeSelectionEvent paramE) {
                    if (paramE.getNewLeadSelectionPath() != null
                        && paramE.getNewLeadSelectionPath() != paramE.getOldLeadSelectionPath()) {
                        /*
                         * Returns the last path element of the selection. This
                         * method is useful only when the selection model allows
                         * a single selection.
                         */
                        final IItem node = (IItem)paramE.getNewLeadSelectionPath().getLastPathComponent();
                        mRtx.moveTo(node.getNodeKey());
                        mNotifier.update();
                    }
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        for (final FocusListener listener : mTree.getFocusListeners()) {
            mTree.removeFocusListener(listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void refreshInit() {
        // Use our Treetank model and renderer.
        final ReadDB db = mGUI.getReadDB();
        mTree.setModel(new TreeModel(db));
        mTree.setCellRenderer(new TreeCellRenderer(db));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 500);
    }
}
