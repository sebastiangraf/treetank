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
package com.treetank.gui;

import java.io.File;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

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
    /** Logger. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(ReadDB.class));

    /** Treetank {@link IDatabase}. */
    private transient IDatabase mDatabase;

    /** Treetank {@link ISession}. */
    private transient ISession mSession;

    /** Treetank {@link IReadTransaction}. */
    private transient IReadTransaction mRtx;

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
            final IDatabase database = Database.openDatabase(paramFile);
            mSession = database.getSession();

            if (mDatabase == null || mDatabase.getFile() == null
                || !(mDatabase.getFile().equals(database.getFile()))) {
                mDatabase = database;

                if (mRtx != null && !mRtx.isClosed()) {
                    mRtx.close();
                }
            }

            if (mRtx == null || mRtx.isClosed()) {
                mRtx = mDatabase.getSession().beginReadTransaction(paramRevision);
            }
            mRtx.moveTo(paramNodekeyToStart);
        } catch (final TreetankException e) {
            LOGWRAPPER.error("TreetankException: " + e.getMessage(), e);
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
     * Get the {@link IReadTransaction} instance.
     * 
     * @return the Rtx.
     */
    public IReadTransaction getRtx() {
        return mRtx;
    }

    /**
     * Close all database related instances.
     */
    public void close() {
        try {
            mRtx.close();
            mSession.close();
            mDatabase.close();
        } catch (final TreetankException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }
}
