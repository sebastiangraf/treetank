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

package org.treetank.cache;

import java.io.File;

import org.treetank.access.DatabaseConfiguration;
import org.treetank.exception.TTIOException;

/**
 * Transactionlog for storing all upcoming nodes in either the ram cache or a
 * persistent second cache.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TransactionLogCache extends AbstractPersistenceCache {

    /**
     * RAM-Based first cache.
     */
    private transient final LRUCache mFirstCache;

    /**
     * Constructor including the {@link DatabaseConfiguration} for persistent
     * storage.
     * 
     * @param paramFile
     *            the config for having a storage-place
     * @param paramRevision
     *            revision number
     * @throws TTIOException
     *             Exception if IO is not successful
     */
    public TransactionLogCache(final File paramFile, final long paramRevision) throws TTIOException {
        super(paramFile);
        final BerkeleyPersistenceCache secondCache = new BerkeleyPersistenceCache(paramFile, paramRevision);
        mFirstCache = new LRUCache(secondCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearPersistent() throws TTIOException {
        mFirstCache.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer getPersistent(final long mKey) throws TTIOException {
        return mFirstCache.get(mKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putPersistent(final long mKey, final NodePageContainer mPage) throws TTIOException {
        mFirstCache.put(mKey, mPage);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.mFirstCache.toString();
    }
}
