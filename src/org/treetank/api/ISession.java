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
 * <h1>ISession</h1>
 * 
 * <p>
 * Each TreeTank file is bound to one instance implementing ISession.
 * Transactions can then be started from this instance. There can only be one
 * IWriteTransaction at the time. It can be committed or aborted through the
 * ISession.
 * </p>
 */
public interface ISession {

  /**
   * Get absolute path to TreeTank file.
   * 
   * @return Absolute path to TreeTank file.
   */
  public String getPath();

  /**
   * Tells whether the session is bound to an encrypted TreeTank file.
   * 
   * @return True if the TreeTank file is encrypted. False else.
   */
  public boolean isEncrypted();

  /**
   * Tells whether the session is bound to a checksummed TreeTank file.
   * 
   * @return True if the TreeTank file is checksummed. False else.
   */
  public boolean isChecksummed();

  /**
   * Begin a read-only transaction on the latest committed revision key.
   * 
   * @return IReadTransaction instance.
   */
  public IReadTransaction beginReadTransaction();

  /**
   * Begin a read-only transaction on the given revision key.
   * 
   * @param revisionKey Revision key to read from.
   * @return IReadTransaction instance.
   */
  public IReadTransaction beginReadTransaction(final long revisionKey);

  /**
   * Begin exclusive read/write transaction.
   * 
   * @return IWriteTransaction instance.
   */
  public IWriteTransaction beginWriteTransaction();

  /**
   * Safely close session and immediately release all resources. A session
   * can not be closed as long as there are running reading or writing
   * transactions.
   */
  public void close();

}
