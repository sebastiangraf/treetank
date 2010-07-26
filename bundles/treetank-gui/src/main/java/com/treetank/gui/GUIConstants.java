package com.treetank.gui;

import static com.treetank.gui.GUICommands.*;

/**
 * <h1>GUIConstants</h1>
 * 
 * <p>
 * Some constants which are used all over the GUI packages.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 * 
 */
public final class GUIConstants {

    // MENU =====================================================================

    /** Menu file. */
    static final String MENUFILE = "File";

    /** Menu views. */
    static final String MENUVIEWS = "Views";

    // MENUBARS =================================================================

    /** Top menu entries. */
    static final String[] MENUBAR = {
        MENUFILE, MENUVIEWS
    };

    /** Separator. */
    static final String SEPARATOR = "-";

    /** Two-dimensional Menu entries, containing the menu item commands. */
    static final Object[][] MENUITEMS = {
        {
            OPEN, SHREDDER, SHREDDER_INTO, SERIALIZE, QUIT
        }, {
            TREE, TEXT, TREEMAP
        }
    };
}
