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
 * $Id: ParentAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
 */

package org.treetank.xmllayer;

import java.util.Iterator;

import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;

/**
 * <h1>AbstractAxisStep</h1>
 * 
 * <p>
 * Provide standard Java iterator capability compatible with the new
 * enhanced for loop available since Java 5.
 * </p>
 */
public abstract class Axis
    implements
    Iterable<INode>,
    Iterator<INode> {

  /** Iterate over transaction exclusive to this step. */
  protected final IReadTransaction mRTX;

  /** Current node returned by <code>next()</code> call. */
  protected INode mCurrentNode;

  /**
   * Bind axis step to transaction.
   * 
   * @param rtx Transaction to operate with.
   */
  public Axis(final IReadTransaction rtx) {
    mRTX = rtx;
    mCurrentNode = null;
  }

  /**
   * {@inheritDoc}
   */
  public final Iterator<INode> iterator() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  public final INode next() {
    return mCurrentNode;
  }

  /**
   * {@inheritDoc}
   */
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public abstract boolean hasNext();

}
