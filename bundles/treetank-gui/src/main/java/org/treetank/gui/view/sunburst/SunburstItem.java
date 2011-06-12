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

import org.treetank.access.WriteTransactionState;
import org.treetank.api.IItem;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.EHover;
import org.treetank.gui.view.IVisualItem;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.sunburst.EDraw.EDrawSunburst;
import org.treetank.gui.view.model.AbsModel;
import org.treetank.gui.view.sunburst.SunburstView.Embedded;

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
public final class SunburstItem implements IVisualItem {

    /** Current {@link IItem} in Treetank. */
    private final IItem mNode;

    // Relations. ============================================
    /** Index to parent node. */
    private final int mIndexToParent;

    /** Number of descendant nodes of the current node. */
    private int mDescendantCount;

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
    private float mAngleEnd;

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
    final QName mQName;

    /** {@link QName} of old node. */
    private transient QName mOldQName;

    /** Depth in the tree. */
    private final int mDepth;

    /** Global minimum of descendant nodes. */
    private final long mMinDescendantCount;

    /** Global maximum of descendant nodes. */
    private final long mMaxDescendantCount;

    /** Structural kind of node. */
    public enum EStructType {
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
    final String mText;

    /** Old text string. */
    private transient String mOldText;

    /** Parent processing applet. */
    private final PApplet mParent;

    /** Kind of diff. */
    transient EDiff mDiff;

    /** Determines if one must be subtracted. */
    private transient boolean mSubtract;

    /** Image to write to. */
    private transient PGraphics mGraphic;

    /** Modification count. */
    private transient int mModifications;

    /** Builder to setup the Items. */
    public static final class Builder {
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

