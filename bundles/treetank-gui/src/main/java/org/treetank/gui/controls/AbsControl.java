/**
 * 
 */
package org.treetank.gui.controls;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import org.treetank.gui.view.IVisualItem;

/**
 * Abstract class to simplify {@link IControl} implementation. Only necessary methods
 * have to be overriden.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class AbsControl implements IControl {

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enabled) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemDragged(IVisualItem paramItem, MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemMoved(IVisualItem paramItem, MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemWheelMoved(IVisualItem paramItem, MouseWheelEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemClicked(IVisualItem paramItem, MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemPressed(IVisualItem paramItem, MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemReleased(IVisualItem paramItem, MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemEntered(IVisualItem paramItem, MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemExited(IVisualItem paramItem, MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemKeyPressed(IVisualItem paramItem, KeyEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemKeyReleased(IVisualItem paramItem, KeyEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void itemKeyTyped(IVisualItem paramItem, KeyEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void mouseEntered(MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void mouseExited(MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void mouseReleased(MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void mouseDragged(MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void mouseMoved(MouseEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void mouseWheelMoved(MouseWheelEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void keyPressed(KeyEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void keyReleased(KeyEvent paramEvent) {
    }

    /** {@inheritDoc} */
    @Override
    public void keyPressed() {
    }

    /** {@inheritDoc} */
    @Override
    public void keyReleased() {
    }

    /** {@inheritDoc} */
    @Override
    public void keyTyped(KeyEvent paramEvent) {
    }

}
