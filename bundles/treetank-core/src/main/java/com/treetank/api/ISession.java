/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: ISession.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.api;

import com.treetank.exception.TreetankException;

/**
 * <h1>ISession</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>
 * Each <code>IDatabase</code> is bound to one instance implementing <code>ISession</code>. Transactions can
 * then be started from this instance. There can only be one <code>IWriteTransaction</code> at the time.
 * However, multiple <code>IReadTransactions</code> can coexist concurrently.
 * </p>
 * 
 * 
 * <h2>User Example</h2>
 * 
 * <p>
 * 
 * <pre>
 * // Simple session with standards as defined in <code>EDatabaseSetting</code> and <code>ESessionSetting</code>. 
 * final IDatabase database = Database.openDatabase(&quot;example.tnk&quot;);
 * final ISession session = database.getSession()
 * 
 * // Session with lower commit-threshold
 * final Properties sessionProps = new Properties();
 * sessionProps.setProperty(ESessionSetting.COMMIT_THRESHOLD.name(), "32");
 * final SessionConfiguration config = new SessionConfiguration(sessionProps);
 * final ISession session = Database.openDatabase(&quot;example&quot;, config);
 * </pre>
 * 
 * </p>
 * 
 * 
 */
public interface ISession {

    /**
     * Begin a read-only transaction on the latest committed revision key.
     * 
     * @return IReadTransaction instance.
     */
    IReadTransaction beginReadTransaction() throws TreetankException;

    /**
     * Begin a read-only transaction on the given revision key.
     * 
     * @param revisionKey
     *            Revision key to read from.
     * @return IReadTransaction instance.
     */
    IReadTransaction beginReadTransaction(final long revisionKey) throws TreetankException;

    /**
     * Begin exclusive read/write transaction without auto commit.
     * 
     * @return IWriteTransaction instance.
     */
    IWriteTransaction beginWriteTransaction() throws TreetankException;

    /**
     * Begin exclusive read/write transaction with auto commit.
     * 
     * @param maxNodeCount
     *            Count of node modifications after which a commit is issued.
     * @param maxTime
     *            Time in seconds after which a commit is issued.
     * @return IWriteTransaction instance.
     */
    IWriteTransaction beginWriteTransaction(final int maxNodeCount, final int maxTime)
        throws TreetankException;

    /**
     * Get number of running read transactions.
     * 
     * @return Number of running read transactions.
     */
    int getReadTransactionCount();

    /**
     * Get number of running write transactions.
     * 
     * @return Number of running write transactions.
     */
    int getWriteTransactionCount();

    /**
     * Safely close session and immediately release all resources. If there are
     * running transactions, they will automatically be closed.
     * 
     * This is an idempotent operation and does nothing if the session is
     * already closed.
     */
    void close() throws TreetankException;

    /**
     * Test if session is closed. Needed for check against database for creation
     * of a new one.
     * 
     * @return if session was closed
     */
    boolean isClosed();

}
