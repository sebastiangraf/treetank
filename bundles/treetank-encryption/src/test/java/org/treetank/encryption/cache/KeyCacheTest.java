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

        final Collection<Map.Entry<String, LinkedList<Long>>> map =
            cache.getAll();

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
