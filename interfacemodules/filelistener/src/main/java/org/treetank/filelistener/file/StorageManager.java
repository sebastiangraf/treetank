package org.treetank.filelistener.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.treetank.access.Storage;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageAlreadyExistsException;
import org.treetank.filelistener.exceptions.StorageNotExistingException;
import org.treetank.filelistener.file.node.FileNodeFactory;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.io.IOUtils;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Andreas Rain
 *
 */
public class StorageManager {

    /**
     * Indizes provided by this class to determine which backend has been
     * chosen.
     */
    public static final int BACKEND_INDEX_JCLOUDS = 0;

    /**
     * The rootpath of where the filelistener saves application dependent data.
     */
    public static final String ROOT_PATH = new StringBuilder().append(System.getProperty("user.home"))
        .append(File.separator).append(".treetank").append(File.separator).toString();

    /**
     * The path where the storage configurations are to find.
     */
    public static final String STORAGE_PATH = new StringBuilder().append(ROOT_PATH).append(
        "storage").append(File.separator).toString();

    /**
     * Create a new storage with the given name and backend.
     * 
     * @param name
     * @param module
     * @return true if successful
     * @throws StorageAlreadyExistsException
     * @throws TTException
     */
    public static boolean createResource(String name, AbstractModule module) throws StorageAlreadyExistsException,
        TTException {
        File file = new File(ROOT_PATH);
        File storageFile = new File(STORAGE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        
        File resourceFile = new File(STORAGE_PATH + File.separator + "resources" + File.separator + name);

        if (resourceFile.exists()) {
            throw new StorageAlreadyExistsException();
        } else {
            StorageConfiguration configuration = new StorageConfiguration(storageFile);                                                                      

            Injector injector =
                Guice.createInjector(module);
            IBackendFactory backend = injector.getInstance(IBackendFactory.class);
            IRevisioning revision = injector.getInstance(IRevisioning.class);

            // Creating and opening the storage.
            // Making it ready for usage.
            Storage.truncateStorage(configuration);
            Storage.createStorage(configuration);

            IStorage storage = Storage.openStorage(storageFile);

            Properties props = StandardSettings.getProps(storageFile.getAbsolutePath(), name);
            ResourceConfiguration mResourceConfig =
                new ResourceConfiguration(props, backend, revision, new FileNodeFactory(),
                    new FilelistenerMetaPageFactory());

            storage.createResource(mResourceConfig);
        }

        return true;
    }

    /**
     * Retrieve a list of all storages.
     * 
     * @return a list of all storage names
     */
    public static List<String> getResources() {
        File resources = new File(STORAGE_PATH + File.separator + "/resources");
        File[] children = resources.listFiles();

        if (children == null) {
            return new ArrayList<String>();
        }

        List<String> storages = new ArrayList<String>();

        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirectory())
                storages.add(children[i].getName());
        }

        return storages;
    }

    /**
     * Retrieve a session from the system for the given Storagename
     * 
     * @param resourceName
     * @return a new {@link ISession} for the resource
     * @throws StorageNotExistingException
     * @throws TTException
     */
    public static ISession getSession(String resourceName) throws StorageNotExistingException, TTException {
        File storageFile = new File(STORAGE_PATH);

        ISession session = null;

        if (!storageFile.exists()) {
            throw new StorageNotExistingException();
        } else {
            new StorageConfiguration(storageFile);

            IStorage storage = Storage.openStorage(storageFile);

            session = storage.getSession(new SessionConfiguration(resourceName, null));

        }

        return session;
    }

    /**
     * Via this method you can
     * remove a storage from the system.
     * 
     * It will delete the whole folder of the configuration.
     * 
     * @param pStorageName
     * @throws TTException
     * @throws StorageNotExistingException
     */
    public static void removeStorage(String pStorageName) throws TTException, StorageNotExistingException {
        File storageConfiguration =
            new File(new StringBuilder().append(STORAGE_PATH).toString());

        IOUtils.recursiveDelete(storageConfiguration);

    }

}
