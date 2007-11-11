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
 * $Id: AttributeAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
 */

package org.treetank.axislayer;

import org.treetank.api.IReadTransaction;

/**
 * <h1>AttributeAxis</h1>
 * 
 * <p>
 * Iterate over all attibutes of a given node.
 * </p>
 */
public class AttributeAxis extends AbstractAxis {

  /** Remember next key to visit. */
  private int mNextIndex;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) mTrx to iterate with.
   */
  public AttributeAxis(final IReadTransaction rtx) {
    super(rtx);
    mNextIndex = 0;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    resetToLastKey();

    if (mNextIndex > 0) {
      getTransaction().moveToParent();
    }

    if (mNextIndex < getTransaction().getAttributeCount()) {
      getTransaction().moveToAttribute(mNextIndex);
      mNextIndex += 1;
      return true;
    } else {
      resetToStartKey();
      return false;
    }
  }

}
