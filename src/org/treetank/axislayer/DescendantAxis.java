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

import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastStack;

/**
 * <h1>DescendantAxis</h1>
 * 
 * <p>
 * Iterate over all descendants of kind ELEMENT or TEXT starting at a given
 * node. Self is not included.
 * </p>
 */
public class DescendantAxis extends AbstractAxis {

  /** Stack for remembering next nodeKey in document order. */
  private FastStack<Long> mRightSiblingKeyStack;

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   */
  public DescendantAxis(final IReadTransaction rtx) {
    super(rtx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {
    super.reset(nodeKey);
    mRightSiblingKeyStack = new FastStack<Long>();
    mNextKey = getTransaction().getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    resetToLastKey();

    // Fail if there is no node anymore.
    if (mNextKey == IConstants.NULL_KEY) {
      resetToStartKey();
      return false;
    }

    getTransaction().moveTo(mNextKey);

    // Always follow first child if there is one.
    if (getTransaction().hasFirstChild()) {
      mNextKey = getTransaction().getFirstChildKey();
      if (getTransaction().hasRightSibling()) {
        mRightSiblingKeyStack.push(getTransaction().getRightSiblingKey());
      }
      return true;
    }

    // Then follow right sibling if there is one.
    if (getTransaction().hasRightSibling()) {
      mNextKey = getTransaction().getRightSiblingKey();
      return true;
    }

    // Then follow right sibling on stack.
    if (mRightSiblingKeyStack.size() > 0) {
      mNextKey = mRightSiblingKeyStack.pop();
      return true;
    }

    // Then end.
    mNextKey = IConstants.NULL_KEY;
    return true;
  }

}
