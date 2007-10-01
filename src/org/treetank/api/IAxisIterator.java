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

package org.treetank.api;

/**
 * <h1>IAxisIterator</h1>
 * 
 * <p>
 * The axis iterator interactively iterates over the TreeTank using
 * an IReadTransaction instance. It assumes, that the cursor is not modified
 * or moved by another thread during an iteration.
 * </p>
 */
public interface IAxisIterator {

  /**
   * Iterate to next node if there is one.
   * 
   * @return Key of node if there is one or NULL_KEY if the
   * iteration is finished.
   * @throws Exception of any kind.
   */
  public boolean next() throws Exception;

}
