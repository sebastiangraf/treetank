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
package org.treetank.diff;

import java.util.Set;


import org.slf4j.LoggerFactory;
import org.treetank.access.WriteTransaction.HashKind;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.diff.DiffFactory.EDiffKind;
import org.treetank.exception.AbsTTException;
import org.treetank.node.AbsStructNode;
import org.treetank.settings.EDatabaseSetting;
import org.treetank.utils.LogWrapper;

/**
 * Abstract diff class which implements common functionality.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsDiff extends AbsDiffObservable {

    /** Logger. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(AbsDiff.class));

    /** Kind of diff. */
    transient EDiffKind mDiffKind;

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
        assert paramDiffKind != null;
        assert paramObservers != null;
        try {
            mDiffKind = paramDiffKind;
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

            synchronized (paramObservers) {
                for (final IDiffObserver observer : paramObservers) {
                    addObserver(observer);
                }
            }
            initialize(hashKind, mNewRev, mOldRev, paramDiffKind);
            diff();
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public EDiff diff(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
        final DepthCounter paramDepth, final EFireDiff paramFireDiff) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;
        assert paramDepth != null;

        EDiff diff = EDiff.SAME;

        // Check for modifications.
        switch (paramNewRtx.getNode().getKind()) {
        case ROOT_KIND:
        case TEXT_KIND:
        case ELEMENT_KIND:
            if (!checkNodes(paramNewRtx, paramOldRtx)) {
                diff = algorithm(paramNewRtx, paramOldRtx, paramDepth);
            }
            break;
        default:
            // Do nothing.
        }

        if (paramFireDiff == EFireDiff.TRUE) {
            fireDiff(diff, paramNewRtx.getNode(), paramOldRtx.getNode(),
                new DiffDepth(paramDepth.getNewDepth(), paramDepth.getOldDepth()));
        }
        return diff;
    }

    /** {@inheritDoc} */
    @Override
    public EDiff optimizedDiff(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
        final DepthCounter paramDepth, final EFireDiff paramFireDiff) {
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
                if (checkNodes(paramNewRtx, paramOldRtx)) {
                    diff = EDiff.SAME;
                } else {
                    diff = algorithm(paramNewRtx, paramOldRtx, paramDepth);
                }
            }
            break;
        default:
            // Do nothing.
        }

        if (paramFireDiff == EFireDiff.TRUE) {
            if (diff == EDiff.SAMEHASH) {
                fireDiff(EDiff.SAME, paramNewRtx.getNode(), paramOldRtx.getNode(),
                    new DiffDepth(paramDepth.getNewDepth(), paramDepth.getOldDepth()));
            } else {
                fireDiff(diff, paramNewRtx.getNode(), paramOldRtx.getNode(),
                    new DiffDepth(paramDepth.getNewDepth(), paramDepth.getOldDepth()));
            }
        }
        return diff;
    }

    /**
     * Main algorithm to compute diffs between two nodes.
     * 
     * @param paramNewRtx
     *            {@link IReadTransaction} on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} on old revision
     * @param paramDepth
     *            {@link DepthCounter} container for current depths of both transaction cursors
     * @return kind of diff
     */
    private EDiff algorithm(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
        final DepthCounter paramDepth) {
        EDiff diff = null;

        // Check if node has been deleted.
        if (paramDepth.getOldDepth() > paramDepth.getNewDepth()) {
            diff = EDiff.DELETED;
        } else if (checkUpdate(paramNewRtx, paramOldRtx)) { // Check if node has been renamed.
            diff = EDiff.UPDATED;
        } else {
            // See if one of the right sibling matches.
            EFoundEqualNode found = EFoundEqualNode.FALSE;
            final long key = paramOldRtx.getNode().getNodeKey();

            while (((AbsStructNode)paramOldRtx.getNode()).hasRightSibling()
                && paramOldRtx.moveToRightSibling() && found == EFoundEqualNode.FALSE) {
                if (checkNodes(paramNewRtx, paramOldRtx)) {
                    found = EFoundEqualNode.TRUE;
                }
            }

            paramOldRtx.moveTo(key);
            diff = found.kindOfDiff();
        }

        assert diff != null;
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
        fireDiff(EDiff.DONE, null, null, null);
    }

    /**
     * Check {@link QName} of nodes.
     * 
     * @param paramNewRtx
     *            {@link IReadTransaction} on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} on old revision
     * @return true if nodes are "equal" according to their {@link QName}s, otherwise false
     */
    boolean checkName(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx) {
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

    /**
     * Check if nodes are equal excluding subtrees.
     * 
     * @param paramNewRtx
     *            {@link IReadTransaction} on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} on old revision
     * @return true if nodes are "equal", otherwise false
     */
    abstract boolean checkNodes(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx);

    /**
     * Check for a rename of a node.
     * 
     * @param paramNewRtx
     *            first {@link IReadTransaction} instance
     * @param paramOldRtx
     *            second {@link IReadTransaction} instance
     * @return kind of diff
     */
    boolean checkUpdate(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;
        boolean updated = false;
        final long newKey = paramNewRtx.getNode().getNodeKey();
        boolean movedNewRtx = paramNewRtx.moveToRightSibling();
        final long oldKey = paramOldRtx.getNode().getNodeKey();
        boolean movedOldRtx = paramOldRtx.moveToRightSibling();
        if (movedNewRtx && movedOldRtx && checkNodes(paramNewRtx, paramOldRtx)) {
            updated = true;
        } else if (!movedNewRtx && !movedOldRtx) {
            movedNewRtx = paramNewRtx.moveToParent();
            movedOldRtx = paramOldRtx.moveToParent();

            if (movedNewRtx && movedOldRtx && checkNodes(paramNewRtx, paramOldRtx)) {
                updated = true;
            }
        }
        paramNewRtx.moveTo(newKey);
        paramOldRtx.moveTo(oldKey);
        if (!updated) {
            updated = paramNewRtx.getNode().getNodeKey() == paramOldRtx.getNode().getNodeKey();
        }
        return updated;
    }
}
