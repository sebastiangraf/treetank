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

import java.util.List;

import javax.xml.namespace.QName;

import com.treetank.api.IItem;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.ViewUtilities;

import processing.core.PApplet;

/**
 * <h1>SunburstItem</h1>
 * 
 * <p>
 * Represents the view and exactly one item in the Sunburst diagram. Note that this class is not immutable
 * (notably because {@link AbsNodes} and all subclasses can be modified), but since it's package private it
 * should be used in a convenient way.
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

    /** The extension of the angle. */
    private final float mExtension;

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

    /** Depth in the tree. */
    private final int mDepth;

    /** Global minimum of descendant nodes. */
    private final long mMinDescendantCount;

    /** Global maximum of descendant nodes. */
    private final long mMaxDescendantCount;

    /** Structural kind of node. */
    enum StructType {
        /** Node is a leaf node. */
        ISLEAF,

        /** Node is an inner node. */
        ISINNERNODE,
    }

    /** Structural kind of node. */
    private final StructType mStructKind;

    /** State which determines if current item is found by an XPath expression or not. */
    private transient EXPathState mXPathState = EXPathState.ISNOTFOUND;

    /** Singleton {@link SunburstGUI} instance. */
    private transient SunburstGUI mGUI;

    /** {@link PApplet} representing the core processing library. */
    private final PApplet mParent;

    /** Text. */
    private final String mText;

    /** Builder to setup the Items. */
    public static final class Builder {
        /** {@link PApplet} representing the core processing library. */
        private final PApplet mParent;

        /** {@link SunburstModel}. */
        private final SunburstModel mModel;

        /** Current {@link IItem} in Treetank. */
        private final IItem mNode;

        /** {@link QName} of current node. */
        private final QName mQName;

        /** {@link NodeRelations} reference. */
        private final NodeRelations mRelations;

        /** The start degree. */
        private final float mAngleStart;

        /** The extension of the angle. */
        private final float mExtension;

        /** Text string. */
        private final String mText;

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
         *            the {@link SunburstModel}
         * @param paramNode
         *            {@link IItem} in Treetank, which belongs to this {@link SunburstItem}
         * @param paramQName
         *            {@link QName} of current node
         * @param paramText
         *            text string in case of a text node
         * @param paramAngleStart
         *            the start degree
         * @param paramExtension
         *            the extension of the angle
         * @param paramRelations
         *            {@link NodeRelations} instance
         * @param paramReadDB
         *            read database
         */
        public Builder(final PApplet paramApplet, final SunburstModel paramModel, final IItem paramNode,
            final QName paramQName, final String paramText, final float paramAngleStart,
            final float paramExtension, final NodeRelations paramRelations, final ReadDB paramReadDB) {
            mParent = paramApplet;
            mModel = paramModel;
            mNode = paramNode;
            mQName = paramQName;
            mText = paramText;
            mAngleStart = paramAngleStart;
            mExtension = paramExtension;
            mRelations = paramRelations;
            mReadDB = paramReadDB;
        }

        /**
         * Build a new sunburst item.
         * 
         * @return a new sunburst item.
         */
        public SunburstItem build() {
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
        mText = paramBuilder.mText;
        mParent = paramBuilder.mParent;
        mStructKind = paramBuilder.mRelations.mStructKind;
        mDescendantCount = paramBuilder.mRelations.mDescendantCount;
        mMinDescendantCount = paramBuilder.mRelations.mMinDescendantCount;
        mMaxDescendantCount = paramBuilder.mRelations.mMaxDescendantCount;
        mAngleStart = paramBuilder.mAngleStart;
        mExtension = paramBuilder.mExtension;
        mAngleCenter = mAngleStart + mExtension / 2;
        mAngleEnd = mAngleStart + mExtension;
        mIndexToParent = paramBuilder.mRelations.mIndexToParent;
        mDepth = paramBuilder.mRelations.mDepth;
    }

    /**
     * Update item, called only when the Treetank storage has changed.
     * 
     * @param paramMappingMode
     *            Specifies the mapping mode (currently only '1' is permitted).
     */
    void update(final int paramMappingMode) {
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
                percent =
                    (float)(mDescendantCount - mMinDescendantCount)
                        / (float)(mMaxDescendantCount - mMinDescendantCount);
                // percent = PApplet.norm(mDescendantCount, mMinDescendantCount, mMaxDescendantCount);
                break;
            case 2:
                percent =
                    (PApplet.log(mDescendantCount) - PApplet.log(mMinDescendantCount))
                        / (PApplet.log(mMaxDescendantCount) - PApplet.log(mMinDescendantCount));
                break;
            case 3:
                percent =
                    (PApplet.sqrt(mDescendantCount) - PApplet.sqrt(mMinDescendantCount))
                        / (PApplet.sqrt(mMaxDescendantCount) - PApplet.sqrt(mMinDescendantCount));
                break;
            default:
            }

            // Colors for leaf nodes and inner nodes.
            switch (mStructKind) {
            case ISLEAF:
                final int from = mParent.color(mGUI.mHueStart, mGUI.mSaturationStart, mGUI.mBrightnessStart);
                final int to = mParent.color(mGUI.mHueEnd, mGUI.mSaturationEnd, mGUI.mBrightnessEnd);
                mCol = mParent.lerpColor(from, to, 1 - percent);
                mLineCol = mCol;
                break;
            case ISINNERNODE:
                float bright = 0;
                bright =
                    PApplet.lerp(mGUI.mInnerNodeBrightnessStart, mGUI.mInnerNodeBrightnessEnd, 1 - percent);
                mCol = mParent.color(0, 0, bright);
                bright =
                    PApplet.lerp(mGUI.mInnerNodeStrokeBrightnessStart, mGUI.mInnerNodeStrokeBrightnessEnd,
                        percent);
                mLineCol = mParent.color(0, 0, bright);
                break;
            default:
                throw new AssertionError("Structural kind not known!");
            }

            // Calculate stroke weight for relations line.
            mLineWeight = PApplet.map(mDepth, 1, depthMax, mGUI.mStrokeWeightStart, mGUI.mStrokeWeightEnd);
            if (mArcLength < mLineWeight) {
                mLineWeight = mArcLength * 0.93f;
            }

            // Calculate bezier controlpoints.
            mC1X = PApplet.cos(mAngleCenter) * mGUI.calcEqualAreaRadius(mDepth - 1, depthMax);
            mC1Y = PApplet.sin(mAngleCenter) * mGUI.calcEqualAreaRadius(mDepth - 1, depthMax);

            final List<SunburstItem> items = mGUI.mItems;
            mC2X = PApplet.cos(items.get(mIndexToParent).mAngleCenter);
            mC2X *= mGUI.calcEqualAreaRadius(mDepth, depthMax);

            mC2Y = PApplet.sin(items.get(mIndexToParent).mAngleCenter);
            mC2Y *= mGUI.calcEqualAreaRadius(mDepth, depthMax);
        }
    }

    // Draw methods ====================================
    /**
     * Draw an arc.
     * 
     * @param paramInnerNodeScale
     *            Scale of inner nodes.
     * @param paramLeafScale
     *            Scale of leaf nodes.
     */
    void drawArc(final float paramInnerNodeScale, final float paramLeafScale) {
        float arcRadius = 0;
        if (mDepth >= 0) {
            switch (mStructKind) {
            case ISLEAF:
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

            // arc(0,0, arcRadius,arcRadius, angleStart, angleEnd);
            arcWrap(0, 0, arcRadius, arcRadius, mAngleStart, mAngleEnd); // normaly arc should workk
        }
    }

    /**
     * Fix for arc it seems that the arc functions has a problem with very tiny angles ...
     * arcWrap is a quick hack to get rid of this problem.
     * 
     * @param paramX
     * @param paramY
     * @param paramW
     * @param paramH
     * @param paramA1
     * @param paramA2
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
     *            Scale of a non leaf node.
     * @param paramLeafScale
     *            Scale of a leaf node.
     */
    void drawRect(final float paramInnerNodeScale, final float paramLeafScale) {
        float rectWidth;
        if (mDepth >= 0) {
            switch (mStructKind) {
            case ISLEAF:
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
            if (mArcLength < diameter) {
                diameter = mArcLength * 0.95f;
            }
            if (mDepth == 0) {
                diameter = 3f;
            }
            mParent.fill(0, 0, mGUI.mDotBrightness);
            mParent.noStroke();
            mParent.ellipse(mX, mY, diameter, diameter);
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
            final List<SunburstItem> items = mGUI.mItems;
            mParent.line(mX, mY, items.get(mIndexToParent).mX, items.get(mIndexToParent).mY);
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
            final List<SunburstItem> items = mGUI.mItems;
            mParent.bezier(mX, mY, mC1X, mC1Y, mC2X, mC2Y, items.get(mIndexToParent).mX, items
                .get(mIndexToParent).mY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String retVal;
        if (mQName == null) {
            retVal =
                new StringBuilder().append("[Depth: ").append(mDepth).append(" Text: ").append(mText).append(
                    " NodeKey: ").append(mNode.getNodeKey()).append("]").toString();
        } else {
            retVal =
                new StringBuilder().append("[Depth: ").append(mDepth).append(" QName: ").append(
                    ViewUtilities.qNameToString(mQName)).append(" NodeKey: ").append(mNode.getNodeKey())
                    .append("]").toString();
        }
        return retVal;
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
     * Get node.
     * 
     * @return the Node
     */
    public IItem getNode() {
        return mNode;
    }
}
