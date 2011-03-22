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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.treetank.api.IDatabase;
import org.treetank.exception.AbsTTException;

/**
 * Wrapper for public access.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class DiffFactory {

    /**
     * Possible kinds of differences between two nodes.
     * 
     * @author Johannes Lichtenberger, University of Konstanz
     * 
     */
    public enum EDiff {
        /** Nodes are the same. */
        SAME,

        /** Nodes are the same (including subtrees), internally used for optimizations. */
        SAMEHASH,

        /** Node has been inserted. */
        INSERTED,

        /** Node has been deleted. */
        DELETED,

        /** Node has been updated. */
        UPDATED
    }

    /**
     * Kind of diff.
     */
    public enum EDiffKind {
        /** Normal diff. */
        NORMAL,

        /** Optimized diff. */
        OPTIMIZED
    }

    /** Determines the kind of diff to invoke. */
    private enum DiffKind {
        /** Full diff. */
        FULL,

        /** Structural diff (doesn't recognize differences in namespace and attribute nodes. */
        STRUCTURAL;
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
        public Void call() throws AbsTTException {
            switch (mDiffKind) {
            case STRUCTURAL:
                new StructuralDiff(mDb, mKey, mNewRev, mOldRev, mKind, mObservers);
                break;
            case FULL:
                new FullDiff(mDb, mKey, mNewRev, mOldRev, mKind, mObservers);
                break;
            default:
            }
            return null;
        }
    }

}
