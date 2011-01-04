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

import com.treetank.api.IDatabase;

/**
 * Wrapper for public access.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class DiffWrapper implements Runnable {

    /** Several implemented diffs. */
    private enum Diff {
        /** No diff at all. */
        NONE {
            @Override
            void invoke(final IDatabase paramDb, final long paramKey, final long paramNewRev,
                final long paramOldRev, final IDiffObserver paramObserver) {
                throw new IllegalStateException(
                    "Before invoking or submitting the thread call one of the diff methods!");
            }
        },

        /** Full diff. */
        FULL {
            @Override
            void invoke(final IDatabase paramDb, final long paramKey, final long paramNewRev,
                final long paramOldRev, final IDiffObserver paramObserver) {
                new FullDiff(paramDb, paramKey, paramNewRev, paramOldRev, paramObserver);
            }
        },

        /** Structural diff. */
        STRUCTURAL {
            @Override
            void invoke(final IDatabase paramDb, final long paramKey, final long paramNewRev,
                final long paramOldRev, final IDiffObserver paramObserver) {
                new StructuralDiff(paramDb, paramKey, paramNewRev, paramOldRev, paramObserver);
            }
        };

        /**
         * Invoke diff.
         * 
         * @param paramDb
         *            {@link IDatabase} instance
         * @param paramKey
         *            start node key
         * @param paramNewRev
         *            new revision number
         * @param paramOldRev
         *            old revision number
         * @param paramObserver
         *            the {@link IDiffObserver}
         */
        abstract void invoke(final IDatabase paramDb, final long paramKey, final long paramNewRev,
            final long paramOldRev, final IDiffObserver paramObserver);
    }

    /** Determines the kind of diff to use. */
    private transient Diff mDiff;

    /** {@link IDatabase} instance. */
    private final IDatabase mDatabase;

    /** Start key. */
    private final long mKey;

    /** New revision. */
    private final long mNewRev;

    /** Old revision. */
    private final long mOldRev;

    /** Difference observer. */
    private final IDiffObserver mObserver;

    /**
     * Constructor.
     * 
     * @param paramDb
     *            {@link IDatabase} instance
     * @param paramKey
     *            key of start node
     * @param paramNewRev
     *            new revision to compare
     * @param paramOldRev
     *            old revision to compare
     * @param paramObserver
     *            observes differences
     */
    public DiffWrapper(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final IDiffObserver paramObserver) {
        if (paramDb == null || paramKey < -1L || paramNewRev < 0 || paramOldRev < 0 || paramObserver == null) {
            throw new IllegalArgumentException();
        }
        if (paramNewRev == paramOldRev || paramNewRev > paramOldRev) {
            throw new IllegalArgumentException(
                "Revision numbers must not be the same and the new revision must have a greater number than the old revision!");
        }
        mDatabase = paramDb;
        mKey = paramKey;
        mDiff = Diff.NONE;
        mNewRev = paramNewRev;
        mOldRev = paramOldRev;
        mObserver = paramObserver;
    }

    /** Do a full diff. */
    public final void fullDiff() {
        mDiff = Diff.FULL;
    }

    /** Do a structural diff. */
    public final void structuralDiff() {
        mDiff = Diff.STRUCTURAL;
    }

    /** {@inheritDoc} */
    @Override
    public final void run() {
        mDiff.invoke(mDatabase, mKey, mNewRev, mOldRev, mObserver);
    }

}
