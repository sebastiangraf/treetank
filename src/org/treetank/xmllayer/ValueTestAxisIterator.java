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
import org.treetank.api.IReadTransaction;
import org.treetank.utils.UTF;

/**
 * <h1>AttributeAxisIteratorTest</h1>
 * 
 * <p>
 * Iterate over all children of kind ATTRIBUTE starting at a given
 * node.
 * </p>
 */
public class ValueTestAxisIterator implements IAxisIterator {

  /** Exclusive (immutable) mTrx to iterate with. */
  private final IReadTransaction mRTX;

  /** Remember next key to visit. */
  private final IAxisIterator mAxis;

  /** Name test to do. */
  private final byte[] mValue;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) mTrx to iterate with.
   * @throws Exception of any kind.
   */
  public ValueTestAxisIterator(
      final IReadTransaction rtx,
      final IAxisIterator axis,
      final byte[] value) throws Exception {
    mRTX = rtx;
    mAxis = axis;
    mValue = value;
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) mTrx to iterate with.
   * @throws Exception of any kind.
   */
  public ValueTestAxisIterator(
      final IReadTransaction rtx,
      final IAxisIterator axis,
      final String value) throws Exception {
    this(rtx, axis, UTF.convert(value));
  }

  /**
   * {@inheritDoc}
   */
  public final boolean next() throws Exception {
    while (mAxis.next() && !(UTF.equals(mRTX.getValue(), mValue)))
      ;
    return mRTX.isSelected();
  }

}