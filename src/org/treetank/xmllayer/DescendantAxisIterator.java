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
import org.treetank.utils.FastLongStack;

/**
 * <h1>DescendantAxisIterator</h1>
 * 
 * <p>
 * Iterate over all descendants of kind ELEMENT or TEXT starting at a given
 * node. Self is not included.
 * </p>
 */
public class DescendantAxisIterator implements IAxisIterator {

  /** Exclusive (immutable) trx to iterate with. */
  private final IReadTransaction trx;

  /** Stack for remembering next nodeKey in document order. */
  private final FastLongStack rightSiblingKeyStack;

  /** The nodeKey of the next node to visit. */
  private long nextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param initTrx Exclusive (immutable) trx to iterate with.
   * @throws Exception of any kind.
   */
  public DescendantAxisIterator(final IReadTransaction initTrx)
      throws Exception {

    // Init members.
    trx = initTrx;
    rightSiblingKeyStack = new FastLongStack();

    // Find delimiter nodeKey.
    final long currentKey = trx.getNodeKey();
    while ((trx.getRightSiblingKey() == IConstants.NULL_KEY)
        && (trx.getParentKey() != IConstants.NULL_KEY)) {
      trx.moveToParent();
    }
    rightSiblingKeyStack.push(trx.getRightSiblingKey());
    trx.moveTo(currentKey);

    nextKey = trx.getFirstChildKey();

  }

  /**
   * {@inheritDoc}
   */
  public final boolean next() throws Exception {

    if (trx.moveTo(nextKey) && trx.getNodeKey() != rightSiblingKeyStack.get(0)) {

      // Always follow first child if there is one.
      if (trx.getFirstChildKey() != IConstants.NULL_KEY) {
        nextKey = trx.getFirstChildKey();
        if (trx.getRightSiblingKey() != IConstants.NULL_KEY) {
          rightSiblingKeyStack.push(trx.getRightSiblingKey());
        }
        return true;
      }

      // Then follow right sibling if there is one.
      if (trx.getRightSiblingKey() != IConstants.NULL_KEY) {
        nextKey = trx.getRightSiblingKey();
        return true;
      }

      // Then follow right sibling on stack.
      nextKey = rightSiblingKeyStack.pop();
      return true;

    } else {
      return false;
    }
  }

}
