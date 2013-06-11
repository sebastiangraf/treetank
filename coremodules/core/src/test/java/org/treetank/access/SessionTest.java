/**
 * 
 */
package org.treetank.access;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.CoreTestHelper.Holder;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ConstructorProps;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IBucketReadTrx;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;

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

    private Holder mHolder;

    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        mHolder = CoreTestHelper.Holder.generateStorage();
        final ResourceConfiguration config =
            mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME));
        CoreTestHelper.Holder.generateSession(mHolder, config);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testParallelSessions() throws TTException {
        final String resource1 = CoreTestHelper.RESOURCENAME + "1";
        final String resource2 = CoreTestHelper.RESOURCENAME + "2";

        final ResourceConfiguration config1 =
            mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), resource1));
        final ResourceConfiguration config2 =
            mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), resource2));

        assertTrue(mHolder.getStorage().createResource(config1));
        assertTrue(mHolder.getStorage().createResource(config2));

        final ISession session1 =
            mHolder.getStorage().getSession(new SessionConfiguration(resource1, StandardSettings.KEY));
        final ISession session2 =
            mHolder.getStorage().getSession(new SessionConfiguration(resource2, StandardSettings.KEY));

        IBucketWriteTrx wtx1 = session1.beginBucketWtx();
        IBucketWriteTrx wtx2 = session2.beginBucketWtx();

        try {
            session1.beginBucketWtx();
            fail();
        } catch (IllegalStateException exc) {

        }

        wtx1.close();
        wtx2.close();

        wtx1 = session1.beginBucketWtx();
        wtx2 = session2.beginBucketWtx();

    }

    @Test
    public void testParallelSessionsOld() throws TTException {
        ResourceConfiguration config =
            mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME + "2"));
        CoreTestHelper.Holder.generateSession(mHolder, config);
        // Take a look at CoreTestHelper#generateWtx, always creating pageTrx under standard ResourceName
        // CoreTestHelper.Holder.generateWtx(mHolder, config);

        config =
            mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME + "3"));
        CoreTestHelper.Holder.generateSession(mHolder, config);
        // Creating second transaction on same resource, conflict is not ResourceName+"2" but ResourceName
        // indirect invoked over CoreTestHelper.generateWtx.
        // CoreTestHelper.Holder.generateWtx(mHolder, config);
    }

    @Test
    public void testBeginPageReadTransaction() throws TTException {
        // generate first valid read transaction
        final IBucketReadTrx pRtx1 = mHolder.getSession().beginBucketRtx(0);
        assertNotNull(pRtx1);
        // generate second valid read transaction
        final IBucketReadTrx pRtx2 = mHolder.getSession().beginBucketRtx(0);
        assertNotNull(pRtx2);
        // asserting they are different
        assertNotSame(pRtx1, pRtx2);
        // beginning transaction with invalid revision number
        try {
            mHolder.getSession().beginBucketRtx(1);
            fail();
        } catch (IllegalArgumentException exc) {
            // must be thrown
        }
    }

    @Test
    public void testBeginPageWriteTransaction() throws TTException {
        // generate first valid write transaction
        final IBucketWriteTrx pWtx1 = mHolder.getSession().beginBucketWtx();
        assertNotNull(pWtx1);
        pWtx1.close();
        // generate second valid write transaction
        final IBucketWriteTrx pWtx2 = mHolder.getSession().beginBucketWtx(0);
        assertNotNull(pWtx2);
        pWtx2.close();
        // asserting they are different
        assertNotSame(pWtx1, pWtx2);
        // beginning transaction with invalid revision number
        try {
            mHolder.getSession().beginBucketWtx(1);
            fail();
        } catch (IllegalArgumentException exc) {
            // must be thrown
        }

    }

    @Test
    public void testClose() throws TTException {
        // generate inlaying write transaction
        final IBucketWriteTrx pWtx1 = mHolder.getSession().beginBucketWtx();
        // close the session
        assertTrue(mHolder.getSession().close());
        assertFalse(mHolder.getSession().close());
        assertTrue(pWtx1.isClosed());
        // beginning transaction with valid revision number on closed session
        try {
            // try to truncate the resource
            mHolder.getSession().beginBucketWtx(0);
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
            assertTrue(mHolder.getSession().close());
            mHolder.getSession().truncate();
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }
        ISession session =
            mHolder.getStorage().getSession(
                new SessionConfiguration(CoreTestHelper.RESOURCENAME, StandardSettings.KEY));
        assertTrue(session.truncate());
        try {
            session.truncate();
        } catch (IllegalStateException exc) {
            // must be thrown
        }

    }

    @Test
    public void testGetMostRecentVersion() {
        assertEquals(0, mHolder.getSession().getMostRecentVersion());
    }

    @Test
    public void testGetConfig() {
        assertEquals(CoreTestHelper.RESOURCENAME, mHolder.getSession().getConfig().mProperties
            .getProperty(ConstructorProps.RESOURCE));
    }

}
