/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.treetank.access.conf.ContructorProps;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.IStorage;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.IBackendReader;
import org.treetank.io.IOUtils;
import org.treetank.page.IConstants;
import org.treetank.page.IndirectPage;
import org.treetank.page.NodePage;
import org.treetank.page.PageReference;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IReferencePage;

/**
 * This class represents one concrete database for enabling several {@link ISession} objects.
 * 
 * @see IStorage
 * @author Sebastian Graf, University of Konstanz
 */
public final class Storage implements IStorage {

    /** Central repository of all running databases. */
    private static final ConcurrentMap<File, Storage> STORAGEMAP = new ConcurrentHashMap<File, Storage>();

    /** Central repository of all running sessions. */
    private final Map<String, Session> mSessions;

    /** StorageConfiguration with fixed settings. */
    private final StorageConfiguration mStorageConfig;

    /**
     * Private constructor.
     * 
     * @param pStorageConf
     *            {@link StorageConfiguration} reference to configure the {@link IStorage}
     * @throws TTException
     *             Exception if something weird happens
     */
    private Storage(final StorageConfiguration pStorageConf) throws TTException {
        mStorageConfig = pStorageConf;
        mSessions = new HashMap<String, Session>();

    }

    // //////////////////////////////////////////////////////////
    // START Creation/Deletion of Storages /////////////////////
    // //////////////////////////////////////////////////////////
    /**
     * Creating a storage. This includes loading the storageconfiguration,
     * building up the structure and preparing everything for login.
     * 
     * 
     * @param pStorageConfig
     *            which are used for the storage, including storage location
     * @return true if creation is valid, false otherwise
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    public static synchronized boolean createStorage(final StorageConfiguration pStorageConfig)
        throws TTIOException {
        boolean returnVal = true;
        // if file is existing, skipping
        if (pStorageConfig.mFile.exists()) {
            return false;
        } else {
            returnVal = pStorageConfig.mFile.mkdirs();
            if (returnVal) {
                // creation of folder structure
                for (StorageConfiguration.Paths paths : StorageConfiguration.Paths.values()) {
                    final File toCreate = new File(pStorageConfig.mFile, paths.getFile().getName());
                    if (paths.isFolder()) {
                        returnVal = toCreate.mkdir();
                    } else {
                        try {
                            returnVal = toCreate.createNewFile();
                        } catch (final IOException exc) {
                            throw new TTIOException(exc);
                        }
                    }
                    if (!returnVal) {
                        break;
                    }
                }
            }
            // serialization of the config
            StorageConfiguration.serialize(pStorageConfig);
            // if something was not correct, delete the partly created
            // substructure
            if (!returnVal) {
                pStorageConfig.mFile.delete();
            }
            return returnVal;
        }
    }

    /**
     * Truncate a storage. This deletes all relevant data. All running sessions
     * must be closed beforehand.
     * 
     * @param pConf
     *            the storage at this path should be deleted.
     * @throws TTException
     *             any kind of false Treetank behaviour
     */
    public static synchronized void truncateStorage(final StorageConfiguration pConf) throws TTException {
        // check that database must be closed beforehand
        if (!STORAGEMAP.containsKey(pConf.mFile)) {
            if (existsStorage(pConf.mFile)) {
                final IStorage storage = new Storage(pConf);
                final File[] resources =
                    new File(pConf.mFile, StorageConfiguration.Paths.Data.getFile().getName()).listFiles();
                for (final File resource : resources) {
                    storage.truncateResource(new SessionConfiguration(resource.getName(), null));
                }
                storage.close();
                // instantiate the database for deletion
                IOUtils.recursiveDelete(pConf.mFile);
            }
        }
    }

