/*
 * Copyright 2007, Marc Kramis
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
