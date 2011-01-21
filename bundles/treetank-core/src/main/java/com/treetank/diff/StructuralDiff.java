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
import com.treetank.exception.TTException;
import com.treetank.node.AbsStructNode;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * Structural diff, thus no attributes and namespace nodes are taken into account. Note that this class is
 * thread safe.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class StructuralDiff extends AbsDiffObservable implements IDiff {

    /** Logger. */
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(StructuralDiff.class));

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
     */
    public StructuralDiff(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers) {
        assert paramDb != null;
        assert paramKey > -2;
        assert paramNewRev >= 0;
        assert paramOldRev >= 0;
        try {
            final IReadTransaction newRev = paramDb.getSession().beginReadTransaction(paramNewRev);
            final IReadTransaction oldRev = paramDb.getSession().beginReadTransaction(paramOldRev);
            newRev.moveTo(paramKey);
            oldRev.moveTo(paramKey);
            for (final IDiffObserver observer : paramObservers) {
                addObserver(observer);
            }
            new Diff(paramDb, newRev, oldRev, paramDiffKind, this).evaluate();
        } catch (final TTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public EDiff diff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx,
        final Depth paramDepth) {
        assert paramFirstRtx != null;
        assert paramSecondRtx != null;

        EDiff diff = EDiff.SAME;

        // Check for modifications.
        switch (paramFirstRtx.getNode().getKind()) {
        case ROOT_KIND:
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (!paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                // Check if node has been deleted.
                if (paramDepth.getOldDepth() > paramDepth.getNewDepth()) {
                    diff = EDiff.DELETED;
                    break;
                }

                // Check if node has been renamed.
                if (checkRename(paramFirstRtx, paramSecondRtx) == EDiff.RENAMED) {
                    diff = EDiff.RENAMED;
                    break;
                }

                // See if current node or one of the right siblings matches.
                EFoundEqualNode found = EFoundEqualNode.FALSE;
                int rightSiblings = 0;
                final long key = paramSecondRtx.getNode().getNodeKey();
                do {
                    if (paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                        found = EFoundEqualNode.TRUE;
                    }

                    if (paramSecondRtx.getNode().getNodeKey() != key) {
                        rightSiblings++;
                    }
                } while (((AbsStructNode)paramSecondRtx.getNode()).hasRightSibling()
                    && paramSecondRtx.moveToRightSibling() && found == EFoundEqualNode.FALSE);
                paramSecondRtx.moveTo(key);
                diff = found.kindOfDiff(rightSiblings);
            }

            break;
        default:
            // Do nothing.
        }

        fireDiff(diff);
        return diff;
    }

    /** {@inheritDoc} */
    @Override
    public EDiff optimizedDiff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx,
        final Depth paramDepth) {
        assert paramFirstRtx != null;
        assert paramSecondRtx != null;

        EDiff diff = EDiff.SAME;

        // Check for modifications.
        switch (paramFirstRtx.getNode().getKind()) {
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (paramFirstRtx.getNode().getHash() != paramSecondRtx.getNode().getHash()) {
                // Check if node has been deleted.
                if (paramDepth.getOldDepth() > paramDepth.getNewDepth()) {
                    diff = EDiff.DELETED;
                    break;
                }

                // Check if node has been renamed.
                if (checkRenameOptimized(paramFirstRtx, paramSecondRtx) == EDiff.RENAMED) {
                    diff = EDiff.RENAMED;
                    break;
                }

                // See if one of the right sibling matches.

                EFoundEqualNode found = EFoundEqualNode.FALSE;
                int rightSiblings = 0;
                final long key = paramSecondRtx.getNode().getNodeKey();
                do {
                    if (paramFirstRtx.getNode().getHash() == paramSecondRtx.getNode().getHash()) {
                        found = EFoundEqualNode.TRUE;
                    }

                    if (paramSecondRtx.getNode().getNodeKey() != key) {
                        rightSiblings++;
                    }
                } while (((AbsStructNode)paramSecondRtx.getNode()).hasRightSibling()
                    && paramSecondRtx.moveToRightSibling() && found == EFoundEqualNode.FALSE);
                paramSecondRtx.moveTo(key);

                diff = found.kindOfDiff(rightSiblings);
            }
            break;
        default:
            // Do nothing.
        }

        fireDiff(diff);
        return diff;
    }

    @Override
    public void done() {
        fireDiff(EDiff.DONE);
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
    private EDiff checkRename(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx) {
        EDiff diff = EDiff.SAME;
        final long firstKey = paramFirstRtx.getNode().getNodeKey();
        boolean movedFirstRtx = paramFirstRtx.moveToRightSibling();
        final long secondKey = paramSecondRtx.getNode().getNodeKey();
        boolean movedSecondRtx = paramSecondRtx.moveToRightSibling();
        if (movedFirstRtx && movedSecondRtx && paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
            diff = EFoundEqualNode.TRUE.kindOfDiff(-1);
        } else if (!movedFirstRtx && !movedSecondRtx) {
            movedFirstRtx = paramFirstRtx.moveToParent();
            movedSecondRtx = paramSecondRtx.moveToParent();

            if (movedFirstRtx && movedSecondRtx && paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                diff = EFoundEqualNode.TRUE.kindOfDiff(-1);
            }
        }
        paramFirstRtx.moveTo(firstKey);
        paramSecondRtx.moveTo(secondKey);
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
    private EDiff checkRenameOptimized(final IReadTransaction paramFirstRtx,
        final IReadTransaction paramSecondRtx) {
        EDiff diff = EDiff.SAME;
        final long firstKey = paramFirstRtx.getNode().getNodeKey();
        boolean movedFirstRtx = paramFirstRtx.moveToRightSibling();
        final long secondKey = paramSecondRtx.getNode().getNodeKey();
        boolean movedSecondRtx = paramSecondRtx.moveToRightSibling();
        if (movedFirstRtx && movedSecondRtx
            && paramFirstRtx.getNode().getHash() == paramSecondRtx.getNode().getHash()) {
            diff = EFoundEqualNode.TRUE.kindOfDiff(-1);
        } else if (!movedFirstRtx && !movedSecondRtx) {
            movedFirstRtx = paramFirstRtx.moveToParent();
            movedSecondRtx = paramSecondRtx.moveToParent();

            if (movedFirstRtx && movedSecondRtx
                && paramFirstRtx.getNode().getHash() == paramSecondRtx.getNode().getHash()) {
                diff = EFoundEqualNode.TRUE.kindOfDiff(-1);
            }
        }
        paramFirstRtx.moveTo(firstKey);
        paramSecondRtx.moveTo(secondKey);
        return diff;
    }
}
