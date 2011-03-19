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

import org.treetank.access.WriteTransaction.HashKind;
import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.diff.AbsDiff.EFireDiff;
import org.treetank.diff.DiffFactory.EDiffKind;
import org.treetank.node.ENodes;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class DiffAxis extends AbsAxis {

    /**
     * Constructor.
     * 
     * @param paramNewRtx
     *            {@link IReadTransaction} on new revision
     * @param paramOldRtx
     *            {@link IReadTransaction} on old revision
     * @param paramDiff
     *            determines the kind of diff
     */
    public DiffAxis(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx) {
        super(paramNewRtx);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        return false;
//        if (getTransaction().getNode().getKind() != ENodes.ROOT_KIND) {
//            if (mHashKind == HashKind.None || mDiffKind == EDiffKind.NORMAL) {
//                mDiff = diff(getTransaction(), mOldRtx, mDepth, EFireDiff.TRUE);
//            } else {
//                mDiff = optimizedDiff(getTransaction(), mOldRtx, mDepth, EFireDiff.TRUE);
//            }
//        }

    }

}
