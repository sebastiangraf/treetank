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
 *     * Neither the name of the <organization> nor the
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

package org.treetank.gui.view;

import java.awt.FlowLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.treetank.gui.GUI;

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
