/*
 * Copyright (c) 2009, Sebastian Graf (Ph.D. Thesis), University of Konstanz
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
 */
package com.treetank.cache;

import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TreetankIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transactionlog for storing all upcoming nodes in either the ram cache or a
 * persistent second cache.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TransactionLogCache extends AbstractPersistenceCache {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogCache.class);

    /**
     * RAM-Based first cache
     */
    private transient final LRUCache firstCache;

    /**
     * Constructor including the {@link SessionConfiguration} for persistent
     * storage.
     * 
     * @param paramConfig
     *            the config for having a storage-place
     */
    public TransactionLogCache(final DatabaseConfiguration paramConfig, final long revision)
        throws TreetankIOException {
        super(paramConfig);
        final BerkeleyPersistenceCache secondCache = new BerkeleyPersistenceCache(paramConfig, revision);
        firstCache = new LRUCache(secondCache);

        // debug
        LOGGER.debug(new StringBuilder("Creating new Transaction Log Cache with Database Configuration ")
            .append(paramConfig).append(" and Revision ").append(revision).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearPersistent() throws TreetankIOException {
        firstCache.clear();

        // debug
        LOGGER.debug(new StringBuilder("Celar Persistence").toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer getPersistent(final long key) throws TreetankIOException {

        // debug
        LOGGER.debug(new StringBuilder("Get Persistence with key ").append(key).toString());

        return firstCache.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putPersistent(final long key, final NodePageContainer page) throws TreetankIOException {
        firstCache.put(key, page);

        // debug
        LOGGER.debug(new StringBuilder("Put Persistence with key ").append(key).append(
            " and Node Page Continer ").append(page.toString()).toString());
    }

}
