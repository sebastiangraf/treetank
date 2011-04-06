/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.gui.view.sunburst;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Encapsulates drawing strategies.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public enum EDraw {
    /** Draw directly. */
    DRAW {
        /** {@inheritDoc} */
        @Override
        void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem,
            final EDrawSunburst paramDraw) {
            paramDraw.drawStrategy(paramGUI, paramItem, this);
        }

        /** {@inheritDoc} */
        @Override
        void update(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            paramItem.update(paramGUI.getMappingMode(), paramGUI.mParent.g);
        }

        /** {@inheritDoc} */
        @Override
        void drawOldRevision(final SunburstGUI paramGUI) {
            drawOldRevision(paramGUI, paramGUI.mParent.g);
        }

        /** {@inheritDoc} */
        @Override
        void drawNewRevision(final SunburstGUI paramGUI) {
            drawNewRevision(paramGUI, paramGUI.mParent.g);
        }
    },

    /** Draw into buffer. */
    UPDATEBUFFER {
        /** {@inheritDoc} */
        @Override
        void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem,
            final EDrawSunburst paramDraw) {
            paramDraw.drawStrategy(paramGUI, paramItem, this);
        }

        /** {@inheritDoc} */
        @Override
        void update(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            paramItem.update(paramGUI.getMappingMode(), paramGUI.mBuffer);
        }

        /** {@inheritDoc} */
        @Override
        void drawOldRevision(final SunburstGUI paramGUI) {
            drawOldRevision(paramGUI, paramGUI.mBuffer);
        }

        /** {@inheritDoc} */
        @Override
        void drawNewRevision(final SunburstGUI paramGUI) {
            drawNewRevision(paramGUI, paramGUI.mBuffer);
        }
    };

    /**
     * Drawing old revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramGraphic
     *            {@link PGraphics} instance
     */
    private static void drawOldRevision(final SunburstGUI paramGUI, final PGraphics paramGraphic) {
        paramGraphic.pushMatrix();
        final float arcRadius = calculateOldRadius(paramGUI, paramGraphic);
        paramGraphic.stroke(200f);
        paramGraphic.arc(0, 0, arcRadius, arcRadius, 0, 2 * PConstants.PI);
        paramGraphic.stroke(0f);

        final String text = "revision " + paramGUI.mDb.getRevisionNumber();
        draw(paramGUI, paramGraphic, text, arcRadius);
        paramGraphic.popMatrix();
    }

    /**
     * Drawing new revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramGraphic
     *            {@link PGraphics} instance
     */
    private static void drawNewRevision(final SunburstGUI paramGUI, final PGraphics paramGraphic) {
        paramGraphic.pushMatrix();
        final float arcRadius = calculateNewRadius(paramGUI);
        paramGraphic.stroke(200f);
        paramGraphic.arc(0, 0, arcRadius, arcRadius, 0, 2 * PConstants.PI);
        paramGraphic.stroke(0f);

        final String text = "revision " + paramGUI.mSelectedRev;
        draw(paramGUI, paramGraphic, text, arcRadius);
        paramGUI.mBuffer.popMatrix();
    }

    /**
     * Draw revision text.
     * 
     * @param paramGUI
     *            {@link GUI} instance
     * @param paramGraphic
     *            {@link PGraphics} instance
     * @param paramItem
     *            {@link SunburstItem} instance
     * @param paramRadius
     *            arc radius
     */
    static void draw(final SunburstGUI paramGUI, final PGraphics paramGraphic, final String paramText,
        final float paramArcRadius) {
        // We must keep track of our position along the curve.
        float arclength = 0;
        // For every box
        for (int i = 0; i < paramText.length(); i++) {
            // Instead of a constant width, we check the width of each character.
            final char currentChar = paramText.charAt(i);
            final float w = 7f;// paramGUI.mParent.textWidth(currentChar);

            // Each box is centered so we move half the width.
            arclength += w / 2;
            // Angle in radians is the arclength divided by the radius.
            // Starting on the left side of the circle by adding PI.
            final float theta = PConstants.PI + arclength / paramArcRadius;

            paramGraphic.pushMatrix();
            // Polar to cartesian coordinate conversion.
            paramGraphic.translate(paramArcRadius * PApplet.cos(theta), paramArcRadius * PApplet.sin(theta));
            // Rotate the box.
            paramGraphic.rotate(theta + PConstants.PI / 2); // rotation is offset by 90 degrees
            // Display the character.
            paramGraphic.fill(0);
            paramGraphic.text(currentChar, 0, 0);
            paramGraphic.popMatrix();
            // Move halfway again.
            arclength += w / 2;
        }
        paramGraphic.noFill();
    }

    /**
     * Calculate radius.
     * 
     * @param paramGUI
     *            {@link GUI} instance
     * @param paramGraphic
     *            {@link PGraphics} instance
     * @return calculated radius
     */
    private static float calculateOldRadius(final SunburstGUI paramGUI, final PGraphics paramGraphic) {
        final int revisionDepth = paramGUI.mOldDepthMax + 1;
        final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
        final float depthWeight =
            paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
        paramGraphic.strokeWeight(depthWeight);
        return radius + depthWeight / 2;
    }

    /**
     * Calculate radius.
     * 
     * @param paramGUI
     *            {@link GUI} instance
     * @return calculated radius
     */
    private static float calculateNewRadius(final SunburstGUI paramGUI) {
        final int revisionDepth = paramGUI.mDepthMax - 1;
        final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
        final float depthWeight =
            paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
        paramGUI.mBuffer.strokeWeight(depthWeight);
        return radius + depthWeight / 2;
    }

    /**
     * Draw arc.
     * 
     * @param paramGUI
     *            {@link GUI} instance
     * @param paramItem
     *            {@link SunburstItem} instance
     */
    protected void drawArc(final SunburstGUI paramGUI, final SunburstItem paramItem) {
        if (paramGUI.mShowArcs) {
            if (paramGUI.mUseArc) {
                paramItem.drawArc(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale);
            } else {
                paramItem.drawRect(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale);
            }
        }
    }

    /**
     * Draw relation line.
     * 
     * @param paramGUI
     *            {@link GUI} instance
     * @param paramItem
     *            {@link SunburstItem} instance
     */
    protected void drawRelation(final SunburstGUI paramGUI, final SunburstItem paramItem) {
        if (paramGUI.mShowLines) {
            if (paramGUI.mUseBezierLine) {
                paramItem.drawRelationBezier();
            } else {
                paramItem.drawRelationLine();
            }
        }
    }

    /**
     * Draw dot.
     * 
     * @param paramGUI
     *            {@link GUI} instance
     * @param paramItem
     *            {@link SunburstItem} instance
     */
    protected void drawDot(final SunburstGUI paramGUI, final SunburstItem paramItem) {
        paramItem.drawDot();
    }

    /**
     * Update a {@link SunburstItem}.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} reference
     * @param paramItem
     *            {@link SunburstItem} to update
     */
    abstract void update(final SunburstGUI paramGUI, final SunburstItem paramItem);

    /**
     * Drawing strategy.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramItem
     *            {@link SunburstItem} to draw
     */
    abstract void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem,
        final EDrawSunburst paramDraw);

    /**
     * Draw old revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     */
    abstract void drawOldRevision(final SunburstGUI paramGUI);

    /**
     * Draw new revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     */
    abstract void drawNewRevision(final SunburstGUI paramGUI);

    /** Determines how to draw. */
    public enum EDrawSunburst {
        /** Normal sunburst view. */
        NORMAL {
            /** {@inheritDoc} */
            @Override
            void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem, final EDraw paramDraw) {
                paramDraw.drawArc(paramGUI, paramItem);
                paramDraw.drawRelation(paramGUI, paramItem);
                paramDraw.drawDot(paramGUI, paramItem);
            }
        },

        /** Compare sunburst view. */
        COMPARE {
            /** {@inheritDoc} */
            @Override
            void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem, final EDraw paramDraw) {
                paramDraw.drawArc(paramGUI, paramItem);
            }
        };

        /**
         * Drawing strategy.
         * 
         * @param paramGUI
         *            {@link SunburstGUI} instance
         * @param paramItem
         *            {@link SunburstItem} to draw
         */
        abstract void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem,
            final EDraw paramDraw);
    }
}
