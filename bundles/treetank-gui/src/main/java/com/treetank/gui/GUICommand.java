package com.treetank.gui;

import java.awt.event.ActionEvent;

/**
 * Interface for GUI menus.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public interface GUICommand {
    /**
     * Invokes a command.
     * 
     * @param e
     *            The action event.
     * @param gui
     *            Main GUI frame.
     */
    void execute(final ActionEvent e, final GUI gui);

    /**
     * Description of the command.
     * 
     * @return the description.
     */
    String desc();
}
