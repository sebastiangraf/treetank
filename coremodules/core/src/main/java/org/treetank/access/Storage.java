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

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.treetank.access.conf.ConstructorProps;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.BackendWriterProxy;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendReader;
import org.treetank.io.IOUtils;
import org.treetank.io.LogKey;
import org.treetank.io.LogValue;

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
    protected final Map<String, Session> mSessions;

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
        mSessions = new ConcurrentHashMap<String, Session>();

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
        if (!pStorageConfig.mFile.exists() && pStorageConfig.mFile.mkdirs()) {
            returnVal =
                IOUtils.createFolderStructure(pStorageConfig.mFile, StorageConfiguration.Paths.values());
            // serialization of the config
            StorageConfiguration.serialize(pStorageConfig);
            // if something was not correct, delete the partly created
            // substructure
            if (!returnVal) {
                pStorageConfig.mFile.delete();
            }
            return returnVal;
        } else {
            return false;
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
        if (pStoragePath.exists()
            && IOUtils.compareStructure(pStoragePath, StorageConfiguration.Paths.values()) == 0) {
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
        final File path = new File(pResConf.mProperties.getProperty(ConstructorProps.RESOURCEPATH));
        if (!path.exists() && path.mkdir()) {
            returnVal = IOUtils.createFolderStructure(path, ResourceConfiguration.Paths.values());

            // serialization of the config
            ResourceConfiguration.serialize(pResConf);

            // Boostrapping the Storage, this is quite dirty because of the initialization of the key, i
            // guess..however...
            bootstrap(this, pResConf);
            return returnVal;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public synchronized boolean truncateResource(final SessionConfiguration pSesConf) throws TTException {
        // check that database must be closed beforehand
        checkState(!mSessions.containsKey(pSesConf.getResource()),
            "Please close all session before truncating!");
        if (existsResource(pSesConf.getResource())) {
            ISession session = getSession(pSesConf);
            if (session.truncate()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
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
        checkState(existsStorage(pFile), "DB could not be opened (since it was not created?) at location %s",
            pFile);
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
        Session returnVal = mSessions.get(pSessionConf.getResource());
        if (returnVal == null) {
            checkState(resourceFile.exists(),
                "Resource could not be opened (since it was not created?) at location %s", resourceFile);
            ResourceConfiguration config =
                ResourceConfiguration.deserialize(mStorageConfig.mFile, pSessionConf.getResource());
            config.mBackend.initialize();
            // reading first reference and instantiate this.
            final IBackendReader backendReader = config.mBackend.getReader();
            UberBucket bucket = backendReader.readUber();
            backendReader.close();

            returnVal = new Session(this, config, pSessionConf, bucket);
            mSessions.put(pSessionConf.getResource(), returnVal);
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean close() throws TTException {
        for (final ISession session : mSessions.values()) {
            session.close();
        }
        return STORAGEMAP.remove(mStorageConfig.mFile) != null;
    }

    // //////////////////////////////////////////////////////////
    // End DB-Operations//////////////////////////////////
    // /////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsResource(String pResourceName) {
        final File resourceFile =
            new File(new File(mStorageConfig.mFile, StorageConfiguration.Paths.Data.getFile().getName()),
                pResourceName);
        return resourceFile.exists();
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

        final IBackend storage = pResourceConf.mBackend;
        storage.initialize();
        final BackendWriterProxy writer =
            new BackendWriterProxy(storage.getWriter(), new File(pResourceConf.mProperties
                .getProperty(org.treetank.access.conf.ConstructorProps.RESOURCEPATH)),
                pResourceConf.mNodeFac, pResourceConf.mMetaFac);
        
        
        
        UberBucket uberBucket = new UberBucket(1, 0, 1);
        long newBucketKey = uberBucket.incrementBucketCounter();
        uberBucket.setReferenceKey(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, newBucketKey);
        uberBucket.setReferenceHash(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, IConstants.NON_HASHED);

        // --- Create revision tree
        // ------------------------------------------------
        // Initialize revision tree to guarantee that there is a revision root
        // bucket.

        IReferenceBucket bucket;
        LogKey key;
        for (int i = 0; i < IConstants.INDIRECT_BUCKET_COUNT.length; i++) {
            bucket = new IndirectBucket(newBucketKey);
            newBucketKey = uberBucket.incrementBucketCounter();
            bucket.setReferenceKey(0, newBucketKey);
            bucket.setReferenceHash(0, IConstants.NON_HASHED);
            key = new LogKey(true, i, 0);
            writer.put(key, new LogValue(bucket, bucket));
        }

        RevisionRootBucket revBucket = new RevisionRootBucket(newBucketKey, 0, 0);

        newBucketKey = uberBucket.incrementBucketCounter();
        // establishing fresh MetaBucket
        MetaBucket metaBucker = new MetaBucket(newBucketKey);
        revBucket.setReferenceKey(RevisionRootBucket.META_REFERENCE_OFFSET, newBucketKey);
        revBucket.setReferenceHash(RevisionRootBucket.META_REFERENCE_OFFSET, IConstants.NON_HASHED);

        newBucketKey = uberBucket.incrementBucketCounter();
        IndirectBucket indirectBucket = new IndirectBucket(newBucketKey);
        revBucket.setReferenceKey(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, newBucketKey);
        revBucket.setReferenceHash(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, IConstants.NON_HASHED);

        // --- Create node tree
        // ----------------------------------------------------

        // Initialize revision tree to guarantee that there is a revision root
        // bucket.

        bucket = indirectBucket;

        for (int i = 0; i < IConstants.INDIRECT_BUCKET_COUNT.length; i++) {
            newBucketKey = uberBucket.incrementBucketCounter();
            bucket.setReferenceKey(0, newBucketKey);
            bucket.setReferenceHash(0, IConstants.NON_HASHED);
            key = new LogKey(false, i, 0);
            writer.put(key, new LogValue(bucket, bucket));
            bucket = new IndirectBucket(newBucketKey);
        }

        final NodeBucket ndp = new NodeBucket(newBucketKey, IConstants.NULL_NODE);
        key = new LogKey(false, IConstants.INDIRECT_BUCKET_COUNT.length, 0);
        writer.put(key, new LogValue(ndp, ndp));

        try {
            writer.commit(uberBucket, metaBucker, revBucket).get();
            writer.closeFormerLog();
        } catch (InterruptedException | ExecutionException exc) {
            throw new TTIOException(exc);
        }
        writer.close();
        storage.close();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mSessions", mSessions).add("mStorageConfig", mStorageConfig)
            .toString();
    }

}
