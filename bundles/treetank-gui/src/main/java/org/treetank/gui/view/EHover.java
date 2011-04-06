/**
 * 
 */
package org.treetank.gui.view;

/**
 * Determines if a {@link IVisualItem} currently is hovered or not.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public enum EHover {
    /** Yes, it's hovered. */
    TRUE {
        @Override
        void setColor(final IVisualItem paramItem) {
            // TODO Auto-generated method stub

        }
    },
    /** No, it's not hovered. */
    FALSE {
        @Override
        void setColor(final IVisualItem paramItem) {

        }
    };

    /**
     * Set hover color.
     * 
     * @param paramItem
     *            {@link IVisualItem} reference
     */
    abstract void setColor(final IVisualItem paramItem);
}
