/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.api;


/**
 * <h1>ISession</h1>
 * 
 * <p>
 * Each IC3 file is bound to one instance implementing ISession. Transactions
 * can then be started from this instance. There can only be one
 * IWriteTransaction at the time. It can be committed or aborted through the
 * ISession.
 * </p>
 */
public interface ISession {

  /**
   * Begin a read-only transaction on the latest committed revision key.
   * 
   * @return IReadTransaction instance.
   * @throws Exception of any kind.
   */
  public IReadTransaction beginReadTransaction() throws Exception;

  /**
   * Begin a read-only transaction on the given revision key.
   * 
   * @param revisionKey Revision key to read from.
   * @return IReadTransaction instance.
   * @throws Exception of any kind or if the given revision key does not exist.
   */
  public IReadTransaction beginReadTransaction(final long revisionKey)
      throws Exception;

  /**
   * Begin exclusive read/write transaction.
   * 
   * @return IWriteTransaction instance.
   * @throws Exception of any kind.
   */
  public IWriteTransaction beginWriteTransaction() throws Exception;

  /**
   * Commit all modifications of the single IWriteTransaction.
   * 
   * @throws Exception if commit failed.
   */
  public void commit() throws Exception;

  /**
   * Abort all modifications of the single IWriteTransaction.
   * 
   * @throws Exception if abort failed.
   */
  public void abort() throws Exception;

  /**
   * Close session.
   * 
   * @throws Exception of any kind.
   */
  public void close() throws Exception;

}
