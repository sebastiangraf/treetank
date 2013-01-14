/**
 * 
 */
package org.treetank.access;

import static org.testng.AssertJUnit.assertEquals;
import static org.treetank.CoreTestHelper.getFakedStructure;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;

/**
 * 
 * Test-case for PageReadTrx.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class PageReadTrxTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getNode(long)}.
     */
    @Test
    public void testGetNode() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#close()}.
     */
    @Test
    public void testClose() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getActualRevisionRootPage()}.
     */
    @Test
    public void testGetActualRevisionRootPage() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#isClosed()}.
     */
    @Test
    public void testIsClosed() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#checkItemIfDeleted(org.treetank.api.INode)}.
     */
    @Test
    public void testCheckItemIfDeleted() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getSnapshotPages(long)}.
     */
    @Test
    public void testGetSnapshotPages() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.treetank.access.PageReadTrx#dereferenceLeafOfTree(org.treetank.io.IBackendReader, long, long)}
     * .
     */
    @Test
    public void testDereferenceLeafOfTree() throws TTIOException {
        int[] offsets = new int[5];

        IBackendReader reader = getFakedStructure(offsets);
        long key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 0);
        // 6 is base key because of 5 layers plus the 1 as start key
        assertEquals(6, key);

        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 127);
        // 6 as base plus 127 as offset on last page
        assertEquals(133, key);

        offsets[3] = 1;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 128);
        // 6 as base plus one additional offset on one level above
        assertEquals(7, key);

        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 16383);
        // 6 as base plus two times 127 as offsets on level above
        assertEquals(260, key);

        offsets[2] = 1;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 16384);
        // 6 as base plus one additional offset on two levels above
        assertEquals(7, key);

        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 2097151);
        // 6 as base plus three times 127 as offsets on levels above
        assertEquals(387, key);

        offsets[1] = 1;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 2097152);
        // 6 as base plus one additional offset on three levels above
        assertEquals(7, key);

        offsets[1] = 127;
        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 268435455);
        // 6 as base plus four times 127 as offsets on levels above
        assertEquals(514, key);

        offsets[0] = 1;
        offsets[1] = 0;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 268435456);
        // 6 as base plus one additional offset on three levels above
        assertEquals(7, key);

        offsets[0] = 127;
        offsets[1] = 127;
        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 34359738367l);
        // 6 as base plus five times 127 as offsets on levels above
        assertEquals(641, key);

    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#nodePageKey(long)}.
     */
    @Test
    public void testNodePageKey() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#nodePageOffset(long)}.
     */
    @Test
    public void testNodePageOffset() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getMetaPage()}.
     */
    @Test
    public void testGetMetaPage() {
        // fail("Not yet implemented");
    }

}
