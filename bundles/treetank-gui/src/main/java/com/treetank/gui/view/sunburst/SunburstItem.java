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

import com.treetank.api.IItem;
import com.treetank.node.AbsNode;

import processing.core.PApplet;

/**
 * <h1>SunburstItem</h1>
 * 
 * <p>
 * Represents the view and exactly one item in the Sunburst diagram. Note that this class is not immutable
 * (notably because AbsStructNodes and all subclasses can be modified), but since it's package private it
 * should be used in a convenient way.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstItem {
    // Relations.
    /** Node depth in the tree. */
    private int mDepth;
    
    /** Index of current node. */
    private int mIndex;
    
    /** Index to parent node. */
    private int mIndexToParent;
    
    /** Number of child nodes of the current node. */
    private long mChildCount;

    // Arc and lines drawing vars.
    private int mCol;
    private int mLineCol;
    private float mLineWeight;
    private float mAngleStart;
    private float mAngleCenter;
    private float mAngleEnd;
    private float mExtension;
    private float mRadius;
    private float mDepthWeight; // stroke weight of the arc
    private float mX;
    private float mY;
    private float mArcLength;
    private float mC1X;
    private float mC1Y;
    private float mC2X;
    private float mC2Y; // bezier controlpoints

    /** Determines if current node is a leaf node. */
    private boolean mIsLeaf;

    /** Singleton {@link SunburstGUI} instance. */
    private transient SunburstGUI mGUI;

    /** {@link IItem}. */
    private final IItem mNode;

    /** {@link PApplet} representing the core processing library. */
    private final PApplet mParent;

    /** {@link List} of {@link SunburstItem}s. */
    private final List<SunburstItem> mItems;

    /** SunburstController. */
    private final SunburstController<? extends AbsModel, ? extends AbsView> mController;

    /** Builder to setup the Items. */
    public static final class Builder {
        /** {@link PApplet} representing the core processing library. */
        private final PApplet mParent;

        /** {@link List} of {@link SunburstItem}s. */
        private final List<SunburstItem> mItems;

        /** SunburstController. */
        private final SunburstController<? extends AbsModel, ? extends AbsView> mController;

        /** {@link IItem}. */
        private final IItem mNode;

        /** Determines if the current node is a leaf node. */
        private final boolean mIsLeaf;

        /** Determines how many children the current node has. */
        private final long mChildCount;

        /**
         * Constructor.
         * 
         * @param paramApplet
         *            The processing core library @see PApplet.
         * @param paramController
         *            {@link SunburstController}.
         * @param paramItem
         *            A Treetank {@link AbsNode}, which should be represented by this sunburst item.
         * @param paramIsLeaf
         *            Determines if the current node is a leaf node.
         * @param paramchildCount
         *            Determines how many children the current node has.
         */
        @SuppressWarnings("unchecked")
        public Builder(final PApplet paramApplet,
            final SunburstController<? extends AbsModel, ? extends AbsView> paramController,
            final IItem paramItem, final boolean paramIsLeaf, final long paramchildCount) {
            mParent = paramApplet;
            mController = paramController;
            mItems = (List<SunburstItem>)paramController.get("Items");
            mNode = paramItem;
            mIsLeaf = paramIsLeaf;
            mChildCount = paramchildCount;
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
        mGUI = SunburstGUI.createGUI(paramBuilder.mParent, paramBuilder.mController);
        mParent = paramBuilder.mParent;
        mController = paramBuilder.mController;
        mItems = paramBuilder.mItems;
        mNode = paramBuilder.mNode;
        mIsLeaf = paramBuilder.mIsLeaf;
        mChildCount = paramBuilder.mChildCount;
    }

    // ------ update function, called only when the input files are changed ------
    void update(int theMappingMode) {
        if (mIndexToParent > -1) {
            final int depthMax = (Integer)mController.get("DepthMax");
            mRadius = calcEqualAreaRadius(mDepth, depthMax);
            mDepthWeight = calcEqualAreaRadius(mDepth + 1, depthMax) - mRadius;
            mX = PApplet.cos(mAngleCenter) * mRadius;
            mY = PApplet.sin(mAngleCenter) * mRadius;

            // chord
            final float startX = PApplet.cos(mAngleCenter) * mRadius;
            final float startY = PApplet.sin(mAngleCenter) * mRadius;
            final float endX = PApplet.cos(mAngleEnd) * mRadius;
            final float endY = PApplet.sin(mAngleEnd) * mRadius;
            mArcLength = PApplet.dist(startX, startY, endX, endY);

            // color mapings
            float percent = 0;
            switch (theMappingMode) {
            case 1:
                // percent = norm(lastModified, lastModifiedOldest, lastModifiedYoungest);
                // break;
                // case 2:
                // percent = norm(fileSize, fileSizeMin, fileSizeMax);
                // break;
                // case 3:
                // percent = norm(fileSize, folderMinFilesize, folderMaxFilesize);
                // break;
            default:
            }

            // Colors for leaf nodes and inner nodes.
            if (mIsLeaf) {
                final int from =
                    mParent.color(mGUI.getHueStart(), mGUI.getSaturationStart(), mGUI.getBrightnessStart());
                final int to =
                    mParent.color(mGUI.getHueEnd(), mGUI.getSaturationEnd(), mGUI.getBrightnessEnd());
                mCol = mParent.lerpColor(from, to, percent);
                mLineCol = mCol;
            } else {
                float bright = 0;
                bright =
                    PApplet.lerp(mGUI.getInnerNodeBrightnessStart(), mGUI.getInnerNodeStrokeBrightnessEnd(),
                        percent);
                mCol = mParent.color(0, 0, bright);
                bright =
                    PApplet.lerp(mGUI.getInnerNodeStrokeBrightnessStart(), mGUI
                        .getInnerNodeStrokeBrightnessEnd(), percent);
                mLineCol = mParent.color(0, 0, bright);
            }

            // Calculate stroke weight for relations line.
            mLineWeight =
                PApplet.map(mDepth, depthMax, 1, mGUI.getStrokeWeightStart(), mGUI.getStrokeWeightEnd());
            if (mArcLength < mLineWeight) {
                mLineWeight = mArcLength * 0.93f;
            }

            // Calculate bezier controlpoints.
            mC1X = PApplet.cos(mAngleCenter) * calcEqualAreaRadius(mDepth - 1, depthMax);
            mC1Y = PApplet.sin(mAngleCenter) * calcEqualAreaRadius(mDepth - 1, depthMax);

            mC2X = PApplet.cos(mItems.get(mIndexToParent).mAngleCenter);
            mC2X *= calcEqualAreaRadius(mDepth, depthMax);

            mC2Y = PApplet.sin(mItems.get(mIndexToParent).mAngleCenter);
            mC2Y *= calcEqualAreaRadius(mDepth, depthMax);
        }
    }

    // ------ draw functions ------
    void drawArc(final float paramInnerNodeScale, final float paramLeafScale) {
        float arcRadius;
        if (mDepth > 0) {
            if (mIsLeaf) {
                mParent.strokeWeight(mDepthWeight * paramLeafScale);
                arcRadius = mRadius + mDepthWeight * paramLeafScale / 2;
            } else {
                mParent.strokeWeight(mDepthWeight * paramInnerNodeScale);
                arcRadius = mRadius + mDepthWeight * paramInnerNodeScale / 2;

            }
            mParent.stroke(mCol);
            // arc(0,0, arcRadius,arcRadius, angleStart, angleEnd);
            arcWrap(0, 0, arcRadius, arcRadius, mAngleStart, mAngleEnd); // normaly arc should work
        }
    }

    // fix for arc
    // it seems that the arc functions has a problem with very tiny angles ...
    // arcWrap is a quick hack to get rid of this problem
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
        if (mDepth > 0) {
            if (mIsLeaf) {
                rectWidth = mRadius + mDepthWeight * paramLeafScale / 2;
            } else {
                rectWidth = mRadius + mDepthWeight * paramInnerNodeScale / 2;
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
        if (mDepth > 0) {
            float diameter = mGUI.getDotSize();
            if (mArcLength < diameter) {
                diameter = mArcLength * 0.95f;
            }
            if (mDepth == 0) {
                diameter = 3;
            }
            mParent.fill(0, 0, mGUI.getDotBrightness());
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
            mParent.line(mX, mY, mItems.get(mIndexToParent).mX, mItems.get(mIndexToParent).mY);
        }
    }

    /**
     * Draw a bezier curve from child to parent.
     */
    void drawRelationBezier() {
        if (mDepth > 1) {
            mParent.stroke(mLineCol);
            mParent.strokeWeight(mLineWeight);
            mParent.bezier(mX, mY, mC1X, mC1Y, mC2X, mC2Y, mItems.get(mIndexToParent).mX, mItems
                .get(mIndexToParent).mY);
        }
    }

    /**
     * Calculate area so that radiuses have equal areas in each depth.
     * 
     * @param paramDepth
     *            The actual depth.
     * @param paramDepthMax
     *            The maximum depth.
     * @return calculated area.
     */
    float calcEqualAreaRadius(final int paramDepth, final int paramDepthMax) {
        return PApplet.sqrt(paramDepth * PApplet.pow(mParent.height / 2, 2) / (paramDepthMax + 1));
    }

    /**
     * Calculate area radius in a linear way.
     * 
     * @param paramDepth
     *            The actual depth.
     * @param paramDepthMax
     *            The maximum depth.
     * @return calculated area.
     */
    float calcAreaRadius(final int paramDepth, final int paramDepthMax) {
        return PApplet.map(paramDepth, 0, paramDepthMax + 1, 0, mParent.height / 2);
    }

    // -------- Getter -------
    /**
     * @return the angleStart.
     */
    float getAngleStart() {
        return mAngleStart;
    }

    /**
     * @return the angleEnd.
     */
    float getAngleEnd() {
        return mAngleEnd;
    }

    /**
     * @return the depth.
     */
    int getDepth() {
        return mDepth;
    }
}
