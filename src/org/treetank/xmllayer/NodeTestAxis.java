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
 * $Id: NodeTestAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
 */

package org.treetank.xmllayer;

import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;

/**
 * <h1>NodeTestAxisIterator</h1>
 * 
 * <p>
 * Only select nodes of kind ELEMENT and TEXT.
 * </p>
 */
public class NodeTestAxis extends Axis {

  /** Remember next key to visit. */
  private final Axis mAxis;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   * @param axis Axis to iterate over.
   */
  public NodeTestAxis(
      final IReadTransaction rtx,
      final Axis axis) {
    super(rtx);
    mAxis = axis;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    // TODO The double next() call works but is not Iterator conformant.
    if (mAxis.hasNext()
        && mAxis.next().getKind() == IConstants.ELEMENT
        || mAxis.next().getKind() == IConstants.TEXT) {
      mCurrentNode = mRTX.getNode();
      return true;
    } else {
      mCurrentNode = null;
      return false;
    }
  }

}
