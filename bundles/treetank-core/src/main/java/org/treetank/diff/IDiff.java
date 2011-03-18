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

import org.treetank.api.IReadTransaction;
import org.treetank.diff.AbsDiffMovement.EFireDiff;
import org.treetank.diff.DiffFactory.EDiff;

/**
 * Diff interface.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
interface IDiff {
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
        final DepthCounter paramDepth, final EFireDiff paramFireDiff);

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
    @Deprecated
    EDiff optimizedDiff(final IReadTransaction paramNewRtx, final IReadTransaction paramOldRtx,
        final DepthCounter paramDepth, final EFireDiff paramFireDiff);

    /**
     * Diff computation done, thus inform listeners.
     */
    void done();
}
