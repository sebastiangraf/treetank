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

  /** Parent axis. */
  private final IAxis mParentAxis;

  /** Child axis to apply to each node found with parent axis. */
  private final IAxis mChildAxis;

  /** Is it the first run of parent axis? */
  private boolean mIsFirst;

  /**
   * Constructor initializing internal state.
   * 
   * @param parentAxis Inner nested axis.
   * @param childAxis Outer nested axis.
   */
  public NestedAxis(final IAxis parentAxis, final IAxis childAxis) {
    super(parentAxis.getTransaction());
    mParentAxis = parentAxis;
    mChildAxis = childAxis;
    mIsFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {
    super.reset(nodeKey);
    if (mParentAxis != null) {
      mParentAxis.reset(nodeKey);
    }
    if (mChildAxis != null) {
      mChildAxis.reset(nodeKey);
    }
    mIsFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    resetToLastKey();

    // Make sure that parent axis is moved for the first time.
    if (mIsFirst) {
      mIsFirst = false;
      if (mParentAxis.hasNext()) {
        mChildAxis.reset(mParentAxis.next());
      } else {
        resetToStartKey();
        return false;
      }
    }

    // Execute child axis for each node found with parent axis.
    boolean hasNext = false;
    while (!(hasNext = mChildAxis.hasNext())) {
      if (mParentAxis.hasNext()) {
        mChildAxis.reset(mParentAxis.next());
      } else {
        break;
      }
    }
    if (hasNext) {
      mChildAxis.next();
      return true;
    }

    resetToStartKey();
    return false;
  }
}
