package org.treetank.filelistener.file;

import static org.testng.Assert.*;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageAlreadyExistsException;
import org.treetank.filelistener.exceptions.StorageNotExistingException;
import org.treetank.filelistener.file.StorageManager;

/**
 * 
 * @author Andreas Rain
 *
 */
public class StorageManagerTest {

    /** Storage name */
    private final String mStorageName = "teststorage";
    
    @BeforeClass
    public void setUp() throws StorageAlreadyExistsException, TTException{
        StorageManager.createResource(mStorageName, StorageManager.BACKEND_INDEX_JCLOUDS);
    }
    
    @Test(enabled=true)
    public void testGetSession() throws StorageNotExistingException, TTException{
        ISession session = StorageManager.getSession(mStorageName);
        assertNotNull(session);
        assertNotNull(session.beginPageWriteTransaction());
        
        assertTrue(session.close());
    }
    
    @Test(enabled=true)
    public void testGetStorages(){
        List<String> storages = StorageManager.getResources();
        
        assertNotNull(storages);
        assertEquals(storages.size(), 1);
        assertEquals(storages.get(0), mStorageName);
    }
    
    @Test(enabled=true)
    public void testRemoveStorage() throws TTException, StorageNotExistingException{
        StorageManager.removeStorage(mStorageName);
        
        List<String> storages = StorageManager.getResources();
        
        assertNotNull(storages);
        assertEquals(storages.size(), 0);
    }
    
}
