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

import com.treetank.api.IDatabase;

/**
 * Wrapper for public access.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class DiffFactory {
    /**
     * Private constructor.
     */
    private DiffFactory() {
        throw new AssertionError();
    }

    /**
     * Do a full diff.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     * @param paramKey
     *            key of start node
     * @param paramNewRev
     *            new revision to compare
     * @param paramOldRev
     *            old revision to compare
     * @param paramObservers
     *            observes differences
     */
    public static void invokeFullDiff(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final Set<IDiffObserver> paramObservers) {
        checkParams(paramDb, paramKey, paramNewRev, paramOldRev, paramObservers);
        new FullDiff(paramDb, paramKey, paramNewRev, paramOldRev, paramObservers);
    }

    /**
     * Do a structural diff.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     * @param paramKey
     *            key of start node
     * @param paramNewRev
     *            new revision to compare
     * @param paramOldRev
     *            old revision to compare
     * @param paramObservers
     *            observe differences
     */
    public static void invokeStructuralDiff(final IDatabase paramDb, final long paramKey,
        final long paramNewRev, final long paramOldRev, final Set<IDiffObserver> paramObservers) {
        checkParams(paramDb, paramKey, paramNewRev, paramOldRev, paramObservers);
        new StructuralDiff(paramDb, paramKey, paramNewRev, paramOldRev, paramObservers);
    }

    /**
     * Check parameters for validity and assign global static variables.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     * @param paramKey
     *            key of start node
     * @param paramNewRev
     *            new revision to compare
     * @param paramOldRev
     *            old revision to compare
     * @param paramObservers
     *            {@link Set} of observers
     */
    private static void checkParams(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final Set<IDiffObserver> paramObservers) {
        if (paramDb == null || paramKey < -1L || paramNewRev < 0 || paramOldRev < 0 || paramObservers == null) {
            throw new IllegalArgumentException();
        }
        if (paramNewRev == paramOldRev || paramNewRev > paramOldRev) {
            throw new IllegalArgumentException(
                "Revision numbers must not be the same and the new revision must have a greater number than the old revision!");
        }
    }

}
