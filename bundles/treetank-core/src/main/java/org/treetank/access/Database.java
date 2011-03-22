/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import org.slf4j.LoggerFactory;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.settings.EDatabaseSetting;
import org.treetank.settings.EStoragePaths;
import org.treetank.utils.LogWrapper;

/**
 * This class represents one concrete database for enabling several {@link ISession} objects.
 * 
 * @see IDatabase
 * @author Sebastian Graf, University of Konstanz
 */
public final class Database implements IDatabase {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(Database.class));

    /** Central repository of all running sessions. */
    private static final ConcurrentMap<File, Database> DATABASEMAP = new ConcurrentHashMap<File, Database>();

    /** Queue with all session registered. */
    private ISession mSession;

    /** DatabaseConfiguration with fixed settings. */
    private DatabaseConfiguration mDatabaseConfiguration;

    /** SessionConfiguration with variable settings. */
    private SessionConfiguration mSessionConfiguration;

    /**
     * Private constructor.
     * 
     * @param paramDBConf
     *            conf for Database
     * @param paramSessionConf
     *            conf for session
     * @throws AbsTTException
     *             Exception if something weird happens
     */
    private Database(final DatabaseConfiguration paramDBConf, final SessionConfiguration paramSessionConf)
        throws AbsTTException {
        this.mDatabaseConfiguration = paramDBConf;
        this.mSessionConfiguration = paramSessionConf;
        this.checkStorage();
    }

    /**
     * Creating a database. This includes loading the database configurations,
     * building up the structure and
     * preparing everything for login.
     * 
     * @param paramConf
     *            which are used for the database
     * @return true of creation is valid, false otherwise
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    public static synchronized boolean createDatabase(final DatabaseConfiguration paramConf)
        throws TTIOException {
        try {
            final File file = paramConf.getFile();
            boolean returnVal = true;
            if (file.exists()) {
                returnVal = false;
            } else {
                returnVal = file.mkdirs();
                if (returnVal) {
                    for (EStoragePaths paths : EStoragePaths.values()) {
                        final File toCreate = new File(paramConf.getFile(), paths.getFile().getName());
                        if (paths.isFolder()) {
                            returnVal = toCreate.mkdir();
                        } else {
                            returnVal = toCreate.createNewFile();
                        }
                        if (!returnVal) {
                            break;
                        }
                    }
                }
            }
            returnVal = paramConf.serialize();
            // if something was not correct, delete the partly created
            // substructure
            if (!returnVal) {
                recursiveDelete(file);
            }
            return returnVal;
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }
    }

    /**
     * Truncate a database. This deletes all relevant data. If there are
     * existing sessions against this
     * database, the method returns null.
     * 
     * @param paramFile
     *            the database at this path should be deleted.
     * @return true if removal is successful, false otherwise
     */
    public static synchronized boolean truncateDatabase(final File paramFile) {
        if (DATABASEMAP.containsKey(paramFile)) {
            return false;
        } else {
            return recursiveDelete(paramFile);
        }
    }

    /**
     * Open database. A database can be opened only once. Afterwards the
     * singleton instance bound to the File
     * is given back.
     * 
     * @param paramFile
     *            where the database is located
     * @return {@link IDatabase} instance.
     * @throws AbsTTException
     *             if something odd happens
     */
    public static synchronized IDatabase openDatabase(final File paramFile) throws AbsTTException {
        return openDatabase(paramFile, new SessionConfiguration());
    }

    /**
     * Open database. A database can be opened only once. Afterwards the
     * singleton instance bound to the File
     * is given back.
     * 
     * @param paramFile
     *            where the database is located sessionConf a {@link SessionConfiguration} object to set up
     *            the session
     * @param paramSessionConf
     *            session conf for the new session
     * @return {@link IDatabase} instance.
     * @throws AbsTTException
     *             if something odd happens
     */
    public static synchronized IDatabase openDatabase(final File paramFile,
        final SessionConfiguration paramSessionConf) throws AbsTTException {
        if (!paramFile.exists() && !createDatabase(new DatabaseConfiguration(paramFile))) {
            throw new TTUsageException("DB could not be created at location", paramFile.toString());
        }
        IDatabase database =
            DATABASEMAP.putIfAbsent(paramFile, new Database(new DatabaseConfiguration(paramFile),
                paramSessionConf));
        if (database == null) {
            database = DATABASEMAP.get(paramFile);
        }
        return database;
    }

    /**
     * This method forces the Database to close an existing instance.
     * 
     * @param paramFile
     *            where the database should be closed
     * @throws AbsTTException
     *             if something weird happens while closing
     */
    public static synchronized void forceCloseDatabase(final File paramFile) throws AbsTTException {
        final IDatabase database = DATABASEMAP.remove(paramFile);
        if (database != null) {
            database.close();
        }
    }

    /**
     * Closing a database. All {@link ISession} instances within this database
     * are closed.
     * 
     * @throws AbsTTException
     *             if close is not successful.
     */
    public synchronized void close() throws AbsTTException {
        if (mSession != null) {
            mSession.close();
        }
        if (mDatabaseConfiguration != null) {
            DATABASEMAP.remove(getFile(), this);
        }
        mDatabaseConfiguration = null;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws AbsTTException
     */
    @Override
    public synchronized ISession getSession() throws AbsTTException {
        if (mSession == null || mSession.isClosed()) {
            mSession = new Session(this.mDatabaseConfiguration, this.mSessionConfiguration);
        }
        return mSession;
    }
    
    /** {@inheritDoc} */
    @Override
    public synchronized DatabaseConfiguration getDatabaseConf() {
        return mDatabaseConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized File getFile() {
        if (mDatabaseConfiguration == null) {
            return null;
        } else {
            return mDatabaseConfiguration.getFile();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int[] getVersion() {
        final int[] versions =
            {
                Integer.parseInt(mDatabaseConfiguration.getProps().getProperty(
                    EDatabaseSetting.VERSION_MAJOR.name())),
                Integer.parseInt(mDatabaseConfiguration.getProps().getProperty(
                    EDatabaseSetting.VERSION_MINOR.name())),
                Integer.parseInt(mDatabaseConfiguration.getProps().getProperty(
                    EDatabaseSetting.VERSION_FIX.name()))
            };
        return versions;
    }

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param paramFile
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    private static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

    /**
     * Checking if storage is valid.
     * 
     * @throws TTUsageException
     *             if storage is not valid
     */
    private void checkStorage() throws TTUsageException {
        final int compareStructure = EStoragePaths.compareStructure(getFile());
        if (compareStructure != 0) {
            throw new TTUsageException("Storage has no valid storage structure."
                + " Compared to the specification, storage has", Integer.toString(compareStructure),
                "elements!");
        }
        final int[] versions =
            {
                Integer.parseInt(EDatabaseSetting.VERSION_MAJOR.getStandardProperty()),
                Integer.parseInt(EDatabaseSetting.VERSION_MINOR.getStandardProperty()),
                Integer.parseInt(EDatabaseSetting.VERSION_FIX.getStandardProperty())
            };
        final int[] storedVersions = getVersion();
        if (storedVersions[0] < versions[0]) {
            throw new TTUsageException("Version Major expected:", Integer.toString(storedVersions[0]),
                "but was", Integer.toString(versions[0]));
        } else {
            if (storedVersions[1] < versions[1]) {
                throw new TTUsageException("Version Minor expected:", Integer
                    .toString(storedVersions[1]), "but was", Integer.toString(versions[1]));
            } else {
                if (storedVersions[2] < versions[2]) {
                    throw new TTUsageException("Version Fix expected:", Integer
                        .toString(storedVersions[2]), "but was", Integer.toString(versions[2]));
                }
            }
        }

    }
}
