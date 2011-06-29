/**
 * 
 */
package org.treetank.gui.view;

/**
 * Interface for processing GUIs.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public interface IProcessingGUI {

    /** Processing.org draw method. */
    void draw();

    /**
     * Update the GUI.
     */
    void update();

    /**
     * Relocate ControlP5 stuff after the frame which includes the view has been resized.
     */
    void relocate();
}
