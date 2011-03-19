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
package org.treetank.gui.view.sunburst;

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
        void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem,
            final EDrawSunburst paramDraw) {
            paramDraw.drawStrategy(paramGUI, paramItem, this);
        }

        @Override
        void drawNewRevision(final SunburstGUI paramGUI) {
            paramGUI.mParent.pushMatrix();
            final float arcRadius = calculateNewRadius(paramGUI);
            paramGUI.mParent.stroke(200f);
            paramGUI.mParent.arc(0, 0, arcRadius, arcRadius, 0, 2 * PConstants.PI);
            paramGUI.mParent.stroke(0f);

            final String text = "revision " + paramGUI.mSelectedRev;
            draw(paramGUI, text, arcRadius);
            paramGUI.mParent.popMatrix();
        }

        @Override
        void drawOldRevision(final SunburstGUI paramGUI) {
            paramGUI.mParent.pushMatrix();
            final float arcRadius = calculateOldRadius(paramGUI);
            paramGUI.mParent.stroke(200f);
            paramGUI.mParent.arc(0, 0, arcRadius, arcRadius, 0, 2 * PConstants.PI);
            paramGUI.mParent.stroke(0f);

            final String text = "revision " + paramGUI.mDb.getRevisionNumber();
            draw(paramGUI, text, arcRadius);
            paramGUI.mParent.popMatrix();
        }

        private void draw(final SunburstGUI paramGUI, final String paramText, final float paramArcRadius) {
            // We must keep track of our position along the curve.
            float arclength = 0;
            // For every box
            for (int i = 0; i < paramText.length(); i++) {
                // Instead of a constant width, we check the width of each character.
                final char currentChar = paramText.charAt(i);
                final float w = paramGUI.mParent.textWidth(currentChar);

                // Each box is centered so we move half the width.
                arclength += w / 2;
                // Angle in radians is the arclength divided by the radius.
                // Starting on the left side of the circle by adding PI.
                final float theta = PConstants.PI + arclength / paramArcRadius;

                paramGUI.mParent.pushMatrix();
                // Polar to cartesian coordinate conversion.
                paramGUI.mParent.translate(paramArcRadius * PApplet.cos(theta),
                    paramArcRadius * PApplet.sin(theta));
                // Rotate the box.
                paramGUI.mParent.rotate(theta + PConstants.PI / 2); // rotation is offset by 90 degrees
                // Display the character.
                paramGUI.mParent.fill(0);
                paramGUI.mParent.text(currentChar, 0, 0);
                paramGUI.mParent.popMatrix();
                // Move halfway again.
                arclength += w / 2;
            }
            paramGUI.mParent.fill(500);
        }

        /**
         * Calculate radius.
         * 
         * @param paramGUI
         *            {@link GUI} instance
         * @return calculated radius
         */
        float calculateOldRadius(final SunburstGUI paramGUI) {
            final int revisionDepth = paramGUI.mOldDepthMax + 1;
            final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
            final float depthWeight =
                paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
            paramGUI.mParent.strokeWeight(depthWeight);
            return radius + depthWeight / 2;
        }

        /**
         * Calculate radius.
         * 
         * @param paramGUI
         *            {@link GUI} instance
         * @return calculated radius
         */
        float calculateNewRadius(final SunburstGUI paramGUI) {
            final int revisionDepth = paramGUI.mDepthMax - 2;
            final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
            final float depthWeight =
                paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
            paramGUI.mParent.strokeWeight(depthWeight);
            return radius + depthWeight / 2;
        }

        @Override
        protected void drawArc(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mShowArcs) {
                if (paramGUI.mUseArc) {
                    paramItem.drawArc(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale);
                } else {
                    paramItem.drawRect(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale);
                }
            }
        }

        @Override
        protected void drawRelation(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mShowLines) {
                if (paramGUI.mUseBezierLine) {
                    paramItem.drawRelationBezier();
                } else {
                    paramItem.drawRelationLine();
                }
            }

        }

        @Override
        protected void drawDot(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            paramItem.drawDot();
        }
    },

    /** Draw into buffer. */
    UPDATEBUFFER {
        /**
         * {@inheritDoc}
         */
        @Override
        void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem,
            final EDrawSunburst paramDraw) {
            paramDraw.drawStrategy(paramGUI, paramItem, this);
        }

        @Override
        void drawOldRevision(final SunburstGUI paramGUI) {
            paramGUI.mBuffer.pushMatrix();
            final float arcRadius = calculateOldRadius(paramGUI);
            paramGUI.mBuffer.stroke(200f);
            paramGUI.mBuffer.arc(0, 0, arcRadius, arcRadius, 0, 2 * PConstants.PI);
            paramGUI.mBuffer.stroke(0f);

            final String text = "revision " + paramGUI.mDb.getRevisionNumber();
            draw(paramGUI, text, arcRadius);
            paramGUI.mBuffer.popMatrix();
        }

        @Override
        void drawNewRevision(final SunburstGUI paramGUI) {
            paramGUI.mBuffer.pushMatrix();
            final float arcRadius = calculateNewRadius(paramGUI);
            paramGUI.mBuffer.stroke(200f);
            paramGUI.mBuffer.arc(0, 0, arcRadius, arcRadius, 0, 2 * PConstants.PI);
            paramGUI.mBuffer.stroke(0f);

            final String text = "revision " + paramGUI.mSelectedRev;
            draw(paramGUI, text, arcRadius);
            paramGUI.mBuffer.popMatrix();
        }

        void draw(final SunburstGUI paramGUI, final String paramText, final float paramArcRadius) {
            // We must keep track of our position along the curve.
            float arclength = 0;
            // For every box
            for (int i = 0; i < paramText.length(); i++) {
                // Instead of a constant width, we check the width of each character.
                final char currentChar = paramText.charAt(i);
                final float w = paramGUI.mParent.textWidth(currentChar);

                // Each box is centered so we move half the width.
                arclength += w / 2;
                // Angle in radians is the arclength divided by the radius.
                // Starting on the left side of the circle by adding PI.
                final float theta = PConstants.PI + arclength / paramArcRadius;

                paramGUI.mBuffer.pushMatrix();
                // Polar to cartesian coordinate conversion.
                paramGUI.mBuffer.translate(paramArcRadius * PApplet.cos(theta),
                    paramArcRadius * PApplet.sin(theta));
                // Rotate the box.
                paramGUI.mBuffer.rotate(theta + PConstants.PI / 2); // rotation is offset by 90 degrees
                // Display the character.
                paramGUI.mBuffer.fill(0);
                paramGUI.mBuffer.text(currentChar, 0, 0);
                paramGUI.mBuffer.popMatrix();
                // Move halfway again.
                arclength += w / 2;
            }
            paramGUI.mBuffer.fill(500);
        }

        /**
         * Calculate radius.
         * 
         * @param paramGUI
         *            {@link GUI} instance
         * @return calculated radius
         */
        float calculateOldRadius(final SunburstGUI paramGUI) {
            final int revisionDepth = paramGUI.mOldDepthMax + 1;
            final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
            final float depthWeight =
                paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
            paramGUI.mBuffer.strokeWeight(depthWeight);
            return radius + depthWeight / 2;
        }

        /**
         * Calculate radius.
         * 
         * @param paramGUI
         *            {@link GUI} instance
         * @return calculated radius
         */
        float calculateNewRadius(final SunburstGUI paramGUI) {
            final int revisionDepth = paramGUI.mDepthMax - 1;
            final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
            final float depthWeight =
                paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
            paramGUI.mBuffer.strokeWeight(depthWeight);
            return radius + depthWeight / 2;
        }

        @Override
        protected void drawArc(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mShowArcs) {
                if (paramGUI.mUseArc) {
                    paramItem.drawArcBuffer(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale,
                        paramGUI.mBuffer);
                } else {
                    paramItem.drawRectBuffer(paramGUI.mInnerNodeArcScale, paramGUI.mLeafArcScale,
                        paramGUI.mBuffer);
                }
            }
        }

        @Override
        protected void drawRelation(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mShowLines) {
                if (paramGUI.mUseBezierLine) {
                    paramItem.drawRelationBezierBuffer(paramGUI.mBuffer);
                } else {
                    paramItem.drawRelationLineBuffer(paramGUI.mBuffer);
                }
            }

        }

        @Override
        protected void drawDot(final SunburstGUI paramGUI, final SunburstItem paramItem) {
            paramItem.drawDotBuffer(paramGUI.mBuffer);
        }
    };

    /**
     * Drawing strategy.
     * 
     * @/home/johannesparam paramGUI
     *            {@link SunburstGUI} instance
     * @param paramItem
     *            {@link SunburstItem} to draw
     */
    abstract void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem,
        final EDrawSunburst paramDraw);

    protected abstract void drawArc(final SunburstGUI paramGUI, final SunburstItem paramItem);

    protected abstract void drawRelation(final SunburstGUI paramGUI, final SunburstItem paramItem);

    protected abstract void drawDot(final SunburstGUI paramGUI, final SunburstItem paramItem);

    /**
     * Drawing old revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     */
    abstract void drawOldRevision(final SunburstGUI paramGUI);

    /**
     * Drawing new revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     */
    abstract void drawNewRevision(final SunburstGUI paramGUI);

    public enum EDrawSunburst {
        NORMAL {
            @Override
            void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem, final EDraw paramDraw) {
                paramDraw.drawArc(paramGUI, paramItem);
                paramDraw.drawRelation(paramGUI, paramItem);
                paramDraw.drawDot(paramGUI, paramItem);
            }
            
            void draw(final SunburstGUI paramGUI, final SunburstItem paramItem, final EDraw paramDraw) {
               
            }
        },

        /**
         * püpü
         */
        COMPARE {
            @Override
            void drawStrategy(final SunburstGUI paramGUI, final SunburstItem paramItem, final EDraw paramDraw) {
                paramDraw.drawArc(paramGUI, paramItem);
            }
            
            void draw(final SunburstGUI paramGUI, final SunburstItem paramItem, final EDraw paramDraw) {
                paramDraw.drawRelation(paramGUI, paramItem);
                paramDraw.drawDot(paramGUI, paramItem);
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
        
        abstract void draw(final SunburstGUI paramGUI, final SunburstItem paramItem, final EDraw paramDraw);
    }
}
