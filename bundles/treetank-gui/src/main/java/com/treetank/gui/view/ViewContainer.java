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
package com.treetank.gui.view;

import java.awt.FlowLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.treetank.gui.GUI;

/**
 * Container for all views.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class ViewContainer extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -9151769742021251809L;

    /** Container singleton instance. */
    private static ViewContainer mContainer;

    /** Main {@link GUI} reference. */
    private final GUI mGUI;

    /** All implementations of the {@link IView} instance. */
    private final IView[] mViews;

    /**
     * Private constructor.
     * 
     * @param paramGUI
     *            the main {@link GUI} reference
     * @param paramViews
     *            views to layout
     */
    private ViewContainer(final GUI paramGUI, final IView... paramViews) {
        mGUI = paramGUI;
        mViews = paramViews;
        setLayout(new FlowLayout());
    }

    /**
     * Create a {@link ViewContainer} singleton instance.
     * 
     * @param paramGUI
     *            the main {@link GUI} reference
     * @param paramViews
     *            {@link IView} implementations to layout
     * @return {@link ViewContainer} singleton instance
     */
    public static ViewContainer getInstance(final GUI paramGUI, final IView... paramViews) {
        if (mContainer == null) {
            mContainer = new ViewContainer(paramGUI, paramViews);
        }

        return mContainer;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void revalidate() {
        super.revalidate();
    }

    /** Layout the views. */
    public void layoutViews() {
        removeAll();
        
        final List<IView> views = visibleViews();

        JComponent tmpView = null;
        int width = 0;
        int i = 0;
        for (final IView view : views) {
            if (views.size() == 1) {
                add(view.component());
                tmpView = view.component();
                break;
//            } else if (i % 2 != 0) {
            } else if (i % 2 != 0 && width < mGUI.getSize().width) {
                assert tmpView != null;
                final JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                if (tmpView instanceof IView) {
                    tmpView = ((IView)tmpView).component();
                }
                pane.setLeftComponent(tmpView);
                pane.setRightComponent(view.component());
                add(pane);
                tmpView = pane;
            } else if (i > 0) {
                assert tmpView != null;
                final JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                pane.setTopComponent(tmpView);
                pane.setBottomComponent(view.component());
                add(pane);
                tmpView = view.component();
            } else {
                tmpView = view.component();
            }
            
            i++;
            width += tmpView.getPreferredSize().width;
        }

        super.revalidate();
        repaint();
    }

    /**
     * Get list of visible views.
     * 
     * @return list of visible views
     */
    private List<IView> visibleViews() {
        final List<IView> views = new LinkedList<IView>();

        for (final IView view : mViews) {
            if (view.isVisible()) {
                views.add(view);
            }
        }

        return views;
    }
}
