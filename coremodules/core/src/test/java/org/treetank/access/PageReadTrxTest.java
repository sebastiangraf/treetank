/**
 * 
 */
package org.treetank.access;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.IndirectPage;

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
        // checking 0 offset
        int[] offsets = new int[5];
        
        IBackendReader reader = getFakedStructure(offsets);
        long key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 0);
        assertEquals(6, key);

        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 127);
        assertEquals(6, key);

        offsets[3] = 1;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 128);
        assertEquals(6, key);

        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 16383);
        assertEquals(6, key);

        offsets[2] = 1;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 16384);
        assertEquals(6, key);

        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 2097151);
        assertEquals(6, key);

        offsets[1] = 1;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 2097152);
        assertEquals(6, key);

        offsets[1] = 127;
        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 268435455);
        assertEquals(6, key);
        
        offsets[0] = 1;
        offsets[1] = 0;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 268435456);
        assertEquals(6, key);
        
        offsets[0] = 127;
        offsets[1] = 127;
        offsets[2] = 127;
        offsets[3] = 127;
        offsets[4] = 127;
        reader = getFakedStructure(offsets);
        key = PageReadTrx.dereferenceLeafOfTree(reader, 1, 34359738367l);
        assertEquals(6, key);

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

    /**
     * Getting a fake structure for testing consiting of different arranged pages.
     * This structure starts with the key 1 and incrementally sets a new pagekey for the defined offsets in
     * the indirectpages to simulate different versions and node-offsets.
     * The key retrieved thereby has always the value 6 (1 (starting) + 5 (number of indirect layers)
     * 
     * @param offsets
     *            an array with offsets internally of the tree.
     * @return a {@link IBackendReader}-mock
     * @throws TTIOException
     */
    private static IBackendReader getFakedStructure(int[] offsets) throws TTIOException {
        assertEquals(5, offsets.length);
        // mocking the reader
        IBackendReader reader = mock(IBackendReader.class);
        // variable storing the related keys to the pages created in the mock
        long pKey = 1;
        // iterating through the tree and..
        for (int i = 0; i < offsets.length; i++) {
            // ...creating a new page with incrementing the page key
            final IndirectPage page = new IndirectPage(pKey);
            // setting the related key to the defined offset and...
            page.setReferenceKey(offsets[i], ++pKey);
            // ...tell the mock to react when the key is demanded.
            when(reader.read(pKey - 1)).thenReturn(page);
        }
        // returning the mock
        return reader;
    }

}
