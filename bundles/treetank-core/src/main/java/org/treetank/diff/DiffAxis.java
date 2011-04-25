/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.diff;

import org.treetank.access.WriteTransaction.HashKind;
import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.diff.AbsDiff.EFireDiff;
import org.treetank.diff.DiffFactory.EDiffOptimized;
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
