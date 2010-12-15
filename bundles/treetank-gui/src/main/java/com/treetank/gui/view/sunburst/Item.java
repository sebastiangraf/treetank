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
package com.treetank.gui.view.sunburst;

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

    /** Child count per depth. */
    transient long mDescendantCount;
    
    /** Child count per depth. */
    transient int mModificationCount;

    /** Builder to simplify item constructor. */
    static final class Builder {

        /** Start angle. */
        transient float mAngle;

        /** End angle. */
        transient float mExtension;

        /** Child count per depth. */
        transient long mChildCountPerDepth = -1L;

        /** Child count per depth. */
        transient long mDescendantCount = -1L;

        /** Index to the parent item. */
        transient int mIndexToParent;
        
        /** Child count per depth. */
        transient int mModificationCount;

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
            assert paramExtension > 0f;
            assert paramIndexToParent >= -1;
            mAngle = paramAngle;
            mExtension = paramExtension;
            mIndexToParent = paramIndexToParent;
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
         * @param paramDescendantCountParent
         *            parent descendant count
         * @return this builder
         */
        Builder setDescendantCount(final long paramDescendantCount) {
            mDescendantCount = paramDescendantCount;
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
         * Setup item.
         */
        void set() {
            assert mChildCountPerDepth != -1L || mDescendantCount != -1L;
            ITEM.setAll(this);
        }
    }

    /**
     * Constructor.
     * 
     * @param paramBuilder
     *            builder
     */
    void setAll(final Builder paramBuilder) {
        mAngle = paramBuilder.mAngle;
        mExtension = paramBuilder.mExtension;
        mIndexToParent = paramBuilder.mIndexToParent;
        mChildCountPerDepth = paramBuilder.mChildCountPerDepth;
        mModificationCount = paramBuilder.mModificationCount;
    }
}
