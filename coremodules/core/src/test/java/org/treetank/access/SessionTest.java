/**
 * 
 */
package org.treetank.access;

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ContructorProps;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.fail;
import com.google.inject.Inject;

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

    private ISession mSession;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        Properties props =
            StandardSettings.getStandardProperties(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        mSession = CoreTestHelper.Holder.generateSession(mResource).getSession();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testBeginPageReadTransaction() throws TTException {
        // generate first valid read transaction
        final IPageReadTrx pRtx1 = mSession.beginPageReadTransaction(0);
        assertNotNull(pRtx1);
        // generate second valid read transaction
        final IPageReadTrx pRtx2 = mSession.beginPageReadTransaction(0);
        assertNotNull(pRtx2);
        // asserting they are different
        assertNotSame(pRtx1, pRtx2);
        // beginning transaction with invalid revision number
        try {
            mSession.beginPageReadTransaction(1);
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }
    }

    @Test
    public void testBeginPageWriteTransaction() throws TTException {
        // generate first valid write transaction
        final IPageWriteTrx pWtx1 = mSession.beginPageWriteTransaction();
        assertNotNull(pWtx1);
        // generate second valid write transaction
        final IPageWriteTrx pWtx2 = mSession.beginPageWriteTransaction(0);
        assertNotNull(pWtx2);
        // asserting they are different
        assertNotSame(pWtx1, pWtx2);
        // beginning transaction with invalid revision number
        try {
            mSession.beginPageWriteTransaction(1);
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }

    }

    @Test
    public void testClose() throws TTException {
        // generate inlaying write transaction
        final IPageWriteTrx pWtx1 = mSession.beginPageWriteTransaction();
        // close the session
        assertTrue(mSession.close());
        assertFalse(mSession.close());
        assertTrue(pWtx1.isClosed());
        // beginning transaction with valid revision number on closed session
        try {
            // try to truncate the resource
            mSession.beginPageWriteTransaction(0);
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }
    }

    @Test
    public void testTruncate() throws TTException {
        // truncate open session
        try {
            // try to truncate the resource
            mSession.truncate();
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }
        assertTrue(mSession.close());
        assertTrue(mSession.truncate());
        assertFalse(mSession.truncate());
    }

    @Test
    public void testGetMostRecentVersion() {
        assertEquals(0, mSession.getMostRecentVersion());
    }

    @Test
    public void testGetConfig() {
        assertEquals(mResource.mProperties.getProperty(ContructorProps.RESOURCE), mSession.getConfig().mProperties.getProperty(ContructorProps.RESOURCE));
    }


}
