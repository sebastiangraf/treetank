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

import org.treetank.access.WriteTransaction.HashKind;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.diff.DiffFactory.EDiffKind;
import org.treetank.exception.AbsTTException;
import org.treetank.node.AbsStructNode;
import org.treetank.node.ENodes;
import org.treetank.settings.EDatabaseSetting;
import org.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * Abstract diff class which implements common functionality.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsDiff extends AbsDiffObservable {

    /** Logger. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(AbsDiff.class));

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
        OLD,

        /** New revision. */
        NEW;
    }

    /**
     * Kind of difference.
     * 
     * @see EDiff
     */
    private transient EDiff mDiff;

    /** Diff kind. */
    private transient EDiffKind mDiffKind;

    /** {@link DepthCounter} instance. */
    private transient DepthCounter mDepth;

    /**
     * Kind of hash method.
     * 
     * @see HashKind
     */
    transient HashKind mHashKind;

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
            synchronized (paramDb) {
                mNewRtx = paramDb.getSession().beginReadTransaction(paramNewRev);
                mOldRtx = paramDb.getSession().beginReadTransaction(paramOldRev);
                mHashKind =
                    HashKind.valueOf(paramDb.getDatabaseConf().getProps()
                        .getProperty(EDatabaseSetting.HASHKIND_TYPE.name()));
            }
            mNewRtx.moveTo(paramKey);
            mOldRtx.moveTo(paramKey);

            synchronized (paramObservers) {
                for (final IDiffObserver observer : paramObservers) {
                    addObserver(observer);
                }
            }
            mDiff = EDiff.SAME;
            mDiffKind = paramDiffKind;
            mDepth = new DepthCounter();
            diffMovement();
        } catch (final AbsTTException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /** Do the diff. */
    void diffMovement() {
        assert mHashKind != null;
        assert mNewRtx != null;
        assert mOldRtx != null;
        assert mDiff != null;
        assert mDiffKind != null;

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
        final AbsStructNode node = paramRtx.getNode();

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
                    switch (paramRevision) {
                    case NEW:
                        mDepth.incrementNewDepth();
                        break;
                    case OLD:
                        mDepth.incrementOldDepth();
                        break;
                    }
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
     * Move to next "anchestor right sibling" node.
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
                switch (paramRevision) {
                case NEW:
                    mDepth.decrementNewDepth();
                    break;
                case OLD:
                    mDepth.decrementOldDepth();
                    break;
                }
            }
        } while (!((AbsStructNode)paramRtx.getNode()).hasRightSibling()
            && ((AbsStructNode)paramRtx.getNode()).hasParent());

        moved = paramRtx.moveToRightSibling();
        return moved;
    }

    /**
     * Diff of nodes.
     * 
     * @param paramNewRtx
     *            {@link IReadTransaction} on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} on old revision
     * @param paramDepth
     *            {@link DepthCounter} container for current depths of both transaction cursors
     * @param paramFireDiff
     *            determines if a diff should be fired
     * @return kind of difference
     */
    EDiff diff(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
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
                diff = diffAlgorithm(paramNewRtx, paramOldRtx, paramDepth);
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

    /**
     * Optimized diff, which skips unnecessary comparsions.
     * 
     * @param paramNewRtx
     *            {@link IReadTransaction} on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} on old revision
     * @param paramDepth
     *            {@link DepthCounter} container for current depths of both transaction cursors
     * @param paramFireDiff
     *            determines if a diff should be fired
     * @return kind of difference
     */
    EDiff optimizedDiff(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
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
                    diff = diffAlgorithm(paramNewRtx, paramOldRtx, paramDepth);
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
    private EDiff diffAlgorithm(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
        final DepthCounter paramDepth) {
        EDiff diff = null;

        // Check if node has been deleted.
        if (paramDepth.getOldDepth() > paramDepth.getNewDepth()) {
            diff = EDiff.DELETED;
        } else if (checkUpdate(paramNewRtx, paramOldRtx)) { // Check if node has been updated.
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
     * Check for an update of a node.
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
