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
 * Contains settings used for updating the model.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstContainer {

    /** Revision to compare. */
    transient long mRevision;

    /** Max depth in the tree. */
    transient int mDepth;

    /** Modification weight. */
    transient float mModWeight;

    /** Node key to start from. */
    transient long mKey;

    /**
     * Set start key.
     * 
     * @param paramKey
     *            node key to start from
     * @return this
     */
    SunburstContainer setKey(final long paramKey) {
        mKey = paramKey;
        return this;
    }

    /**
     * Set revision to compare.
     * 
     * @param paramRevision
     *            the Revision to set
     * @return this
     */
    SunburstContainer setRevision(final long paramRevision) {
        mRevision = paramRevision;
        return this;
    }

    /**
     * Set modification weight.
     * 
     * @param paramModWeight
     *            the modWeight to set
     * @return this
     */
    SunburstContainer setModWeight(final float paramModWeight) {
        mModWeight = paramModWeight;
        return this;
    }

    /**
     * Set all remaining member variables.
     * 
     * @param paramRevision
     *            revision to compare
     * @param paramDepth
     *            Depth in the tree
     * @param paramModificationWeight
     *            weighting of modifications
     * @return this
     */
    SunburstContainer setAll(final long paramRevision, final int paramDepth,
        final float paramModificationWeight) {
        mRevision = paramRevision;
        mDepth = paramDepth;
        mModWeight = paramModificationWeight;
        return this;
    }
}
