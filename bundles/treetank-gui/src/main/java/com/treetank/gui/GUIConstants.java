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
package com.treetank.gui;

import static com.treetank.gui.GUICommands.*;

import java.awt.Color;

/**
 * <h1>GUIConstants</h1>
 * 
 * <p>Some constants which are used all over the GUI packages.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz.
 * 
 */
public final class GUIConstants {
    
    // COLORS ===================================================================
    
    /** Document root color. */
    public static final Color DOC_COLOR = new Color(128, 0, 0);
    
    /** Element color. */
    public static final Color ELEMENT_COLOR = new Color(0, 0, 128);

    /** Attribute color. */
    public static final Color ATTRIBUTE_COLOR = new Color(0, 128, 0);
    
    /** Namespace color. */
    public static final Color NAMESPACE_COLOR = new Color(128, 128, 128);
    
    /** Text color. */
    public static final Color TEXT_COLOR = Color.BLACK;
  
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
            OPEN, SHREDDER, SHREDDER_UPDATE, SERIALIZE, QUIT
        }, {
            TREE, TEXT, TREEMAP
        }
    };
    
    /**
     * Constructor.
     */
    private GUIConstants() {
        // No instance allowed.
    }
}
