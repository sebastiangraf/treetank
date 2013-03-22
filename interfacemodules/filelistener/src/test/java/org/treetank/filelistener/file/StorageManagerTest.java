package org.treetank.filelistener.file;

import static org.testng.Assert.*;

import java.util.List;

import org.testng.TestRunner;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageAlreadyExistsException;
import org.treetank.filelistener.exceptions.StorageNotExistingException;
import org.treetank.filelistener.file.StorageManager;
import org.treetank.filelistener.file.node.FileNodeFactory;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory;
import org.treetank.io.IBackend;
import org.treetank.io.jclouds.JCloudsStorage;
import org.treetank.revisioning.IRevisioning;
import org.treetank.revisioning.SlidingSnapshot;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

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
    @BeforeClass
    public void setUp() throws StorageAlreadyExistsException, TTException{
        StorageManager.createResource(mStorageName, new ModuleSetter().setNodeFacClass(FileNodeFactory.class).setMetaFacClass(FilelistenerMetaPageFactory.class)
            .setRevisioningClass(SlidingSnapshot.class).setBackendClass(JCloudsStorage.class).createModule());
    }
    
    /**
     * @throws StorageNotExistingException
     * @throws TTException
     */
    @Test(enabled=true)
    public void testGetSession() throws StorageNotExistingException, TTException{
        ISession session = StorageManager.getSession(mStorageName);
        assertNotNull(session);
        assertNotNull(session.beginPageWriteTransaction());
        
        assertTrue(session.close());
    }
    
    /**
     * 
     */
    @Test(enabled=true)
    public void testGetStorages(){
        List<String> storages = StorageManager.getResources();
        
        assertNotNull(storages);
        assertEquals(storages.size(), 1);
        assertEquals(storages.get(0), mStorageName);
    }
    
    /**
     * @throws TTException
     * @throws StorageNotExistingException
     */
    @Test(enabled=true)
    public void testRemoveStorage() throws TTException, StorageNotExistingException{
        StorageManager.removeStorage(mStorageName);
        
        List<String> storages = StorageManager.getResources();
        
        assertNotNull(storages);
        assertEquals(storages.size(), 0);
    }
    
}
