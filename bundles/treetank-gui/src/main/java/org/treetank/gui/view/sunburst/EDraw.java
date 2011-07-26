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

import org.treetank.gui.GUI;
import org.treetank.gui.view.EHover;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.sunburst.SunburstItem.EStructType;

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
        public void drawStrategy(final AbsSunburstGUI paramGUI, final SunburstItem paramItem,
            final EDrawSunburst paramDraw) {
            paramDraw.drawStrategy(paramGUI, paramItem, this);
        }

        /** {@inheritDoc} */
        @Override
        public void drawRings(final AbsSunburstGUI paramGUI) {
            if (paramGUI.mParent.recorder != null) {
                drawStaticRings(paramGUI, paramGUI.mParent.recorder);
            }
            drawStaticRings(paramGUI, paramGUI.mParent.g);
        }

        /** {@inheritDoc} */
        @Override
        public void update(final AbsSunburstGUI paramGUI, final SunburstItem paramItem) {
            paramItem.update(paramGUI.getMappingMode(), paramGUI.mParent.g);
        }

        /** {@inheritDoc} */
        @Override
        public void drawOldRevision(final AbsSunburstGUI paramGUI) {
            drawStaticOldRevision(paramGUI, paramGUI.mParent.g);
        }

        /** {@inheritDoc} */
        @Override
        public void drawNewRevision(final AbsSunburstGUI paramGUI) {
            drawStaticNewRevision(paramGUI, paramGUI.mParent.g);
        }

        /** {@inheritDoc} */
        @Override
        public void drawModificationRel(final AbsSunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mParent.recorder != null) {
                drawStaticModifcationRel(paramGUI, paramItem, paramGUI.mParent.recorder);
            }
            drawStaticModifcationRel(paramGUI, paramItem, paramGUI.mParent.g);
        }

        /** {@inheritDoc} */
        @Override
        public void drawLabel(AbsSunburstGUI paramGUI, SunburstItem paramItem) {
            if (paramGUI.isShowArcs() && !paramGUI.isShowLines()) {
                if (paramGUI.mParent.recorder != null) {
                    drawStaticLabel(paramGUI, paramGUI.mParent.recorder, paramItem);
                }
                drawStaticLabel(paramGUI, paramGUI.mParent.g, paramItem);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void drawHover(AbsSunburstGUI paramGUI, SunburstItem paramItem) {
            if (paramGUI.mParent.recorder != null) {
                paramItem.hover(paramGUI.mParent.recorder);
                drawStaticLabel(paramGUI, paramGUI.mParent.recorder, paramItem);
            }
            paramItem.hover(paramGUI.mParent.g);
            drawStaticLabel(paramGUI, paramGUI.mParent.g, paramItem);

        }
    },

    /** Draw into buffer. */
    UPDATEBUFFER {
        /** {@inheritDoc} */
        @Override
        public void drawStrategy(final AbsSunburstGUI paramGUI, final SunburstItem paramItem,
            final EDrawSunburst paramDraw) {
            paramDraw.drawStrategy(paramGUI, paramItem, this);
        }

        /** {@inheritDoc} */
        @Override
        public void drawRings(final AbsSunburstGUI paramGUI) {
            if (paramGUI.mParent.recorder != null) {
                drawStaticRings(paramGUI, paramGUI.mParent.recorder);
            }
            drawStaticRings(paramGUI, paramGUI.getBuffer());
        }

        /** {@inheritDoc} */
        @Override
        public void update(final AbsSunburstGUI paramGUI, final SunburstItem paramItem) {
            paramItem.update(paramGUI.getMappingMode(), paramGUI.getBuffer());
        }

        /** {@inheritDoc} */
        @Override
        public void drawOldRevision(final AbsSunburstGUI paramGUI) {
            drawStaticOldRevision(paramGUI, paramGUI.getBuffer());
        }

        /** {@inheritDoc} */
        @Override
        public void drawNewRevision(final AbsSunburstGUI paramGUI) {
            drawStaticNewRevision(paramGUI, paramGUI.getBuffer());
        }

        /** {@inheritDoc} */
        @Override
        public void drawModificationRel(final AbsSunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mParent.recorder != null) {
                drawStaticModifcationRel(paramGUI, paramItem, paramGUI.mParent.recorder);
            }
            drawStaticModifcationRel(paramGUI, paramItem, paramGUI.getBuffer());
        }

        /** {@inheritDoc} */
        @Override
        public void drawLabel(AbsSunburstGUI paramGUI, SunburstItem paramItem) {
            if (paramGUI.isShowArcs() && !paramGUI.isShowLines()) {
                if (paramGUI.mParent.recorder != null) {
                    drawStaticLabel(paramGUI, paramGUI.mParent.recorder, paramItem);
                }
                drawStaticLabel(paramGUI, paramGUI.getBuffer(), paramItem);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void drawHover(final AbsSunburstGUI paramGUI, final SunburstItem paramItem) {
            if (paramGUI.mParent.recorder != null) {
                paramItem.hover(paramGUI.mParent.recorder);
                drawStaticLabel(paramGUI, paramGUI.mParent.recorder, paramItem);
            }
            paramItem.hover(paramGUI.getBuffer());
            drawStaticLabel(paramGUI, paramGUI.getBuffer(), paramItem);
        }
    };

    private static void drawStaticLabel(final AbsSunburstGUI paramGUI, final PGraphics paramGraphic,
        final SunburstItem paramItem) {
        final int depth = paramItem.getDepth();
        final float startAngle = paramItem.getAngleStart();
        final float endAngle = paramItem.getAngleEnd();
        final float scale =
            paramItem.getStructKind() == EStructType.ISINNERNODE ? paramGUI.getInnerNodeArcScale() : paramGUI
                .getLeafArcScale();
        if (scale >= 0.7) {
            final float fontHeight = paramGUI.mParent.textAscent() + paramGUI.mParent.textDescent();
            final float size = depth == 0 ? 15 : PApplet.map(depth, 0, paramGUI.mDepthMax, 13, 11);
            paramGraphic.textSize(size);
            paramGraphic.textLeading(0f);
            final String text =
                paramItem.mQName == null ? paramItem.mText : ViewUtilities.qNameToString(paramItem.mQName);
            float arcRadius = paramGUI.calcEqualAreaRadius(depth, paramGUI.mDepthMax);
            float arc = draw(paramGraphic, text, arcRadius, startAngle, EDisplay.NO, EReverseDirection.NO);
            if (arc < endAngle) {
                if (depth == 0) {
                    // Must be the root-Element.
                    paramGraphic.pushMatrix();
                    paramGraphic.fill(0);
                    paramGraphic.text(text, 0 - paramGraphic.textWidth(text) / 2f, -12f);
                    paramGraphic.popMatrix();
                    paramGraphic.noFill();
                    // arc = draw(paramGraphic, text, arcRadius, 0, EDisplay.NO, EReverseDirection.NO);
                    // final float theta = PConstants.PI + 0.5f * PConstants.PI - 0.5f * arc;
                    // draw(paramGraphic, text, arcRadius, theta, EDisplay.YES, EReverseDirection.NO);
                } else if ((startAngle + endAngle) / 2 < PConstants.PI) {
                    // Bottom half.
                    float radius = (paramGUI.calcEqualAreaRadius(depth + 1, paramGUI.mDepthMax) - arcRadius);
                    radius *= scale;
                    final float depthDiff =
                        depth < 5 ? radius + 0.5f * fontHeight : radius + 0.4f * fontHeight;
                    arcRadius += (0.5f * depthDiff);
                    // (((radius + paramGUI.mParent.textAscent()) / 2) - paramGUI.mParent.textAscent() / 2);

                    draw(paramGraphic, text, arcRadius, endAngle - ((endAngle - arc) * 0.5f), EDisplay.YES,
                        EReverseDirection.YES);
                } else {
                    // Top half.
                    float radius = (paramGUI.calcEqualAreaRadius(depth + 1, paramGUI.mDepthMax) - arcRadius);
                    radius *= scale;
                    final float depthDiff =
                        depth < 5 ? radius - 0.7f * fontHeight : radius - 0.5f * fontHeight;
                    arcRadius += (0.5f * depthDiff);

                    draw(paramGraphic, text, arcRadius, (endAngle - arc) * 0.5f + startAngle, EDisplay.YES,
                        EReverseDirection.NO);
                }
            }
        }
    }

    /**
     * Drawing old revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramGraphic
     *            {@link PGraphics} instance
     */
    private static void drawStaticOldRevision(final AbsSunburstGUI paramGUI, final PGraphics paramGraphic) {
        paramGraphic.pushMatrix();
        float arcRadius = calculateOldRadius(paramGUI, paramGraphic);
        if (paramGUI.mParent.recorder != null) {
            drawRevision(paramGUI, paramGUI.mParent.recorder, arcRadius);
        }
        drawRevision(paramGUI, paramGraphic, arcRadius);
        final String text =
            new StringBuilder("matching nodes in revision ").append(paramGUI.mOldSelectedRev)
                .append(" and revision ").append(paramGUI.mSelectedRev).toString();
        arcRadius = paramGUI.calcEqualAreaRadius(paramGUI.mOldDepthMax + 1, paramGUI.mDepthMax);
        arcRadius +=
            (paramGUI.calcEqualAreaRadius(paramGUI.mOldDepthMax + 2, paramGUI.mDepthMax) - arcRadius) / 3;
        final float arc = draw(paramGraphic, text, arcRadius, 0, EDisplay.NO, EReverseDirection.NO);
        final float theta = PConstants.PI + 0.5f * PConstants.PI - 0.5f * arc;
        if (paramGUI.mParent.recorder != null) {
            draw(paramGUI.mParent.recorder, text, arcRadius, theta, EDisplay.YES, EReverseDirection.NO);
        }
        draw(paramGraphic, text, arcRadius, theta, EDisplay.YES, EReverseDirection.NO);
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
    private static void drawStaticNewRevision(final AbsSunburstGUI paramGUI, final PGraphics paramGraphic) {
        paramGraphic.pushMatrix();
        float arcRadius = calculateNewRadius(paramGUI, paramGraphic);
        if (paramGUI.mParent.recorder != null) {
            drawRevision(paramGUI, paramGUI.mParent.recorder, arcRadius);
        }
        drawRevision(paramGUI, paramGraphic, arcRadius);
        final String text =
            new StringBuilder("changed nodes in revision ").append(paramGUI.mSelectedRev)
                .append(" from revision ").append(paramGUI.mOldSelectedRev).toString();
        arcRadius = paramGUI.calcEqualAreaRadius(paramGUI.mDepthMax - 1, paramGUI.mDepthMax) + 5;
        arcRadius +=
            (paramGUI.calcEqualAreaRadius(paramGUI.mDepthMax - 1, paramGUI.mDepthMax) - arcRadius) / 5;
        final float arc = draw(paramGraphic, text, arcRadius, 0, EDisplay.NO, EReverseDirection.NO);
        final float theta = PConstants.PI + 0.5f * PConstants.PI - 0.5f * arc;
        if (paramGUI.mParent.recorder != null) {
            draw(paramGUI.mParent.recorder, text, arcRadius, theta, EDisplay.YES, EReverseDirection.NO);
        }
        draw(paramGraphic, text, arcRadius, theta, EDisplay.YES, EReverseDirection.NO);
        paramGraphic.popMatrix();
    }

    private static void drawStaticModifcationRel(final AbsSunburstGUI paramGUI, final SunburstItem paramItem,
        final PGraphics paramGraphic) {
        if (paramGUI.mUseDiffView == EView.DIFF && EView.DIFF.getValue() && paramGUI.isShowArcs()
            && paramItem.getDepth() == paramGUI.mOldDepthMax + 2) {
            switch (paramItem.mDiff) {
            case INSERTED:
                paramGraphic.stroke(200, 100, paramGUI.getDotBrightness(), 30);
                break;
            case DELETED:
                paramGraphic.stroke(360, 100, paramGUI.getDotBrightness(), 30);
                break;
            case UPDATED:
                paramGraphic.stroke(120, 100, paramGUI.getDotBrightness(), 30);
                break;
            }
            if (paramItem.getGreyState() == EGreyState.YES) {
                paramGraphic.stroke(0);
            }
            final SunburstItem item =
                (SunburstItem)paramGUI.mControl.getModel().getItem(paramItem.getIndexToParent());
            for (int i = item.getDepth() + 1; i < paramGUI.mOldDepthMax + 1; i++) {
                float radius = paramGUI.calcEqualAreaRadius(i, paramGUI.mDepthMax);
                final float depthWeight = paramGUI.calcEqualAreaRadius(i + 1, paramGUI.mDepthMax) - radius;
                paramGraphic.strokeWeight(depthWeight);
                radius += (depthWeight / 2);
                paramGraphic.arc(0, 0, radius, radius, paramItem.getAngleStart(), paramItem.getAngleEnd());
            }
        }
    }

    /**
     * Drawing hierarchy rings.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramGraphic
     *            {@link PGraphics} instance
     * @param paramDraw
     *            determines the drawing strategy
     */
    private static void drawStaticRings(final AbsSunburstGUI paramGUI, final PGraphics paramGraphic) {
        int depthMax = paramGUI.mDepthMax;
        if (paramGUI.mUseDiffView == EView.NODIFF) {
            depthMax += 1;
        }
        for (int depth = 0; depth < depthMax; depth++) {
            final float radius = paramGUI.calcEqualAreaRadius(depth, paramGUI.mDepthMax);
            paramGraphic.stroke(300f);
            paramGraphic.arc(0, 0, radius, radius, 0, 2 * PConstants.PI);
        }
    }

    /**
     * Draw revision text.
     * 
     * @param paramGraphic
     *            {@link PGraphics} instance
     * @param paramItem
     *            {@link SunburstItem} instance
     * @param paramRadius
     *            arc radius
     * @param paramTheta
     *            angle in radians where text starts
     * @param paramDisplay
     *            determines if text should be displayed or not
     * @param paramReverseDirection
     *            determines if text should be in the reverse direction or not
     */
    private static float draw(final PGraphics paramGraphic, final String paramText,
        final float paramArcRadius, final float paramTheta, final EDisplay paramDisplay,
        final EReverseDirection paramReverseDrawDirection) {
        float retVal = paramTheta;

        assert paramTheta >= 0f && paramTheta <= PConstants.TWO_PI;
        String text = paramText;

        // We must keep track of our position along the curve.
        float arclength = 0;
        // For every box.
        for (int i = 0; i < text.length(); i++) {
            // Instead of a constant width, we check the width of each character.
            final char currentChar = text.charAt(i);
            final float w = paramGraphic.textWidth(currentChar + "") + 1; // Work around.

            // Each box is centered so we move half the width.
            arclength += currentChar != 'i' ? w / 2 : w;
            // Angle in radians is the arclength divided by the radius.
            // Starting on the left side of the circle by adding PI.
            final float theta =
                paramReverseDrawDirection == EReverseDirection.YES ? paramTheta - arclength / paramArcRadius
                    : paramTheta + arclength / paramArcRadius;
            retVal += (arclength / paramArcRadius);

            paramGraphic.pushMatrix();
            // Polar to cartesian coordinate conversion.
            paramGraphic.translate(paramArcRadius * PApplet.cos(theta), paramArcRadius * PApplet.sin(theta));
            // Rotate the box.
            if (paramReverseDrawDirection == EReverseDirection.YES) {
                paramGraphic.rotate(theta - PConstants.PI / 2); // rotation is offset by 90 degrees
            } else {
                paramGraphic.rotate(theta + PConstants.PI / 2); // rotation is offset by 90 degrees
            }
            if (paramDisplay == EDisplay.YES) {
                // Display the character.
                paramGraphic.fill(0);
                paramGraphic.text(currentChar, 0, 0);
            }
            paramGraphic.popMatrix();
            // Move halfway again.
            arclength += w / 2;
        }
        paramGraphic.noFill();
        return retVal = paramTheta + arclength / paramArcRadius;
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
    private static float calculateOldRadius(final AbsSunburstGUI paramGUI, final PGraphics paramGraphic) {
        final int revisionDepth = paramGUI.mOldDepthMax + 1;
        final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
        final float depthWeight =
            paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
        if (paramGUI.mParent.recorder != null) {
            paramGUI.mParent.recorder.strokeWeight(depthWeight);
        }
        paramGraphic.strokeWeight(depthWeight);
        return radius + depthWeight / 2;
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
    private static float calculateNewRadius(final AbsSunburstGUI paramGUI, final PGraphics paramGraphic) {
        final int revisionDepth = paramGUI.mDepthMax - 1;
        final float radius = paramGUI.calcEqualAreaRadius(revisionDepth, paramGUI.mDepthMax);
        final float depthWeight =
            paramGUI.calcEqualAreaRadius(revisionDepth + 1, paramGUI.mDepthMax) - radius;
        if (paramGUI.mParent.recorder != null) {
            paramGUI.mParent.recorder.strokeWeight(depthWeight);
        }
        paramGraphic.strokeWeight(depthWeight);
        return radius + depthWeight / 2;
    }

    /**
     * Draw revision.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramGraphic
     *            {@link PGraphics} instance
     * @param paramArcRadius
     *            arc radius
     */
    private static void drawRevision(final AbsSunburstGUI paramGUI, final PGraphics paramGraphic,
        final float paramArcRadius) {
        paramGraphic.stroke(200f);
        paramGraphic.arc(0, 0, paramArcRadius, paramArcRadius, 0, 2 * PConstants.PI);
        paramGraphic.stroke(0f);
    }

    /**
     * Draw arc.
     * 
     * @param paramGUI
     *            {@link GUI} instance
     * @param paramItem
     *            {@link SunburstItem} instance
     */
    protected void drawArc(final AbsSunburstGUI paramGUI, final SunburstItem paramItem) {
        if (paramGUI.isShowArcs()) {
            if (paramGUI.mUseArc) {
                paramItem.drawArc(paramGUI.getInnerNodeArcScale(), paramGUI.getLeafArcScale(), EHover.FALSE);
            } else {
                paramItem.drawRect(paramGUI.getInnerNodeArcScale(), paramGUI.getLeafArcScale(), EHover.FALSE);
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
    public void drawRelation(final AbsSunburstGUI paramGUI, final SunburstItem paramItem) {
        if (paramGUI.isShowLines()) {
            if (paramGUI.isUseBezierLine()) {
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
    public void drawDot(final AbsSunburstGUI paramGUI, final SunburstItem paramItem) {
        paramItem.drawDot(EHover.FALSE);
    }

    /**
     * Update a {@link SunburstItem}.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} reference
     * @param paramItem
     *            {@link SunburstItem} to update
     */
    public abstract void update(final AbsSunburstGUI paramGUI, final SunburstItem paramItem);

    /**
     * Drawing strategy.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramItem
     *            {@link SunburstItem} to draw
     * @param paramDraw
     */
    public abstract void drawStrategy(final AbsSunburstGUI paramGUI, final SunburstItem paramItem,
        final EDrawSunburst paramDraw);

    /**
     * Drawing strategy for hovering.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     * @param paramItem
     *            {@link SunburstItem} to draw
     * @param paramDraw
     */
    public abstract void drawHover(final AbsSunburstGUI paramGUI, final SunburstItem paramItem);

    /**
     * Draw old revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     */
    public abstract void drawOldRevision(final AbsSunburstGUI paramGUI);

    /**
     * Draw new revision ring.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     */
    public abstract void drawNewRevision(final AbsSunburstGUI paramGUI);

    /**
     * Draw label.
     * 
     * @param paramItem
     *            {@link SunburstItem} instance
     * @param paramGUI
     *            {@link AbsSunburstGUI} reference
     */
    public abstract void drawLabel(final AbsSunburstGUI paramGUI, final SunburstItem paramItem);

    /**
     * Drawing hierarchy rings.
     * 
     * @param paramGUI
     *            {@link SunburstGUI} instance
     */
    public abstract void drawRings(final AbsSunburstGUI paramGUI);

    public abstract void drawModificationRel(final AbsSunburstGUI paramGUI, final SunburstItem paramItem);

    /** Determines how to draw. */
    public enum EDrawSunburst {
        /** Normal sunburst view. */
        NORMAL {
            /** {@inheritDoc} */
            @Override
            void drawStrategy(final AbsSunburstGUI paramGUI, final SunburstItem paramItem,
                final EDraw paramDraw) {
                paramDraw.drawArc(paramGUI, paramItem);
                paramDraw.drawRelation(paramGUI, paramItem);
                paramDraw.drawDot(paramGUI, paramItem);
                paramDraw.drawLabel(paramGUI, paramItem);
            }
        },

        /** Compare sunburst view. */
        COMPARE {
            /** {@inheritDoc} */
            @Override
            void drawStrategy(final AbsSunburstGUI paramGUI, final SunburstItem paramItem,
                final EDraw paramDraw) {
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
         * @param paramDraw
         *            determines if it has to be drawn into an offscreen buffer or directly to the screen
         */
        abstract void drawStrategy(final AbsSunburstGUI paramGUI, final SunburstItem paramItem,
            final EDraw paramDraw);
    }

    /** Determines if text should be displayed or not. */
    private enum EDisplay {
        /** Yes it should be displayed. */
        YES,

        /** No it shouldn't be displayed. */
        NO
    }

    /** Determines if text direction should be reversed or not. */
    private enum EReverseDirection {
        /** Yes it should be displayed. */
        YES,

        /** No it shouldn't be displayed. */
        NO
    }
}
