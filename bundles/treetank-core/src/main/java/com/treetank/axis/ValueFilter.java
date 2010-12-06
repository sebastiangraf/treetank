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

package com.treetank.axis;

import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.node.ENodes;
import com.treetank.utils.TypedValue;

/**
 * <h1>ValueAxisTest</h1>
 * 
 * <p>
 * Only match nodes of kind TEXT whoms value matches.
 * </p>
 */
public class ValueFilter extends AbsFilter implements IFilter {

    /** Value test to do. */
    private final byte[] mValue;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Transaction to bind filter to.
     * @param mValue
     *            Value to find.
     */
    public ValueFilter(final IReadTransaction rtx, final byte[] mValue) {
        super(rtx);
        this.mValue = mValue;
    }

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Transaction to bind filter to.
     * @param mValue
     *            Value to find.
     */
    public ValueFilter(final IReadTransaction rtx, final String mValue) {
        this(rtx, TypedValue.getBytes(mValue));
    }

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Transaction to bind filter to.
     * @param mValue
     *            Value to find.
     */
    public ValueFilter(final IReadTransaction rtx, final int mValue) {
        this(rtx, TypedValue.getBytes(mValue));
    }

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Transaction to bind filter to.
     * @param mValue
     *            Value to find.
     */
    public ValueFilter(final IReadTransaction rtx, final long mValue) {
        this(rtx, TypedValue.getBytes(mValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean filter() {
        return (getTransaction().getNode().getKind() == ENodes.TEXT_KIND || getTransaction().getNode()
            .getKind() == ENodes.ATTRIBUTE_KIND) && (TypedValue.equals(getTransaction().getNode()
            .getRawValue(), mValue));
    }

}
