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

import java.util.Set;

import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.diff.DiffFactory.EDiffKind;
import org.treetank.exception.AbsTTException;

/**
 * Structural diff, thus no attributes and namespace nodes are taken into account. Note that this class is
 * thread safe.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class StructuralDiff extends AbsDiff {

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
     * @param mKind
     *            kind of diff (optimized or not)
     * @param paramObservers
     *            {@link Set} of observes
     * @throws AbsTTException
     *             if retrieving the session fails
     */
    public StructuralDiff(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final EDiffKind mKind, final Set<IDiffObserver> paramObservers)
        throws AbsTTException {
        super(paramDb, paramKey, paramNewRev, paramOldRev, mKind, paramObservers);
    }

    /** {@inheritDoc} */
    @Override
    boolean checkNodes(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx) {
        boolean found = false;
        if (paramNewRtx.getNode().getNodeKey() == paramOldRtx.getNode().getNodeKey()) {
            found = true;
        }
        return found;
    }
}
