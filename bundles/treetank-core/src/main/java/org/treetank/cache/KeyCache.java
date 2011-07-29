package org.treetank.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * An LRU cache, based on <code>LinkedHashMap</code> holding
 * last key changes for a user.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class KeyCache {

    /**
     * Capacity of the cache.
     */
    static final int CACHE_CAPACITY = 100;

    /**
     * The collection to hold the maps.
     */
    private final Map<String, LinkedList<LinkedList<Long>>> mMap;

    /**
     * Constructor creates a new key cache.
     */
    public KeyCache() {
        mMap = new LinkedHashMap<String, LinkedList<LinkedList<Long>>>(CACHE_CAPACITY) {
            private static final long serialVersionUID = 1;

            @Override
            protected boolean
                removeEldestEntry(final Map.Entry<String, LinkedList<LinkedList<Long>>> mEldest) {
                boolean returnVal = false;
                if (size() > CACHE_CAPACITY) {
                    returnVal = true;
                }
                return returnVal;
            }
        };

    }

    /**
     * Returns the stored <code>LinkedList</code> of corresponding user.
     * 
     * @param paramUser
     *            User key.
     * @return linked list of user.
     */
    public final LinkedList<LinkedList<Long>> get(final String paramUser) {
        final LinkedList<LinkedList<Long>> list = mMap.get(paramUser);
        return list; // returns null if no value for this user exists in cache.
    }

    /**
     * Stores a new entry in cache consisting of a user name as key and
     * a linked list for storing node keys as value.
     * 
     * @param paramUser
     *            user name as key.
     * @param paramList
     *            linked list as values.
     */
    public final void put(final String paramUser, final LinkedList<Long> paramList) {

        final LinkedList<LinkedList<Long>> mList = new LinkedList<LinkedList<Long>>();
        mList.add(paramList);

        mMap.put(paramUser, mList);
    }

    /**
     * Clears the cache.
     */
    public final void clear() {
        mMap.clear();
    }

    /**
     * Returns the number of used entries in the cache.
     * 
     * @return the number of entries currently in the cache.
     */
    public final int usedEntries() {
        return mMap.size();
    }

    /**
     * Returns a <code>Collection</code> that contains a copy of all cache
     * entries.
     * 
     * @return a <code>Collection</code> with a copy of the cache content.
     */
    public final Collection<Map.Entry<String, LinkedList<LinkedList<Long>>>> getAll() {
        return new ArrayList<Map.Entry<String, LinkedList<LinkedList<Long>>>>(mMap.entrySet());
    }
}
