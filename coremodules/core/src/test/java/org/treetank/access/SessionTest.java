/**
 * 
 */
package org.treetank.access;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.TestHelper;

/**
 * Testcase for Session.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class SessionTest {

    @BeforeMethod
    public void setUp() throws Exception {
        TestHelper.deleteEverything();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
    }
    
    /**
     * Test method for
     * {@link org.treetank.access.Session#Session(org.treetank.access.Database, org.treetank.access.conf.ResourceConfiguration, org.treetank.access.conf.SessionConfiguration)}
     * .
     */
    @Test
    public void testSession() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageReadTransaction(long)}.
     */
    @Test
    public void testBeginPageReadTransaction() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageWriteTransaction()}.
     */
    @Test
    public void testBeginPageWriteTransaction() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageWriteTransaction(long, long)}.
     */
    @Test
    public void testBeginPageWriteTransactionLongLong() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#close()}.
     */
    @Test
    public void testClose() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#assertAccess(long)}.
     */
    @Test
    public void testAssertAccess() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#truncate()}.
     */
    @Test
    public void testTruncate() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.treetank.access.Session#setLastCommittedUberPage(org.treetank.page.UberPage)}.
     */
    @Test
    public void testSetLastCommittedUberPage() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#getMostRecentVersion()}.
     */
    @Test
    public void testGetMostRecentVersion() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#getConfig()}.
     */
    @Test
    public void testGetConfig() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#deregisterPageTrx(org.treetank.api.IPageReadTrx)}.
     */
    @Test
    public void testDeregisterPageTrx() {
//        fail("Not yet implemented");
    }

}
