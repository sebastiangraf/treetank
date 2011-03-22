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
 *     * Neither the name of the University of Konstanz nor the
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

package org.treetank.api;

import org.treetank.exception.AbsTTException;

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
 * // Simple session with standards as defined in <code>EDatabaseSetting</code> and 
 * <code>ESessionSetting</code>. 
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
     * @throws AbsTTException
     *             If can't begin Read Transaction.
     * @return IReadTransaction instance.
     */
    IReadTransaction beginReadTransaction() throws AbsTTException;

    /**
     * Begin a read-only transaction on the given revision key.
     * 
     * @param revisionKey
     *            Revision key to read from.
     * @throws AbsTTException
     *             If can't begin Read Transaction.
     * @return IReadTransaction instance.
     */
    IReadTransaction beginReadTransaction(final long revisionKey) throws AbsTTException;

    /**
     * Begin exclusive read/write transaction without auto commit.
     * 
     * @throws AbsTTException
     *             If can't begin Write Transaction.
     * @return IWriteTransaction instance.
     */
    IWriteTransaction beginWriteTransaction() throws AbsTTException;

    /**
     * Begin exclusive read/write transaction with auto commit.
     * 
     * @param maxNodeCount
     *            Count of node modifications after which a commit is issued.
     * @param maxTime
     *            Time in seconds after which a commit is issued.
     * @throws AbsTTException
     *             If can't begin Write Transaction.
     * @return IWriteTransaction instance.
     */
    IWriteTransaction beginWriteTransaction(final int maxNodeCount, final int maxTime)
        throws AbsTTException;

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
     * 
     * @throws AbsTTException
     *             If can't close session.
     */
    void close() throws AbsTTException;

    /**
     * Test if session is closed. Needed for check against database for creation
     * of a new one.
     * 
     * @return if session was closed
     */
    boolean isClosed();

}