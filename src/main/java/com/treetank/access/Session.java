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

package com.treetank.access;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankUsageException;
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

    /** Session state. */
    private SessionState mSessionState;

    /** Was session closed? */
    private boolean mClosed;

    /**
     * Hidden constructor.
     * 
     * @param databaseConf
     *            DatabaseConfiguration for general setting about the storage
     * @param sessionConf
     *            SessionConfiguration for handling this specific session
     * @throws TreetankException
     */
    protected Session(final DatabaseConfiguration databaseConf,
            final SessionConfiguration sessionConf) throws TreetankException {
        mSessionState = new SessionState(databaseConf, sessionConf);
        mClosed = false;
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
        mSessionState.assertValidRevision(revisionKey);
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

    @Override
    public boolean isClosed() {
        return mClosed;
    }

}
