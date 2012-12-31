/**
 * 
 */
package org.treetank.access;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;

import com.google.inject.Inject;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Testcase for Session.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class SessionTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private CoreTestHelper.Holder mHolder;

    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        Properties props =
            StandardSettings.getStandardProperties(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        ResourceConfiguration mResource = mResourceConfig.create(props);
        mHolder = CoreTestHelper.Holder.generateStorage(mResource);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }

    /**
     * Test method for
     * {@link org.treetank.access.Session#Session(org.treetank.access.Database, org.treetank.access.conf.ResourceConfiguration, org.treetank.access.conf.SessionConfiguration)}
     * .
     * 
     * @throws TTException
     */
    @Test
    public void testSession() throws TTException {
        SessionConfiguration sessionConfig = new SessionConfiguration(CoreTestHelper.RESOURCENAME, null);
        ISession session = mHolder.getStorage().getSession(sessionConfig);
        assertEquals(0, session.getMostRecentVersion());
        ISession sameSession = mHolder.getStorage().getSession(sessionConfig);
        assertTrue(session == sameSession);
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageReadTransaction(long)}.
     */
    @Test
    public void testBeginPageReadTransaction() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageWriteTransaction()}.
     */
    @Test
    public void testBeginPageWriteTransaction() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#beginPageWriteTransaction(long, long)}.
     */
    @Test
    public void testBeginPageWriteTransactionLongLong() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#close()}.
     */
    @Test
    public void testClose() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#assertAccess(long)}.
     */
    @Test
    public void testAssertAccess() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#truncate()}.
     */
    @Test
    public void testTruncate() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.treetank.access.Session#setLastCommittedUberPage(org.treetank.page.UberPage)}.
     */
    @Test
    public void testSetLastCommittedUberPage() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#getMostRecentVersion()}.
     */
    @Test
    public void testGetMostRecentVersion() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#getConfig()}.
     */
    @Test
    public void testGetConfig() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.treetank.access.Session#deregisterPageTrx(org.treetank.api.IPageReadTrx)}.
     */
    @Test
    public void testDeregisterPageTrx() {
        // fail("Not yet implemented");
    }

}
