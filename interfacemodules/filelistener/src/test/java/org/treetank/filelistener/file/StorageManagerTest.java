package org.treetank.filelistener.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageAlreadyExistsException;
import org.treetank.filelistener.exceptions.StorageNotExistingException;
import org.treetank.filelistener.file.node.FileNodeFactory;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory;

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
        StorageManager.createResource(mStorageName, new ModuleSetter().setNodeFacClass(FileNodeFactory.class)
            .setMetaFacClass(FilelistenerMetaPageFactory.class).createModule());
    }

    @AfterMethod
    public void tearDown() throws TTException, StorageNotExistingException {
        StorageManager.removeStorage(mStorageName);
    }

    /**
     * @throws StorageNotExistingException
     * @throws TTException
     */
    @Test
    public void testGetSession() throws StorageNotExistingException, TTException {
        ISession session = StorageManager.getSession(mStorageName);
        assertNotNull(session);
        assertNotNull(session.beginPageWriteTransaction());

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
     * @throws StorageNotExistingException
     */
    @Test
    public void testRemoveStorage() throws TTException, StorageNotExistingException {
        StorageManager.removeStorage(mStorageName);

        List<String> storages = StorageManager.getResources();

        assertNotNull(storages);
        assertEquals(storages.size(), 0);
    }

}
