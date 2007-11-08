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

package org.treetank.axislayer;

import org.treetank.api.IAxis;
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
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(final IAxis axis, final byte[] value) {
    super(axis.getTransaction());
    mAxis = axis;
    mValue = value;
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(final AbstractAxis axis, final String value) {
    this(axis, UTF.getBytes(value));
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(final AbstractAxis axis, final int value) {
    this(axis, UTF.getBytes(value));
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(final AbstractAxis axis, final long value) {
    this(axis, UTF.getBytes(value));
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    resetToLastKey();
    while (mAxis.hasNext()) {
      if (getTransaction().isText()
          && (UTF.equals(getTransaction().getValue(), mValue))) {
        return true;
      }
    }
    resetToStartKey();
    return false;
  }

}
