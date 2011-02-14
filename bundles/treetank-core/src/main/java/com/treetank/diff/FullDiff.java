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
final class FullDiff extends AbsDiff implements IDiff {

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
            if (!paramNewRtx.getNode().equals(paramOldRtx.getNode())
                && checkNodes(paramNewRtx, paramOldRtx) == EFoundEqualNode.FALSE) {
                // Check if node has been deleted.
                if (paramDepth.getOldDepth() > paramDepth.getNewDepth()) {
                    diff = EDiff.DELETED;
                    diff.setNode(paramOldRtx.getNode());
                    break;
                }

                // Check if node has been renamed.
                if (checkRename(paramNewRtx, paramOldRtx) == EDiff.RENAMED) {
                    diff = EDiff.RENAMED;
                    break;
                }

                // See if one of the right sibling matches.
                EFoundEqualNode found = EFoundEqualNode.FALSE;
                int rightSiblings = 0;
                final long key = paramOldRtx.getNode().getNodeKey();
                do {
                    if (paramNewRtx.getNode().equals(paramOldRtx.getNode())) {
                        // if (paramOldRtx.getNode().getKind() == ENodes.TEXT_KIND) {
                        // found = EFoundEqualNode.TRUE;
                        // } else {
                        // found = checkNodes(paramNewRtx, paramOldRtx);
                        // }
                        assert paramOldRtx.getNode().getKind() != ENodes.TEXT_KIND;
                        found = checkNodes(paramNewRtx, paramOldRtx);
                    }

                    if (paramOldRtx.getNode().getNodeKey() != key) {
                        rightSiblings++;
                    }
                } while (((AbsStructNode)paramOldRtx.getNode()).hasRightSibling()
                    && paramOldRtx.moveToRightSibling() && found == EFoundEqualNode.FALSE);
                paramOldRtx.moveTo(key);

                diff = found.kindOfDiff(rightSiblings);
                if (diff == EDiff.DELETED) {
                    diff.setNode(paramOldRtx.getNode());
                }
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
    private EFoundEqualNode checkNodes(final IReadTransaction paramFirstRtx,
        final IReadTransaction paramSecondRtx) {
        assert paramFirstRtx != null;
        assert paramSecondRtx != null;

        EFoundEqualNode found = EFoundEqualNode.FALSE;

        if (paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
            final long nodeKey = paramFirstRtx.getNode().getNodeKey();

            if (paramFirstRtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                if (((ElementNode)paramFirstRtx.getNode()).getNamespaceCount() == 0
                    && ((ElementNode)paramFirstRtx.getNode()).getAttributeCount() == 0
                    && ((ElementNode)paramSecondRtx.getNode()).getAttributeCount() == 0
                    && ((ElementNode)paramSecondRtx.getNode()).getNamespaceCount() == 0) {
                    found = EFoundEqualNode.TRUE;
                } else {
                    if (((ElementNode)paramFirstRtx.getNode()).getNamespaceCount() == 0) {
                        found = EFoundEqualNode.TRUE;
                    } else {
                        for (int i = 0; i < ((ElementNode)paramFirstRtx.getNode()).getNamespaceCount(); i++) {
                            paramFirstRtx.moveToNamespace(i);
                            for (int j = 0; j < ((ElementNode)paramSecondRtx.getNode()).getNamespaceCount(); j++) {
                                paramSecondRtx.moveToNamespace(i);

                                if (paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                                    found = EFoundEqualNode.TRUE;
                                    break;
                                }
                            }
                            paramFirstRtx.moveTo(nodeKey);
                            paramSecondRtx.moveTo(nodeKey);
                        }
                    }

                    if (found == EFoundEqualNode.TRUE) {
                        for (int i = 0; i < ((ElementNode)paramFirstRtx.getNode()).getAttributeCount(); i++) {
                            paramFirstRtx.moveToAttribute(i);
                            for (int j = 0; j < ((ElementNode)paramSecondRtx.getNode()).getAttributeCount(); j++) {
                                paramSecondRtx.moveToAttribute(i);

                                if (paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                                    found = EFoundEqualNode.TRUE;
                                    break;
                                }
                            }
                            paramFirstRtx.moveTo(nodeKey);
                            paramSecondRtx.moveTo(nodeKey);
                        }
                    }
                }
            } else {
                found = EFoundEqualNode.TRUE;
            }
        }

        return found;
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
        if (movedFirstRtx && movedSecondRtx && paramFirstRtx.getNode().equals(paramSecondRtx.getNode())
            && checkNodes(paramFirstRtx, paramSecondRtx) == EFoundEqualNode.TRUE) {
            diff = EFoundEqualNode.TRUE.kindOfDiff(-1);
        } else if (!movedFirstRtx && !movedSecondRtx) {
            movedFirstRtx = paramFirstRtx.moveToParent();
            movedSecondRtx = paramSecondRtx.moveToParent();

            if (movedFirstRtx && movedSecondRtx && paramFirstRtx.getNode().equals(paramSecondRtx.getNode())
                && checkNodes(paramFirstRtx, paramSecondRtx) == EFoundEqualNode.TRUE) {
                diff = EFoundEqualNode.TRUE.kindOfDiff(-1);
            }
        }
        paramFirstRtx.moveTo(firstKey);
        paramSecondRtx.moveTo(secondKey);
        return diff;
    }

    /** {@inheritDoc} */
    @Override
    public EDiff optimizedDiff(final IReadTransaction paramFirstRtx, final IReadTransaction paramSecondRtx,
        final Depth paramDepth) {
        // TODO Auto-generated method stub
        return null;
    }
}
