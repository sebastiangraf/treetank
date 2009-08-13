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

import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.page.AbstractPage;

/**
 * Interface for all upcoming cache implementations. Can be a weak one, a
 * LRU-based one or a persistent. However, clear, put and get must to be
 * provided. Instances of this class are used with {@link IReadTransaction}s as
 * well as with {@link IWriteTransaction}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface ICache {
	/**
	 * Clearing the cache. That is removing all elements.
	 */
	public void clear();

	/**
	 * Getting a page related to a given nodepagekey.
	 * 
	 * @param key
	 *            the key for the requested {@link AbstractPage}
	 * @return {@link AbstractPage} instance related to this key
	 */
	public AbstractPage get(final long key);

	/**
	 * Putting an {@link AbstractPage} into the cache with a corresponding
	 * nodepagekey.
	 * 
	 * @param key
	 *            for putting the page in the cache.
	 * @param page
	 *            should be putted in the cache as well.
	 */
	public void put(final long key, final AbstractPage page);

}
