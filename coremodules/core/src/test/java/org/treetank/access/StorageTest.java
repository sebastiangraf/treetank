package org.treetank.access;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.access.conf.ConstructorProps;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.testutil.CoreTestHelper;
import org.treetank.testutil.ModuleFactory;

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
        createStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        if (mStorage != null) {
            mStorage.close();
        }
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testCreateAndExistsAndTruncateStorage() throws TTException {
        // check if exists
        assertTrue(Storage.existsStorage(CoreTestHelper.PATHS.PATH1.getFile()));
        // unsuccessful re-creation
        assertFalse(Storage.createStorage(CoreTestHelper.PATHS.PATH1.getConfig()));
        // check against non-existing storage
        assertFalse(Storage.existsStorage(CoreTestHelper.PATHS.PATH2.getFile()));
        // creating a new resource, successful
        assertTrue(mStorage.createResource(mResource));
        assertTrue(mStorage.close());
        // removing storage
        Storage.truncateStorage(CoreTestHelper.PATHS.PATH1.getConfig());
        // check if storage exists, unsuccessful
        assertFalse(Storage.existsStorage(CoreTestHelper.PATHS.PATH1.getFile()));
        // Check against resource as well....
        // Creating Storage, successful
        assertTrue(Storage.createStorage(CoreTestHelper.PATHS.PATH1.getConfig()));
        // Opening Storage and check it is not null
        mStorage = Storage.openStorage(CoreTestHelper.PATHS.PATH1.getFile());
        assertNotNull(mStorage);
        // check against resource, should not exist
        assertFalse(mStorage.existsResource(mResource.mProperties.getProperty(ConstructorProps.RESOURCE)));

    }

    @Test
    public void testOpenStorage() throws TTException {
        // get another storage and check if the internal singleton is pointing to the same reference
        IStorage storage = Storage.openStorage(CoreTestHelper.PATHS.PATH1.getFile());
        assertTrue(storage == mStorage);
        assertTrue(mStorage.close());
        try {
            // try to get a non-created storage
            Storage.openStorage(CoreTestHelper.PATHS.PATH2.getFile());
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }
    }

    @Test
    public void testCreateAndExistsAndTruncateResource() throws TTException {
        // creating a new resource, successful
        assertTrue(mStorage.createResource(mResource));
        // check if existing, successful
        assertTrue(mStorage.existsResource(mResource.mProperties.getProperty(ConstructorProps.RESOURCE)));
        // creating the same resource, unsuccessful
        assertFalse(mStorage.createResource(mResource));
        // getting the session
        ISession session =
            mStorage.getSession(new SessionConfiguration(mResource.mProperties
                .getProperty(ConstructorProps.RESOURCE), null));
        try {
            // try to truncate the resource
            mStorage.truncateResource(new SessionConfiguration(mResource.mProperties
                .getProperty(ConstructorProps.RESOURCE), null));
            fail();
        } catch (IllegalStateException exc) {
            // must be thrown
        }
        assertTrue(session.close());
        // truncating resource
        assertTrue(mStorage.truncateResource(new SessionConfiguration(mResource.mProperties
            .getProperty(ConstructorProps.RESOURCE), null)));
        // trying to truncate not existing resource
        assertFalse(mStorage.truncateResource(new SessionConfiguration("notExistingResource", null)));
        // check against resource, should not exist
        assertFalse(mStorage.existsResource(mResource.mProperties.getProperty(ConstructorProps.RESOURCE)));
        // creating a new resource, successful
        assertTrue(mStorage.createResource(mResource));
        // check if existing, successful
        assertTrue(mStorage.existsResource(mResource.mProperties.getProperty(ConstructorProps.RESOURCE)));
        // creating the same resource, unsuccessful
        assertFalse(mStorage.createResource(mResource));
    }

    @Test
    public void testGetSession() throws TTException {
        // creating a new resource, successful
        assertTrue(mStorage.createResource(mResource));
        // getting the session
        ISession session =
            mStorage.getSession(new SessionConfiguration(mResource.mProperties
                .getProperty(ConstructorProps.RESOURCE), null));
        // asserting new resource and bootstrap was successful
        assertEquals(0, session.getMostRecentVersion());
        // instance-check to have only one session per resource
        ISession sameSession =
            mStorage.getSession(new SessionConfiguration(mResource.mProperties
                .getProperty(ConstructorProps.RESOURCE), null));
        assertEquals(session, sameSession);
    }

    @Test
    public void testClose() throws TTException {
        // creating a new resource, successful
        assertTrue(mStorage.createResource(mResource));
        // generating a session for test of inlying close
        ISession session =
            mStorage.getSession(new SessionConfiguration(mResource.mProperties
                .getProperty(ConstructorProps.RESOURCE), null));
        // closing storage with inlying session
        assertTrue(mStorage.close());
        // check that second close is unsuccessful
        assertFalse(session.close());
        assertFalse(mStorage.close());
    }

    @Test
    public void testListResourcesAndLocation() throws TTException {
        // checking location
        assertEquals(CoreTestHelper.PATHS.PATH1.getFile(), mStorage.getLocation());
        // no resources, checking against it
        assertEquals(0, mStorage.listResources().length);
        // creating a resource and checking against the new resource
        assertTrue(mStorage.createResource(mResource));
        assertEquals(1, mStorage.listResources().length);
        assertEquals(mResource.mProperties.getProperty(ConstructorProps.RESOURCE),
            mStorage.listResources()[0]);
    }

    private void createStorage() throws TTException {
        // check if exists
        assertFalse(Storage.existsStorage(CoreTestHelper.PATHS.PATH1.getFile()));
        // Creating Storage, successful
        assertTrue(Storage.createStorage(CoreTestHelper.PATHS.PATH1.getConfig()));
        // Opening Storage and check it is not null
        mStorage = Storage.openStorage(CoreTestHelper.PATHS.PATH1.getFile());
        assertNotNull(mStorage);
    }

}
