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

import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.node.AbsStructNode;

/**
 * Structural diff, thus no attributes and namespace nodes are taken into account.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class StructuralDiff implements IDiff {
    /**
     * Constructor.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     * @param paramFirstRtx
     *            first {@link IReadTransaction}, on the new revision
     * @param paramSecondRtx
     *            second {@link IReadTransaction}, on the old revision
     */
    public StructuralDiff(final IDatabase paramDb, final IReadTransaction paramFirstRtx,
        final IReadTransaction paramSecondRtx) {
        // Assertions inside Diff class.
        new Diff(paramDb, paramFirstRtx, paramSecondRtx, this).evaluate();
    }

    /** {@inheritDoc} */
    @Override
    public EDiff diff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx) {
        assert paramFirstRtx != null;
        assert paramSecondRtx != null;

        EDiff mod = EDiff.SAME;

        // Check for modifications.
        switch (paramFirstRtx.getNode().getKind()) {
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (!paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                FoundEqualNode found = FoundEqualNode.FALSE;
                int rightSiblings = 0;

                // See if one of the right sibling matches.
                final long key = paramFirstRtx.getNode().getNodeKey();
                do {
                    if (paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                        found = FoundEqualNode.TRUE;
                    }

                    if (paramFirstRtx.getNode().getNodeKey() != key) {
                        rightSiblings++;
                    }
                } while (((AbsStructNode)paramFirstRtx.getNode()).hasRightSibling()
                    && paramFirstRtx.moveToRightSibling() && found == FoundEqualNode.FALSE);
                paramFirstRtx.moveTo(key);

                mod = found.kindOfDiff(rightSiblings);
            }
            break;
        default:
            // Do nothing.
        }

        return mod;
    }

    /** {@inheritDoc} */
    @Override
    public EDiff optimizedDiff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx) {
        assert paramFirstRtx != null;
        assert paramSecondRtx != null;

        EDiff mod = EDiff.SAME;

        // Check for modifications.
        switch (paramFirstRtx.getNode().getKind()) {
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (paramFirstRtx.getNode().getHash() != paramSecondRtx.getNode().getHash()) {
                FoundEqualNode found = FoundEqualNode.FALSE;
                int rightSiblings = 0;

                // See if one of the right sibling matches.
                final long key = paramFirstRtx.getNode().getNodeKey();
                do {
                    if (paramFirstRtx.getNode().getHash() == paramSecondRtx.getNode().getHash()) {
                        found = FoundEqualNode.TRUE;
                    }

                    if (paramFirstRtx.getNode().getNodeKey() != key) {
                        rightSiblings++;
                    }
                } while (((AbsStructNode)paramFirstRtx.getNode()).hasRightSibling()
                    && paramFirstRtx.moveToRightSibling() && found == FoundEqualNode.FALSE);
                paramFirstRtx.moveTo(key);

                mod = found.kindOfDiff(rightSiblings);
            }
            break;
        default:
            // Do nothing.
        }

        return mod;
    }
}
