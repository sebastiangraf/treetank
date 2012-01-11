/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
package org.treetank.encryption.cache;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

public class KeyCacheTest {

    KeyCache cache;

    @Before
    public void setUp() throws Exception {
        cache = new KeyCache();

        final LinkedList<Long> keys1 = new LinkedList<Long>();
        keys1.add(1L);
        keys1.add(2L);

        final LinkedList<Long> keys2 = new LinkedList<Long>();
        keys2.add(3L);
        keys2.add(4L);

        cache.put("aUser1", keys1);
        cache.put("aUser2", keys2);

    }

    @Test
    public void testPutAndGet() {
        long id;

        final LinkedList<Long> keyCache1 = cache.get("aUser1");
        id = keyCache1.get(0);
        assertEquals(id, 1);
        id = keyCache1.get(1);
        assertEquals(id, 2);

        final LinkedList<Long> keyCache2 = cache.get("aUser2");
        id = keyCache2.get(0);
        assertEquals(id, 3);
        id = keyCache2.get(1);
        assertEquals(id, 4);
    }

    @Test
    public void testEntries() {
        assertEquals(cache.usedEntries(), 2);
    }

    @Test
    public void testClear() {
        cache.clear();
        assertEquals(cache.usedEntries(), 0);
    }

    @Test
    public void testCollection() {

        final String[] users = new String[] {
            "aUser1", "aUser2"
        };
        final long[] keys = new long[] {
            1, 2, 3, 4
        };

        final Collection<Map.Entry<String, LinkedList<Long>>> map = cache.getAll();

        final Iterator<Entry<String, LinkedList<Long>>> iter = map.iterator();

        assertEquals(map.size(), 2);

        int i = 0;
        int k = 0;
        while (iter.hasNext()) {
            final Entry entry = iter.next();
            final String key = (String)entry.getKey();
            final LinkedList<Long> value = (LinkedList<Long>)entry.getValue();

            assertEquals(key, users[i]);

            for (int j = 0; j < value.size(); j++) {
                assertEquals((long)value.get(j), keys[k]);
                k++;
            }

            i++;
        }

    }

}
