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

import javax.xml.namespace.QName;

import org.treetank.api.IItem;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.ViewUtilities;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * <h1>SunburstItem</h1>
 * 
 * <p>
 * Represents one item in the Sunburst diagram. Note that this class is not immutable (notably because
 * {@link AbsNodes} and all subclasses can be modified), but since it's package private it should be used in a
 * convenient way.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstItem {

    /** Current {@link IItem} in Treetank. */
    final IItem mNode;

    // Relations. ============================================
    /** Index to parent node. */
    private final int mIndexToParent;

    /** Number of descendant nodes of the current node. */
    private final long mDescendantCount;

    // Arc and lines drawing vars. ===========================
    /** Color of item. */
    private transient int mCol;

    /** Color of relation line. */
    private transient int mLineCol;

    /** Relation line weight. */
    private transient float mLineWeight;

    // Angle variables. ======================================
    /** The start of the angle in radians. */
    private final float mAngleStart;

    /** The center of the angle in radians. */
    private final float mAngleCenter;

    /** The end of the angle in radians. */
    private final float mAngleEnd;

    /** Radius of the current depth. */
    private transient float mRadius;

    /** Stroke weight of the arc. */
    private transient float mDepthWeight;

    /** X coordinate control point of the relation line. */
    private transient float mX;

    /** Y coordinate control point of the relation line. */
    private transient float mY;

    /** Distance between the two relation points (child/parent). */
    private transient float mArcLength;

    // Bezier controlpoints. =================================
    /** X coordinate of first bezier control point. */
    private float mC1X;

    /** Y coordinate of first bezier control point. */
    private float mC1Y;

    /** X coordinate of second bezier control point. */
    private float mC2X;

    /** Y coordinate of second bezier control point. */
    private float mC2Y;

    /** {@link QName} of current node. */
    private final QName mQName;

    /** {@link QName} of old node. */
    private transient QName mOldQName;

    /** Depth in the tree. */
    private final int mDepth;

    /** Global minimum of descendant nodes. */
    private final long mMinDescendantCount;

    /** Global maximum of descendant nodes. */
    private final long mMaxDescendantCount;

    /** Structural kind of node. */
    enum EStructType {
        /** Node is a leaf node. */
        ISLEAFNODE,

        /** Node is an inner node. */
        ISINNERNODE,
    }

    /** Structural kind of node. */
    private final EStructType mStructKind;

    /** State which determines if current item is found by an XPath expression or not. */
    private transient EXPathState mXPathState = EXPathState.ISNOTFOUND;

    /** Singleton {@link SunburstGUI} instance. */
    private transient SunburstGUI mGUI;

    /** Text string. */
    private final String mText;

    /** Old text string. */
    private transient String mOldText;

    /** Parent processing applet. */
    private final PApplet mParent;

    /** Kind of diff. */
    private transient EDiff mDiff;

    /** Determines if one must be subtracted. */
    private transient boolean mSubtract;

    /** Builder to setup the Items. */
    static final class Builder {
        /** {@link PApplet} representing the core processing library. */
        private final PApplet mParent;

        /** {@link AbsModel}. */
        private final AbsModel mModel;

        /** Current {@link IItem} in Treetank. */
        private transient IItem mNode;

        /** {@link QName} of current node. */
        private transient QName mQName;

        /** {@link QName} of old node. */
        private transient QName mOldQName;

        /** {@link NodeRelations} reference. */
        private final NodeRelations mRelations;

        /** The start degree. */
        private final float mAngleStart;

        /** The extension of the angle. */
        private final float mExtension;

        /** Text string. */
        private transient String mText;

        /** Old text string. */
        private transient String mOldText;

        /** Kind of diff. */
        private transient EDiff mDiff;

        /**
         * Read database.
         * 
         * @see ReadDB
         */
        private final ReadDB mReadDB;

        /**
         * Constructor.
         * 
         * @param paramApplet
         *            the processing core library @see PApplet
         * @param paramModel
         *            the {@link AbsModel}
         * @param paramAngleStart
         *            the start degree
         * @param paramExtension
         *            the extension of the angle
         * @param paramRelations
         *            {@link NodeRelations} instance
         * @param paramReadDB
         *            read database
         */
        Builder(final PApplet paramApplet, final AbsModel paramModel, final float paramAngleStart,
            final float paramExtension, final NodeRelations paramRelations, final ReadDB paramReadDB) {
            mParent = paramApplet;
            mModel = paramModel;
            mAngleStart = paramAngleStart;
            mExtension = paramExtension;
            mRelations = paramRelations;
            mReadDB = paramReadDB;
        }

        /**
         * Set the node.
         * 
         * @param paramNode
         *            {@link IItem} in Treetank, which belongs to this {@link SunburstItem}
         * @return this builder
         */
        Builder setNode(final IItem paramNode) {
            assert paramNode != null;
            mNode = paramNode;
            return this;
        }

        /**
         * Set {@link QName}.
         * 
         * @param paramQName
         *            {@link QName} of the current node.
         * @return this builder
         */
        Builder setQName(final QName paramQName) {
            assert paramQName != null;
            mQName = paramQName;
            return this;
        }

        /**
         * Set old {@link QName}.
         * 
         * @param paramOldQName
         *            {@link QName} of the current node.
         * @return this builder
         */
        Builder setOldQName(final QName paramOldQName) {
            assert paramOldQName != null;
            mOldQName = paramOldQName;
            return this;
        }

        /**
         * Set character content.
         * 
         * @param paramText
         *            text string in case of a text node
         * @return this builder
         */
        Builder setText(final String paramText) {
            assert paramText != null;
            mText = paramText;
            return this;
        }

        /**
         * Set old character content.
         * 
         * @param paramText
         *            text string in case of a text node
         * @return this builder
         */
        Builder setOldText(final String paramOldText) {
            assert paramOldText != null;
            mOldText = paramOldText;
            return this;
        }

        /**
         * Set kind of diff.
         * 
         * @param paramDiff
         *            {@link EDiff}
         * @return this builder
         */
        Builder setDiff(final EDiff paramDiff) {
            assert paramDiff != null;
            mDiff = paramDiff;
            return this;
        }

        /**
         * Build a new sunburst item.
         * 
         * @return a new sunburst item
         */
        SunburstItem build() {
            assert mNode != null;
            assert mQName != null || mText != null;
            return new SunburstItem(this);
        }
    }

    /**
     * Constructor.
     * 
     * @param paramBuilder
     *            The Builder to build a new sunburst item.
     */
    private SunburstItem(final Builder paramBuilder) {
        // Returns GUI singleton instance.
        mGUI = SunburstGUI.getInstance(paramBuilder.mParent, paramBuilder.mModel, paramBuilder.mReadDB);

        mNode = paramBuilder.mNode;
        mQName = paramBuilder.mQName;
        mOldQName = paramBuilder.mOldQName;
        mText = paramBuilder.mText;
        mOldText = paramBuilder.mOldText;
        mParent = paramBuilder.mParent;
        mStructKind = paramBuilder.mRelations.mStructKind;
        mDescendantCount = paramBuilder.mRelations.mDescendantCount;
        mMinDescendantCount = paramBuilder.mRelations.mMinDescendantCount;
        mMaxDescendantCount = paramBuilder.mRelations.mMaxDescendantCount;
        mIndexToParent = paramBuilder.mRelations.mIndexToParent;
        mDepth = paramBuilder.mRelations.mDepth;
        mSubtract = paramBuilder.mRelations.mSubtract;
        mAngleStart = paramBuilder.mAngleStart + 0.003f;
        mDiff = paramBuilder.mDiff;
        mAngleCenter = mAngleStart + paramBuilder.mExtension / 2;
        mAngleEnd = mAngleStart + paramBuilder.mExtension - 0.003f;
    }

    /**
     * Update item, called only when the Treetank storage has changed.
     * 
     * @param paramMappingMode
     *            specifies the mapping mode
     * @param paramBuffer
     *            offline buffer image
     */
    void update(final int paramMappingMode, final PGraphics paramBuffer) {
        assert paramMappingMode == 1 || paramMappingMode == 2 || paramMappingMode == 3;
        if (mIndexToParent > -1) {
            final int depthMax = mGUI.mDepthMax;
            mRadius = mGUI.calcEqualAreaRadius(mDepth, depthMax);
            mDepthWeight = mGUI.calcEqualAreaRadius(mDepth + 1, depthMax) - mRadius;
            mX = PApplet.cos(mAngleCenter) * mRadius;
            mY = PApplet.sin(mAngleCenter) * mRadius;

            // Chord.
            final float startX = PApplet.cos(mAngleCenter) * mRadius;
            final float startY = PApplet.sin(mAngleCenter) * mRadius;
            final float endX = PApplet.cos(mAngleEnd) * mRadius;
            final float endY = PApplet.sin(mAngleEnd) * mRadius;
            mArcLength = PApplet.dist(startX, startY, endX, endY);

            // Color mapings.
            float percent = 0;
            switch (paramMappingMode) {
            case 1:
                if (mMinDescendantCount == 0
                    && mMaxDescendantCount == 0
                    || (mMinDescendantCount == mMaxDescendantCount && mMaxDescendantCount == mDescendantCount)) {
                    percent = 0;
                } else {
                    percent =
                        (float)(mDescendantCount - mMinDescendantCount)
                            / (float)(mMaxDescendantCount - mMinDescendantCount);
                }
                break;
            case 2:
                percent =
                    (float)(PApplet.log(mDescendantCount) - PApplet.log(mMinDescendantCount))
                        / (float)(PApplet.log(mMaxDescendantCount) - PApplet.log(mMinDescendantCount));
                break;
            case 3:
                if (mMinDescendantCount == 0
                    && mMaxDescendantCount == 0
                    || (mMinDescendantCount == mMaxDescendantCount && mMaxDescendantCount == mDescendantCount)) {
                    percent = 0;
                } else {
                    percent =
                        (float)(PApplet.sqrt(mDescendantCount) - PApplet.sqrt(mMinDescendantCount))
                            / (float)(PApplet.sqrt(mMaxDescendantCount) - PApplet.sqrt(mMinDescendantCount));
                }
                break;
            default:
            }

            // Colors for element and other nodes.
            switch (mNode.getKind()) {
            case ELEMENT_KIND:
                float bright =
                    PApplet.lerp(mGUI.mInnerNodeBrightnessStart, mGUI.mInnerNodeBrightnessEnd, percent);
                if (paramBuffer == null) {
                    mCol = mParent.color(0, 0, bright);
                } else {
                    mCol = paramBuffer.color(0, 0, bright);
                }
                bright =
                    PApplet.lerp(mGUI.mInnerNodeStrokeBrightnessStart, mGUI.mInnerNodeStrokeBrightnessEnd,
                        percent);
                if (paramBuffer == null) {
                    mLineCol = mParent.color(0, 0, 1 - bright);
                } else {
                    mLineCol = paramBuffer.color(0, 0, 1 - bright);
                }
                break;
            case TEXT_KIND:
            case COMMENT_KIND:
            case PROCESSING_KIND:
                if (paramBuffer == null) {
                    final int from =
                        mParent.color(mGUI.mHueStart, mGUI.mSaturationStart, mGUI.mBrightnessStart);
                    final int to = mParent.color(mGUI.mHueEnd, mGUI.mSaturationEnd, mGUI.mBrightnessEnd);
                    mCol = mParent.lerpColor(from, to, percent);
                } else {
                    final int from =
                        paramBuffer.color(mGUI.mHueStart, mGUI.mSaturationStart, mGUI.mBrightnessStart);
                    final int to = paramBuffer.color(mGUI.mHueEnd, mGUI.mSaturationEnd, mGUI.mBrightnessEnd);
                    mCol = paramBuffer.lerpColor(from, to, percent);
                }
                mLineCol = mCol;
                break;
            default:
                throw new IllegalStateException("Node type currently not supported!");
            }

            // Calculate stroke weight for relations line.
            mLineWeight = PApplet.map(mDepth, 1, depthMax, mGUI.mStrokeWeightStart, mGUI.mStrokeWeightEnd);
            if (mArcLength < mLineWeight) {
                mLineWeight = mArcLength * 0.93f;
            }

            // Calculate bezier controlpoints.
            mC1X = PApplet.cos(mAngleCenter) * mGUI.calcEqualAreaRadius(mDepth - 1, depthMax);
            mC1Y = PApplet.sin(mAngleCenter) * mGUI.calcEqualAreaRadius(mDepth - 1, depthMax);

            mC2X = PApplet.cos(mGUI.mModel.getItem(mIndexToParent).mAngleCenter);
            mC2X *= mGUI.calcEqualAreaRadius(mDepth, depthMax);

            mC2Y = PApplet.sin(mGUI.mModel.getItem(mIndexToParent).mAngleCenter);
            mC2Y *= mGUI.calcEqualAreaRadius(mDepth, depthMax);
        }
    }

    // Draw methods ====================================
    /**
     * Draw an arc.
     * 
     * @param paramInnerNodeScale
     *            scale of inner nodes
     * @param paramLeafScale
     *            scale of leaf nodes
     */
    void drawArc(final float paramInnerNodeScale, final float paramLeafScale) {
        float arcRadius = 0;
        if (mDepth >= 0) {
            switch (mStructKind) {
            case ISLEAFNODE:
                mParent.strokeWeight(mDepthWeight * paramLeafScale);
                arcRadius = mRadius + mDepthWeight * paramLeafScale / 2;
                break;
            case ISINNERNODE:
                mParent.strokeWeight(mDepthWeight * paramInnerNodeScale);
                arcRadius = mRadius + mDepthWeight * paramInnerNodeScale / 2;
                break;
            default:
                throw new AssertionError("Structural kind not known!");
            }

            mXPathState.setStroke(mParent, mCol);

            // mParent.arc(0, 0, arcRadius, arcRadius, mAngleStart, mAngleEnd);
            arcWrap(0, 0, arcRadius, arcRadius, mAngleStart, mAngleEnd); // normaly arc should work
        }
    }

    /**
     * Fix for arc it seems that the arc functions has a problem with very tiny angles ...
     * arcWrap is a quick hack to get rid of this problem.
     * 
     * @param paramX
     *            X position of middle point
     * @param paramY
     *            Y position of middle point
     * @param paramW
     *            width of ellipse
     * @param paramH
     *            height of ellipse
     * @param paramA1
     *            angle to start from
     * @param paramA2
     *            angle to end
     */
    void arcWrap(final float paramX, final float paramY, final float paramW, final float paramH,
        final float paramA1, final float paramA2) {
        if (mArcLength > 2.5) {
            mParent.arc(paramX, paramY, paramW, paramH, paramA1, paramA2);
        } else {
            mParent.strokeWeight(mArcLength);
            mParent.pushMatrix();
            mParent.rotate(mAngleCenter);
            mParent.translate(mRadius, 0);
            mParent.line(0, 0, (paramW - mRadius) * 2, 0);
            mParent.popMatrix();
        }
    }

    /**
     * Draw current sunburst item as a rectangle.
     * 
     * @param paramInnerNodeScale
     *            scale of a non leaf node
     * @param paramLeafScale
     *            scale of a leaf node
     */
    void drawRect(final float paramInnerNodeScale, final float paramLeafScale) {
        float rectWidth;
        if (mDepth >= 0) {
            switch (mStructKind) {
            case ISLEAFNODE:
                rectWidth = mRadius + mDepthWeight * paramLeafScale / 2;
                break;
            case ISINNERNODE:
                rectWidth = mRadius + mDepthWeight * paramInnerNodeScale / 2;
                break;
            default:
                throw new AssertionError("Structural kind not known!");
            }

            mParent.stroke(mCol);
            mParent.strokeWeight(mArcLength);
            mParent.pushMatrix();
            mParent.rotate(mAngleCenter);
            mParent.translate(mRadius, 0);
            mParent.line(0, 0, (rectWidth - mRadius) * 2, 0);
            mParent.popMatrix();
        }
    }

    /**
     * Draw a dot which are the bezier-curve anchors.
     */
    void drawDot() {
        if (mDepth >= 0) {
            float diameter = mGUI.mDotSize;
            if (mDepth > 0 && mArcLength < diameter) {
                diameter = mArcLength * 0.95f;
            }
            mParent.noStroke();
            if (mGUI.mUseDiffView) {
                switch (mDiff) {
                case INSERTED:
                    mParent.fill(200, 100, mGUI.mDotBrightness);
                    break;
                case DELETED:
                    mParent.fill(360, 100, mGUI.mDotBrightness);
                    break;
                case UPDATED:
                    mParent.fill(120, 100, mGUI.mDotBrightness);
                    break;
                default:
                    mParent.fill(0, 0, 1 - mGUI.mDotBrightness);
                }

                mParent.ellipse(mX, mY, diameter, diameter);
            } else {
                mParent.colorMode(PConstants.HSB);
                mParent.fill(0, 0, 1 - mGUI.mDotBrightness);
                mParent.ellipse(mX, mY, diameter, diameter);
            }
            mParent.noFill();
        }
    }

    /**
     * Draw a straight line from child to parent.
     */
    void drawRelationLine() {
        if (mDepth > 0) {
            mParent.stroke(mLineCol);
            mParent.strokeWeight(mLineWeight);
            mParent.line(mX, mY, mGUI.mModel.getItem(mIndexToParent).mX,
                mGUI.mModel.getItem(mIndexToParent).mY);
        }
    }

    /**
     * Draw a bezier curve from child to parent.
     */
    void drawRelationBezier() {
        if (mDepth > 0) {
            assert mIndexToParent >= 0;
            mParent.stroke(mLineCol);
            if (mLineWeight < 0) {
                mLineWeight *= -1;
            }
            mParent.strokeWeight(mLineWeight);
            mParent.bezier(mX, mY, mC1X, mC1Y, mC2X, mC2Y, mGUI.mModel.getItem(mIndexToParent).mX,
                mGUI.mModel.getItem(mIndexToParent).mY);
        }
    }

    /**
     * Draw an arc.
     * 
     * @param paramInnerNodeScale
     *            scale of inner nodes
     * @param paramLeafScale
     *            scale of leaf nodes
     * @param paramBuffer
     *            offline buffer to draw to
     */
    void drawArcBuffer(final float paramInnerNodeScale, final float paramLeafScale,
        final PGraphics paramBuffer) {
        float arcRadius = 0;
        if (mDepth >= 0) {
            switch (mStructKind) {
            case ISLEAFNODE:
                paramBuffer.strokeWeight(mDepthWeight * paramLeafScale);
                arcRadius = mRadius + mDepthWeight * paramLeafScale / 2;
                break;
            case ISINNERNODE:
                paramBuffer.strokeWeight(mDepthWeight * paramInnerNodeScale);
                arcRadius = mRadius + mDepthWeight * paramInnerNodeScale / 2;
                break;
            default:
                throw new AssertionError("Structural kind not known!");
            }

            mXPathState.setStrokeBuffer(paramBuffer, mCol);

            // mParent.arc(0, 0, arcRadius, arcRadius, mAngleStart, mAngleEnd);
            arcWrapBuffer(0, 0, arcRadius, arcRadius, mAngleStart, mAngleEnd, paramBuffer); // normaly arc //
                                                                                            // should // work
        }
    }

    /**
     * Fix for arc it seems that the arc functions has a problem with very tiny angles ...
     * arcWrap is a quick hack to get rid of this problem.
     * 
     * @param paramX
     *            X position of middle point
     * @param paramY
     *            Y position of middle point
     * @param paramW
     *            width of ellipse
     * @param paramH
     *            height of ellipse
     * @param paramA1
     *            angle to start from
     * @param paramA2
     *            angle to end
     * @param paramBuffer
     *            offline buffer to draw to
     */
    void arcWrapBuffer(final float paramX, final float paramY, final float paramW, final float paramH,
        final float paramA1, final float paramA2, final PGraphics paramBuffer) {
        if (mArcLength > 2.5) {
            paramBuffer.arc(paramX, paramY, paramW, paramH, paramA1, paramA2);
        } else {
            paramBuffer.strokeWeight(mArcLength);
            paramBuffer.pushMatrix();
            paramBuffer.rotate(mAngleCenter);
            paramBuffer.translate(mRadius, 0);
            paramBuffer.line(0, 0, (paramW - mRadius) * 2, 0);
            paramBuffer.popMatrix();
        }
    }

    /**
     * Draw current sunburst item as a rectangle.
     * 
     * @param paramInnerNodeScale
     *            scale of a non leaf node
     * @param paramLeafScale
     *            scale of a leaf node
     * @param paramBuffer
     *            offline buffer to draw to
     */
    void drawRectBuffer(final float paramInnerNodeScale, final float paramLeafScale,
        final PGraphics paramBuffer) {
        float rectWidth;
        if (mDepth >= 0) {
            switch (mStructKind) {
            case ISLEAFNODE:
                rectWidth = mRadius + mDepthWeight * paramLeafScale / 2;
                break;
            case ISINNERNODE:
                rectWidth = mRadius + mDepthWeight * paramInnerNodeScale / 2;
                break;
            default:
                throw new AssertionError("Structural kind not known!");
            }

            paramBuffer.stroke(mCol);
            paramBuffer.strokeWeight(mArcLength);
            paramBuffer.pushMatrix();
            paramBuffer.rotate(mAngleCenter);
            paramBuffer.translate(mRadius, 0);
            paramBuffer.line(0, 0, (rectWidth - mRadius) * 2, 0);
            paramBuffer.popMatrix();
        }
    }

    /**
     * Draw a dot which are the bezier-curve anchors.
     * 
     * @param paramBuffer
     *            offline buffer to draw to
     */
    void drawDotBuffer(final PGraphics paramBuffer) {
        if (mDepth >= 0) {
            float diameter = mGUI.mDotSize;
            if (mDepth > 0 && mArcLength < diameter) {
                diameter = mArcLength * 0.95f;
            }
            paramBuffer.noStroke();
            if (mGUI.mUseDiffView) {
                switch (mDiff) {
                case INSERTED:
                    paramBuffer.fill(200, 100, mGUI.mDotBrightness);
                    break;
                case DELETED:
                    paramBuffer.fill(360, 100, mGUI.mDotBrightness);
                    break;
                case UPDATED:
                    paramBuffer.fill(120, 100, mGUI.mDotBrightness);
                    break;
                default:
                    paramBuffer.fill(0, 0, 1 - mGUI.mDotBrightness);
                }

                paramBuffer.ellipse(mX, mY, diameter, diameter);
            } else {
                paramBuffer.colorMode(PConstants.HSB);
                paramBuffer.fill(0, 0, 1 - mGUI.mDotBrightness);
                paramBuffer.ellipse(mX, mY, diameter, diameter);
            }
            paramBuffer.noFill();
        }
    }

    /**
     * Draw a straight line from child to parent.
     * 
     * @param paramBuffer
     *            offline buffer to draw to
     */
    void drawRelationLineBuffer(final PGraphics paramBuffer) {
        if (mDepth > 0) {
            paramBuffer.stroke(mLineCol);
            paramBuffer.strokeWeight(mLineWeight);
            paramBuffer.line(mX, mY, mGUI.mModel.getItem(mIndexToParent).mX,
                mGUI.mModel.getItem(mIndexToParent).mY);
        }
    }

    /**
     * Draw a bezier curve from child to parent.
     * 
     * @param paramBuffer
     *            offline buffer to draw to
     */
    void drawRelationBezierBuffer(final PGraphics paramBuffer) {
        if (mDepth > 0) {
            assert mIndexToParent >= 0;
            paramBuffer.stroke(mLineCol);
            if (mLineWeight < 0) {
                mLineWeight *= -1;
            }
            paramBuffer.strokeWeight(mLineWeight);
            paramBuffer.bezier(mX, mY, mC1X, mC1Y, mC2X, mC2Y, mGUI.mModel.getItem(mIndexToParent).mX,
                mGUI.mModel.getItem(mIndexToParent).mY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String retVal;
        if (mQName == null) {
            final StringBuilder builder =
                new StringBuilder().append("[Depth: ").append(mDepth).append(" Text: ").append(mText);
            updated(builder);
            builder.append(" NodeKey: ").append(mNode.getNodeKey()).append("]");
            retVal = builder.toString();
        } else {
            final StringBuilder builder =
                new StringBuilder().append("[Depth: ").append(mDepth).append(" QName: ")
                    .append(ViewUtilities.qNameToString(mQName));
            updated(builder);
            builder.append(" NodeKey: ").append(mNode.getNodeKey()).append("]");
            retVal = builder.toString();
        }
        return retVal;
    }

    /**
     * Node has been updated so append to {@link StringBuilder}.
     * 
     * @param builder
     *            {@link StringBuilder} instance
     */
    private void updated(StringBuilder builder) {
        if (mDiff != null && mDiff == EDiff.UPDATED) {
            if (mOldQName != null) {
                builder.append(" old QName: ").append(ViewUtilities.qNameToString(mOldQName));
            } else if (mOldText != null && !mOldText.isEmpty()) {
                builder.append(" old Text: ").append(mOldText);
            }
        }
    }

    /**
     * Set XPath state.
     * 
     * @param paramState
     *            set state to this value
     */
    void setXPathState(final EXPathState paramState) {
        mXPathState = paramState;
    }

    // Getter ==========================================
    /**
     * Get angle start.
     * 
     * @return the angleStart.
     */
    float getAngleStart() {
        return mAngleStart;
    }

    /**
     * Get angle end.
     * 
     * @return the angleEnd.
     */
    float getAngleEnd() {
        return mAngleEnd;
    }

    /**
     * Get current depth.
     * 
     * @return the depth.
     */
    int getDepth() {
        return mDepth;
    }

    /**
     * Get subtract.
     * 
     * @return true if one has to be subtracted
     */
    boolean getSubtract() {
        return mSubtract;
    }

    /**
     * Get node.
     * 
     * @return the Node
     */
    public IItem getNode() {
        return mNode;
    }
}
