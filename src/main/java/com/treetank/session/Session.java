/*
q * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: Session.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.session;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.settings.EStoragePaths;
import com.treetank.utils.ItemList;

/**
 * <h1>Session</h1>
 * 
 * <p>
 * Makes sure that there only is a single session instance bound to a TreeTank
 * file.
 * </p>
 */
public final class Session implements ISession {

    /** Central repository of all running sessions. */
    private static final ConcurrentMap<File, ISession> SESSION_MAP = new ConcurrentHashMap<File, ISession>();

    /** Session state. */
    private SessionState mSessionState;

    /** Was session closed? */
    private boolean mClosed;

    /**
     * Hidden constructor.
     * 
     * @param sessionState
     *            State assigned to session.
     */
    private Session(final SessionState sessionState) {
        mSessionState = sessionState;
        mClosed = false;
    }

    /**
     * Bind new session to given TreeTank file.
     * 
     * @param file
     *            TreeTank file to bind new session to.
     * @return New session bound to given TreeTank file.
     * @throws TreetankException
     *             if a session was already bound or the persistent storage is
     *             not accessable
     */
    public static ISession beginSession(final File file)
            throws TreetankException {
        return beginSession(new SessionConfiguration(file));
    }

    /**
     * Bind new session to given TreeTank file.
     * 
     * @param path
     *            Path to TreeTank file.
     * @return New session bound to given TreeTank file.
     * @throws TreetankException
     *             if a session was already bound or the persistent storage is
     *             not accessable
     */
    public static ISession beginSession(final String path)
            throws TreetankException {
        return beginSession(new SessionConfiguration(new File(path)));
    }

    /**
     * Bind new session to given TreeTank file.
     * 
     * @param sessionConfiguration
     *            Configuration of session.
     * @return New session bound to given TreeTank file.
     * @throws TreetankException
     *             if a session was already bound or the persistent storage is
     *             not accessable
     */
    public static ISession beginSession(
            final SessionConfiguration sessionConfiguration)
            throws TreetankException {

        if (SESSION_MAP.putIfAbsent(sessionConfiguration.getFile(),
                new Session(new SessionState(sessionConfiguration))) != null) {
            throw new TreetankUsageException(new StringBuilder(
                    "There was already a session bound to ").append(
                    sessionConfiguration.getFile().getAbsolutePath())
                    .toString());
        }

        return SESSION_MAP.get(sessionConfiguration.getFile());
    }

    /**
     * Removes the specified TreeTank file.
     * 
     * @param file
     *            TreeTank file to remove.
     */
    public static void removeSession(final File file) throws TreetankException {
        synchronized (SESSION_MAP) {
            ISession session = SESSION_MAP.get(file);
            if (session == null) {
                if (file.exists() && !EStoragePaths.recursiveDelete(file)) {
                    throw new TreetankIOException(new StringBuilder(
                            "Could not delete file '").append(file).append("'")
                            .toString());
                }
            } else {
                throw new TreetankUsageException(new StringBuilder(
                        "There already is a session bound to '").append(file)
                        .append("'").toString());
            }
        }
    }

    /**
     * Removes the specified TreeTank file.
     * 
     * @param path
     *            TreeTank file to remove.
     */
    public static void removeSession(final String path)
            throws TreetankException {
        removeSession(new File(path));
    }

    /**
     * Closing all open Sessions.
     * 
     * @throws TreetankException
     *             if something weird happen
     */
    public static void closeSession(final File file) throws TreetankException {
        synchronized (SESSION_MAP) {
            final ISession session = SESSION_MAP.remove(file);
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Closing all open Sessions.
     * 
     * @throws TreetankException
     *             if something weird happen
     */
    public static void closeSession(final String path) throws TreetankException {
        closeSession(new File(path));
    }

    /**
     * {@inheritDoc}
     */
    public File getFile() {
        assertNotClosed();
        return mSessionState.getSessionConfiguration().getFile();
    }

    /**
     * {@inheritDoc}
     */
    public String getAbsolutePath() {
        assertNotClosed();
        return mSessionState.getSessionConfiguration().getFile()
                .getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    public long getVersionMajor() {
        assertNotClosed();
        return mSessionState.getVersionMajor();
    }

    /**
     * {@inheritDoc}
     */
    public long getVersionMinor() {
        assertNotClosed();
        return mSessionState.getVersionMinor();
    }

    /**
     * {@inheritDoc}
     */
    public IReadTransaction beginReadTransaction() throws TreetankException {
        assertNotClosed();
        return mSessionState.beginReadTransaction(new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    public IReadTransaction beginReadTransaction(final long revisionKey)
            throws TreetankException {
        assertNotClosed();
        return mSessionState.beginReadTransaction(revisionKey, new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    public IWriteTransaction beginWriteTransaction() throws TreetankException {
        assertNotClosed();
        return mSessionState.beginWriteTransaction(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    public IWriteTransaction beginWriteTransaction(final int maxNodeCount,
            final int maxTime) throws TreetankException {
        assertNotClosed();
        return mSessionState.beginWriteTransaction(maxNodeCount, maxTime);
    }

    /**
     * {@inheritDoc}
     */
    public int getReadTransactionCount() {
        return mSessionState.getReadTransactionCount();
    }

    /**
     * {@inheritDoc}
     */
    public int getWriteTransactionCount() {
        return mSessionState.getWriteTransactionCount();
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws TreetankException {
        if (!mClosed) {
            SESSION_MAP.remove(mSessionState.getSessionConfiguration()
                    .getFile());
            mSessionState.close();
            mSessionState = null;
            mClosed = true;
        }
    }

    /**
     * Required to close file handle.
     * 
     * @throws Throwable
     *             if the finalization of the superclass does not work.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Make sure that the session is not yet closed when calling this method.
     */
    private void assertNotClosed() {
        if (mClosed) {
            throw new IllegalStateException("Session is already closed.");
        }
    }

    public String toString() {
        return new StringBuilder("Session: ").append(
                mSessionState.getSessionConfiguration().getFile()).toString();

    }

}
