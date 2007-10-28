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
 * $Id: NameTestAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
 */

package org.treetank.axislayer;

import org.treetank.api.IAxis;

/**
 * <h1>NameTestAxis</h1>
 * 
 * <p>
 * Find all ELEMENTS provided by some axis iterator that match a given name.
 * Note that this is efficiently done with a single String and multiple integer
 * comparisons.
 * </p>
 */
public class NameTestAxis extends AbstractAxis {

  /** Remember next key to visit. */
  private final IAxis mAxis;

  /** Name test to do. */
  private final int mNameKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis iterator providing ELEMENTS.
   * @param name Name ELEMENTS must match to.
   */
  public NameTestAxis(final IAxis axis, final String name) {
    super(axis.getTransaction());
    mAxis = axis;
    mNameKey = getTransaction().keyForName(name);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    while ((mAxis.hasNext()) && !(mAxis.next().getLocalPartKey() == mNameKey)) {
      // Nothing to do here.
    }
    if (getTransaction().isSelected()) {
      setCurrentNode(getTransaction().getNode());
      return true;
    } else {
      setCurrentNode(null);
      return false;
    }
  }

}
