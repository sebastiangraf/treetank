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

import java.util.Iterator;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>AbstractAxis</h1>
 * 
 * <p>
 * Provide standard Java iterator capability compatible with the new
 * enhanced for loop available since Java 5.
 * </p>
 * 
 * <p>
 * All implementations must make sure to call super.hasNext() as the first
 * thing in hasNext().
 * </p>
 * 
 * <p>
 * All users must make sure to call next() after hasNext() evaluated to true.
 * </p>
 */
public abstract class AbstractAxis implements IAxis {

  /** Iterate over transaction exclusive to this step. */
  private final IReadTransaction mRTX;

  /** Key of last found node. */
  private long mKey;

  /** Make sure next() can only be called after hasNext(). */
  private boolean mNext;

  /** Key of node where axis started. */
  private final long mStartKey;

  /**
   * Bind axis step to transaction.
   * 
   * @param rtx Transaction to operate with.
   */
  public AbstractAxis(final IReadTransaction rtx) {
    mRTX = rtx;
    mStartKey = mRTX.getNodeKey();
    mKey = mStartKey;
    mNext = false;
  }

  /**
   * {@inheritDoc}
   */
  public final Iterator<Long> iterator() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public final Long next() {
    if (!mNext) {
      throw new IllegalStateException(
          "IAxis.next() must be called exactely once after hasNext()"
              + " evaluated to true.");
    }
    mKey = mRTX.getNodeKey();
    mNext = false;
    return mKey;
  }

  /**
   * {@inheritDoc}
   */
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction getTransaction() {
    return mRTX;
  }

  /**
   * Make sure the transaction points to the node it started with. This must
   * be called just before hasNext() == false.
   * 
   * @return Key of node where transaction was before the first call of
   *         hasNext().
   */
  public final long resetToStartKey() {
    mRTX.moveTo(mStartKey);
    mNext = false;
    return mStartKey;
  }

  /**
   * Make sure the transaction points to the node after the last hasNext().
   * This must be called first in hasNext().
   * 
   * @return Key of node where transaction was after the last call of
   *         hasNext().
   */
  public final long resetToLastKey() {
    mRTX.moveTo(mKey);
    mNext = true;
    return mKey;
  }

  /**
   * {@inheritDoc}
   */
  public abstract boolean hasNext();

}
