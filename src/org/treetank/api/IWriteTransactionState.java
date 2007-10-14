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
 * $Id: IReadTransaction.java 2987 2007-10-01 12:58:09Z kramis $
 */

package org.treetank.api;

import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageWriter;

/**
 * <h1>IWriteTransactionState</h1>
 * 
 * <p>
 * Interface to access the state of a IReadTransaction.
 * </p>
 */
public interface IWriteTransactionState extends IReadTransactionState {

  /**
   * Get the page writer used within this transaction.
   * 
   * @return PageWriter instance.
   */
  public PageWriter getPageWriter();

  public NodePage prepareNodePage(final long nodePageKey) throws Exception;

}
