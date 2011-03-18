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
package org.treetank.gui.view.sunburst;

import com.treetank.diff.DiffFactory.EDiff;

/**
 * Item container to simplify {@link EMoved} enum.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class Item {

    /** Item instance. */
    public static final Item ITEM = new Item();

    /** Builder instance. */
    public static final Builder BUILDER = new Builder();

    /** Start angle. */
    transient float mAngle;

    /** End angle. */
    transient float mExtension;

    /** Index to the parent item. */
    transient int mIndexToParent;

    /** Child count per depth. */
    transient long mChildCountPerDepth;

    /** Descendant count. */
    transient int mDescendantCount;

    /** Parent descendant count. */
    transient int mParentDescendantCount;

    /** Modification count on a node (counts subtree modifications). */
    transient int mModificationCount;

    /** Modification count on the parent node (counts subtree modifications). */
    transient int mParentModificationCount;

    /** Determines if 1 must be subtracted. */
    transient boolean mSubtract;
    
    /** Kind of diff of the current node. */
    transient EDiff mDiff;

    /** Builder to simplify item constructor. */
    static final class Builder {

        /** Start angle. */
        transient float mAngle;

        /** End angle. */
        transient float mExtension;

        /** Child count per depth. */
        transient long mChildCountPerDepth = -1L;

        /** Child count per depth. */
        transient int mDescendantCount = -1;

        /** Parent descendant count. */
        transient int mParentDescendantCount;

        /** Index to the parent item. */
        transient int mIndexToParent;

        /** Modification count on a node (counts subtree modifications). */
        transient int mModificationCount;

        /** Modification count on the parent node (counts subtree modifications). */
        transient int mParentModificationCount;

        /** Determines if 1 must be subtracted. */
        transient boolean mSubtract;

        /** Kind of diff of the current node. */
        transient EDiff mDiff;

        /**
         * Private constructor to prevent instantiation other then via the public
         * Builder instance.
         */
        private Builder() {
        }

        /**
         * Set all fields.
         * 
         * @param paramAngle
         *            start angle
         * @param paramExtension
         *            end angle
         * @param paramIndexToParent
         *            index to the parent item
         * @return this builder
         */
        Builder set(final float paramAngle, final float paramExtension, final int paramIndexToParent) {
            assert paramAngle >= 0f;
            assert paramExtension >= 0f;
            assert paramIndexToParent >= -1;
            mAngle = paramAngle;
            mExtension = paramExtension;
            mIndexToParent = paramIndexToParent;
            return this;
        }

        /**
         * Set subtract.
         * 
         * @param paramSubtract
         *            determines if one must be subtracted
         * @return this builder
         */
        Builder setSubtract(final boolean paramSubtract) {
            mSubtract = paramSubtract;
            return this;
        }

        /**
         * Set child count per depth.
         * 
         * @param paramChildCountPerDepth
         *            child count per depth
         * @return this builder
         */
        Builder setChildCountPerDepth(final long paramChildCountPerDepth) {
            mChildCountPerDepth = paramChildCountPerDepth;
            return this;
        }

        /**
         * Set descendant count.
         * 
         * @param paramDescendantCount
         *            descendant count
         * @return this builder
         */
        Builder setDescendantCount(final int paramDescendantCount) {
            mDescendantCount = paramDescendantCount;
            return this;
        }

        /**
         * Set parent descendant count.
         * 
         * @param paramParentDescCount
         *            parent descendant count
         * @return this builder
         */
        Builder setParentDescendantCount(final int paramParentDescCount) {
            mParentDescendantCount = paramParentDescCount;
            return this;
        }

        /**
         * Set modification count.
         * 
         * @param paramModificationCount
         *            modification count
         * @return this builder
         */
        Builder setModificationCount(final int paramModificationCount) {
            mModificationCount = paramModificationCount;
            return this;
        }

        /**
         * Set parent modification count.
         * 
         * @param paramParentModificationCount
         *            parent modification count
         * @return this builder
         */
        Builder setParentModificationCount(final int paramParentModificationCount) {
            mParentModificationCount = paramParentModificationCount;
            return this;
        }

        /**
         * Set kind of diff.
         * 
         * @param paramDiff
         *            {@link EDiff} -- kind of diff
         * @return this builder
         */
        Builder setDiff(final EDiff paramDiff) {
            mDiff = paramDiff;
            return this;
        }

        /**
         * Setup item.
         */
        void set() {
            assert mChildCountPerDepth != -1L || mDescendantCount != -1;
            ITEM.setAll(this);
        }
    }

    /**
     * Private constructor to prevent instantiation other then via the public
     * Item instance.
     */
    private Item() {
    }

    /**
     * Constructor.
     * 
     * @param paramBuilder
     *            the builder
     */
    void setAll(final Builder paramBuilder) {
        mAngle = paramBuilder.mAngle;
        mExtension = paramBuilder.mExtension;
        mIndexToParent = paramBuilder.mIndexToParent;
        mChildCountPerDepth = paramBuilder.mChildCountPerDepth;
        mModificationCount = paramBuilder.mModificationCount;
        mParentModificationCount = paramBuilder.mParentModificationCount;
        mDescendantCount = paramBuilder.mDescendantCount;
        mParentDescendantCount = paramBuilder.mParentDescendantCount;
        mSubtract = paramBuilder.mSubtract;
        mDiff = paramBuilder.mDiff;
    }
}
