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
