package org.treetank.filelistener.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageAlreadyExistsException;
import org.treetank.filelistener.exceptions.ResourceNotExistingException;
import org.treetank.filelistener.file.data.FileDataFactory;
import org.treetank.filelistener.file.data.FilelistenerMetaDataFactory;
import org.treetank.io.IOUtils;

/**
 * 
 * @author Andreas Rain
 * 
 */
public class StorageManagerTest {

    /** Storage name */
    private final String mStorageName = "teststorage";

    /**
     * @throws StorageAlreadyExistsException
     * @throws TTException
     */
    @BeforeMethod
    public void setUp() throws StorageAlreadyExistsException, TTException {
        IOUtils.recursiveDelete(new File(StorageManager.ROOT_PATH));
        StorageManager.createResource(mStorageName, new ModuleSetter().setDataFacClass(FileDataFactory.class)
            .setMetaFacClass(FilelistenerMetaDataFactory.class).createModule());
    }

    @AfterMethod
    public void tearDown() throws TTException, ResourceNotExistingException {
        StorageManager.removeResource(mStorageName);
    }

    /**
     * @throws ResourceNotExistingException
     * @throws TTException
     */
    @Test
    public void testGetSession() throws ResourceNotExistingException, TTException {
        ISession session = StorageManager.getSession(mStorageName);
        assertNotNull(session);
        assertNotNull(session.beginBucketWtx());
        assertTrue(session.close());
    }

    /**
     * 
     */
    @Test
    public void testGetStorages() {
        List<String> storages = StorageManager.getResources();
        assertNotNull(storages);
        assertEquals(storages.size(), 1);
        assertEquals(storages.get(0), mStorageName);
    }

    /**
     * @throws TTException
     * @throws ResourceNotExistingException
     */
    @Test(enabled = false) // Not enabled because used in tear down already.
    public void testRemoveResource() throws TTException, ResourceNotExistingException {
        StorageManager.removeResource(mStorageName);

        List<String> storages = StorageManager.getResources();

        assertNotNull(storages);
        assertEquals(storages.size(), 0);
    }

}
