package org.treetank.encryption.database.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class KeyManagerTest {

    KeyManager testManager;

    @Before
    public void setUp() throws Exception {
        final Set<Long> keys = new HashSet<Long>();
        keys.add(0L);
        keys.add(1L);

        testManager = new KeyManager("aUser", keys);

        testManager.addKey(2L);
        testManager.removeKey(1L);
    }

    @Test
    public void testManagerName() {
        assertEquals(testManager.getUser(), "aUser");
    }

    @Test
    public void testManagerKeys() {

        final Set<Long> keys = testManager.getKeySet();

        assertEquals(keys.size(), 2);

        assertTrue(keys.contains(0L));
        assertTrue(keys.contains(2L));
    }

}
