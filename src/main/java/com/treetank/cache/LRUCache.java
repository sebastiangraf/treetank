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

import com.treetank.page.AbstractPage;

/**
 * An LRU cache, based on <code>LinkedHashMap</code>. This cache can hold an
 * possible second cache as a second layer for example for storing data in a
 * persistent way.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public class LRUCache implements ICache {

	/**
	 * Capacity of the cache. Number of stored pages
	 */
	protected final static int CACHE_CAPACITY = 10;

	/**
	 * The collection to hold the maps.
	 */
	private LinkedHashMap<Long, AbstractPage> map;

	/**
	 * The reference to the second cache.
	 */
	private final ICache secondCache;

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
		map = new LinkedHashMap<Long, AbstractPage>(CACHE_CAPACITY) {
			// (an anonymous inner class)
			private static final long serialVersionUID = 1;

			@Override
			protected boolean removeEldestEntry(
					Map.Entry<Long, AbstractPage> eldest) {
				if (size() > CACHE_CAPACITY) {
					secondCache.put(eldest.getKey(), eldest.getValue());
					return true;
				} else {
					return false;
				}

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
	public AbstractPage get(long key) {
		AbstractPage page = map.get(key);
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
	public void put(long key, AbstractPage value) {
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
	public Collection<Map.Entry<Long, AbstractPage>> getAll() {
		return new ArrayList<Map.Entry<Long, AbstractPage>>(map.entrySet());
	}

}
