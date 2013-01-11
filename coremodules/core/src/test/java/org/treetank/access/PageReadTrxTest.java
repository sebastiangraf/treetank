/**
 * 
 */
package org.treetank.access;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getNode(long)}.
     */
    @Test
    public void testGetNode() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#close()}.
     */
    @Test
    public void testClose() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getActualRevisionRootPage()}.
     */
    @Test
    public void testGetActualRevisionRootPage() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#isClosed()}.
     */
    @Test
    public void testIsClosed() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#checkItemIfDeleted(org.treetank.api.INode)}.
     */
    @Test
    public void testCheckItemIfDeleted() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getSnapshotPages(long)}.
     */
    @Test
    public void testGetSnapshotPages() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.treetank.access.PageReadTrx#dereferenceLeafOfTree(org.treetank.io.IBackendReader, long, long)}
     * .
     */
    @Test
    public void testDereferenceLeafOfTree() {
        IBackendReader reader = mock(IBackendReader.class);
        

    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#nodePageKey(long)}.
     */
    @Test
    public void testNodePageKey() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#nodePageOffset(long)}.
     */
    @Test
    public void testNodePageOffset() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.PageReadTrx#getMetaPage()}.
     */
    @Test
    public void testGetMetaPage() {
//        fail("Not yet implemented");
    }

}
