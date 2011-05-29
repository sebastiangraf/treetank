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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.AbsIOFactory;
import org.treetank.settings.EStoragePaths;

/**
 * This class represents one concrete database for enabling several {@link ISession} objects.
 * 
 * @see IDatabase
 * @author Sebastian Graf, University of Konstanz
 */
public final class FileDatabase implements IDatabase {

    /** Central repository of all running sessions. */
    private static final ConcurrentMap<File, FileDatabase> DATABASEMAP =
        new ConcurrentHashMap<File, FileDatabase>();

    /** DatabaseConfiguration with fixed settings. */
    final DatabaseConfiguration mDatabaseConfiguration;

    /** File for storing the DB */
    public final File mFile;

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
    private FileDatabase(final File paramFile, final DatabaseConfiguration paramDBConf) throws AbsTTException {
        this.mFile = paramFile;
        this.mDatabaseConfiguration = paramDBConf;
    }

    /**
     * Creating a database. This includes loading the database configurations,
     * building up the structure and
     * preparing everything for login.
     * 
     * @param paramFile
     *            the file where the storage is
     * @param paramConf
     *            which are used for the database
     * @return true of creation is valid, false otherwise
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    public static synchronized boolean createDatabase(final File paramFile,
        final DatabaseConfiguration paramConf) throws TTIOException {
        try {
            boolean returnVal = true;
            if (paramFile.exists()) {
                returnVal = false;
            } else {
                returnVal = paramFile.mkdirs();
                if (returnVal) {
                    for (EStoragePaths paths : EStoragePaths.values()) {
                        final File toCreate = new File(paramFile, paths.getFile().getName());
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
            // if something was not correct, delete the partly created
            // substructure
            if (!returnVal) {
                recursiveDelete(paramFile);
            }
            return returnVal;
        } catch (final IOException exc) {
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
     *            where the database is located sessionConf a {@link SessionConfiguration} object to set up
     *            the session
     * @param paramSessionConf
     *            session conf for the new session
     * @return {@link IDatabase} instance.
     * @throws AbsTTException
     *             if something odd happens
     */
    public static synchronized IDatabase openDatabase(final File paramFile) throws AbsTTException {
        if (!paramFile.exists() && !createDatabase(paramFile, new DatabaseConfiguration.Builder().build())) {
            throw new TTUsageException("DB could not be created at location", paramFile.toString());
        }
        final FileDatabase database =
            new FileDatabase(paramFile, new DatabaseConfiguration.Builder().build());
        final FileDatabase returnVal = DATABASEMAP.putIfAbsent(paramFile, database);
        if (returnVal == null) {
            return database;
        } else {
            return returnVal;
        }

    }

    /**
     * This method forces the Database to close an existing instance.
     * 
     * @param paramFile
     *            where the database should be closed
     * @throws AbsTTException
     *             if something weird happens while closing
     */
    public static synchronized void closeDatabase(final File paramFile) throws AbsTTException {
        DATABASEMAP.remove(paramFile);
    }

    /** {@inheritDoc} */
    @Override
    public DatabaseConfiguration getDatabaseConf() {
        return mDatabaseConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return mDatabaseConfiguration.mBinaryVersion;
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
     * {@inheritDoc}
     * 
     * @throws AbsTTException
     */
    @Override
    public synchronized ISession getSession(final SessionConfiguration paramSessionConfiguration)
        throws AbsTTException {
        AbsIOFactory.registerInstance(mFile, mDatabaseConfiguration, paramSessionConfiguration);
        return new Session(this.mDatabaseConfiguration, paramSessionConfiguration);
    }

    private final void generateResource() {

    }

    /**
     * Checking if storage is valid.
     * 
     * @throws TTUsageException
     *             if storage is not valid
     */
    private void checkResource() throws TTUsageException {
        final int compareStructure = EStoragePaths.compareStructure(mFile);
        if (compareStructure != 0) {
            throw new TTUsageException("Storage has no valid storage structure."
                + " Compared to the specification, storage has", Integer.toString(compareStructure),
                "elements!");
        }
        final String version = DatabaseConfiguration.BINARY;

        final String storedVersions = getVersion();
        if (!version.equals(storedVersions)) {
            throw new TTUsageException("Versions Differ, Expected Version:", version, "but was",
                storedVersions);
        }

    }

}
