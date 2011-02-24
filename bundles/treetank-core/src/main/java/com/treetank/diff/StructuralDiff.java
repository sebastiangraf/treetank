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
import com.treetank.diff.DiffFactory.EDiffKind;
import com.treetank.exception.AbsTTException;
import com.treetank.node.ENodes;

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

    /**
     * Check for a rename of a node.
     * 
     * @param paramNewRtx
     *            first {@link IReadTransaction} instance
     * @param paramOldRtx
     *            second {@link IReadTransaction} instance
     * @return kind of diff
     */
    @Override
    boolean checkRename(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx) {
        boolean renamed = false;
        final long newKey = paramNewRtx.getNode().getNodeKey();
        boolean movedNewRtx = paramNewRtx.moveToRightSibling();
        final long oldKey = paramOldRtx.getNode().getNodeKey();
        boolean movedOldRtx = paramOldRtx.moveToRightSibling();
        if (movedNewRtx && movedOldRtx && checkNodes(paramNewRtx, paramOldRtx)) {
            renamed = true;
        } else if (!movedNewRtx && !movedOldRtx) {
            movedNewRtx = paramNewRtx.moveToParent();
            movedOldRtx = paramOldRtx.moveToParent();

            if (movedNewRtx && movedOldRtx && paramNewRtx.getNode().equals(paramOldRtx.getNode())) {
                renamed = true;
            }
        }
        paramNewRtx.moveTo(newKey);
        paramOldRtx.moveTo(oldKey);
        return renamed;
    }

    /** {@inheritDoc} */
    @Override
    boolean checkNodes(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx) {
        boolean found = false;
        if (paramNewRtx.getNode().getKind() == paramOldRtx.getNode().getKind()) {
            switch (paramNewRtx.getNode().getKind()) {
            case ELEMENT_KIND:
                if (paramNewRtx.getQNameOfCurrentNode().equals(paramOldRtx.getQNameOfCurrentNode())) {
                    found = true;
                }
                break;
            case TEXT_KIND:
                if (paramNewRtx.getValueOfCurrentNode().equals(paramOldRtx.getValueOfCurrentNode())) {
                    found = true;
                }
                break;
            default:
            }
        }
        return found;
    }
}
