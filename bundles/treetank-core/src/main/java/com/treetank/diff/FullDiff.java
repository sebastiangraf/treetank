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
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * Full diff including attributes and namespaces. Note that this class is thread safe.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class FullDiff extends AbsDiffObservable implements IDiff {

    /** Logger. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(FullDiff.class));

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
     *            {@link Set} of Observers, which listen for the kinds of diff between two nodes
     */
    FullDiff(final IDatabase paramDb, final long paramKey, final long paramNewRev, final long paramOldRev,
        final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers) {
        assert paramDb != null;
        assert paramKey > -2;
        assert paramNewRev >= 0;
        assert paramOldRev >= 0;
        assert paramObservers != null;
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
    public EDiff diff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx) {
        assert paramFirstRtx != null;
        assert paramSecondRtx != null;

        EDiff diff = EDiff.SAME;

        // Check for modifications.
        switch (paramFirstRtx.getNode().getKind()) {
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (!paramFirstRtx.getNode().equals(paramSecondRtx.getNode())
                || paramFirstRtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                assert paramSecondRtx.getNode().getKind() == ENodes.ELEMENT_KIND;
                FoundEqualNode found = FoundEqualNode.FALSE;
                int rightSiblings = 0;

                // See if one of the right sibling matches.
                final long key = paramFirstRtx.getNode().getNodeKey();
                do {
                    if (paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                        if (paramFirstRtx.getNode().getKind() == ENodes.TEXT_KIND) {
                            found = FoundEqualNode.TRUE;
                        } else {
                            checkNodes(paramFirstRtx, paramSecondRtx);
                        }
                    }

                    if (paramFirstRtx.getNode().getNodeKey() != key) {
                        rightSiblings++;
                    }
                } while (((AbsStructNode)paramFirstRtx.getNode()).hasRightSibling()
                    && paramFirstRtx.moveToRightSibling() && found == FoundEqualNode.FALSE);
                paramFirstRtx.moveTo(key);

                diff = found.kindOfDiff(rightSiblings);
            }
            break;
        default:
            // Do nothing.
        }

        fireDiff(diff);
        return diff;
    }

    /**
     * Check if nodes are equal.
     * 
     * @param paramFirstRtx
     *            {@link IReadTransaction} on new revision
     * @param paramSecondRtx
     *            {@link IReadTransaction} on old revision
     * 
     * @return if nodes are equal or not
     */
    private FoundEqualNode checkNodes(final IReadTransaction paramFirstRtx,
        final IReadTransaction paramSecondRtx) {
        assert paramFirstRtx.getNode().getKind() == ENodes.ELEMENT_KIND;
        assert paramFirstRtx.getNode().equals(paramSecondRtx.getNode());

        FoundEqualNode found = FoundEqualNode.FALSE;

        if (paramFirstRtx.getNode().getKind() == paramSecondRtx.getNode().getKind()) {
            final long nodeKey = paramFirstRtx.getNode().getNodeKey();
            for (int i = 0; i < ((ElementNode)paramFirstRtx.getNode()).getChildCount(); i++) {
                found = FoundEqualNode.FALSE;
                paramFirstRtx.moveToAttribute(i);
                if (i < ((ElementNode)paramSecondRtx.getNode()).getChildCount()) {
                    paramSecondRtx.moveToAttribute(i);
                } else {
                    break;
                }

                if (paramFirstRtx.getNode() == paramSecondRtx.getNode()) {
                    found = FoundEqualNode.TRUE;
                } else {
                    break;
                }
                paramFirstRtx.moveTo(nodeKey);
                paramSecondRtx.moveTo(nodeKey);
            }

            for (int i = 0; i < ((ElementNode)paramFirstRtx.getNode()).getChildCount(); i++) {
                found = FoundEqualNode.FALSE;
                paramFirstRtx.moveTo(i);
                if (i < ((ElementNode)paramSecondRtx.getNode()).getChildCount()) {
                    paramSecondRtx.moveToNamespace(i);
                } else {
                    break;
                }

                if (paramFirstRtx.getNode() == paramSecondRtx.getNode()) {
                    found = FoundEqualNode.TRUE;
                } else {
                    break;
                }
                paramFirstRtx.moveTo(nodeKey);
                paramSecondRtx.moveTo(nodeKey);
            }
        }

        return found;
    }

    /** {@inheritDoc} */
    @Override
    public EDiff optimizedDiff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void done() {
        fireDiff(EDiff.DONE);
    }
}
