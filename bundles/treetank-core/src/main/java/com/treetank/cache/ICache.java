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
package com.treetank.cache;


/**
 * Interface for all upcoming cache implementations. Can be a weak one, a
 * LRU-based one or a persistent. However, clear, put and get must to be
 * provided. Instances of this class are used with <code> IReadTransactions</code> as well as with
 * <code>IWriteTransactions</code>.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface ICache {
    /**
     * Clearing the cache. That is removing all elements.
     */
    void clear();

    /**
     * Getting a page related to a given nodepagekey.
     * 
     * @param mKey
     *            the key for the requested {@link NodePageContainer}
     * @return {@link NodePageContainer} instance related to this key
     */
    NodePageContainer get(final long mKey);

    /**
     * Putting an {@link NodePage} into the cache with a corresponding
     * nodepagekey.
     * 
     * @param mKey
     *            for putting the page in the cache.
     * @param mPage
     *            should be putted in the cache as well.
     */
    void put(final long mKey, final NodePageContainer mPage);

}
