/**
 * 
 */
package org.treetank.gui.controls;

import java.awt.event.*;
import java.util.EventListener;

import org.treetank.gui.view.IVisualItem;

/**
 * Listener interface for processing user interface events on a Display.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 */
public interface IControl extends EventListener, MouseListener, MouseMotionListener, MouseWheelListener,
    KeyListener {
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
}
