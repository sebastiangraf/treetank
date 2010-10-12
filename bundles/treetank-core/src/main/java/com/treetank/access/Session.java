/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.access;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.utils.ItemList;

/**
 * <h1>Session</h1>
 * 
 * <p>
 * Makes sure that there only is a single session instance bound to a TreeTank file.
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
     * @param mDatabaseConf
     *            DatabaseConfiguration for general setting about the storage
     * @param mSessionConf
     *            SessionConfiguration for handling this specific session
     * @throws TreetankException
     *             Exception if something weird happens
     */
    protected Session(final DatabaseConfiguration mDatabaseConf, final SessionConfiguration mSessionConf)
        throws TreetankException {
        mSessionState = new SessionState(mDatabaseConf, mSessionConf);
        mClosed = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IReadTransaction beginReadTransaction() throws TreetankException {
        assertNotClosed();
        return mSessionState.beginReadTransaction(new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IReadTransaction beginReadTransaction(final long revisionKey)
        throws TreetankException {
        assertNotClosed();
        mSessionState.assertValidRevision(revisionKey);
        return mSessionState.beginReadTransaction(revisionKey, new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IWriteTransaction beginWriteTransaction() throws TreetankException {
        assertNotClosed();
        return mSessionState.beginWriteTransaction(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IWriteTransaction beginWriteTransaction(final int maxNodeCount, final int maxTime)
        throws TreetankException {
        assertNotClosed();
        return mSessionState.beginWriteTransaction(maxNodeCount, maxTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int getReadTransactionCount() {
        return mSessionState.getReadTransactionCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int getWriteTransactionCount() {
        return mSessionState.getWriteTransactionCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws TreetankException {
        if (!mClosed) {
            mSessionState.close();
            mSessionState = null;
            mClosed = true;
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
    public synchronized boolean isClosed() {
        return mClosed;
    }

}
