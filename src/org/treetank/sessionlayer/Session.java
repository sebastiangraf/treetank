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
 * $Id:Session.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import java.io.File;
import java.util.logging.Logger;

import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;

/**
 * <h1>Session</h1>
 * 
 * <p>
 * Singelton per TreeTank file. Manages transaction handling and links
 * to page layer. Multiple threads can concurrently create
 * transactions on this object but only one IWriteTransaction can exist at
 * any time.
 * </p>
 */
public final class Session implements ISession {

  /** Logger. */
  private static final Logger LOGGER =
      Logger.getLogger(Session.class.getName());

  /** Session configuration. */
  private final SessionState mSessionState;

  public Session(final File file) throws Exception {
    this(new SessionConfiguration(file.getAbsolutePath()));
  }

  /**
   * Convenient constructor.
   * 
   * @param path Path to TreeTank file.
   * @throws Exception of any kind.
   */
  public Session(final String path) throws Exception {
    this(new SessionConfiguration(new File(path).getAbsolutePath()));
  }

  /**
   * Constructor to bind to a TreeTank file.
   * 
   * <p>
   * The beacon logic works as follows:
   * 
   * <ol>
   * <li><code>Primary beacon == secondary beacon</code>: OK.</li>
   * <li><code>Primary beacon != secondary beacon</code>: try to recover...
   *    <ol type="i">
   *    <li><code>Checksum(uberpage) == primary beacon</code>:
   *        truncate file and write secondary beacon - OK.</li>
   *    <li><code>Checksum(uberpage) == secondary beacon</code>:
   *        write primary beacon - OK.</li>
   *    <li><code>Checksum(uberpage) != secondary beacon 
   *        != primary beacon</code>: NOK.</li>
   *    </ol>
   * </li>
   * </ol>
   * </p>
   * 
   * @param sessionConfiguration Session configuration for the TreeTank.
   * @throws Exception of any kind.
   */
  public Session(final SessionConfiguration sessionConfiguration)
      throws Exception {

    mSessionState = new SessionState(sessionConfiguration);

  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction beginReadTransaction() throws Exception {
    return mSessionState.beginReadTransaction();
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction beginReadTransaction(final long revisionKey)
      throws Exception {

    return mSessionState.beginReadTransaction(revisionKey);
  }

  /**
   * {@inheritDoc}
   */
  public final IWriteTransaction beginWriteTransaction() throws Exception {

    return mSessionState.beginWriteTransaction();
  }

  /**
   * {@inheritDoc}
   */
  public final void close() throws Exception {
    mSessionState.close();
  }

  /**
   * Required to close file handle.
   * 
   * @throws Throwable if the finalization of the superclass does not work.
   */
  protected void finalize() throws Throwable {
    try {
      close();
    } finally {
      super.finalize();
    }
  }

}
