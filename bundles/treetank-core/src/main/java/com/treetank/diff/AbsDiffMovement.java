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

import com.treetank.access.WriteTransaction.HashKind;
import com.treetank.api.IReadTransaction;
import com.treetank.diff.DiffFactory.EDiff;
import com.treetank.diff.DiffFactory.EDiffKind;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;

/**
 * Main diff class.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsDiffMovement implements IDiff {

    /** Determines if a diff should be fired or not. */
    enum EFireDiff {
        /** Yes, it should be fired. */
        TRUE,
        
        /** No, it shouldn't be fired. */
        FALSE
    }
    
    /** Determines the current revision. */
    enum ERevision {
        /** Old revision. */
        OLD {
            /** {@inheritDoc} */
            @Override
            void incrementDepth(final Depth paramDepth) {
                paramDepth.incrementOldDepth();
            }

            /** {@inheritDoc} */
            @Override
            void decrementDepth(final Depth paramDepth) {
                paramDepth.decrementOldDepth();
            }
        },

        /** New revision. */
        NEW {
            /** {@inheritDoc} */
            @Override
            void incrementDepth(final Depth paramDepth) {
                paramDepth.incrementNewDepth();
            }

            /** {@inheritDoc} */
            @Override
            void decrementDepth(final Depth paramDepth) {
                paramDepth.decrementNewDepth();
            }
        };

        /**
         * Increment depth.
         * 
         * @param paramDepth
         *            {@link Depth} instance
         */
        abstract void incrementDepth(final Depth paramDepth);

        /**
         * Decrement depth.
         * 
         * @param paramDepth
         *            {@link Depth} instance
         */
        abstract void decrementDepth(final Depth paramDepth);
    }

    /** {@link Depth} container for depths in both revisions. */
    private transient Depth mDepth;

    /** First {@link IReadTransaction}. */
    private transient IReadTransaction mNewRtx;

    /** Second {@link IReadTransaction}. */
    private transient IReadTransaction mOldRtx;

    /**
     * Kind of difference.
     * 
     * @see EDiff
     */
    private transient EDiff mDiff;

    /** Diff kind. */
    private transient EDiffKind mDiffKind;
    
    /**
     * Kind of hash method.
     * 
     * @see HashKind
     */
    transient HashKind mHashKind;

    /**
     * Initialize.
     * 
     * @param paramHashKind
     *            {@link HashKind} instance
     * @param paramNewRtx
     *            first {@link IReadTransaction}, on the new revision
     * @param paramOldRtx
     *            second {@link IReadTransaction}, on the old revision
     * @param paramDiffKind
     *            kind of diff (optimized or not)
     */
    void initialize(final HashKind paramHashKind, final IReadTransaction paramNewRtx,
        final IReadTransaction paramOldRtx, final EDiffKind paramDiffKind) {
        if (paramNewRtx == null || paramNewRtx == null) {
            throw new IllegalArgumentException();
        }

        mHashKind = paramHashKind;
        mNewRtx = paramNewRtx;
        mOldRtx = paramOldRtx;
        mDiff = EDiff.SAME;
        mDiffKind = paramDiffKind;
        mDepth = new Depth();
    }

    /** Do the diff. */
    void diff() {
        assert mHashKind != null;
        assert mNewRtx != null;
        assert mOldRtx != null;
        assert mDiff != null;
        assert mDiffKind != null;
        assert mDepth != null;

        // Check first nodes.
        if (mNewRtx.getNode().getKind() != ENodes.ROOT_KIND) {
            if (mHashKind == HashKind.None || mDiffKind == EDiffKind.NORMAL) {
                mDiff = diff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
            } else {
                mDiff = optimizedDiff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
            }
        }

        // Iterate over new revision.
        while (mDiff == EDiff.DELETED || moveCursor(mNewRtx, ERevision.NEW)) {
            if (mDiff != EDiff.INSERTED) {
                moveCursor(mOldRtx, ERevision.OLD);
            }

            if (mHashKind == HashKind.None || mDiffKind == EDiffKind.NORMAL) {
                mDiff = diff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
            } else {
                mDiff = optimizedDiff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
            }
        }

        // Nodes deleted in old rev at the end of the tree.
        if (mOldRtx.getNode().getKind() != ENodes.ROOT_KIND) {
            while (moveCursor(mOldRtx, ERevision.OLD)) {
                if (mHashKind == HashKind.None || mDiffKind == EDiffKind.NORMAL) {
                    mDiff = diff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
                } else {
                    mDiff = optimizedDiff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
                }
            }
        }

        done();
    }

    /**
     * Move cursor forward.
     * 
     * @param paramRtx
     *            the {@link IReadTransaction} to use
     * @param paramRevision
     *            the {@link ERevision} constant
     * @return true, if cursor moved, false otherwise
     */
    boolean moveCursor(final IReadTransaction paramRtx, final ERevision paramRevision) {
        assert paramRtx != null;

        boolean moved = false;
        final AbsStructNode node = (AbsStructNode)paramRtx.getNode();

        if (node.hasFirstChild()) {
            if (node.getKind() != ENodes.ROOT_KIND && mDiffKind == EDiffKind.OPTIMIZED
                && mHashKind != HashKind.None && (mDiff == EDiff.SAMEHASH || mDiff == EDiff.DELETED)) {
                moved = paramRtx.moveToRightSibling();

                if (!moved) {
                    moved = moveToNextNode(paramRtx, paramRevision);
                }
            } else {
                moved = paramRtx.moveToFirstChild();
                if (moved) {
                    paramRevision.incrementDepth(mDepth);
                }
            }
        } else if (node.hasRightSibling()) {
            moved = paramRtx.moveToRightSibling();
        } else {
            moved = moveToNextNode(paramRtx, paramRevision);
        }

        return moved;
    }

    /**
     * Move to next sibling node.
     * 
     * @param paramRtx
     *            the {@link IReadTransaction} to use
     * @param paramRevision
     *            the {@link ERevision} constant
     * @return true, if cursor moved, false otherwise
     */
    private boolean moveToNextNode(final IReadTransaction paramRtx, final ERevision paramRevision) {
        boolean moved = false;
        do {
            moved = paramRtx.moveToParent();
            if (moved) {
                paramRevision.decrementDepth(mDepth);
            }
        } while (!((AbsStructNode)paramRtx.getNode()).hasRightSibling()
            && ((AbsStructNode)paramRtx.getNode()).hasParent());

        moved = paramRtx.moveToRightSibling();
        return moved;
    }
}
