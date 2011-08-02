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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.AbsIOFactory;

/**
 * This class represents one concrete database for enabling several {@link ISession} objects.
 * 
 * @see IDatabase
 * @author Sebastian Graf, University of Konstanz
 */
public final class Database implements IDatabase {

    /** Central repository of all running sessions. */
    private static final ConcurrentMap<File, Database> DATABASEMAP = new ConcurrentHashMap<File, Database>();

    /** DatabaseConfiguration with fixed settings. */
    final DatabaseConfiguration mDBConfig;

    /**
     * Private constructor.
     * 
     * @param paramDBConf
     *            {@link DatabaseConfiguration} reference to configure the {@link IDatabase}
     * @throws AbsTTException
     *             Exception if something weird happens
     */
    private Database(final DatabaseConfiguration paramDBConf) throws AbsTTException {
        mDBConfig = paramDBConf;
    }

    /**
     * Creating a database. This includes loading the database configurations,
     * building up the structure and preparing everything for login.
     * 
     * @param paramFile
     *            the file where the storage is
     * @param paramConf
     *            which are used for the database
     * @return true if creation is valid, false otherwise
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    public static synchronized boolean createDatabase(final File paramFile,
        final DatabaseConfiguration.Builder paramConf) throws TTIOException {
        // try {
        boolean returnVal = true;
        if (paramFile.exists()) {
            returnVal = false;
        } else {
            returnVal = paramFile.mkdirs();
            // if (returnVal) {
            // for (EStoragePaths paths : EStoragePaths.values()) {
            // final File toCreate = new File(paramFile, paths.getFile().getName());
            // if (paths.isFolder()) {
            // returnVal = toCreate.mkdir();
            // } else {
            // returnVal = toCreate.createNewFile();
            // }
            // if (!returnVal) {
            // break;
            // }
            // }
            // }
        }
        // if something was not correct, delete the partly created
        // substructure
        if (!returnVal) {
            paramFile.delete();
        }
        return returnVal;
        // } catch (final IOException exc) {
        // throw new TTIOException(exc);
        // }
    }

    /**
     * Truncate a database. This deletes all relevant data. If there are
     * existing sessions against this database, the method returns null.
     * 
     * @param paramFile
     *            the database at this path should be deleted.
     * @return true if removal is successful, false otherwise
     * @throws TTIOException
     */
    public static synchronized void truncateDatabase(final File paramFile) throws TTIOException {
        // check that database must be closed beforehand
        if (!DATABASEMAP.containsKey(paramFile)) {
            AbsIOFactory.truncateStorage(paramFile);
        }
    }

    /**
     * Open database. A database can be opened only once. Afterwards the
     * singleton instance bound to the File is given back.
     * 
     * @param paramFile
     *            where the database is located sessionConf a {@link SessionConfiguration} object to set up
     *            the session
     * @return {@link IDatabase} instance.
     * @throws AbsTTException
     *             if something odd happens
     */
    public static synchronized IDatabase openDatabase(final File paramFile) throws AbsTTException {
        if (!paramFile.exists()) {
            throw new TTUsageException("DB could not be opened (since it was not created?) at location",
                paramFile.toString());
        }
        final Database database =
            new Database(new DatabaseConfiguration.Builder().setFile(paramFile).build());
        final Database returnVal = DATABASEMAP.putIfAbsent(paramFile, database);
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
        return mDBConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return mDBConfig.mBinaryVersion;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws AbsTTException
     */
    @Override
    public synchronized ISession getSession(final SessionConfiguration.Builder paramSessionConfig)
        throws AbsTTException {
        paramSessionConfig.setDBConfig(mDBConfig);
        final SessionConfiguration config = paramSessionConfig.build();
        final File storageFile = config.mPath;
        AbsIOFactory.registerInstance(storageFile, mDBConfig, config);
        final boolean bla = AbsIOFactory.getInstance(config).exists();
        return new Session(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.mDBConfig);
        return builder.toString();
    }

}
