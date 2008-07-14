/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.axislayer;

import org.treetank.api.IAxis;
import org.treetank.api.IFilter;

/**
 * <h1>TestAxis</h1>
 * 
 * <p>
 * Perform a test on a given axis.
 * </p>
 */
public class FilterAxis extends AbstractAxis implements IAxis {

  /** Axis to test. */
  private final IAxis mAxis;

  /** Test to apply to axis. */
  private final IFilter[] mAxisFilter;

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis to iterate over.
   * @param axisTest Test to perform for each node found with axis.
   */
  public FilterAxis(final IAxis axis, final IFilter... axisTest) {
    super(axis.getTransaction());
    mAxis = axis;
    mAxisFilter = axisTest;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {
    super.reset(nodeKey);
    if (mAxis != null) {
      mAxis.reset(nodeKey);
    }
  }

  /**
   * {@inheritDoc}
   */
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
  public IAxis getAxis() {
    
    return mAxis;
  }
  
}