        /** Modification count. */
        private transient int mModifications;

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
         *            {@link ReadDB} instance
         */
        public Builder(final PApplet paramApplet, final AbsModel paramModel, final float paramAngleStart,
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
        public Builder setNode(final IItem paramNode) {
            assert paramNode != null;
            mNode = paramNode;
            return this;
        }

        /**
         * Set modification count.
         * 
         * @param paramModifications
         *            counted modifications in subtree of current node
         * 
         * @return this builder
         */
        public Builder setModifcations(final int paramModifications) {
            assert paramModifications >= 0;
            mModifications = paramModifications;
            return this;
        }

        /**
         * Set {@link QName}.
         * 
         * @param paramQName
         *            {@link QName} of the current node.
         * @return this builder
         */
        public Builder setQName(final QName paramQName) {
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
        public Builder setOldQName(final QName paramOldQName) {
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
        public Builder setText(final String paramText) {
            assert paramText != null;
            mText = paramText;
            return this;
        }

        /**
         * Set old character content.
         * 
         * @param paramOldText
         *            text string in case of a text node
         * @return this builder
         */
        public Builder setOldText(final String paramOldText) {
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
        public Builder setDiff(final EDiff paramDiff) {
            assert paramDiff != null;
            mDiff = paramDiff;
            return this;
        }

        /**
         * Build a new sunburst item.
         * 
         * @return a new sunburst item
         */
        public SunburstItem build() {
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
        mGUI =
            SunburstGUI.getInstance(paramBuilder.mParent, ((Embedded)paramBuilder.mParent).getController(),
                paramBuilder.mReadDB);

        mNode = paramBuilder.mNode;
        mQName = paramBuilder.mQName;
        mOldQName = paramBuilder.mOldQName;
        mText = paramBuilder.mText;
        mOldText = paramBuilder.mOldText;
        mParent = paramBuilder.mParent;
        mModifications = paramBuilder.mModifications;
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
     * @param paramGraphic
     *            offline buffer image
     */
    @Override
    public void update(final int paramMappingMode, final PGraphics paramGraphic) {
        assert paramMappingMode == 1 || paramMappingMode == 2 || paramMappingMode == 3;
        assert paramGraphic != null;
        mGraphic = paramGraphic;
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
                    PApplet.lerp(mGUI.getInnerNodeBrightnessStart(), mGUI.getInnerNodeBrightnessEnd(), percent);
                mCol = paramGraphic.color(0, 0, bright);

                // bright =
                // PApplet.lerp(mGUI.mInnerNodeStrokeBrightnessStart, mGUI.mInnerNodeStrokeBrightnessEnd,
                // percent);
                mLineCol = paramGraphic.color(0, 0, 0);
                break;
            case TEXT_KIND:
            case COMMENT_KIND:
            case PROCESSING_KIND:
                final int from =
                    paramGraphic.color(mGUI.getHueStart(), mGUI.getSaturationStart(), mGUI.getBrightnessStart());
                final int to = paramGraphic.color(mGUI.getHueEnd(), mGUI.getSaturationEnd(), mGUI.getBrightnessEnd());
                mCol = paramGraphic.lerpColor(from, to, percent);

                mLineCol = mCol;
                break;
            default:
                throw new IllegalStateException("Node type currently not supported!");
            }

            // Calculate stroke weight for relations line.
            mLineWeight = PApplet.map(mDepth, 0, depthMax, mGUI.getStrokeWeightStart(), mGUI.getStrokeWeightEnd());
            if (mArcLength < mLineWeight) {
                mLineWeight = mArcLength * 0.93f;
            }

            // Calculate bezier controlpoints.
            mC1X = PApplet.cos(mAngleCenter) * mGUI.calcEqualAreaRadius(mDepth - 1, depthMax);
            mC1Y = PApplet.sin(mAngleCenter) * mGUI.calcEqualAreaRadius(mDepth - 1, depthMax);

            mC2X = PApplet.cos(((SunburstItem)mGUI.mControl.getModel().getItem(mIndexToParent)).mAngleCenter);
            mC2X *= mGUI.calcEqualAreaRadius(mDepth, depthMax);

            mC2Y = PApplet.sin(((SunburstItem)mGUI.mControl.getModel().getItem(mIndexToParent)).mAngleCenter);
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
     * @param paramHover
     *            determines if item currently is hovered or not
     */
    void drawArc(final float paramInnerNodeScale, final float paramLeafScale, final EHover paramHover) {
        assert paramInnerNodeScale > 0f;
        assert paramLeafScale > 0f;
        assert mGraphic != null;
        float arcRadius = 0;
        switch (mStructKind) {
        case ISLEAFNODE:
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.strokeWeight(mDepthWeight * paramLeafScale);
            }
            mGraphic.strokeWeight(mDepthWeight * paramLeafScale);
            arcRadius = mRadius + mDepthWeight * paramLeafScale / 2;
            break;
        case ISINNERNODE:
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.strokeWeight(mDepthWeight * paramInnerNodeScale);
            }
            mGraphic.strokeWeight(mDepthWeight * paramInnerNodeScale);
            arcRadius = mRadius + mDepthWeight * paramInnerNodeScale / 2;
            break;
        default:
            throw new AssertionError("Structural kind not known!");
        }

        mXPathState.setStroke(mGraphic, mGUI.mParent.recorder, mCol, paramHover);
        if (paramHover == EHover.TRUE) {
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.noFill();
            }
            mGraphic.noFill();
        }

        // mParent.arc(0, 0, arcRadius, arcRadius, mAngleStart, mAngleEnd);
        arcWrap(0, 0, arcRadius, arcRadius, mAngleStart, mAngleEnd); // normaly arc should work
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
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.arc(paramX, paramY, paramW, paramH, paramA1, paramA2);
            }
            mGraphic.arc(paramX, paramY, paramW, paramH, paramA1, paramA2);
        } else {
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.strokeWeight(mArcLength);
                mGUI.mParent.recorder.pushMatrix();
                mGUI.mParent.recorder.rotate(mAngleCenter);
                mGUI.mParent.recorder.translate(mRadius, 0);
                mGUI.mParent.recorder.line(0, 0, (paramW - mRadius) * 2, 0);
                mGUI.mParent.recorder.popMatrix();
            }
            mGraphic.strokeWeight(mArcLength);
            mGraphic.pushMatrix();
            mGraphic.rotate(mAngleCenter);
            mGraphic.translate(mRadius, 0);
            mGraphic.line(0, 0, (paramW - mRadius) * 2, 0);
            mGraphic.popMatrix();
        }
    }

    /**
     * Draw current sunburst item as a rectangle.
     * 
     * @param paramInnerNodeScale
     *            scale of a non leaf node
     * @param paramLeafScale
     *            scale of a leaf node
     * @param paramHover
     *            determines if item currently is hovered or not
     */
    void drawRect(final float paramInnerNodeScale, final float paramLeafScale, final EHover paramHover) {
        float rectWidth;
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

        mXPathState.setStroke(mGraphic, mGUI.mParent.recorder, mCol, paramHover);
        if (mGUI.mParent.recorder != null) {
            if (paramHover == EHover.TRUE) {
                mGUI.mParent.recorder.noFill();
            }
            mGUI.mParent.recorder.strokeWeight(mArcLength);
            mGUI.mParent.recorder.pushMatrix();
            mGUI.mParent.recorder.rotate(mAngleCenter);
            mGUI.mParent.recorder.translate(mRadius, 0);
            mGUI.mParent.recorder.line(0, 0, (rectWidth - mRadius) * 2, 0);
            mGUI.mParent.recorder.popMatrix();
        }
        if (paramHover == EHover.TRUE) {
            mGraphic.noFill();
        }
        mGraphic.stroke(mCol);
        mGraphic.strokeWeight(mArcLength);
        mGraphic.pushMatrix();
        mGraphic.rotate(mAngleCenter);
        mGraphic.translate(mRadius, 0);
        mGraphic.line(0, 0, (rectWidth - mRadius) * 2, 0);
        mGraphic.popMatrix();
    }

