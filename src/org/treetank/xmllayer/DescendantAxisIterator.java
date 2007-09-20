/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.xmllayer;

import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastLongStack;
import org.treetank.utils.IConstants;

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
