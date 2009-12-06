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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An LRU cache, based on <code>LinkedHashMap</code>. This cache can hold an
 * possible second cache as a second layer for example for storing data in a
 * persistent way.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public final class LRUCache implements ICache {

    /**
     * Capacity of the cache. Number of stored pages
     */
    final static int CACHE_CAPACITY = 10;

    /**
     * The collection to hold the maps.
     */
    private transient final Map<Long, NodePageContainer> map;

    /**
     * The reference to the second cache.
     */
    private transient final ICache secondCache;

    /**
     * Creates a new LRU cache.
     * 
     * @param paramSecondCache
     *            the reference to the second cache where the data is stored
     *            when it gets removed from the first one.
     * 
     */
    public LRUCache(final ICache paramSecondCache) {
        secondCache = paramSecondCache;
        map = new LinkedHashMap<Long, NodePageContainer>(CACHE_CAPACITY) {
            // (an anonymous inner class)
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(
                    final Map.Entry<Long, NodePageContainer> eldest) {
                boolean returnVal = false;
                if (size() > CACHE_CAPACITY) {
                    secondCache.put(eldest.getKey(), eldest.getValue());
                    returnVal = true;
                }
                return returnVal;

            }
        };
    }

    /**
     * Constructor with no second cache.
     */
    public LRUCache() {
        this(new NullCache());
    }

    /**
     * Retrieves an entry from the cache.<br>
     * The retrieved entry becomes the MRU (most recently used) entry.
     * 
     * @param key
     *            the key whose associated value is to be returned.
     * @return the value associated to this key, or null if no value with this
     *         key exists in the cache.
     */
    public NodePageContainer get(final long key) {
        NodePageContainer page = map.get(key);
        if (page == null) {
            page = secondCache.get(key);
        }
        return page;
    }

    /**
     * 
     * Adds an entry to this cache. If the cache is full, the LRU (least
     * recently used) entry is dropped.
     * 
     * @param key
     *            the key with which the specified value is to be associated.
     * @param value
     *            a value to be associated with the specified key.
     */
    public void put(final long key, final NodePageContainer value) {
        map.put(key, value);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        map.clear();
        secondCache.clear();
    }

    /**
     * Returns the number of used entries in the cache.
     * 
     * @return the number of entries currently in the cache.
     */
    public int usedEntries() {
        return map.size();
    }

    /**
     * Returns a <code>Collection</code> that contains a copy of all cache
     * entries.
     * 
     * @return a <code>Collection</code> with a copy of the cache content.
     */
    public Collection<Map.Entry<Long, NodePageContainer>> getAll() {
        return new ArrayList<Map.Entry<Long, NodePageContainer>>(map.entrySet());
    }

}
