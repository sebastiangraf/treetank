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

import java.util.concurrent.Future;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * Each <code>IStorage</code> contains multiple resources. To each resource, one {@link ISession} can be
 * bound.
 * 
 * Transactions can then be started from this instance. There can only be one {@link IBucketWriteTrx} at the
 * time.
 * However, multiple {@link IBucketReadTrx} can coexist concurrently:
 * 
 * * <code>
 *      //Ensure, storage and resources are created
 *      final IStorage storage = Storage.openStorage(FILE);
 *      final ISession session =
 *           storage.getSession(new SessionConfiguration(RESOURCENAME, KEY));
 *      final IBucketReadTrx pRtx = session.beginBucketRtx(REVISION);
 *      final IBucketWriteTrx pWtx = session.beginBucketWtx();
 * </code>
 * 
 */
public interface ISession {

    /**
     * Deregisters a registered bucket transaction.
     * 
     * @param pTrx
     *            to be deregistered.
     * @return true if successful, false otherwise
     */
    boolean deregisterBucketTrx(final IBucketReadTrx pTrx) throws TTIOException;

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
    long getMostRecentVersion() throws TTIOException;

    /**
     * Begin exclusive write transaction on the bucket layer
     * 
     * 
     * @return a {@link IBucketReadTrx} instance
     * @throws TTException
     */
    IBucketWriteTrx beginBucketWtx() throws TTException;

    /**
     * Begin exclusive write transaction on the bucket layer with fixed revisions.
     * 
     * @param pRevToRepresent
     * @return a {@link IBucketReadTrx} instance
     * @throws TTException
     */
    IBucketWriteTrx beginBucketWtx(final long pRevToRepresent) throws TTException;

    /**
     * Begin exclusive read transaction on the bucket layer
     * 
     * @param pRevKey
     *            revision key for the revision ask
     * @return a {@link IBucketReadTrx} instance
     * @throws TTException
     */
    IBucketReadTrx beginBucketRtx(final long pRevKey) throws TTException;

    /**
     * Safely close session and immediately release all resources. If there are
     * running transactions, they will automatically be closed.
     * 
     * This is an idempotent operation and does nothing if the session is
     * already closed.
     * 
     * @return true if successful, false otherwise
     * @throws TTException
     *             If can't close session.
     */
    boolean close() throws TTException;

    /**
     * Truncating the resource where this {@link ISession} is bound to. Note that the session must be closed
     * first.
     * 
     * @return true if successful, false otherwise
     * @throws TTException
     *             if anything weird happens.
     */
    boolean truncate() throws TTException;

    /**
     * Waiting and blocking for running commit. Necessary before new commits are triggered or new transactions
     * are started.
     * 
     * @throws TTIOException
     *             if something goes weird
     */
    void waitForRunningCommit() throws TTIOException;

    /**
     * Setting running commit to session.
     * 
     * @param pRunningCommit
     *            to be set
     */
    void setRunningCommit(Future<Void> pRunningCommit);

}
