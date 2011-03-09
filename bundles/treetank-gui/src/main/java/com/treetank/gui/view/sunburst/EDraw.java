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
import processing.core.PConstants;

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

        @Override
        void drawRevision(final SunburstGUI paramGUI) {
            final float arcRadius = calculateRadius(paramGUI);
            paramGUI.mParent.stroke(200f);
            paramGUI.mParent.arc(0, 0, arcRadius, arcRadius, 0, 2 * PConstants.PI);
            paramGUI.mParent.stroke(0f);

            // We must keep track of our position along the curve.
            float arclength = 0;

            final String text = "revision " + paramGUI.mSelectedRev;
            // For every box
            for (int i = 0; i < text.length(); i++) {
                // Instead of a constant width, we check the width of each character.
                final char currentChar = text.charAt(i);
                final float w = paramGUI.mParent.textWidth(currentChar);

                // Each box is centered so we move half the width.
                arclength += w / 2;
                // Angle in radians is the arclength divided by the radius.
                // Starting on the left side of the circle by adding PI.
                final float theta = PConstants.PI + arclength / arcRadius;

                paramGUI.mParent.pushMatrix();
                // Polar to cartesian coordinate conversion.
                paramGUI.mParent.translate(arcRadius * PApplet.cos(theta), arcRadius * PApplet.sin(theta));
                // Rotate the box.
                paramGUI.mParent.rotate(theta + PConstants.PI / 2); // rotation is offset by 90 degrees
                // Display the character.
                paramGUI.mParent.fill(0);
                paramGUI.mParent.text(currentChar, 0, 0);
                paramGUI.mParent.popMatrix();
                // Move halfway again.
                arclength += w / 2;
            }

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

        @Override
        void drawRevision(final SunburstGUI paramGUI) {
            final float arcRadius = calculateRadius(paramGUI);
            paramGUI.mBuffer.stroke(200f);
            paramGUI.mBuffer.arc(0, 0, arcRadius, arcRadius, 0, 2 * PConstants.PI);
            paramGUI.mBuffer.stroke(0f);

            // We must keep track of our position along the curve.
            float arclength = 0;

            final String text = "revision " + paramGUI.mSelectedRev;
            // For every box
            for (int i = 0; i < text.length(); i++) {
                // Instead of a constant width, we check the width of each character.
                final char currentChar = text.charAt(i);
                final float w = paramGUI.mParent.textWidth(currentChar);

                // Each box is centered so we move half the width.
                arclength += w / 2;
                // Angle in radians is the arclength divided by the radius.
                // Starting on the left side of the circle by adding PI.
                final float theta = PConstants.PI + arclength / arcRadius;

                paramGUI.mBuffer.pushMatrix();
                // Polar to cartesian coordinate conversion.
                paramGUI.mBuffer.translate(arcRadius * PApplet.cos(theta), arcRadius * PApplet.sin(theta));
                // Rotate the box.
                paramGUI.mBuffer.rotate(theta + PConstants.PI / 2); // rotation is offset by 90 degrees
                // Display the character.
                paramGUI.mBuffer.fill(0);
                paramGUI.mBuffer.text(currentChar, 0, 0);
                paramGUI.mBuffer.popMatrix();
                // Move halfway again.
                arclength += w / 2;
            }
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

    /**
     * Drawing revision to compare.
     * 
     * @param paramGUI
     *            {@link SunburstGUI}.
     */
    abstract void drawRevision(final SunburstGUI paramGUI);

    private static float calculateRadius(final SunburstGUI paramGUI) {
        final int revisionDepth = paramGUI.mOldDepthMax + 1;
        final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
        final float depthWeight =
            paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
        paramGUI.mParent.strokeWeight(depthWeight);
        paramGUI.mBuffer.strokeWeight(depthWeight);
        return radius + depthWeight / 2;
    }
}
