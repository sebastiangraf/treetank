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
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.EFixed;

/**
 * Main diff class.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class Diff implements IExpression {

    /** First {@link IReadTransaction}. */
    private final IReadTransaction mFirstRtx;

    /** Second {@link IReadTransaction}. */
    private final IReadTransaction mSecondRtx;

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
     * @param paramFirstRtx
     *            first {@link IReadTransaction}, on the new revision
     * @param paramSecondRtx
     *            second {@link IReadTransaction}, on the old revision
     * @param paramDiffImpl
     *            diff implementation of {@link IDiff}
     * @param paramDiffKind
     *            kind of diff (optimized or not)
     */
    Diff(final IDatabase paramDb, final IReadTransaction paramFirstRtx,
        final IReadTransaction paramSecondRtx, final EDiffKind paramDiffKind, final IDiff paramDiffImpl) {
        if (paramDb == null || paramFirstRtx == null || paramFirstRtx == null || paramDiffImpl == null) {
            throw new IllegalArgumentException();
        }

        mHashKind =
            HashKind.valueOf(paramDb.getDatabaseConf().getProps()
                .getProperty(EDatabaseSetting.HASHKIND_TYPE.name()));

        mFirstRtx = paramFirstRtx;
        mSecondRtx = paramSecondRtx;
        mDiff = EDiff.SAME;
        mDiffKind = paramDiffKind;
        mDiffImpl = paramDiffImpl;
    }

    /** {@inheritDoc} */
    @Override
    public void evaluate() {
        // Iterate over new revision.
        while (moveCursor(mFirstRtx)) {
            if (mDiff == EDiff.SAME || mDiff == EDiff.RENAMED) {
                moveCursor(mSecondRtx);
            }

            if (mHashKind == HashKind.None || mDiffKind == EDiffKind.NORMAL) {
                mDiff = mDiffImpl.diff(mFirstRtx, mSecondRtx);
            } else {
                mDiff = mDiffImpl.optimizedDiff(mFirstRtx, mSecondRtx);
            }
        }

        // Nodes deleted in old rev (secondRtx) at the end of the tree.
        while (moveCursor(mSecondRtx)) {
            if (mHashKind == HashKind.None || mDiffKind == EDiffKind.NORMAL) {
                mDiff = mDiffImpl.diff(mFirstRtx, mSecondRtx);
            } else {
                mDiff = mDiffImpl.optimizedDiff(mFirstRtx, mSecondRtx);
            }
        }

        mDiffImpl.done();
    }

    /**
     * Move cursor forward.
     * 
     * @param paramRtx
     *            the {@link IReadTransaction} to use
     * @return true, if cursor moved, false otherwise
     */
    private boolean moveCursor(final IReadTransaction paramRtx) {
        assert paramRtx != null;

        boolean moved = false;
        final AbsStructNode node = (AbsStructNode)paramRtx.getNode();

        if (node.hasFirstChild()) {
            if (mDiffKind == EDiffKind.OPTIMIZED && mHashKind != HashKind.None && mDiff == EDiff.SAME) {
                moved = paramRtx.moveToRightSibling();
            } else {
                moved = paramRtx.moveToFirstChild();
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
            } while (!((AbsStructNode)paramRtx.getNode()).hasRightSibling()
                && ((AbsStructNode)paramRtx.getNode()).hasParent());
            
            moved = paramRtx.moveToRightSibling(); 
        }

        return moved;
    }
}
