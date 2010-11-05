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

import java.awt.event.MouseEvent;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.gui.GUIProp;
import com.treetank.gui.IView;
import com.treetank.gui.view.ViewNotifier;

import processing.core.PApplet;

/**
 * <h1>SunView</h1>
 * 
 * <p>
 * Main sunbirst class.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstView extends PApplet implements IView {

    /** Width of the frame. */
    private static final int WIDTH = 1000;

    /** Height of the frame. */
    private static final int HEIGHT = 800;

    /** {@link SunburstGUI} which represents the GUI interface of the Sunburst view. */
    private transient SunburstGUI mGUI;
    
    /** {@link ViewNotifier} to notify views of changes. */
    private final ViewNotifier mNotifier;

    /**
     * Constructor.
     * 
     * @param paramNotifier
     *                  {@link ViewNotifier} instance.
     */
    public SunburstView(final ViewNotifier paramNotifier) {
        mNotifier = paramNotifier;
    }
    
    @Override
    public void setup() {
        size(WIDTH, HEIGHT);

        final SunburstController<SunburstModel, SunburstGUI> controller =
            new SunburstController<SunburstModel, SunburstGUI>();
        mGUI = SunburstGUI.createGUI(this, controller);
        final SunburstModel model = new SunburstModel(this, mNotifier.getGUI().getReadDB(), controller);

        controller.addView(mGUI);
        controller.addModel(model);

        frame.setTitle("press 'o' to select an input folder!");

        mGUI.setupGUI();
    }
    
    

    @Override
    public void draw() {
        mGUI.draw();
    }

    @Override
    public void mouseEntered(final MouseEvent paramEvent) {
        mGUI.mouseEntered(paramEvent);
    }

    @Override
    public void mouseExited(final MouseEvent paramEvent) {
        mGUI.mouseExited(paramEvent);
    }

    @Override
    public void keyReleased() {
        mGUI.keyReleased();
    }
    

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public boolean isVisible() {
        return GUIProp.EShowViews.SHOWSUNBURST.getValue();
    }

    @Override
    public void refreshUpdate() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void refreshInit() {
        // TODO Auto-generated method stub
        
    }

    /**
     * Main method.
     * 
     * @param args
     *            Arguments not used.
     */
    public static void main(final String[] args) {
        PApplet.main(new String[] {
            "--present", "Sunburst"
        });
    }
}
