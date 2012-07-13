/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.service.xml.diff;

import static org.treetank.node.IConstants.ELEMENT;
import static org.treetank.node.IConstants.ROOT;
import static org.treetank.node.IConstants.ROOT_NODE;
import static org.treetank.node.IConstants.TEXT;

import javax.xml.namespace.QName;

import org.treetank.access.NodeReadTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.api.INodeReadTrx;
import org.treetank.exception.TTException;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.service.xml.diff.DiffFactory.Builder;
import org.treetank.service.xml.diff.DiffFactory.EDiff;
import org.treetank.service.xml.diff.DiffFactory.EDiffOptimized;

/**
 * Abstract diff class which implements common functionality.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsDiff extends AbsDiffObservable {

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
     * Kind of hash method.
     * 
     * @see HashKind
     */
    transient HashKind mHashKind;

    /**
     * Kind of difference.
     * 
     * @see EDiff
     */
    private transient EDiff mDiff;

    /** Diff kind. */
    private transient EDiffOptimized mDiffKind;

    /** {@link DepthCounter} instance. */
    private transient DepthCounter mDepth;

    /** Key of "root" node in new revision. */
    private transient long mRootKey;

    /**
     * Constructor.
     * 
     * @param paramBuilder
     *            {@link Builder} reference
     * @throws TTException
     *             if setting up transactions failes
     */
    AbsDiff(final Builder paramBuilder) throws TTException {
        assert paramBuilder != null;

        mDiffKind = paramBuilder.mKind;
        synchronized (paramBuilder.mSession) {
            mNewRtx = new NodeReadTrx(paramBuilder.mSession.beginPageReadTransaction(paramBuilder.mNewRev));
            mOldRtx = new NodeReadTrx(paramBuilder.mSession.beginPageReadTransaction(paramBuilder.mOldRev));
            mHashKind = HashKind.Postorder;
        }
        mNewRtx.moveTo(paramBuilder.mKey);
        mOldRtx.moveTo(paramBuilder.mKey);
        mRootKey = paramBuilder.mKey;

        synchronized (paramBuilder.mObservers) {
            for (final IDiffObserver observer : paramBuilder.mObservers) {
                addObserver(observer);
            }
        }
        mDiff = EDiff.SAME;
        mDiffKind = paramBuilder.mKind;
        mDepth = new DepthCounter(paramBuilder.mNewDepth, paramBuilder.mOldDepth);
        // System.out.println("NEW REV: " + paramBuilder.mNewRev + " new rev: "
        // +
        // mNewRtx.getRevisionNumber());
    }

    /**
     * Do the diff.
     * 
     * @throws TTException
     *             if setting up transactions failes
     */
    void diffMovement() throws TTException {
        assert mHashKind != null;
        assert mNewRtx != null;
        assert mOldRtx != null;
        assert mDiff != null;
        assert mDiffKind != null;

        // Check first nodes.
        if (mNewRtx.getNode().getKind() != ROOT) {
            if (mHashKind == HashKind.None || mDiffKind == EDiffOptimized.NO) {
                mDiff = diff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
            } else {
                mDiff = optimizedDiff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
            }
        }

        // Iterate over new revision.
        while ((mOldRtx.getNode().getKind() != ROOT && mDiff == EDiff.DELETED)
            || moveCursor(mNewRtx, ERevision.NEW)) {
            if (mDiff != EDiff.INSERTED) {
                moveCursor(mOldRtx, ERevision.OLD);
            }

            if (mNewRtx.getNode().getKind() != ROOT || mOldRtx.getNode().getKind() != ROOT) {
                if (mHashKind == HashKind.None || mDiffKind == EDiffOptimized.NO) {
                    mDiff = diff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
                } else {
                    mDiff = optimizedDiff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
                }
            }
        }

        // Nodes deleted in old rev at the end of the tree.
        if (mOldRtx.getNode().getKind() != ROOT) {
            // First time it might be EDiff.INSERTED where the cursor doesn't
            // move.
            while (mDiff == EDiff.INSERTED || moveCursor(mOldRtx, ERevision.OLD)) {
                if (mHashKind == HashKind.None || mDiffKind == EDiffOptimized.NO) {
                    mDiff = diff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
                } else {
                    mDiff = optimizedDiff(mNewRtx, mOldRtx, mDepth, EFireDiff.TRUE);
                }
            }
        }

        done();
    }

    /**
     * Move cursor one node forward in pre order.
     * 
     * @param paramRtx
     *            the {@link IReadTransaction} to use
     * @param paramRevision
     *            the {@link ERevision} constant
     * @return true, if cursor moved, false otherwise
     */
    boolean moveCursor(final INodeReadTrx paramRtx, final ERevision paramRevision) {
        assert paramRtx != null;

        boolean moved = false;

        final IStructNode node = ((IStructNode)paramRtx.getNode());
        if (node.hasFirstChild()) {
            if (node.getKind() != ROOT && mDiffKind == EDiffOptimized.HASHED
                && mHashKind != HashKind.None && (mDiff == EDiff.SAMEHASH || mDiff == EDiff.DELETED)) {
                moved = paramRtx.moveTo(((IStructNode)paramRtx.getNode()).getRightSiblingKey());

                if (!moved) {
                    moved = moveToFollowingNode(paramRtx, paramRevision);
                }
            } else {
                moved = paramRtx.moveTo(((IStructNode)paramRtx.getNode()).getFirstChildKey());
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
            if (paramRtx.getNode().getNodeKey() == mRootKey) {
                paramRtx.moveTo(ROOT_NODE);
            } else {
                moved = paramRtx.moveTo(((IStructNode)paramRtx.getNode()).getRightSiblingKey());
            }
        } else {
            moved = moveToFollowingNode(paramRtx, paramRevision);
        }

        return moved;
    }

    /**
     * Move to next following node.
     * 
     * @param paramRtx
     *            the {@link IReadTransaction} to use
     * @param paramRevision
     *            the {@link ERevision} constant
     * @return true, if cursor moved, false otherwise
     */
    private boolean moveToFollowingNode(final INodeReadTrx paramRtx, final ERevision paramRevision) {
        boolean moved = false;
        while (!((IStructNode)paramRtx.getNode()).hasRightSibling()
            && ((IStructNode)paramRtx.getNode()).hasParent() && paramRtx.getNode().getNodeKey() != mRootKey) {
            moved = paramRtx.moveTo(paramRtx.getNode().getParentKey());
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
        }

        if (paramRtx.getNode().getNodeKey() == mRootKey) {
            paramRtx.moveTo(ROOT_NODE);
        }

        moved = paramRtx.moveTo(((IStructNode)paramRtx.getNode()).getRightSiblingKey());
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
     *            {@link DepthCounter} container for current depths of both
     *            transaction cursors
     * @param paramFireDiff
     *            determines if a diff should be fired
     * @return kind of difference
     */
    EDiff diff(final INodeReadTrx paramNewRtx, final INodeReadTrx paramOldRtx, final DepthCounter paramDepth,
        final EFireDiff paramFireDiff) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;
        assert paramDepth != null;

        EDiff diff = EDiff.SAME;

        // Check for modifications.
        switch (paramNewRtx.getNode().getKind()) {
        case ROOT:
        case TEXT:
        case ELEMENT:
            if (!checkNodes(paramNewRtx, paramOldRtx)) {
                diff = diffAlgorithm(paramNewRtx, paramOldRtx, paramDepth);
            }
            break;
        default:
            // Do nothing.
        }

        if (paramFireDiff == EFireDiff.TRUE) {
            fireDiff(diff, ((IStructNode)paramNewRtx.getNode()), ((IStructNode)paramOldRtx.getNode()),
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
     *            {@link DepthCounter} container for current depths of both
     *            transaction cursors
     * @param paramFireDiff
     *            determines if a diff should be fired
     * @return kind of difference
     */
    EDiff optimizedDiff(final INodeReadTrx paramNewRtx, final INodeReadTrx paramOldRtx,
        final DepthCounter paramDepth, final EFireDiff paramFireDiff) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;
        assert paramDepth != null;

        EDiff diff = EDiff.SAMEHASH;

        // Check for modifications.
        switch (paramNewRtx.getNode().getKind()) {
        case ROOT:
        case TEXT:
        case ELEMENT:
            if (paramNewRtx.getNode().getNodeKey() != paramOldRtx.getNode().getNodeKey()
                || paramNewRtx.getNode().getHash() != paramOldRtx.getNode().getHash()) {
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
                fireDiff(EDiff.SAME, ((IStructNode)paramNewRtx.getNode()), ((IStructNode)paramOldRtx
                    .getNode()), new DiffDepth(paramDepth.getNewDepth(), paramDepth.getOldDepth()));
            } else {
                fireDiff(diff, ((IStructNode)paramNewRtx.getNode()), ((IStructNode)paramOldRtx.getNode()),
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
     *            {@link DepthCounter} container for current depths of both
     *            transaction cursors
     * @return kind of diff
     */
    private EDiff diffAlgorithm(final INodeReadTrx paramNewRtx, final INodeReadTrx paramOldRtx,
        final DepthCounter paramDepth) {
        EDiff diff = null;

        // Check if node has been deleted.
        if (paramDepth.getOldDepth() > paramDepth.getNewDepth()) {
            diff = EDiff.DELETED;
        } else if (checkUpdate(paramNewRtx, paramOldRtx)) { // Check if node has
                                                            // been updated.
            diff = EDiff.UPDATED;
        } else {
            // See if one of the right sibling matches.
            EFoundEqualNode found = EFoundEqualNode.FALSE;
            final long key = paramOldRtx.getNode().getNodeKey();

            while (((IStructNode)paramOldRtx.getNode()).hasRightSibling()
                && paramOldRtx.moveTo(((IStructNode)paramOldRtx.getNode()).getRightSiblingKey())
                && found == EFoundEqualNode.FALSE) {
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
     * @return true if nodes are "equal" according to their {@link QName}s,
     *         otherwise false
     */
    boolean checkName(final INodeReadTrx paramNewRtx, final INodeReadTrx paramOldRtx) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;
        boolean found = false;
        if (paramNewRtx.getNode().getKind() == paramOldRtx.getNode().getKind()) {
            switch (paramNewRtx.getNode().getKind()) {
            case ELEMENT:
                if (paramNewRtx.getQNameOfCurrentNode().equals(paramOldRtx.getQNameOfCurrentNode())) {
                    found = true;
                }
                break;
            case TEXT:
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
    abstract boolean checkNodes(final INodeReadTrx paramNewRtx, final INodeReadTrx paramOldRtx);

    /**
     * Check for an update of a node.
     * 
     * @param paramNewRtx
     *            first {@link IReadTransaction} instance
     * @param paramOldRtx
     *            second {@link IReadTransaction} instance
     * @return kind of diff
     */
    boolean checkUpdate(final INodeReadTrx paramNewRtx, final INodeReadTrx paramOldRtx) {
        assert paramNewRtx != null;
        assert paramOldRtx != null;
        boolean updated = false;
        final long newKey = paramNewRtx.getNode().getNodeKey();
        boolean movedNewRtx = paramNewRtx.moveTo(((IStructNode)paramNewRtx.getNode()).getRightSiblingKey());
        final long oldKey = paramOldRtx.getNode().getNodeKey();
        boolean movedOldRtx = paramOldRtx.moveTo(((IStructNode)paramOldRtx.getNode()).getRightSiblingKey());
        if (movedNewRtx && movedOldRtx && checkNodes(paramNewRtx, paramOldRtx)) {
            updated = true;
        } else if (!movedNewRtx && !movedOldRtx) {
            movedNewRtx = paramNewRtx.moveTo(paramNewRtx.getNode().getParentKey());
            movedOldRtx = paramOldRtx.moveTo(paramOldRtx.getNode().getParentKey());

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
