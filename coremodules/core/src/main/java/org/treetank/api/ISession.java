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

package org.treetank.api;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.exception.TTException;

/**
 * Each <code>IStorage</code> contains multiple resources. To each resource, one {@link ISession} can be
 * bound.
 * 
 * Transactions can then be started from this instance. There can only be one {@link IPageWriteTrx} at the
 * time.
 * However, multiple {@link IPageReadTrx} can coexist concurrently:
 * 
 * * <code>
 *      //Ensure, storage and resources are created
 *      final IStorage storage = Storage.openStorage(FILE);
 *      final ISession session =
 *           storage.getSession(new SessionConfiguration(RESOURCENAME, KEY));
 *      final IPageReadTrx pRtx = session.beginPageReadTransaction(REVISION);
 *      final IPageWriteTrx pWtx = session.beginPageWriteTransaction();
 * </code>
 * 
 */
public interface ISession {

    /**
     * Deregisters a registered page transaction.
     * 
     * @param pTrx
     *            to be deregistered.
     */
    void deregisterPageTrx(final IPageReadTrx pTrx);

    /**
     * Checks for valid revision.
     * 
     * @param pRevision
     *            revision parameter to check
     */
    void assertAccess(final long pRevision);

    /**
     * Getting the resource configuration
     * 
     * @return the config of the resource
     */
    ResourceConfiguration getConfig();

    /**
     * Getting the most recent version from the storage.
     * 
     * @return the most recent version
     */
    long getMostRecentVersion();

    /**
     * Begin exclusive write transaction on the page layer
     * 
     * 
     * @return a {@link IPageReadTrx} instance
     * @throws TTException
     */
    IPageWriteTrx beginPageWriteTransaction() throws TTException;

    /**
     * Begin exclusive write transaction on the page layer with fixed revisions.
     * 
     * @param pRevToRepresent
     * @param pRevToStore
     * @return a {@link IPageReadTrx} instance
     * @throws TTException
     */
    IPageWriteTrx beginPageWriteTransaction(final long pRevToRepresent) throws TTException;

    /**
     * Begin exclusive read transaction on the page layer
     * 
     * @param pRevKey
     *            revision key for the revision ask
     * @return a {@link IPageReadTrx} instance
     * @throws TTException
     */
    IPageReadTrx beginPageReadTransaction(final long pRevKey) throws TTException;

    /**
     * Safely close session and immediately release all resources. If there are
     * running transactions, they will automatically be closed.
     * 
     * This is an idempotent operation and does nothing if the session is
     * already closed.
     * 
     * @throws TTException
     *             If can't close session.
     */
    void close() throws TTException;

    /**
     * Truncating the resource where this {@link ISession} is bound to. Note that the session must be closed
     * first.
     * 
     * @throws TTException
     *             if anything weird happens.
     */
    void truncate() throws TTException;

}
