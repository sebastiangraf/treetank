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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.io.TreetankIOException;
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
    private static final Map<String, ISession> SESSION_MAP = new HashMap<String, ISession>();

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
     * @throws IOException
     *             if there is a problem with opening the given file.
     */
    public static final ISession beginSession(final File file)
            throws IOException {
        return beginSession(file.getAbsolutePath());
    }

    /**
     * Bind new session to given TreeTank file.
     * 
     * @param path
     *            Path to TreeTank file.
     * @return New session bound to given TreeTank file.
     */
    public static final ISession beginSession(final String path) {
        return beginSession(new SessionConfiguration(path));
    }

    /**
     * Bind new session to given TreeTank file.
     * 
     * @param sessionConfiguration
     *            Configuration of session.
     * @return New session bound to given TreeTank file.
     */
    public static final ISession beginSession(
            final SessionConfiguration sessionConfiguration) {

        ISession session = null;

        synchronized (SESSION_MAP) {
            session = SESSION_MAP.get(sessionConfiguration.getAbsolutePath());
            if (session == null) {
                try {
                    session = new Session(
                            new SessionState(sessionConfiguration));
                } catch (TreetankIOException e) {
                    throw new RuntimeException(e);

                }
                SESSION_MAP
                        .put(sessionConfiguration.getAbsolutePath(), session);
            } else {
                throw new IllegalStateException(
                        "There already is a session bound to "
                                + sessionConfiguration.getAbsolutePath());
            }
        }

        return session;
    }

    /**
     * Removes the specified TreeTank file.
     * 
     * @param file
     *            TreeTank file to remove.
     */
    public static final void removeSession(final File file) {
        removeSession(file.getAbsolutePath());
    }

    /**
     * Removes the specified TreeTank file.
     * 
     * @param path
     *            TreeTank file to remove.
     */
    public static final void removeSession(final String path) {
        synchronized (SESSION_MAP) {
            ISession session = SESSION_MAP.get(path);
            if (session == null) {
                if (new File(path).exists() && !recursiveDelete(new File(path))) {
                    throw new RuntimeException("Could not delete file '" + path
                            + "'");
                }
            } else {
                throw new IllegalStateException(
                        "There already is a session bound to '" + path + "'");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public final String getFileName() {
        assertNotClosed();
        return mSessionState.getSessionConfiguration().getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    public final String getAbsolutePath() {
        assertNotClosed();
        return mSessionState.getSessionConfiguration().getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isEncrypted() {
        assertNotClosed();
        return mSessionState.getSessionConfiguration().isEncrypted();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isChecksummed() {
        assertNotClosed();
        return mSessionState.getSessionConfiguration().isChecksummed();
    }

    /**
     * {@inheritDoc}
     */
    public final long getVersionMajor() {
        assertNotClosed();
        return mSessionState.getVersionMajor();
    }

    /**
     * {@inheritDoc}
     */
    public final long getVersionMinor() {
        assertNotClosed();
        return mSessionState.getVersionMinor();
    }

    /**
     * {@inheritDoc}
     */
    public final IReadTransaction beginReadTransaction() {
        assertNotClosed();
        return mSessionState.beginReadTransaction(new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    public final IReadTransaction beginReadTransaction(final long revisionKey) {
        assertNotClosed();
        return mSessionState.beginReadTransaction(revisionKey, new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    public final IWriteTransaction beginWriteTransaction()
            throws TreetankIOException {
        assertNotClosed();
        return mSessionState.beginWriteTransaction(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    public final IWriteTransaction beginWriteTransaction(
            final int maxNodeCount, final int maxTime)
            throws TreetankIOException {
        assertNotClosed();
        return mSessionState.beginWriteTransaction(maxNodeCount, maxTime);
    }

    /**
     * {@inheritDoc}
     */
    public final int getReadTransactionCount() {
        return mSessionState.getReadTransactionCount();
    }

    /**
     * {@inheritDoc}
     */
    public final int getWriteTransactionCount() {
        return mSessionState.getWriteTransactionCount();
    }

    /**
     * {@inheritDoc}
     */
    public final void close() {
        if (!mClosed) {
            synchronized (SESSION_MAP) {
                SESSION_MAP.remove(mSessionState.getSessionConfiguration()
                        .getAbsolutePath());
            }
            try {
                mSessionState.close();
            } catch (TreetankIOException e) {
                throw new RuntimeException(e);
            }
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
    private final void assertNotClosed() {
        if (mClosed) {
            throw new IllegalStateException("Session is already closed.");
        }
    }

    private final static boolean recursiveDelete(final File file) {
        if (file.isDirectory()) {
            for (final File child : file.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

}
