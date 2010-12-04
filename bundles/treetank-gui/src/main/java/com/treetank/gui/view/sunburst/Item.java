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
 * Item container to simplify moved enum.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class Item {
    /** Start angle. */
    transient float mAngle;

    /** End angle. */
    transient float mExtension;

    /** Child count per depth. */
    transient long mChildCountPerDepth;

    /** Index to the parent item. */
    transient int mIndexToParent;

    /**
     * Set all fields.
     * 
     * @param paramAngle
     *            Start angle
     * @param paramExtension
     *            End angle
     * @param paramChildCountPerDepth
     *            Child count per depth
     * @param paramIndexToParent
     *            Index to the parent item
     * @return NodeRelations instance.
     */
    Item setAll(final float paramAngle, final float paramExtension, final long paramChildCountPerDepth,
        final int paramIndexToParent) {
        assert paramAngle >= 0f;
        assert paramExtension > 0f;
        assert paramChildCountPerDepth >= 0;
        assert paramIndexToParent >= -1;
        mAngle = paramAngle;
        mExtension = paramExtension;
        mChildCountPerDepth = paramChildCountPerDepth;
        mIndexToParent = paramIndexToParent;
        return this;
    }
}
