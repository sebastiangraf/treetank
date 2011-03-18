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

package org.treetank.axis;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;

/**
 * <h1>TestAxis</h1>
 * 
 * <p>
 * Perform a test on a given axis.
 * </p>
 */
public class FilterAxis extends AbsAxis {

    /** Axis to test. */
    private final AbsAxis mAxis;

    /** Test to apply to axis. */
    private final IFilter[] mAxisFilter;

    /**
     * Constructor initializing internal state.
     * 
     * @param axis
     *            Axis to iterate over.
     * @param axisTest
     *            Test to perform for each node found with axis.
     */
    public FilterAxis(final AbsAxis axis, final IFilter... axisTest) {
        super(axis.getTransaction());
        mAxis = axis;
        mAxisFilter = axisTest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long mNodeKey) {
        super.reset(mNodeKey);
        if (mAxis != null) {
            mAxis.reset(mNodeKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasNext() {
        resetToLastKey();
        while (mAxis.hasNext()) {
            mAxis.next();
            boolean filterResult = true;
            for (final IFilter filter : mAxisFilter) {
                filterResult = filterResult && filter.filter();
            }
            if (filterResult) {
                return true;
            }
        }
        resetToStartKey();
        return false;
    }

    /**
     * Returns the inner axis.
     * 
     * @return the axis
     */
    public final AbsAxis getAxis() {
        return mAxis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setTransaction(final IReadTransaction rtx) {
        super.setTransaction(rtx);
        mAxis.setTransaction(rtx);
        for (IFilter filter : mAxisFilter) {
            filter.setTransaction(rtx);
        }
    }

}
