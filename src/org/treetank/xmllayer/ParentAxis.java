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

package org.treetank.xmllayer;

import org.treetank.api.IReadTransaction;

/**
 * <h1>ParentAxis</h1>
 * 
 * <p>
 * Iterate to parent node starting at a given node. Self is not included.
 * </p>
 */
public class ParentAxis extends Axis {

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

  /** Track number of calls of next. */
  private boolean mIsFirstNext;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   */
  public ParentAxis(final IReadTransaction rtx) {
    super(rtx);
    mIsFirstNext = true;
    mNextKey = rtx.getParentKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    mCurrentNode = mRTX.moveTo(mNextKey);
    if (mIsFirstNext && (mCurrentNode != null)) {
      mIsFirstNext = false;
      return true;
    } else {
      return false;
    }
  }

}
