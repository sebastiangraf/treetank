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

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public enum EDraw {
    /** Draw directly. */
    DRAW {
        /**
         * {@inheritDoc}
         */
        @Override
        void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mShowArcs) {
                if (paramGUI.mUseArc) {
                    paramItem.drawArc(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale);
                } else {
                    paramItem.drawRect(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale);
                }
            }

            if (paramGUI.mShowLines) {
                if (paramGUI.mUseBezierLine) {
                    paramItem.drawRelationBezier();
                } else {
                    paramItem.drawRelationLine();
                }
            }

            paramItem.drawDot();
        }
    },

    /** Draw into buffer. */
    UPDATEBUFFER {
        /**
         * {@inheritDoc}
         */
        @Override
        void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mShowArcs) {
                if (paramGUI.mUseArc) {
                    paramItem.drawArcBuffer(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale,
                        paramGUI.mBuffer);
                } else {
                    paramItem.drawRectBuffer(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale,
                        paramGUI.mBuffer);
                }
            }

            if (paramGUI.mShowLines) {
                if (paramGUI.mUseBezierLine) {
                    paramItem.drawRelationBezierBuffer(paramGUI.mBuffer);
                } else {
                    paramItem.drawRelationLineBuffer(paramGUI.mBuffer);
                }
            }

            paramItem.drawDotBuffer(paramGUI.mBuffer);
        }
    };

    /**
     * Drawing strategy.
     * 
     * @param paramGUI
     *            {@link SunburstGUI}.
     * @param paramItem
     *            {@link SunburstItem} to draw
     */
    abstract void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem);
}
