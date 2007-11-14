/*
 * Copyright 2007, Marc Kramis
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * 
 * <p>
 * Auto commit mode is the default mode and works as follows:
 * <ul>
 *  <li>Commit when the number of modifications exceeds
 *      <code>IConstants.COMMIT_THRESHOLD</code>.</li>
 * </ul>
 * </p>
 */
public interface ISession {

  /**
   * Get file name of TreeTank file.
   * 
   * @return File name of TreeTank file.
   */
  public String getFileName();

  /**
   * Get absolute path to TreeTank file.
   * 
   * @return Absolute path to TreeTank file.
   */
  public String getAbsolutePath();

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
   * Get the major revision of the TreeTank version.
   * 
   * @return Major revision of TreeTank version.
   */
  public int getVersionMajor();

  /**
   * Get the minor revision of the TreeTank version.
   * 
   * @return Minor revision of TreeTank version.
   */
  public int getVersionMinor();

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
   * Begin exclusive read/write transaction. Uses auto commit as default.
   * 
   * @return IWriteTransaction instance.
   */
  public IWriteTransaction beginWriteTransaction();

  /**
   * Begin exclusive read/write transaction.
   * 
   * @param autoCommit True activates auto commit.
   * @return IWriteTransaction instance.
   */
  public IWriteTransaction beginWriteTransaction(final boolean autoCommit);

  /**
   * Safely close session and immediately release all resources. A session
   * can not be closed as long as there are running reading or writing
   * transactions.
   * 
   * This is an idempotent operation and does nothing if the session is
   * already closed.
   */
  public void close();

}
