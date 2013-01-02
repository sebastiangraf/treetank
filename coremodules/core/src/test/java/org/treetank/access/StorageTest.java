package org.treetank.access;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

import com.google.inject.Inject;

/**
 * Testcase for Session.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class StorageTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    // Reference set by testOpenStorage, might be null otherwise.
    private IStorage mStorage;

    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        Properties props =
            StandardSettings.getStandardProperties(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testCreateStorage() throws TTIOException {
        // Creating Storage, successful
        assertTrue(Storage.createStorage(CoreTestHelper.PATHS.PATH1.getConfig()));
        // unsuccessful re-creation
        assertFalse(Storage.createStorage(CoreTestHelper.PATHS.PATH1.getConfig()));
    }

    @Test
    public void testTruncateStorage() {
    }

    @Test
    public void testExistsStorage() throws TTIOException {
        // creation of storage
        assertTrue(Storage.createStorage(CoreTestHelper.PATHS.PATH1.getConfig()));
        // check if exists
        assertTrue(Storage.existsStorage(CoreTestHelper.PATHS.PATH1.getFile()));
        assertFalse(Storage.existsStorage(CoreTestHelper.PATHS.PATH2.getFile()));
    }

    @Test
    public void testCreateResource() throws TTException {
        assertTrue(Storage.createStorage(CoreTestHelper.PATHS.PATH1.getConfig()));
        mStorage = Storage.openStorage(CoreTestHelper.PATHS.PATH1.getFile());
        assertNotNull(mStorage);
        IStorage storage = Storage.openStorage(CoreTestHelper.PATHS.PATH1.getFile());
        assertTrue(storage == mStorage);
        mStorage.close();
    }

    @Test
    public void testTruncateResource() {
    }

    @Test
    public void testOpenStorage() {

    }

    @Test
    public void testGetSession() throws TTException {
        // SessionConfiguration sessionConfig = new SessionConfiguration(CoreTestHelper.RESOURCENAME, null);
        // ISession session = mHolder.getStorage().getSession(sessionConfig);
        // assertEquals(0, session.getMostRecentVersion());
        // ISession sameSession = mHolder.getStorage().getSession(sessionConfig);
        // assertTrue(session == sameSession);
    }

    @Test
    public void testClose() {
    }

    @Test
    public void testExistsResource() {
    }

    @Test
    public void testListResources() {
    }

    @Test
    public void testGetLocation() {
    }

}
