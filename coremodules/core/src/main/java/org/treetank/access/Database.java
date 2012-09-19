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

import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.IConstants;
import org.treetank.io.IOUtils;

/**
 * This class represents one concrete database for enabling several {@link ISession} objects.
 * 
 * @see IDatabase
 * @author Sebastian Graf, University of Konstanz
 */
public final class Database implements IDatabase {

    /** Central repository of all running databases. */
    private static final ConcurrentMap<File, Database> DATABASEMAP = new ConcurrentHashMap<File, Database>();

    /** Central repository of all running sessions. */
    private final Map<String, Session> mSessions;

    /** DatabaseConfiguration with fixed settings. */
    private final DatabaseConfiguration mDBConfig;

    /**
     * Private constructor.
     * 
     * @param paramDBConf
     *            {@link ResourceConfiguration} reference to configure the {@link IDatabase}
     * @throws TTException
     *             Exception if something weird happens
     */
    private Database(final DatabaseConfiguration paramDBConf) throws TTException {
        mDBConfig = paramDBConf;
        mSessions = new HashMap<String, Session>();

    }

    // //////////////////////////////////////////////////////////
    // START Creation/Deletion of Databases /////////////////////
    // //////////////////////////////////////////////////////////
    /**
     * Creating a database. This includes loading the database configuration,
     * building up the structure and preparing everything for login.
     * 
     * 
     * @param pDBConfig
     *            which are used for the database, including storage location
     * @return true if creation is valid, false otherwise
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    public static synchronized boolean createDatabase(final DatabaseConfiguration pDBConfig)
        throws TTIOException {
        boolean returnVal = true;
        // if file is existing, skipping
        if (pDBConfig.mFile.exists()) {
            return false;
        } else {
            returnVal = pDBConfig.mFile.mkdirs();
            if (returnVal) {
                // creation of folder structure
                for (DatabaseConfiguration.Paths paths : DatabaseConfiguration.Paths.values()) {
                    final File toCreate = new File(pDBConfig.mFile, paths.getFile().getName());
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
            DatabaseConfiguration.serialize(pDBConfig);
            // if something was not correct, delete the partly created
            // substructure
            if (!returnVal) {
                pDBConfig.mFile.delete();
            }
            return returnVal;
        }
    }

    /**
     * Truncate a database. This deletes all relevant data. All running sessions
     * must be closed beforehand.
     * 
     * @param pConf
     *            the database at this path should be deleted.
     * @throws TTException
     *             any kind of false Treetank behaviour
     */
    public static synchronized void truncateDatabase(final DatabaseConfiguration pConf) throws TTException {
        // check that database must be closed beforehand
        if (!DATABASEMAP.containsKey(pConf.mFile)) {
            if (existsDatabase(pConf.mFile)) {
                final IDatabase database = new Database(pConf);
                final File[] resources =
                    new File(pConf.mFile, DatabaseConfiguration.Paths.Data.getFile().getName()).listFiles();
                for (final File resource : resources) {
                    database.truncateResource(new SessionConfiguration(resource.getName(), null));
                }
                database.close();
                // instantiate the database for deletion
                IOUtils.recursiveDelete(pConf.mFile);
            }
        }
    }

    /**
     * Check if Database exists or not at a given path.
     * 
     * @param pStoragePath
     *            to be checked.
     * @return true if existing, false otherwise.
     */
    public static synchronized boolean existsDatabase(final File pStoragePath) {
        // if file is existing and folder is a tt-dataplace, delete it
        if (pStoragePath.exists() && DatabaseConfiguration.Paths.compareStructure(pStoragePath) == 0) {
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
    public synchronized boolean createResource(final ResourceConfiguration pResConf) throws TTIOException {
        boolean returnVal = true;
        // Setting the missing params in the settings, this overrides already
        // set data.
        final File path =
            new File(new File(pResConf.mProperties.getProperty(IConstants.DBFILE),
                DatabaseConfiguration.Paths.Data.getFile().getName()), pResConf.mProperties
                .getProperty(IConstants.RESOURCE));
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
                    pResConf.mProperties.getProperty(IConstants.DBFILE)).append(" manually!").toString());
            }
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
            new File(new File(mDBConfig.mFile, DatabaseConfiguration.Paths.Data.getFile().getName()),
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
     * @return {@link IDatabase} instance.
     * @throws TTException
     *             if something odd happens
     */
    public static synchronized IDatabase openDatabase(final File pFile) throws TTException {
        if (!existsDatabase(pFile)) {
            throw new TTUsageException("DB could not be opened (since it was not created?) at location",
                pFile.toString());
        }
        DatabaseConfiguration config = DatabaseConfiguration.deserialize(pFile);
        final Database database = new Database(config);
        final IDatabase returnVal = DATABASEMAP.putIfAbsent(pFile, database);
        if (returnVal == null) {
            return database;
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
            new File(new File(mDBConfig.mFile, DatabaseConfiguration.Paths.Data.getFile().getName()),
                pSessionConf.getResource());
        Session returnVal = mSessions.get(resourceFile);
        if (returnVal == null) {
            if (!resourceFile.exists()) {
                throw new TTUsageException(
                    "Resource could not be opened (since it was not created?) at location", resourceFile
                        .toString());
            }
            ResourceConfiguration config =
                ResourceConfiguration.deserialize(mDBConfig.mFile, pSessionConf.getResource());

            returnVal = new Session(this, config, pSessionConf);
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
        DATABASEMAP.remove(mDBConfig.mFile);
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
        builder.append("Database [mSessions=");
        builder.append(mSessions);
        builder.append(", mDBConfig=");
        builder.append(mDBConfig);
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
            new File(new File(mDBConfig.mFile, DatabaseConfiguration.Paths.Data.getFile().getName()),
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
        return new File(mDBConfig.mFile, DatabaseConfiguration.Paths.Data.getFile().getName()).list();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getLocation() {
        return mDBConfig.mFile;
    }
}
