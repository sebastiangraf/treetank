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

package org.treetank.axis.filter;

import org.treetank.api.IFilter;
import org.treetank.api.IReadTransaction;

/**
 * <h1>NestedFilter</h1>
 * <p>
 * Nests two or more IFilters.
 * </p>
 */
public class NestedFilter extends AbsFilter {

    /** Tests to apply. */
    private final IFilter[] mFilter;

    /**
     * Default constructor.
     * 
     * @param rtx
     *            Transaction this filter is bound to.
     * @param axisTest
     *            Test to perform for each node found with axis.
     */
    public NestedFilter(final IReadTransaction rtx, final IFilter... axisTest) {

        super(rtx);
        mFilter = axisTest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean filter() {

        boolean filterResult = true;

        for (final IFilter filter : mFilter) {
            filterResult = filterResult && filter.filter();
        }

        return filterResult;
    }
}
