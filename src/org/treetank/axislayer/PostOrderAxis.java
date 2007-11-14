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
 * <h1>PostOrder</h1>
 * 
 * <p>
 * Iterate over the whole tree starting with the last node.
 * </p>
 */
public class PostOrderAxis extends AbstractAxis {

  /** For remembering last parent. */
  private final FastStack<Long> mLastParent;

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *            Exclusive (immutable) trx to iterate with.
   * @param startAtBeginning
   *            Starting at the beginning of the tree and though just
   *            traversing the whole tree..No, the root is not the start!
   */
  public PostOrderAxis(
      final IReadTransaction rtx,
      final boolean startAtBeginning) {
    super(rtx);
    mLastParent = new FastStack<Long>();
    mLastParent.push(IConstants.NULL_KEY);
    mNextKey = rtx.getNodeKey();
    if (startAtBeginning) {
      startAtBeginning();
    }

  }

  /**
   * Method to start at the beginning of the tree.
   */
  private final void startAtBeginning() {
    getTransaction().moveToDocumentRoot();
    while (getTransaction().hasFirstChild()) {
      mLastParent.push(getTransaction().getNodeKey());
      getTransaction().moveToFirstChild();

      mNextKey = getTransaction().getNodeKey();
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasNext() {
    resetToLastKey();
    long key = getTransaction().moveTo(mNextKey);
    if (getTransaction().isSelected()) {
      while (getTransaction().hasFirstChild() && key != mLastParent.peek()) {
        mLastParent.push(key);
        key = getTransaction().moveToFirstChild();
      }
      if (key == mLastParent.peek()) {
        mLastParent.pop();
      }

      if (getTransaction().hasRightSibling()) {
        mNextKey = getTransaction().getRightSiblingKey();

      } else {
        mNextKey = mLastParent.peek();
      }

      return true;

    } else {
      resetToStartKey();
      return false;
    }
  }

}
