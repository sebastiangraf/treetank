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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.treetank.api.IDatabase;
import com.treetank.exception.AbsTTException;

/**
 * Wrapper for public access.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class DiffFactory {

    /**
     * Kind of diff.
     */
    public enum EDiffKind {
        /** Normal diff. */
        NORMAL,

        /** Optimized diff. */
        OPTIMIZED;
    }

    /** Determines the kind of diff to invoke. */
    private enum DiffKind {
        /** Full diff. */
        FULL {
            @Override
            protected void invoke(final IDatabase paramDb, final long paramKey, final long paramNewRev,
                final long paramOldRev, final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers)
                throws AbsTTException {
                new FullDiff(paramDb, paramKey, paramNewRev, paramOldRev, paramDiffKind, paramObservers);
            }
        },

        /** Structural diff (doesn't recognize differences in namespace and attribute nodes. */
        STRUCTURAL {
            @Override
            protected void invoke(final IDatabase paramDb, final long paramKey, final long paramNewRev,
                final long paramOldRev, final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers)
                throws AbsTTException {
                new StructuralDiff(paramDb, paramKey, paramNewRev, paramOldRev, paramDiffKind, paramObservers);
            }
        };

        /**
         * Invoke diff.
         * 
         * @param paramDb
         *            {@link IDatabase} instance
         * @param paramKey
         *            key of start node
         * @param paramNewRev
         *            new revision to compare
         * @param paramOldRev
         *            old revision to compare
         * @param paramDiffKind
         *            kind of diff (optimized or not)
         * @param paramObservers
         *            observes differences
         * @throws AbsTTException
         *             if retrieving session from database fails
         */
        protected abstract void invoke(final IDatabase paramDb, final long paramKey, final long paramNewRev,
            final long paramOldRev, final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers)
            throws AbsTTException;
    }

    /** Kind of diff to invoke. */
    private static DiffKind mDiffKind;

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
     * @param paramDiffKind
     *            kind of diff (optimized or not)
     * @param paramObservers
     *            observes differences
     */
    public static void invokeFullDiff(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers) {
        checkParams(paramDb, paramKey, paramNewRev, paramOldRev, paramDiffKind, paramObservers);
        mDiffKind = DiffKind.FULL;
        final ExecutorService exes = Executors.newSingleThreadExecutor();
        exes.submit(new Invoke(paramDb, paramKey, paramNewRev, paramOldRev, paramDiffKind, paramObservers));
        exes.shutdown();
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
     * @param paramDiffKind
     *            kind of diff (optimized or not)
     * @param paramObservers
     *            observe differences
     */
    public static void invokeStructuralDiff(final IDatabase paramDb, final long paramKey,
        final long paramNewRev, final long paramOldRev, final EDiffKind paramDiffKind,
        final Set<IDiffObserver> paramObservers) {
        checkParams(paramDb, paramKey, paramNewRev, paramOldRev, paramDiffKind, paramObservers);
        mDiffKind = DiffKind.STRUCTURAL;
        final ExecutorService exes = Executors.newSingleThreadExecutor();
        exes.submit(new Invoke(paramDb, paramKey, paramNewRev, paramOldRev, paramDiffKind, paramObservers));
        exes.shutdown();
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
     * @param paramDiffKind
     *            kind of diff (optimized or not)
     * @param paramObservers
     *            {@link Set} of observers
     */
    private static void checkParams(final IDatabase paramDb, final long paramKey, final long paramNewRev,
        final long paramOldRev, final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers) {
        if (paramDb == null || paramKey < -1L || paramNewRev < 0 || paramOldRev < 0 || paramObservers == null
            || paramDiffKind == null) {
            throw new IllegalArgumentException();
        }
        if (paramNewRev == paramOldRev || paramNewRev < paramOldRev) {
            throw new IllegalArgumentException(
                "Revision numbers must not be the same and the new revision must have a greater number than the old revision!");
        }
    }

    /** Invoke diff. */
    private static class Invoke implements Callable<Void> {

        /** {@link IDatabase} reference. */
        private final IDatabase mDb;

        /** Start key. */
        private final long mKey;

        /** New revision. */
        private final long mNewRev;

        /** Old revision. */
        private final long mOldRev;

        /** Diff kind. */
        private final EDiffKind mKind;

        /** {@link Set} of {@link IDiffObserver}s. */
        private final Set<IDiffObserver> mObservers;

        /**
         * Constructor.
         * 
         * @param paramDb
         *            {@link IDatabase} reference
         * @param paramKey
         *            start key
         * @param paramNewRev
         *            new revision
         * @param paramOldRev
         *            old revision
         * @param paramDiffKind
         *            diff kind
         * @param paramObservers
         *            {@link Set} of {@link IDiffObserver}s
         */
        Invoke(final IDatabase paramDb, final long paramKey, final long paramNewRev, final long paramOldRev,
            final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers) {
            mDb = paramDb;
            mKey = paramKey;
            mNewRev = paramNewRev;
            mOldRev = paramOldRev;
            mKind = paramDiffKind;
            mObservers = paramObservers;
        }

        @Override
        public Void call() throws Exception {
            mDiffKind.invoke(mDb, mKey, mNewRev, mOldRev, mKind, mObservers);
            return null;
        }
    }

}
