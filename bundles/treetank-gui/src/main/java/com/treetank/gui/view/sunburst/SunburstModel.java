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

import org.slf4j.LoggerFactory;

import processing.core.PApplet;
import processing.core.PConstants;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.SunburstItem.StructKind;
import com.treetank.gui.view.tree.TreeModel;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.LogWrapper;

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
     * @param paramKey
     *            Node key to start from.
     * @return {@link List} of {@link SunburstItem}s.
     */
    List<SunburstItem> traverseTree(final long paramKey) {
        LOGWRAPPER.debug("Build sunburst items.");
        assert mRtx != null;
        final long nodeKey = mRtx.getNode().getNodeKey();
        mRtx.moveTo(paramKey);

        // Initialize variables.
        float angleOffset = 0f;
        float oldAngle = 0f;
        float angle = 0f;
        float tmpAngle = 0f;
        int depth = 0;
        int indexToParent = -1;
        final NodeRelations relations = new NodeRelations();

        // Iterate over descendant axis.
        for (final IAxis axis = new DescendantAxis(mRtx, true); axis.hasNext(); axis.next()) {
            if (mRtx.getNode().getKind() == ENodes.ROOT_KIND) {
                continue;
            }
            
            // If there is an angle change (= entering a new child node) reset angleOffset
            if (oldAngle != angle) {
                angleOffset = 0f;
            }

            // Compute min and max child count of the children of the current node.
            long minChildCount = Long.MAX_VALUE;
            long maxChildCount = Long.MIN_VALUE;
            final long key = mRtx.getNode().getNodeKey();
            if (((AbsStructNode)mRtx.getNode()).hasFirstChild()) {
                mRtx.moveToFirstChild();
                do {
                    final AbsStructNode node = (AbsStructNode)mRtx.getNode();
                    minChildCount = Math.min(node.getChildCount(), minChildCount);
                    maxChildCount = Math.max(node.getChildCount(), maxChildCount);
                } while (((AbsStructNode)mRtx.getNode()).hasRightSibling() && mRtx.moveToRightSibling());

                mRtx.moveTo(key);
            }

            // Add a sunburst item.
            final AbsStructNode node = (AbsStructNode)mRtx.getNode();
            final StructKind structKind = node.hasFirstChild() ? StructKind.ISINNERNODE : StructKind.ISLEAF;
            final long childCount = node.getChildCount();
            final float anglePerChild = PConstants.TWO_PI / childCount;
            final float extension = ((AbsStructNode)mRtx.getNode()).getChildCount() * anglePerChild;
            relations.setAll(depth, structKind, childCount, minChildCount, maxChildCount, indexToParent);
            mItems.add(new SunburstItem.Builder(mParent, mController, node, (angle + angleOffset)
                % PConstants.TWO_PI, extension, relations).build());

            // Increment angle offset.
            angleOffset += extension;
            oldAngle = angle;

            // Determines if angle needs to be adjusted.
            if (node.hasFirstChild()) {
                depth++;
                angle += angleOffset;
                tmpAngle = angle;
                indexToParent++;
            } else if (!node.hasRightSibling()) {
                depth--;
                angle = tmpAngle;
                oldAngle = angle;
                indexToParent--;
            }
        }

        mRtx.moveTo(nodeKey);
        return mItems;
    }
}
