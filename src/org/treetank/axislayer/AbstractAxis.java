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

package org.treetank.axislayer;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.treetank.api.IAxis;
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
public abstract class AbstractAxis implements IAxis {

  /** Iterate over transaction exclusive to this step. */
  private final IReadTransaction mRTX;

  /** Current node returned by <code>next()</code> call. */
  private INode mCurrentNode;

  /**
   * Bind axis step to transaction.
   * 
   * @param rtx Transaction to operate with.
   */
  public AbstractAxis(final IReadTransaction rtx) {
    mRTX = rtx;
    mCurrentNode = null;
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction getTransaction() {
    return mRTX;
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
    if (mCurrentNode == null) {
      throw new NoSuchElementException();
    }
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

  /**
   * @param currentNode The currentnNode to set.
   */
  public final void setCurrentNode(final INode currentNode) {
    mCurrentNode = currentNode;
  }

  /**
   * @return The current node.
   */
  public final INode getCurrentNode() {
    return mCurrentNode;
  }

}
