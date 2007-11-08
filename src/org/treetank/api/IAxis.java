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
 * $Id: INode.java 3277 2007-10-25 19:30:30Z kramis $
 */

package org.treetank.api;

import java.util.Iterator;

/**
 * <h1>IAxis</h1>
 * 
 * <p>
 * Interface to iterate over the TreeTank according to an iteration logic.
 * All implementations must comply with the following:
 * <li>next() must be called exactly once after hasNext() yields true.</li>
 * <li>after hasNext() is false, the transaction points to the node where
 *     it started</li>
 * <li>before each hasNext(), the cursor is guaranteed to point to the last
 *     node found with hasNext().</li>
 * </p>
 * <p>
 * This behavior can be achieved by:
 * <li>Always call super.hasNext() as the first thing in hasNext().</li>
 * <li>Always call reset() before return false in hasNext().</li> 
 * </p>
 */
public interface IAxis extends Iterator<Long>, Iterable<Long> {

  /**
   * Access transaction to which this axis is bound.
   * 
   * @return Transaction to which this axis is bound.
   */
  public IReadTransaction getTransaction();

}
