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
 * $Id$
 */

package org.treetank.xmllayer;

import org.treetank.api.IAxisIterator;
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastStack;

/**
 * <h1>DescendantAxisIterator</h1>
 * 
 * <p>
 * Iterate over all descendants of kind ELEMENT or TEXT starting at a given
 * node. Self is not included.
 * </p>
 */
@Deprecated
public class DescendantAxisIterator implements IAxisIterator {

  /** Exclusive (immutable) trx to iterate with. */
  private final IReadTransaction mRTX;

  /** Stack for remembering next nodeKey in document order. */
  private final FastStack<Long> mRightSiblingKeyStack;

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   */
  public DescendantAxisIterator(final IReadTransaction rtx) {

    // Init members.
    mRTX = rtx;
    mRightSiblingKeyStack = new FastStack<Long>();

    // Find delimiter nodeKey.
    final long currentKey = mRTX.getNodeKey();
    while ((mRTX.getRightSiblingKey() == IConstants.NULL_KEY)
        && (mRTX.getParentKey() != IConstants.NULL_KEY)) {
      mRTX.moveToParent();
    }
    mRightSiblingKeyStack.push(mRTX.getRightSiblingKey());
    mRTX.moveTo(currentKey);

    mNextKey = mRTX.getFirstChildKey();

  }

  /**
   * {@inheritDoc}
   */
  public final boolean next() {

    if (mRTX.moveTo(mNextKey) != null
        && mRTX.getNodeKey() != mRightSiblingKeyStack.get(0)) {

      // Always follow first child if there is one.
      if (mRTX.getFirstChildKey() != IConstants.NULL_KEY) {
        mNextKey = mRTX.getFirstChildKey();
        if (mRTX.getRightSiblingKey() != IConstants.NULL_KEY) {
          mRightSiblingKeyStack.push(mRTX.getRightSiblingKey());
        }
        return true;
      }

      // Then follow right sibling if there is one.
      if (mRTX.getRightSiblingKey() != IConstants.NULL_KEY) {
        mNextKey = mRTX.getRightSiblingKey();
        return true;
      }

      // Then follow right sibling on stack.
      mNextKey = mRightSiblingKeyStack.pop();
      return true;

    } else {
      return false;
    }
  }

}
