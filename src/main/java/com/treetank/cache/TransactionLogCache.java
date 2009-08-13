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
 * 
 */
package com.treetank.cache;

import com.treetank.page.AbstractPage;
import com.treetank.session.SessionConfiguration;

/**
 * Transactionlog for storing all upcoming nodes in either the ram cache or a
 * persistent second cache.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class TransactionLogCache extends AbstractPersistenceCache {

	/**
	 * RAM-Based first cache
	 */
	private final LRUCache firstCache;

	/**
	 * Persistent second cache
	 */
	private final BerkeleyPersistenceCache secondCache;

	/**
	 * Constructor including the {@link SessionConfiguration} for persistent
	 * storage.
	 * 
	 * @param paramConfig
	 *            the config for having a storage-place
	 */
	public TransactionLogCache(final SessionConfiguration paramConfig) {
		super(paramConfig);
		secondCache = new BerkeleyPersistenceCache(paramConfig);
		firstCache = new LRUCache(secondCache);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		firstCache.clear();
		super.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractPage get(long key) {
		return firstCache.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(long key, AbstractPage page) {
		firstCache.put(key, page);
	}

}
