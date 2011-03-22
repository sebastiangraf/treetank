/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.utils.ItemList;

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
     * @throws AbsTTException
     *             Exception if something weird happens
     */
    protected Session(final DatabaseConfiguration mDatabaseConf, final SessionConfiguration mSessionConf)
        throws AbsTTException {
        mSessionState = new SessionState(mDatabaseConf, mSessionConf);
        mClosed = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IReadTransaction beginReadTransaction() throws AbsTTException {
        assertNotClosed();
        return mSessionState.beginReadTransaction(new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IReadTransaction beginReadTransaction(final long revisionKey)
        throws AbsTTException {
        assertNotClosed();
        mSessionState.assertValidRevision(revisionKey);
        return mSessionState.beginReadTransaction(revisionKey, new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IWriteTransaction beginWriteTransaction() throws AbsTTException {
        assertNotClosed();
        return mSessionState.beginWriteTransaction(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IWriteTransaction beginWriteTransaction(final int maxNodeCount, final int maxTime)
        throws AbsTTException {
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
    public synchronized void close() throws AbsTTException {
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
