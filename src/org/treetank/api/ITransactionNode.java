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
 * $Id: INode.java 2987 2007-10-01 12:58:09Z kramis $
 */

package org.treetank.api;

/**
 * <h1>ITransactionNode</h1>
 * 
 * Bind an INode to a transaction.
 */
public interface ITransactionNode {

  /**
   * Set transaction to bind node to it.
   * 
   * @param rtx Transaction to bind node to.
   */
  public void setTransaction(final IReadTransaction rtx);

}
