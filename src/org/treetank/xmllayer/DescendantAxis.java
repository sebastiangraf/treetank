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

package org.treetank.xmllayer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
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

    // Find delimiter nodeKey.
    final long currentKey = rtx.getNodeKey();
    while ((rtx.getRightSiblingKey() == IConstants.NULL_KEY)
        && (rtx.getParentKey() != IConstants.NULL_KEY)) {
      rtx.moveToParent();
    }
    mRightSiblingKeyStack.push(rtx.getRightSiblingKey());
    rtx.moveTo(currentKey);

    mNextKey = rtx.getFirstChildKey();

  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    final INode node = mRTX.moveTo(mNextKey);
    if ((node != null) && (node.getNodeKey() != mRightSiblingKeyStack.get(0))) {

      // Always follow first child if there is one.
      if (node.getFirstChildKey() != IConstants.NULL_KEY) {
        mNextKey = node.getFirstChildKey();
        if (node.getRightSiblingKey() != IConstants.NULL_KEY) {
          mRightSiblingKeyStack.push(node.getRightSiblingKey());
        }
        mCurrentNode = node;
        return true;
      }

      // Then follow right sibling if there is one.
      if (node.getRightSiblingKey() != IConstants.NULL_KEY) {
        mNextKey = node.getRightSiblingKey();
        mCurrentNode = node;
        return true;
      }

      // Then follow right sibling on stack.
      mNextKey = mRightSiblingKeyStack.pop();
      mCurrentNode = node;
      return true;

    } else {
      mCurrentNode = null;
      return false;
    }
  }

}
