/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

package org.treetank.gui;

import java.io.File;

import org.treetank.access.FileDatabase;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.exception.AbsTTException;

/**
 * <h1>ReadDB</h1>
 * 
 * <p>
 * Provides access to a Treetank storage.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class ReadDB {

    /** Treetank {@link IDatabase}. */
    private transient IDatabase mDatabase;

    /** Treetank {@link ISession}. */
    private transient ISession mSession;

    /** Treetank {@link IReadTransaction}. */
    private transient IReadTransaction mRtx;

    /** Revision number. */
    private transient long mRevision;

    /**
     * Constructor.
     * 
     * @param paramFile
     *            The {@link File} to open.
     * @param paramRevision
     *            The revision to open.
     */
    public ReadDB(final File paramFile, final long paramRevision) {
        this(paramFile, paramRevision, 0);
    }

    /**
     * Constructor.
     * 
     * @param paramFile
     *            The {@link File} to open.
     * @param paramRevision
     *            The revision to open.
     * @param paramNodekeyToStart
     *            The key of the node where the transaction initially has to move to.
     */
    public ReadDB(final File paramFile, final long paramRevision, final long paramNodekeyToStart) {
        try {
            // Initialize database.
            mDatabase = FileDatabase.openDatabase(paramFile);
            mSession = mDatabase.getSession(new SessionConfiguration());
            mRtx = mSession.beginReadTransaction(paramRevision);
            mRtx.moveTo(paramNodekeyToStart);
            mRevision = mRtx.getRevisionNumber();
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Get the {@link IDatabase} instance.
     * 
     * @return the Database.
     */
    public IDatabase getDatabase() {
        return mDatabase;
    }

    /**
     * Get the {@link ISession} instance.
     * 
     * @return the Session.
     */
    public ISession getSession() {
        return mSession;
    }

    /**
     * Get revision number.
     * 
     * @return current revision number or 0 if a TreetankIOException occured
     */
    public long getRevisionNumber() {
        return mRevision;
    }

    /**
     * Get current node key.
     * 
     * @return node key
     */
    public long getNodeKey() {
        return mRtx.getNode().getNodeKey();
    }

    /**
     * Set node key.
     * 
     * @param paramNodeKey
     *            node key
     */
    public void setNodeKey(final long paramNodeKey) {
        mRtx.moveTo(paramNodeKey);
    }

    /**
     * Close all database related instances.
     */
    public void close() {
        try {
            mRtx.close();
            mSession.close();
        } catch (final AbsTTException exc) {
            exc.printStackTrace();
        }
    }
}
