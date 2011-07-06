/**
 * 
 */
package org.treetank.gui.view.sunburst;

import org.treetank.gui.view.EHover;

import processing.core.PGraphics;

/**
 * 
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public enum EGreyState {
    /** 
     * Current {@link SunburstItem} doesn't have to be greyed out.
     */
    NO {
        @Override
        public void setStroke(final PGraphics paramGraphic, final PGraphics paramRecorder, int paramColor,
            EHover paramHover) {
            // Do nothing.
        }
    },

    /**
     * Current {@link SunburstItem} has to be greyed out.
     */
    YES {
        @Override
        public void setStroke(PGraphics paramGraphic, PGraphics paramRecorder, int paramColor,
            EHover paramHover) {
            paramGraphic.stroke(0);
        }
    };

    /**
     * Set stroke.
     * 
     * @param paramGraphic
     *            {@link PGraphics} instance
     * @param paramRecorder
     *            {@link PGraphics} instance for recording PDFs
     * @param paramColor
     *            the color to use
     * @param paramHover
     *            determines if current item should be hovered or not
     */
    public abstract void setStroke(final PGraphics paramGraphic, final PGraphics paramRecorder,
        final int paramColor, final EHover paramHover);
}
