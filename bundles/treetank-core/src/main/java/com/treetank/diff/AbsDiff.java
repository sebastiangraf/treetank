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

import com.treetank.access.WriteTransaction.HashKind;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.diff.DiffFactory.EDiffKind;
import com.treetank.exception.AbsTTException;
import com.treetank.node.AbsStructNode;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsDiff extends AbsDiffObservable implements IDiff {

    /** Logger. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(AbsDiff.class));

    /** {@link IReadTransaction} on new revision. */
    private transient IReadTransaction mNewRev;

    /** {@link IReadTransaction} on old revision. */
    private transient IReadTransaction mOldRev;

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
    AbsDiff(final IDatabase paramDb, final long paramKey, final long paramNewRev, final long paramOldRev,
        final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers) {
        assert paramDb != null;
        assert paramKey >= 0;
        assert paramNewRev >= 0;
        assert paramOldRev >= 0;
        assert paramNewRev > paramOldRev;
        assert paramObservers != null;
        try {
            HashKind hashKind = null;
            synchronized (paramDb) {
                mNewRev = paramDb.getSession().beginReadTransaction(paramNewRev);
                mOldRev = paramDb.getSession().beginReadTransaction(paramOldRev);
                hashKind =
                    HashKind.valueOf(paramDb.getDatabaseConf().getProps()
                        .getProperty(EDatabaseSetting.HASHKIND_TYPE.name()));
            }
            mNewRev.moveTo(paramKey);
            mOldRev.moveTo(paramKey);
            for (final IDiffObserver observer : paramObservers) {
                addObserver(observer);
            }
            initialize(hashKind, mNewRev, mOldRev, paramDiffKind);
            diff();
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public EDiff optimizedDiff(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
        final Depth paramDepth) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;
        assert paramDepth != null;

        EDiff diff = EDiff.SAMEHASH;

        // Check for modifications.
        switch (paramNewRtx.getNode().getKind()) {
        case ROOT_KIND:
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (paramNewRtx.getNode().getHash() != paramOldRtx.getNode().getHash()) {
                // Check if nodes are the same (even if subtrees may vary).
                if (checkNodes(paramNewRtx, paramOldRtx) == EFoundEqualNode.TRUE) {
                    diff = EDiff.SAME;
                    break;
                }

                // Check if node has been deleted.
                if (paramDepth.getOldDepth() > paramDepth.getNewDepth()) {
                    diff = EDiff.DELETED;
                    diff.setNode(paramOldRtx.getNode());
                    break;
                }

                // Check if node has been renamed.
                if (checkOptimizedRename(paramNewRtx, paramOldRtx) == EDiff.RENAMED) {
                    diff = EDiff.RENAMED;
                    break;
                }

                // See if one of the right sibling matches.
                EFoundEqualNode found = EFoundEqualNode.FALSE;
                int rightSiblings = 0;
                final long key = paramOldRtx.getNode().getNodeKey();
                do {
                    if (paramNewRtx.getNode().getHash() == paramOldRtx.getNode().getHash()) {
                        found = EFoundEqualNode.TRUE;
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

    @Override
    public void done() {
        try {
            mNewRev.close();
            mOldRev.close();
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        fireDiff(EDiff.DONE);
    }

    /**
     * Check for a rename of a node.
     * 
     * @param paramNewRtx
     *            {@link IReadTransaction} instance on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} instance on old revision
     * @return kind of diff
     */
    abstract EDiff
        checkOptimizedRename(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx);

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
    abstract EFoundEqualNode checkNodes(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx);
}
