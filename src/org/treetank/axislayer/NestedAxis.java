/*
 * Copyright (c) 2007, Marc Kramis
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

/**
 * <h1>ChainedAxis</h1>
 * 
 * <p>
 * Chains two axis operations.
 * </p>
 */
public class NestedAxis extends AbstractAxis implements IAxis {

  /** Axis to test. */
  private final IAxis mInnerAxis;

  /** Test to apply to axis. */
  private final IAxis mOuterAxis;

  /**
   * Constructor initializing internal state.
   * 
   * @param innerAxis Inner nested axis.
   * @param outerAxis Outer nested axis.
   */
  public NestedAxis(final IAxis innerAxis, final IAxis outerAxis) {
    super(innerAxis.getTransaction());
    mInnerAxis = innerAxis;
    mOuterAxis = outerAxis;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {
    super.reset(nodeKey);
    if (mInnerAxis != null) {
      mInnerAxis.reset(nodeKey);
    }
    if (mOuterAxis != null) {
      mOuterAxis.reset(nodeKey);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    resetToLastKey();

    boolean hasNext = false;
    while (!(hasNext = mOuterAxis.hasNext())) {
      if (mInnerAxis.hasNext()) {
        mOuterAxis.reset(mInnerAxis.next());
      } else {
        break;
      }
    }
    if (hasNext) {
      mOuterAxis.next();
      return true;
    }

    resetToStartKey();
    return false;
  }
}
