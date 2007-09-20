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

/**
 * <h1>AttributeAxisIteratorTest</h1>
 * 
 * <p>
 * Iterate over all children of kind ATTRIBUTE starting at a given
 * node.
 * </p>
 */
public class NameTestAxisIterator implements IAxisIterator {

  /** Exclusive (immutable) mTrx to iterate with. */
  private final IReadTransaction mTrx;

  /** Remember next key to visit. */
  private final IAxisIterator mAxis;

  /** Name test to do. */
  private final int mNameKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param trx Exclusive (immutable) mTrx to iterate with.
   * @throws Exception of any kind.
   */
  public NameTestAxisIterator(
      final IReadTransaction trx,
      final IAxisIterator axis,
      final String name) throws Exception {
    mTrx = trx;
    mAxis = axis;
    mNameKey = mTrx.keyForName(name);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean next() throws Exception {
    while (mAxis.next() && !(mTrx.getLocalPartKey() == mNameKey))
      ;
    return mTrx.isSelected();
  }

}
