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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;

/**
 * <h1>Session</h1>
 * 
 * <p>
 * Makes sure that there only is a single session instance bound to a
 * TreeTank file. 
 * </p>
 */
public final class Session implements ISession {

  /** Central repository of all running sessions. */
  private static final Map<String, ISession> SESSION_MAP =
      new HashMap<String, ISession>();

  /** Session state. */
  private SessionState mSessionState;

  /**
   * Hidden constructor.
   * 
   * @param sessionState State assigned to session.
   */
  private Session(final SessionState sessionState) {
    mSessionState = sessionState;
  }

  /**
   * Bind new session to given TreeTank file.
   * 
   * @param file TreeTank file to bind new session to.
   * @return New session bound to given TreeTank file.
   * @throws IOException if there is a problem with opening the given file.
   */
  public static final ISession beginSession(final File file) throws IOException {
    return beginSession(file.getAbsolutePath());
  }

  /**
   * Bind new session to given TreeTank file.
   * 
   * @param path Path to TreeTank file.
   * @return New session bound to given TreeTank file.
   * @throws IOException if there is a problem with opening the given file.
   */
  public static final ISession beginSession(final String path)
      throws IOException {
    return beginSession(new SessionConfiguration(path));
  }

  /**
   * Bind new session to given TreeTank file.
   * 
   * @param sessionConfiguration Configuration of session.
   * @return New session bound to given TreeTank file.
   * @throws IOException if there is a problem with opening the given file.
   */
  public static final ISession beginSession(
      final SessionConfiguration sessionConfiguration) throws IOException {

    ISession session = null;

    synchronized (SESSION_MAP) {
      session = SESSION_MAP.get(sessionConfiguration.getAbsolutePath());
      if (session == null) {
        session = new Session(new SessionState(sessionConfiguration));
        SESSION_MAP.put(sessionConfiguration.getAbsolutePath(), session);
      } else {
        throw new IllegalStateException("There already is a session bound to "
            + sessionConfiguration.getAbsolutePath());
      }
    }

    return session;
  }

  /**
   * {@inheritDoc}
   */
  public final String getFileName() {
    return mSessionState.getSessionConfiguration().getFileName();
  }

  /**
   * {@inheritDoc}
   */
  public final String getAbsolutePath() {
    return mSessionState.getSessionConfiguration().getAbsolutePath();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isEncrypted() {
    return mSessionState.getSessionConfiguration().isEncrypted();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isChecksummed() {
    return mSessionState.getSessionConfiguration().isChecksummed();
  }

  /**
   * {@inheritDoc}
   */
  public final int getVersionMajor() {
    return mSessionState.getSessionConfiguration().getVersionMajor();
  }

  /**
   * {@inheritDoc}
   */
  public final int getVersionMinor() {
    return mSessionState.getSessionConfiguration().getVersionMinor();
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction beginReadTransaction() {
    return mSessionState.beginReadTransaction();
  }

  /**
   * {@inheritDoc}
   */
  public final IReadTransaction beginReadTransaction(final long revisionKey) {
    return mSessionState.beginReadTransaction(revisionKey);
  }

  /**
   * {@inheritDoc}
   */
  public final IWriteTransaction beginWriteTransaction() {
    return mSessionState.beginWriteTransaction();
  }

  /**
   * {@inheritDoc}
   */
  public final void close() {
    SESSION_MAP.remove(mSessionState
        .getSessionConfiguration()
        .getAbsolutePath());
    mSessionState.close();
    mSessionState = null;
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
