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

import processing.core.PApplet;

/**
 * XPath enum to determine if current item is found by an XPath expression or not.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public enum EXPathState {
    /** Item is found. */
    ISFOUND(StateType.ISFOUND),

    /** Default: Item is not found. */
    ISNOTFOUND(StateType.ISNOTFOUND);

    /** {@link StateType}. */
    private final StateType mStateType;

    /**
     * Constructor.
     * 
     * @param paramType
     *            private {@link StateType}
     */
    EXPathState(final StateType paramType) {
        mStateType = paramType;
    }

    /**
     * Set stroke.
     * 
     * @param paramApplet
     *            Processing {@link PApplet} core.
     * @param paramColor
     *            The color to use.
     */
    void setStroke(final PApplet paramApplet, final int paramColor) {
        mStateType.setStroke(paramApplet, paramColor);
    }

    /** Determines state of XPath expression evaluation. */
    private enum StateType {
        /** Item is found. */
        ISFOUND {
            @Override
            void setStroke(final PApplet paramApplet, final int paramColor) {
                paramApplet.stroke(1);
            }
        },

        /** Default: Item is not found. */
        ISNOTFOUND {
            @Override
            void setStroke(final PApplet paramApplet, final int paramColor) {
                paramApplet.stroke(paramColor);
            }
        };

        /**
         * Set stroke.
         * 
         * @param paramApplet
         *            Processing {@link PApplet} core.
         * @param paramColor
         *            The color to use.
         */
        abstract void setStroke(final PApplet paramApplet, final int paramColor);
    }
}
