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

import org.treetank.nodelayer.IReadTransaction;

/**
 * <h1>AttributeAxisIteratorTest</h1>
 * 
 * <p>
 * Iterate over all children of kind ATTRIBUTE starting at a given
 * node.
 * </p>
 */
public class AttributeAxisIterator implements IAxisIterator {

  /** Exclusive (immutable) mTrx to iterate with. */
  private final IReadTransaction mTrx;

  /** Remember next key to visit. */
  private int mNextIndex;

  /**
   * Constructor initializing internal state.
   * 
   * @param trx Exclusive (immutable) mTrx to iterate with.
   * @throws Exception of any kind.
   */
  public AttributeAxisIterator(final IReadTransaction trx) throws Exception {
    mTrx = trx;
    mNextIndex = 0;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean next() throws Exception {
    if (mNextIndex < mTrx.getAttributeCount()) {
      if (mNextIndex > 0) {
        mTrx.moveToParent();
      }
      mTrx.moveToAttribute(mNextIndex);
      mNextIndex += 1;
      return true;
    } else {
      if (mNextIndex > 0) {
        mTrx.moveToParent();
      }
      return false;
    }
  }

}
