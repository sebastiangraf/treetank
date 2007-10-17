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

import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>IPage</h1>
 * 
 * <p>
 * Each page must implement this interface to allow recursive commit on all
 * dirty pages and to recursively serialize all dirty pages.
 * </p>
 */
public interface IPage {

  /**
   * Is the page dirty?
   * 
   * @return True if the page is dirty and must be committed, false else.
   */
  public boolean isDirty();

  /**
   * Recursively call commit on all referenced pages.
   * 
   * @param state IWriteTransaction state.
   * @throws Exception occurring during commit operation.
   */
  public void commit(final IWriteTransactionState state) throws Exception;

  /**
   * Serialize self into object output stream.
   * 
   * @param out Object output stream.
   * @throws Exception if the stream encounters an error.
   */
  public void serialize(final FastByteArrayWriter out) throws Exception;

}
