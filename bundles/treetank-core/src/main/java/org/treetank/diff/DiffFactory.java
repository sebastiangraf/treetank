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

package org.treetank.diff;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;
import org.treetank.api.IDatabase;
import org.treetank.exception.AbsTTException;
import org.treetank.utils.LogWrapper;

/**
 * Wrapper for public access.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class DiffFactory {

    /**
     * Possible kinds of differences between two nodes.
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
        FULL {
            @Override
            void invoke(final Builder paramBuilder) {
                new FullDiff(paramBuilder);
            }
        },

        /** Structural diff (doesn't recognize differences in namespace and attribute nodes. */
        STRUCTURAL {
            @Override
            void invoke(final Builder paramBuilder) {
                new StructuralDiff(paramBuilder);
            }
        };

        /**
         * Invoke diff.
         * 
         * @param paramBuilder
         *            {@link Builder} reference
         */
        abstract void invoke(final Builder paramBuilder);
    }

    /** Kind of diff to invoke. */
    private static DiffKind mDiffKind;

    /** Builder to simplify static methods. */
    public static final class Builder {

        /** {@link IDatabase} reference. */
        final IDatabase mDb;

        /** Start key. */
        final long mKey;

        /** New revision. */
        final long mNewRev;

        /** Old revision. */
        final long mOldRev;

        /** Depth of "root" node in new revision. */
        transient int mNewDepth;

        /** Depth of "root" node in old revision. */
        transient int mOldDepth;

        /** Diff kind. */
        final EDiffKind mKind;

        /** {@link Set} of {@link IDiffObserver}s. */
        final Set<IDiffObserver> mObservers;

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
         * @param paramDiffKind
         *            kind of diff (optimized or not)
         * @param paramObservers
         *            {@link Set} of observers
         */
        public Builder(final IDatabase paramDb, final long paramKey, final long paramNewRev,
            final long paramOldRev, final EDiffKind paramDiffKind, final Set<IDiffObserver> paramObservers) {
            mDb = paramDb;
            mKey = paramKey;
            mNewRev = paramNewRev;
            mOldRev = paramOldRev;
            mKind = paramDiffKind;
            mObservers = paramObservers;
        }

        /**
         * Set new depth.
         * 
         * @param paramNewDepth
         *            depth of "root" node in new revision
         * @return this builder
         */
        public Builder setNewDepth(final int paramNewDepth) {
            mNewDepth = paramNewDepth;
            return this;
        }

        /**
         * Set old depth.
         * 
         * @param paramOldDepth
         *            depth of "root" node in old revision
         * @return this builder
         */
        public Builder setOldDepth(final int paramOldDepth) {
            mOldDepth = paramOldDepth;
            return this;
        }
    }

    /**
     * Private constructor.
     */
    private DiffFactory() {
        // No instantiation allowed.
        throw new AssertionError("No instantiation allowed!");
    }

    /**
     * Do a full diff.
     * 
     * @param paramBuilder
     *            {@link Builder} reference
     */
    public static void invokeFullDiff(final Builder paramBuilder) {
        checkParams(paramBuilder);
        mDiffKind = DiffKind.FULL;
        final ExecutorService exes = Executors.newSingleThreadExecutor();
        exes.submit(new Invoke(paramBuilder));
        exes.shutdown();
    }

    /**
     * Do a structural diff.
     * 
     * @param paramBuilder
     *            {@link Builder} reference
     */
    public static void invokeStructuralDiff(final Builder paramBuilder) {
        checkParams(paramBuilder);
        mDiffKind = DiffKind.STRUCTURAL;
        final ExecutorService exes = Executors.newSingleThreadExecutor();
        exes.submit(new Invoke(paramBuilder));
        exes.shutdown();
    }

    /**
     * Check parameters for validity and assign global static variables.
     * 
     * @param paramBuilder
     *            {@link Builder} reference
     */
    private static void checkParams(final Builder paramBuilder) {
        if (paramBuilder.mDb == null || paramBuilder.mKey < -1L || paramBuilder.mNewRev < 0
            || paramBuilder.mOldRev < 0 || paramBuilder.mObservers == null || paramBuilder.mKind == null) {
            throw new IllegalArgumentException("No valid arguments specified!");
        }
        if (paramBuilder.mNewRev == paramBuilder.mOldRev || paramBuilder.mNewRev < paramBuilder.mOldRev) {
            throw new IllegalArgumentException(
                "Revision numbers must not be the same and the new revision must have a greater number than the old revision!");
        }
    }

    /** Invoke diff. */
    private static class Invoke implements Callable<Void> {

        /** {@link Builder} reference. */
        private final Builder mBuilder;

        /**
         * Constructor.
         * 
         * @param paramBuilder
         *            {@link Builder} reference
         */
        Invoke(final Builder paramBuilder) {
            mBuilder = paramBuilder;
        }

        @Override
        public Void call() throws AbsTTException {
            mDiffKind.invoke(mBuilder);
            return null;
        }
    }

}
