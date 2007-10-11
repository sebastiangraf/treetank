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
  private final IReadTransaction mRTX;

  /** Has another child node. */
  private long mNextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   * @throws Exception of any kind.
   */
  public ChildAxisIterator(final IReadTransaction rtx) throws Exception {
    mRTX = rtx;
    mNextKey = mRTX.getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean next() throws Exception {
    if (mRTX.moveTo(mNextKey)) {
      mNextKey = mRTX.getRightSiblingKey();
      return true;
    } else {
      return false;
    }
  }

}
