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
 * $Id: ValueTestAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
 */

package org.treetank.xmllayer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.UTF;

/**
 * <h1>ValueTestAxis</h1>
 * 
 * <p>
 * Iterate over all children of kind ATTRIBUTE starting at a given
 * node.
 * </p>
 */
public class ValueTestAxis extends AbstractAxis {

  /** Remember next key to visit. */
  private final IAxis mAxis;

  /** Name test to do. */
  private final byte[] mValue;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) mTrx to iterate with.
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(
      final IReadTransaction rtx,
      final IAxis axis,
      final byte[] value) {
    super(rtx);
    mAxis = axis;
    mValue = value;
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) mTrx to iterate with.
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(
      final IReadTransaction rtx,
      final AbstractAxis axis,
      final String value) {
    this(rtx, axis, UTF.convert(value));
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    while (mAxis.hasNext()
        && (mAxis.next().isText())
        & !(UTF.equals(mAxis.next().getValue(), mValue))) {
      // Nothing to do.
    }
    if (mRTX.isSelected()) {
      mCurrentNode = mRTX.getNode();
      return true;
    } else {
      mCurrentNode = null;
      return false;
    }
  }

}