    /**
     * Draw a dot which are the bezier-curve anchors.
     * 
     * @param paramHover
     *            determines if item currently is hovered or not
     */
    void drawDot(final EHover paramHover) {
        float diameter = mGUI.getDotSize();

        if (paramHover == EHover.TRUE) {
            diameter = diameter * 2f;
        }

        if (mDepth > 0 && mArcLength < diameter) {
            diameter = mArcLength * 0.95f;
        }

        if (mGUI.mParent.recorder != null) {
            mGUI.mParent.recorder.noStroke();
        }
        mGraphic.noStroke();
        if (mGUI.mUseDiffView) {
            switch (mDiff) {
            case INSERTED:
                if (mGUI.mParent.recorder != null) {
                    mGUI.mParent.recorder.fill(200, 100, mGUI.getDotBrightness());
                }
                mGraphic.fill(200, 100, mGUI.getDotBrightness());
                break;
            case DELETED:
                if (mGUI.mParent.recorder != null) {
                    mGUI.mParent.recorder.fill(360, 100, mGUI.getDotBrightness());
                }
                mGraphic.fill(360, 100, mGUI.getDotBrightness());
                break;
            case UPDATED:
                if (mGUI.mParent.recorder != null) {
                    mGUI.mParent.recorder.fill(120, 100, mGUI.getDotBrightness());
                }
                mGraphic.fill(120, 100, mGUI.getDotBrightness());
                break;
            default:
                // EDiff.SAME.
                dot();
            }

        } else {
            dot();
        }
        if (mGUI.mParent.recorder != null) {
            mGUI.mParent.recorder.ellipse(mX, mY, diameter, diameter);
            mGUI.mParent.recorder.noFill();
        }
        mGraphic.ellipse(mX, mY, diameter, diameter);
        mGraphic.noFill();
    }

    /**
     * Draw black or white dot determined through the background brightness.
     * 
     * @param paramHover
     *            determines if item is hovered or not
     */
    private void dot() {
        if (mGUI.mParent.recorder != null) {
            if (mGUI.getBackgroundBrightness() < 30) {
                mGUI.mParent.recorder.fill(0, 0, 20);
            } else {
                mGUI.mParent.recorder.fill(0, 0, 0);
            }
        }

        if (mGUI.getBackgroundBrightness() < 30) {
            mGraphic.fill(0, 0, 20);
        } else {
            mGraphic.fill(0, 0, 0);
        }
    }

