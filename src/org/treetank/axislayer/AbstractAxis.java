/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: ParentAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
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