    /**
     * Check if Storage exists or not at a given path.
     * 
     * @param pStoragePath
     *            to be checked.
     * @return true if existing, false otherwise.
     */
    public static synchronized boolean existsStorage(final File pStoragePath) {
        // if file is existing and folder is a tt-dataplace, delete it
        if (pStoragePath.exists() && StorageConfiguration.Paths.compareStructure(pStoragePath) == 0) {
            return true;
        } else {
            return false;
        }

    }

    // //////////////////////////////////////////////////////////
    // END Creation/Deletion of Databases ///////////////////////
    // //////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////
    // START Creation/Deletion of Resources /////////////////////
    // //////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean createResource(final ResourceConfiguration pResConf) throws TTException {
        boolean returnVal = true;
        // Setting the missing params in the settings, this overrides already
        // set data.
        final File path =
            new File(new File(pResConf.mProperties.getProperty(ContructorProps.STORAGEPATH),
                StorageConfiguration.Paths.Data.getFile().getName()), pResConf.mProperties
                .getProperty(ContructorProps.RESOURCE));
        // if file is existing, skipping
        if (path.exists()) {
            return false;
        } else {
            returnVal = path.mkdir();
            if (returnVal) {
                // creation of the folder structure
                for (ResourceConfiguration.Paths paths : ResourceConfiguration.Paths.values()) {
                    final File toCreate = new File(path, paths.getFile().getName());
                    if (paths.isFolder()) {
                        returnVal = toCreate.mkdir();
                    } else {
                        try {
                            returnVal = toCreate.createNewFile();
                        } catch (final IOException exc) {
                            throw new TTIOException(exc);
                        }
                    }
                    if (!returnVal) {
                        break;
                    }
                }
            }
            // serialization of the config
            ResourceConfiguration.serialize(pResConf);
            // if something was not correct, delete the partly created
            // substructure
            if (!returnVal) {
                throw new IllegalStateException(new StringBuilder("Failure, please remove folder ").append(
                    pResConf.mProperties.getProperty(ContructorProps.STORAGEPATH)).append(" manually!")
                    .toString());
            }

            // Boostrapping the Storage, this is quite dirty because of the initialization of the key, i
            // guess..however...
            bootstrap(this, pResConf);
            return returnVal;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public synchronized void truncateResource(final SessionConfiguration pResConf) throws TTException {
        final File resourceFile =
            new File(new File(mStorageConfig.mFile, StorageConfiguration.Paths.Data.getFile().getName()),
                pResConf.getResource());
        // check that database must be closed beforehand
        if (!mSessions.containsKey(resourceFile) && existsResource(pResConf.getResource())) {
            ISession session = getSession(pResConf);
            session.close();
            session.truncate();
        }
    }

    // //////////////////////////////////////////////////////////
    // END Creation/Deletion of Resources ///////////////////////
    // //////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////
    // START Opening of Databases ///////////////////////
    // //////////////////////////////////////////////////////////
    /**
     * Open database. A database can be opened only once. Afterwards the
     * singleton instance bound to the File is given back.
     * 
     * @param pFile
     *            where the database is located sessionConf a {@link SessionConfiguration} object to set up
     *            the session
     * @return {@link IStorage} instance.
     * @throws TTException
     *             if something odd happens
     */
    public static synchronized IStorage openStorage(final File pFile) throws TTException {
        if (!existsStorage(pFile)) {
            throw new TTUsageException("DB could not be opened (since it was not created?) at location",
                pFile.toString());
        }
        StorageConfiguration config = StorageConfiguration.deserialize(pFile);
        final Storage storage = new Storage(config);
        final IStorage returnVal = STORAGEMAP.putIfAbsent(pFile, storage);
        if (returnVal == null) {
            return storage;
        } else {
            return returnVal;
        }
    }

    // //////////////////////////////////////////////////////////
    // END Opening of Databases ///////////////////////
    // //////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////
    // START DB-Operations//////////////////////////////////
    // /////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ISession getSession(final SessionConfiguration pSessionConf) throws TTException {

        final File resourceFile =
            new File(new File(mStorageConfig.mFile, StorageConfiguration.Paths.Data.getFile().getName()),
                pSessionConf.getResource());
        Session returnVal = mSessions.get(resourceFile);
        if (returnVal == null) {
            if (!resourceFile.exists()) {
                throw new TTUsageException(
                    "Resource could not be opened (since it was not created?) at location", resourceFile
                        .toString());
            }
            ResourceConfiguration config =
                ResourceConfiguration.deserialize(mStorageConfig.mFile, pSessionConf.getResource());

            // reading first reference and instantiate this.
            final IBackendReader backendReader = config.mStorage.getReader();
            final PageReference firstRef = backendReader.readFirstReference();
            UberPage page = (UberPage)firstRef.getPage();
            backendReader.close();

            returnVal = new Session(this, config, pSessionConf, page);
            mSessions.put(pSessionConf.getResource(), returnVal);
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws TTException {
        for (final ISession session : mSessions.values()) {
            session.close();
        }
        STORAGEMAP.remove(mStorageConfig.mFile);
    }

    // //////////////////////////////////////////////////////////
    // End DB-Operations//////////////////////////////////
    // /////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Storage [mSessions=");
        builder.append(mSessions);
        builder.append(", mStorageConfig=");
        builder.append(mStorageConfig);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Closing a resource. This callback is necessary due to centralized
     * handling of all sessions within a database.
     * 
     * @param pResourceName
     *            to be closed
     * @return true if close successful, false otherwise
     */
    protected boolean removeSession(final String pResourceName) {
        return mSessions.remove(pResourceName) != null ? true : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsResource(String pResourceName) {
        final File resourceFile =
            new File(new File(mStorageConfig.mFile, StorageConfiguration.Paths.Data.getFile().getName()),
                pResourceName);
        // if file is existing and folder is a tt-dataplace, delete it
        if (resourceFile.exists() && ResourceConfiguration.Paths.compareStructure(resourceFile) == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] listResources() {
        return new File(mStorageConfig.mFile, StorageConfiguration.Paths.Data.getFile().getName()).list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getLocation() {
        return mStorageConfig.mFile;
    }

    /**
     * Boostraping a resource within this storage.
     * 
     * @param pStorage
     *            storage where the new resource should be created in.
     * @param pResourceConf
     *            related {@link ResourceConfiguration} for the new resource
     * @throws TTException
     */
    private static void bootstrap(final Storage pStorage, final ResourceConfiguration pResourceConf)
        throws TTException {
        SessionConfiguration config =
            new SessionConfiguration(pResourceConf.mProperties.getProperty(ContructorProps.RESOURCE), null);
        UberPage uberPage = new UberPage(0, new PageReference());

        // --- Create revision tree
        // ------------------------------------------------

        // Initialize revision tree to guarantee that there is a revision root
        // page.
        IReferencePage page = null;
        PageReference reference = uberPage.getReferences()[0];

        // Remaining levels.
        for (int i = 0, l = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; i < l; i++) {
            page = new IndirectPage();
            reference.setPage(page);
            reference = page.getReferences()[0];
        }

        final RevisionRootPage rrp = new RevisionRootPage(0, -1);
        reference.setPage(rrp);

        // --- Create node tree
        // ----------------------------------------------------

        // Initialize revision tree to guarantee that there is a revision root
        // page.
        page = null;
        reference = rrp.getIndirectPageReference();

        // Remaining levels.
        for (int i = 0, l = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; i < l; i++) {
            page = new IndirectPage();
            reference.setPage(page);
            reference = page.getReferences()[0];
        }

        final NodePage ndp = new NodePage(0);
        reference.setPage(ndp);

        Session session = new Session(pStorage, pResourceConf, config, uberPage);
        IPageWriteTrx trx = session.beginPageWriteTransaction(0, 0);
        trx.commit();
        trx.close();
        session.close();
    }

}
