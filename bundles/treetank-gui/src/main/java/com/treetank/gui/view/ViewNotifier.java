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

import java.util.HashSet;
import java.util.Set;

import com.treetank.gui.GUI;
import com.treetank.gui.IView;

/**
 * <h1>ViewNotifier</h1>
 * 
 * <p>Notifies views of changes (observer pattern).</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class ViewNotifier {
    /** Reference to main window. */
    private final GUI mGUI;

    /** Attached views. */
    private Set<IView> mViews;

    /**
     * Constructor.
     * 
     * @param paramGUI
     *            Reference to {@link GUI}.
     */
    public ViewNotifier(final GUI paramGUI) {
        mGUI = paramGUI;
        mViews = new HashSet<IView>();
    }

    /**
     * Adds a new view.
     * 
     * @param paramView
     *            view to be added
     */
    public void add(final IView paramView) {
        mViews.add(paramView);
    }
    
    /**
     * Notifies all views of a data reference change.
     */
    public void init() {
        for (final IView view : mViews) {
            view.refreshInit();
        }
    }
    
    /**
     * Notifies all views of updates in the data structure.
     */
    public void update() {
        for (final IView view : mViews) {
            if (view.isVisible()) {
                view.refreshUpdate();
            }
        }
    }
    
    /** 
     * Get the main {@link GUI} frame. 
     * 
     * @return the gui 
     */
    public GUI getGUI() {
        return mGUI;
    }
}
