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
 * <h1>ChildAxisIteratorTest</h1>
 * 
 * <p>
 * Iterate over all children of kind ELEMENT or TEXT starting at a given
 * node. Self is not included.
 * </p>
 */
public class ChildAxisIterator implements IAxisIterator {

  /** Exclusive (immutable) trx to iterate with. */
  private final IReadTransaction trx;

  /** Has another child node. */
  private long nextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param initTrx Exclusive (immutable) trx to iterate with.
   * @throws Exception of any kind.
   */
  public ChildAxisIterator(final IReadTransaction initTrx) throws Exception {
    trx = initTrx;
    nextKey = trx.getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean next() throws Exception {
    if (trx.moveTo(nextKey)) {
      nextKey = trx.getRightSiblingKey();
      return true;
    } else {
      return false;
    }
  }

}
