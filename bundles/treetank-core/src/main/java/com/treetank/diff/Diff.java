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
import com.treetank.api.IDatabase;
import com.treetank.api.IExpression;
import com.treetank.api.IReadTransaction;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.EFixed;

/**
 * Main diff class.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class Diff implements IExpression {

    /** Determines the current revision. */
    private enum ERevision {
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
    private final Depth mDepth;

    /** First {@link IReadTransaction}. */
    private final IReadTransaction mNewRtx;

    /** Second {@link IReadTransaction}. */
    private final IReadTransaction mOldRtx;

    /**
     * Kind of difference.
     * 
     * @see EDiff
     */
    private transient EDiff mDiff;

    /** Diff implementation of the {@link IDiff} interface. */
    private final IDiff mDiffImpl;

    /**
     * Kind of hash method.
     * 
     * @see HashKind
     */
    private final HashKind mHashKind;

    /** Diff kind. */
    private final EDiffKind mDiffKind;

    /**
     * Constructor.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     * @param paramNewRtx
     *            first {@link IReadTransaction}, on the new revision
     * @param paramOldRtx
     *            second {@link IReadTransaction}, on the old revision
     * @param paramDiffImpl
     *            diff implementation of {@link IDiff}
     * @param paramDiffKind
     *            kind of diff (optimized or not)
     */
    Diff(final IDatabase paramDb, final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
        final EDiffKind paramDiffKind, final IDiff paramDiffImpl) {
        if (paramDb == null || paramNewRtx == null || paramNewRtx == null || paramDiffImpl == null) {
            throw new IllegalArgumentException();
        }

        mHashKind =
            HashKind.valueOf(paramDb.getDatabaseConf().getProps()
                .getProperty(EDatabaseSetting.HASHKIND_TYPE.name()));

        mNewRtx = paramNewRtx;
        mOldRtx = paramOldRtx;
        mDiff = EDiff.SAME;
        mDiffKind = paramDiffKind;
        mDiffImpl = paramDiffImpl;
        mDepth = new Depth();
    }

    /** {@inheritDoc} */
    @Override
    public void evaluate() {
        // Iterate over new revision.
        while (mDiff == EDiff.DELETED || moveCursor(mNewRtx, ERevision.NEW)) {
            if (mDiff != EDiff.INSERTED) {
                moveCursor(mOldRtx, ERevision.OLD);
            }

            if (mHashKind == HashKind.None || mDiffKind == EDiffKind.NORMAL) {
                mDiff = mDiffImpl.diff(mNewRtx, mOldRtx, mDepth);
            } else {
                mDiff = mDiffImpl.optimizedDiff(mNewRtx, mOldRtx);
            }
        }

        // Nodes deleted in old rev (secondRtx) at the end of the tree.
        while (moveCursor(mOldRtx, ERevision.OLD)) {
            if (mHashKind == HashKind.None || mDiffKind == EDiffKind.NORMAL) {
                mDiff = mDiffImpl.diff(mNewRtx, mOldRtx, mDepth);
            } else {
                mDiff = mDiffImpl.optimizedDiff(mNewRtx, mOldRtx);
            }
        }

        mDiffImpl.done();
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
    private boolean moveCursor(final IReadTransaction paramRtx, final ERevision paramRevision) {
        assert paramRtx != null;

        boolean moved = false;
        final AbsStructNode node = (AbsStructNode)paramRtx.getNode();

        if (node.hasFirstChild()) {
            if (node.getKind() != ENodes.ROOT_KIND && mDiffKind == EDiffKind.OPTIMIZED
                && mHashKind != HashKind.None && mDiff == EDiff.SAME) {
                moved = paramRtx.moveToRightSibling();
            } else {
                moved = paramRtx.moveToFirstChild();
                if (moved) {
                    paramRevision.incrementDepth(mDepth);
                }
            }
        } else if (node.hasRightSibling()) {
            moved = paramRtx.moveToRightSibling();
        } else {
            do {
                if (paramRtx.getNode().getNodeKey() == (Long)EFixed.ROOT_NODE_KEY.getStandardProperty()) {
                    moved = false;
                    break;
                }
                moved = paramRtx.moveToParent();
                if (moved) {
                    paramRevision.decrementDepth(mDepth);
                }
            } while (!((AbsStructNode)paramRtx.getNode()).hasRightSibling()
                && ((AbsStructNode)paramRtx.getNode()).hasParent());

            moved = paramRtx.moveToRightSibling();
        }

        return moved;
    }
}
