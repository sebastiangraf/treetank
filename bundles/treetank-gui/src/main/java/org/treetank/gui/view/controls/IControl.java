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
package org.treetank.gui.view.controls;

import java.awt.event.*;
import java.util.EventListener;
import java.util.concurrent.CountDownLatch;

import controlP5.ControlEvent;
import controlP5.ControlListener;

import org.treetank.gui.view.IVisualItem;
import org.treetank.gui.view.model.IModel;

/**
 * Listener interface for processing user interface events on a Display.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 */
public interface IControl extends EventListener, MouseListener, MouseMotionListener, MouseWheelListener,
    KeyListener, ControlListener {
    /** Represents the use of the left mouse button */
    public static final int LEFT_MOUSE_BUTTON = InputEvent.BUTTON1_MASK;
    /** Represents the use of the middle mouse button */
    public static final int MIDDLE_MOUSE_BUTTON = InputEvent.BUTTON2_MASK;
    /** Represents the use of the right mouse button */
    public static final int RIGHT_MOUSE_BUTTON = InputEvent.BUTTON3_MASK;

    /**
     * Indicates if this Control is currently enabled.
     * 
     * @return true if the control is enabled, false if disabled
     */
    public boolean isEnabled();

    /**
     * Sets the enabled status of this control.
     * 
     * @param enabled
     *            true to enable the control, false to disable it
     */
    public void setEnabled(boolean enabled);

    // -- Actions performed on VisualItems ------------------------------------

    /**
     * Invoked when a mouse button is pressed on a VisualItem and then dragged.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link MouseEvent} instance
     */
    public void itemDragged(IVisualItem paramItem, MouseEvent paramEvent);

    /**
     * Invoked when the mouse cursor has been moved onto a VisualItem but
     * no buttons have been pushed.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link MouseEvent} instance
     */
    public void itemMoved(IVisualItem paramItem, MouseEvent paramEvent);

    /**
     * Invoked when the mouse wheel is rotated while the mouse is over a
     * VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link MouseWheelEvent} instance
     */
    public void itemWheelMoved(IVisualItem paramItem, MouseWheelEvent paramEvent);

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * a VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link MouseEvent} instance
     */
    public void itemClicked(IVisualItem paramItem, MouseEvent paramEvent);

    /**
     * Invoked when a mouse button has been pressed on a VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link MouseEvent} instance
     */
    public void itemPressed(IVisualItem paramItem, MouseEvent paramEvent);

    /**
     * Invoked when a mouse button has been released on a VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link MouseEvent} instance
     */
    public void itemReleased(IVisualItem paramItem, MouseEvent paramEvent);

    /**
     * Invoked when the mouse enters a VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link MouseEvent} instance
     */
    public void itemEntered(IVisualItem paramItem, MouseEvent paramEvent);

    /**
     * Invoked when the mouse exits a VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link MouseEvent} instance
     */
    public void itemExited(IVisualItem paramItem, MouseEvent paramEvent);

    /**
     * Invoked when a key has been pressed, while the mouse is over
     * a VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link KeyEvent} instance
     */
    public void itemKeyPressed(IVisualItem paramItem, KeyEvent paramEvent);

    /**
     * Invoked when a key has been released, while the mouse is over
     * a VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link KeyEvent} instance
     */
    public void itemKeyReleased(IVisualItem paramItem, KeyEvent paramEvent);

    /**
     * Invoked when a key has been typed, while the mouse is over
     * a VisualItem.
     * 
     * @param paramItem
     *            {@link IVisualItem} instance
     * @param paramEvent
     *            {@link KeyEvent} instance
     */
    public void itemKeyTyped(IVisualItem paramItem, KeyEvent paramEvent);

    // -- Actions performed on the Display ------------------------------------

    /**
     * Invoked when the mouse enters the Display.
     */
    @Override
    public void mouseEntered(MouseEvent paramEvent);

    /**
     * Invoked when the mouse exits the Display.
     */
    @Override
    public void mouseExited(MouseEvent paramEvent);

    /**
     * Invoked when a mouse button has been pressed on the Display but NOT
     * on a VisualItem.
     */
    @Override
    public void mousePressed(MouseEvent paramEvent);

    /**
     * Invoked when a mouse button has been released on the Display but NOT
     * on a VisualItem.
     */
    @Override
    public void mouseReleased(MouseEvent paramEvent);

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * the Display, but NOT on a VisualItem.
     */
    @Override
    public void mouseClicked(MouseEvent paramEvent);

    /**
     * Invoked when a mouse button is pressed on the Display (but NOT a
     * VisualItem) and then dragged.
     */
    @Override
    public void mouseDragged(MouseEvent paramEvent);

    /**
     * Invoked when the mouse cursor has been moved on the Display (but NOT a
     * VisualItem) and no buttons have been pushed.
     */
    @Override
    public void mouseMoved(MouseEvent paramEvent);

    /**
     * Invoked when the mouse wheel is rotated while the mouse is over the
     * Display (but NOT a VisualItem).
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent paramEvent);

    /**
     * Invoked when a key has been pressed, while the mouse is NOT
     * over a VisualItem.
     */
    @Override
    public void keyPressed(KeyEvent paramEvent);

    /**
     * Invoked when a key has been released, while the mouse is NOT
     * over a VisualItem.
     */
    @Override
    public void keyReleased(KeyEvent paramEvent);

    /**
     * Invoked when a key has been pressed (for processing), while the mouse is NOT
     * over a VisualItem.
     */
    public void keyPressed();

    /**
     * Invoked when a key has been released (for processing), while the mouse is NOT
     * over a VisualItem.
     */
    public void keyReleased();

    /**
     * Invoked when a key has been typed, while the mouse is NOT
     * over a VisualItem.
     */
    @Override
    public void keyTyped(KeyEvent paramEvent);

    /**
     * Get model.
     * 
     * @return Model associated with the Controller
     */
    public IModel getModel();
}
