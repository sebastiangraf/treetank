package org.treetank.encryption.database.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.treetank.encryption.utils.NodeEncryption;

public class KeySelectorTest {

    KeySelector testSelector;

    @Before
    public void setUp() throws Exception {
        final LinkedList<Long> parents = new LinkedList<Long>();
        parents.add(1L);
        final LinkedList<Long> childs = new LinkedList<Long>();
        childs.add(2L);

        final byte[] mSecret = NodeEncryption.generateSecretKey();

        testSelector = new KeySelector("aName", parents, childs, 0, 0, mSecret);

        testSelector.setPrimaryKey(0);
        testSelector.addChild(3L);
        testSelector.addParent(4L);
        testSelector.increaseRevision();
        testSelector.increaseVersion();
        testSelector.removeChild(2L);
        testSelector.removeParent(1L);
    }

    @Test
    public void testSelectorName() {
        assertEquals(testSelector.getName(), "aName");
    }

    @Test
    public void testSelectorParents() {
        final long longId = testSelector.getParents().get(0);
        assertEquals(longId, 4L);

    }

    @Test
    public void testSelectorChilds() {
        final long longId = testSelector.getChilds().get(0);
        assertEquals(longId, 3L);

    }

    @Test
    public void testSelectorRevsion() {
        final int intId = testSelector.getRevision();
        assertEquals(intId, 1);

    }

    @Test
    public void testSelectorVersion() {
        final int intId = testSelector.getVersion();
        assertEquals(intId, 1);

    }

    @Test
    public void testSelectorPrimaryKey() {
        final long longId = testSelector.getPrimaryKey();
        assertEquals(longId, 0);

    }

    @Test
    public void testSelectorSecretKey() {
        final byte[] secretKey = testSelector.getSecretKey();
        assertNotNull(secretKey);

    }
}
