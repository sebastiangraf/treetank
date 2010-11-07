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

import java.util.ArrayList;
import java.util.List;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.ChildAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.tree.TreeModel;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * <h1>SunburstModel</h1>
 * 
 * <p>
 * The model, which interacts with Treetank.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstModel extends AbsModel {
    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(TreeModel.class));

    /** {@link List} of sunburst items. */
    private transient List<SunburstItem> mItems;

    /** Treetank {@link IReadTransaction}. */
    private transient IReadTransaction mRtx;

    /** The controller. */
    private final SunburstController<? extends AbsModel, ? extends AbsView> mController;

    /** The processing {@link PApplet} core library. */
    private final PApplet mParent;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            The processing {@link PApplet} core library.
     * @param paramDb
     *            {@link ReadDB} instance.
     * @param paramController
     *            The {@link SunburstController}.
     */
    SunburstModel(final PApplet paramApplet, final ReadDB paramDb,
        final SunburstController<? extends AbsModel, ? extends AbsView> paramController) {
        mController = paramController;
        mItems = new ArrayList<SunburstItem>();
        mParent = paramApplet;
        mRtx = paramDb.getRtx();
    }

    /**
     * Get maximum depth in the tree.
     * 
     * @return depthMax.
     */
    Integer getDepthMax() {
        int depthMax = 0;

        for (final SunburstItem item : mItems) {
            depthMax = PApplet.max(item.getDepth(), depthMax);
        }

        return depthMax;
    }

    /**
     * Get sunburst items.
     * 
     * @return List of {@link SunburstItem}s.
     */
    List<SunburstItem> getItems() {
        return mItems;
    }

    /**
     * Traverse the tree and create sunburst items.
     * 
     * Precondition: Node has to be either the document root or an element.
     * 
     * @return {@link List} of {@link SunburstItem}s.
     */
    List<SunburstItem> traverseTree() {
        LOGWRAPPER.debug("Build sunburst items.");
        assert mRtx != null;
        // Assert that it's the node is of the right kind.
        final AbsStructNode startNode = (AbsStructNode)mRtx.getNode();
        assert startNode.getKind().equals(ENodes.ELEMENT_KIND)
            || startNode.getKind().equals(ENodes.ROOT_KIND);

        // Initialize variables.
        float angleOffset = 0f;
        float oldAngle = 0f;
        float angle = 0f;
        long minChildCount = Long.MAX_VALUE;
        long maxChildCount = Long.MIN_VALUE;
        int depth = 0;
        int indexToParent = -1;

        // Iterate over descendant axis.
        for (final IAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
            indexToParent++;

            final long childCount = ((AbsStructNode)mRtx.getNode()).getChildCount();
            final float anglePerChild = PConstants.TWO_PI / childCount;

            // If there is an angle change (= entering a new child node) reset angleOffset
            if (oldAngle != angle) {
                angleOffset = 0f;
            }

            // Compute min and max child count of the children of the current node.
            final long key = mRtx.getNode().getNodeKey();
            mRtx.moveToFirstChild();
            do {
                final AbsStructNode node = (AbsStructNode)mRtx.getNode();
                minChildCount = Math.min(node.getChildCount(), minChildCount);
                maxChildCount = Math.max(node.getChildCount(), maxChildCount);
            } while (((AbsStructNode)mRtx.getNode()).hasRightSibling() && mRtx.moveToRightSibling());
            mRtx.moveTo(key);

            final float extension = ((AbsStructNode)mRtx.getNode()).getChildCount() * anglePerChild;

            // Add a sunburst item.
            addItem(depth, angle, angleOffset, extension, minChildCount, maxChildCount, indexToParent);

            // Increment angle offset.
            angleOffset += extension;
            oldAngle = angle;

            // Determines if angle needs to be adjusted.
            if (((AbsStructNode)mRtx.getNode()).hasFirstChild()) {
                mRtx.moveToFirstChild();
                depth++;
                angle += angleOffset;
            }
        }

        return mItems;
    }

    /**
     * Traverse children of node and build sunburst items.
     * 
     * @return {@link List} of {@link SunburstItem}s.
     */
    List<SunburstItem> traverseChildren() {
        LOGWRAPPER.debug("Build sunburst items.");

        final IAxis axis = new ChildAxis(mRtx);
        while (axis.hasNext()) {
            axis.next();
            // addItem();
        }

        return mItems;
    }

    /**
     * Add a sunburst item.
     * 
     * @param paramDepth
     *            Depth in the tree.
     * @param paramAngle
     *            The start angle.
     * @param paramAngleOffset
     *            Angle offset.
     * @param paramExtension
     *            Angle extension.
     * @param paramMinChildCount
     *            Min child count of current child nodes.
     * @param paramMaxChildCount
     *            Max child count of current child nodes.
     * @param paramIndexToParent
     *            Index of parent node.
     */
    private void addItem(final int paramDepth, final float paramAngle, final float paramAngleOffset,
        final float paramExtension, final long paramMinChildCount, final long paramMaxChildCount,
        final int paramIndexToParent) {
        final AbsStructNode node = (AbsStructNode)mRtx.getNode();
        final boolean isLeaf = node.hasFirstChild() ? false : true;
        final long childCount = node.getChildCount();
        mItems.add(new SunburstItem.Builder(mParent, mController, node, paramDepth, isLeaf, childCount,
            (paramAngle + paramAngleOffset) % PConstants.TWO_PI, paramExtension, paramMinChildCount,
            paramMaxChildCount, paramIndexToParent).build());
    }
}
