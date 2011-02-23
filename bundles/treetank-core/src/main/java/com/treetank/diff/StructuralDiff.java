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
package com.treetank.diff;

import java.util.Set;

import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.diff.DiffFactory.EDiff;
import com.treetank.diff.DiffFactory.EDiffKind;
import com.treetank.exception.AbsTTException;
import com.treetank.node.AbsStructNode;

/**
 * Structural diff, thus no attributes and namespace nodes are taken into account. Note that this class is
 * thread safe.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class StructuralDiff extends AbsDiff {

    /**
     * Constructor.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     * @param paramKey
     *            key of (sub)tree to check
     * @param paramNewRev
     *            new revision key
     * @param paramOldRev
     *            old revision key
     * @param paramDiffKind
     *            kind of diff (optimized or not)
     * @param paramObservers
     *            {@link Set} of observes
     * @throws AbsTTException
     *             if retrieving the session fails
     */
    public StructuralDiff(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers)
        throws AbsTTException {
        super(paramDb, paramKey, paramNewRev, paramOldRev, paramDiffKind, paramObservers);
    }

    /** {@inheritDoc} */
    @Override
    public EDiff diff(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
        final Depth paramDepth) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;

        EDiff diff = EDiff.SAME;

        // Check for modifications.
        switch (paramNewRtx.getNode().getKind()) {
        case ROOT_KIND:
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (!paramNewRtx.getNode().equals(paramOldRtx.getNode())) {
                // Check if node has been deleted.
                if (paramDepth.getOldDepth() > paramDepth.getNewDepth()) {
                    diff = EDiff.DELETED;
                    break;
                }

                // Check if node has been renamed.
                if (checkRename(paramNewRtx, paramOldRtx)) {
                    diff = EDiff.RENAMED;
                    break;
                }

                // See if current node or one of the right siblings matches.
                EFoundEqualNode found = EFoundEqualNode.FALSE;
                int rightSiblings = 0;
                final long key = paramOldRtx.getNode().getNodeKey();
                do {
                    if (paramNewRtx.getNode().equals(paramOldRtx.getNode())) {
                        found = EFoundEqualNode.TRUE;
                    }

                    if (paramOldRtx.getNode().getNodeKey() != key) {
                        rightSiblings++;
                    }
                } while (((AbsStructNode)paramOldRtx.getNode()).hasRightSibling()
                    && paramOldRtx.moveToRightSibling() && found == EFoundEqualNode.FALSE);
                paramOldRtx.moveTo(key);
                diff = found.kindOfDiff(rightSiblings);
            }

            break;
        default:
            // Do nothing.
        }

        fireDiff(diff, paramNewRtx.getNode(), paramOldRtx.getNode());
        return diff;
    }

    /**
     * Check for a rename of a node.
     * 
     * @param paramFirstRtx
     *            first {@link IReadTransaction} instance
     * @param paramSecondRtx
     *            second {@link IReadTransaction} instance
     * @return kind of diff
     */
    @Override
    boolean checkRename(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx) {
        boolean renamed = false;
        final long firstKey = paramFirstRtx.getNode().getNodeKey();
        boolean movedFirstRtx = paramFirstRtx.moveToRightSibling();
        final long secondKey = paramSecondRtx.getNode().getNodeKey();
        boolean movedSecondRtx = paramSecondRtx.moveToRightSibling();
        if (movedFirstRtx && movedSecondRtx && paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
            renamed = true;
        } else if (!movedFirstRtx && !movedSecondRtx) {
            movedFirstRtx = paramFirstRtx.moveToParent();
            movedSecondRtx = paramSecondRtx.moveToParent();

            if (movedFirstRtx && movedSecondRtx && paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                renamed = true;
            }
        }
        paramFirstRtx.moveTo(firstKey);
        paramSecondRtx.moveTo(secondKey);
        return renamed;
    }

    /** {@inheritDoc} */
    @Override
    EFoundEqualNode checkNodes(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx) {
        EFoundEqualNode found = EFoundEqualNode.FALSE;
        if (paramNewRtx.getNode().equals(paramOldRtx.getNode())) {
            found = EFoundEqualNode.TRUE;
        }
        return found;
    }
}
