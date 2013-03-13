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
import org.treetank.api.IPageReadTrx;
import org.treetank.api.IPageWriteTrx;
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
    
    @Test(enabled = false)
    public void testParallelSessions() throws TTException{
        ResourceConfiguration config =
        mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
            .getAbsolutePath(), CoreTestHelper.RESOURCENAME+"2"));
        CoreTestHelper.Holder.generateSession(mHolder, config);
        CoreTestHelper.Holder.generateWtx(mHolder, config);
        
        config =
        mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
            .getAbsolutePath(), CoreTestHelper.RESOURCENAME+"3"));
        CoreTestHelper.Holder.generateSession(mHolder, config);
        CoreTestHelper.Holder.generateWtx(mHolder, config);
    }

    @Test
    public void testBeginPageReadTransaction() throws TTException {
        // generate first valid read transaction
        final IPageReadTrx pRtx1 = mHolder.getSession().beginPageReadTransaction(0);
        assertNotNull(pRtx1);
        // generate second valid read transaction
        final IPageReadTrx pRtx2 = mHolder.getSession().beginPageReadTransaction(0);
        assertNotNull(pRtx2);
        // asserting they are different
        assertNotSame(pRtx1, pRtx2);
        // beginning transaction with invalid revision number
        try {
            mHolder.getSession().beginPageReadTransaction(1);
            fail();
        } catch (IllegalArgumentException exc) {
            // must be thrown
        }
    }

    @Test
    public void testBeginPageWriteTransaction() throws TTException {
        // generate first valid write transaction
        final IPageWriteTrx pWtx1 = mHolder.getSession().beginPageWriteTransaction();
        assertNotNull(pWtx1);
        pWtx1.close();
        // generate second valid write transaction
        final IPageWriteTrx pWtx2 = mHolder.getSession().beginPageWriteTransaction(0);
        assertNotNull(pWtx2);
        pWtx2.close();
        // asserting they are different
        assertNotSame(pWtx1, pWtx2);
        // beginning transaction with invalid revision number
        try {
            mHolder.getSession().beginPageWriteTransaction(1);
            fail();
        } catch (IllegalArgumentException exc) {
            // must be thrown
        }

    }

    @Test
    public void testClose() throws TTException {
        // generate inlaying write transaction
        final IPageWriteTrx pWtx1 = mHolder.getSession().beginPageWriteTransaction();
        // close the session
        assertTrue(mHolder.getSession().close());
        assertFalse(mHolder.getSession().close());
        assertTrue(pWtx1.isClosed());
        // beginning transaction with valid revision number on closed session
        try {
            // try to truncate the resource
            mHolder.getSession().beginPageWriteTransaction(0);
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
