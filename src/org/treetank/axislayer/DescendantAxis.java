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
 * $Id: DescendantAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
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
  private final FastStack<Long> mRightSiblingKeyStack;

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   */
  public DescendantAxis(final IReadTransaction rtx) {
    super(rtx);
    mRightSiblingKeyStack = new FastStack<Long>();
    mNextKey = rtx.getFirstChildKey();
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

    mRTX.moveTo(mNextKey);

    // Always follow first child if there is one.
    if (mRTX.hasFirstChild()) {
      mNextKey = mRTX.getFirstChildKey();
      if (mRTX.hasRightSibling()) {
        mRightSiblingKeyStack.push(mRTX.getRightSiblingKey());
      }
      return true;
    }

    // Then follow right sibling if there is one.
    if (mRTX.hasRightSibling()) {
      mNextKey = mRTX.getRightSiblingKey();
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