    /**
     * Draw a straight line from child to parent.
     */
    void drawRelationLine() {
        if (mIndexToParent > -1) {
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.stroke(mLineCol);
            }
            mGraphic.stroke(mLineCol);
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.strokeWeight(mLineWeight);
            }
            mGraphic.strokeWeight(mLineWeight);
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.line(mX, mY,
                    ((SunburstItem)mGUI.mControl.getModel().getItem(mIndexToParent)).mX,
                    ((SunburstItem)mGUI.mControl.getModel().getItem(mIndexToParent)).mY);
            }
            mGraphic.line(mX, mY, ((SunburstItem)mGUI.mControl.getModel().getItem(mIndexToParent)).mX,
                ((SunburstItem)mGUI.mControl.getModel().getItem(mIndexToParent)).mY);
        }
    }

    /**
     * Draw a bezier curve from child to parent.
     */
    void drawRelationBezier() {
        if (mIndexToParent > -1) {
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.stroke(mLineCol);
            }
            mGraphic.stroke(mLineCol);
            if (mLineWeight < 0) {
                mLineWeight *= -1;
            }
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.strokeWeight(mLineWeight);
            }
            mGraphic.strokeWeight(mLineWeight);
            final SunburstItem item = (SunburstItem)mGUI.mControl.getModel().getItem(mIndexToParent);
            if (mGUI.mParent.recorder != null) {
                mGUI.mParent.recorder.bezier(mX, mY, mC1X, mC1Y, mC2X, mC2Y, item.mX, item.mY);
            }
            mGraphic.bezier(mX, mY, mC1X, mC1Y, mC2X, mC2Y, item.mX, item.mY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String retVal;
        final StringBuilder builder = new StringBuilder().append("[Depth: ").append(mDepth);
        if (mQName == null) {
            builder.append(" Text: ").append(mText);
        } else {
            builder.append(" QName: ").append(WriteTransactionState.buildName(mQName));
        }
        updated(builder);
        if (mModifications > 0) {
            builder.append(" ModifcationCount: ").append(mModifications);
        }
        builder.append(" NodeKey: ").append(mNode.getNodeKey()).append("]");
        retVal = builder.toString();
        return retVal;
    }

    /**
     * Node has been updated so append to {@link StringBuilder}.
     * 
     * @param builder
     *            {@link StringBuilder} instance
     */
    void updated(StringBuilder builder) {
        if (mDiff != null && mDiff == EDiff.UPDATED) {
            if (mOldQName != null) {
                builder.append(" old QName: ").append(WriteTransactionState.buildName(mOldQName));
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
    public void setXPathState(final EXPathState paramState) {
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
    public boolean getSubtract() {
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

    /** {@inheritDoc} */
    @Override
    public void hover() {
        mGraphic = mParent.g;
        if (mGUI.isShowArcs() && !mGUI.isShowLines()) {
            if (mGUI.mUseArc) {
                drawArc(mGUI.getInnerNodeArcScale(), mGUI.getLeafArcScale(), EHover.TRUE);
            } else {
                drawRect(mGUI.getInnerNodeArcScale(), mGUI.getLeafArcScale(), EHover.TRUE);
            }

            if (mGUI.isUseBezierLine()) {
                drawRelationBezier();
            } else {
                drawRelationLine();
            }
            drawDot(EHover.TRUE);
            //
            // for (int index = mGUI.mHitTestIndex + 1;; index++) {
            // if (index < mGUI.mModel.mItems.size()) {
            // final SunburstItem item = mGUI.mModel.getItem(index);
            // if (item.mDepth == mDepth + 1 && mGUI.mShowLines) {
            // if (mGUI.mUseBezierLine) {
            // item.drawRelationBezier();
            // } else {
            // item.drawRelationLine();
            // }
            // } else {
            // break;
            // }
            // }
            // }
            //
        } else {
            float tmpLineWeight = mLineWeight;
            mLineWeight += 5f;
            if (mGUI.isUseBezierLine()) {
                drawRelationBezier();
            } else {
                drawRelationLine();
            }
            mLineWeight = tmpLineWeight;
            drawDot(EHover.TRUE);

        }
    }

    /**
     * Get index of parent node.
     * 
     * @return index of parent node
     */
    public int getIndexToParent() {
        return mIndexToParent;
    }

    /**
     * Calculate new extension.
     */
    public void calcNewExtension() {
        mDescendantCount--;
        final SunburstItem parent = (SunburstItem)mGUI.mControl.getModel().getItem(mIndexToParent);
        // Calculate extension.
        float extension = 2 * PConstants.PI;
        float parentModificationCount = parent.getModificationCount();
        if (mIndexToParent > -1) {
            if (parent.getSubtract()) {
                parentModificationCount -= 1;
            }
            float parExtension = parent.getAngleEnd() - parent.getAngleStart();
            extension =
                (1 - mGUI.getModificationWeight())
                    * (parExtension * (float)mDescendantCount / ((float)parent.getDescendantCount() - 1f))
                    + mGUI.getModificationWeight()
                    * (parExtension * (float)mModifications / ((float)parentModificationCount - 1f));
        }
        mAngleEnd = mAngleStart + extension;
    }

    /**
     * Set modification count.
     * 
     * @param paramModificationCount
     *            new modification count
     */
    public void setModificationCount(final int paramModificationCount) {
        assert paramModificationCount >= 1;
        mModifications = paramModificationCount;
    }

    /**
     * Get modification count.
     * 
     * @return modification count
     */
    public int getModificationCount() {
        return mModifications;
    }

    /**
     * Set descendant count.
     * 
     * @param paramDescendantCount
     *            new descendant count (actually descendant-or-self)
     */
    public void setDescendantCount(final int paramDescendantCount) {
        assert paramDescendantCount >= 1;
        mDescendantCount = paramDescendantCount;
    }

    /**
     * Get descendant count.
     * 
     * @return descendant count
     */
    public int getDescendantCount() {
        return mDescendantCount;
    }

    /**
     * Set angle end.
     * 
     * @param paramAngleEnd
     *            new angle end
     */
    public void setAngleEnd(final float paramAngleEnd) {
        assert paramAngleEnd > 0f;
        mAngleEnd = paramAngleEnd;
    }

    /** {@inheritDoc} */
    @Override
    public long getNodeKey() {
        return mNode.getNodeKey();
    }
}
