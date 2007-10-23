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
 * $Id: ChildAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
 */

package org.treetank.xmllayer;

import org.treetank.api.IReadTransaction;

/**
 * <h1>ChildAxisIterator</h1>
 * 
 * <p>
 * Iterate over all children of kind ELEMENT or TEXT starting at a given
 * node. Self is not included.
 * </p>
 */
public class ChildAxis extends AbstractAxis {

  /** Has another child node. */
  private long mNextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   */
  public ChildAxis(final IReadTransaction rtx) {
    super(rtx);
    mNextKey = rtx.getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    if (mRTX.moveTo(mNextKey)) {
      mNextKey = mRTX.getRightSiblingKey();
      mCurrentNode = mRTX.getNode();
      return true;
    } else {
      mCurrentNode = null;
      return false;
    }
  }

}
