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

import com.treetank.utils.FastWeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple RAM implementation with the help of a {@link FastWeakHashMap}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class RAMCache implements ICache {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RAMCache.class);

    /**
     * local instance
     */
    private final transient FastWeakHashMap<Long, NodePageContainer> map;

    /**
     * Simple constructor
     */
    public RAMCache() {
        super();
        map = new FastWeakHashMap<Long, NodePageContainer>();

        // debug
        LOGGER.debug(new StringBuilder("Creating new RAMCache").toString());
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        map.clear();

        // debug
        LOGGER.debug(new StringBuilder("Clear RAMCache").toString());
    }

    /**
     * {@inheritDoc}
     */
    public NodePageContainer get(final long key) {
        // debug
        LOGGER.debug(new StringBuilder("Get Node Page Container with ").append(key).toString());

        return map.get(key);

    }

    /**
     * {@inheritDoc}
     */
    public void put(final long key, final NodePageContainer page) {
        map.put(key, page);

        // debug
        LOGGER.debug(new StringBuilder("Put Node Page Container with ").append(key).append(" ").append(page)
            .toString());
    }

}
