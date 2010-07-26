/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.access;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.EStoragePaths;

/**
 * This class represents one concrete database for enabling several {@link ISession} objects.
 * 
 * @see IDatabase
 * @author Sebastian Graf, University of Konstanz
 */
public final class Database implements IDatabase {

    /** Central repository of all running sessions. */
    private static final ConcurrentMap<File, Database> DATABASEMAP =
        new ConcurrentHashMap<File, Database>();

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
     * @throws TreetankException
     *             Exception if something weird happens
     */
    private Database(final DatabaseConfiguration paramDBConf, final SessionConfiguration paramSessionConf)
        throws TreetankException {
        this.mDatabaseConfiguration =
            paramDBConf;
        this.mSessionConfiguration =
            paramSessionConf;
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
     * @throws TreetankIOException
     *             if something odd happens within the creation process.
     */
    public static synchronized boolean createDatabase(final DatabaseConfiguration paramConf)
        throws TreetankIOException {
        try {
            final File file =
                paramConf.getFile();
            boolean returnVal =
                true;
            if (file.exists()) {
                returnVal =
                    false;
            } else {
                returnVal =
                    file.mkdirs();
                if (returnVal) {
                    for (EStoragePaths paths : EStoragePaths.values()) {
                        final File toCreate =
                            new File(paramConf.getFile(), paths.getFile().getName());
                        if (paths.isFolder()) {
                            returnVal =
                                toCreate.mkdir();
                        } else {
                            returnVal =
                                toCreate.createNewFile();
                        }
                        if (!returnVal) {
                            break;
                        }
                    }
                }
            }
            returnVal =
                paramConf.serialize();
            // if something was not correct, delete the partly created
            // substructure
            if (!returnVal) {
                recursiveDelete(file);
            }
            return returnVal;
        } catch (final IOException exc) {
            throw new TreetankIOException(exc);
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
     * @throws TreetankException
     *             if something odd happens
     */
    public static synchronized IDatabase openDatabase(final File paramFile) throws TreetankException {
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
     * @throws TreetankException
     *             if something odd happens
     */
    public static synchronized IDatabase openDatabase(final File paramFile,
        final SessionConfiguration paramSessionConf) throws TreetankException {
        if (!paramFile.exists() && !createDatabase(new DatabaseConfiguration(paramFile))) {
            throw new TreetankUsageException("DB could not be created at location", paramFile.toString());
        }
        IDatabase database =
            DATABASEMAP.putIfAbsent(paramFile, new Database(new DatabaseConfiguration(paramFile),
                paramSessionConf));
        if (database == null) {
            database =
                DATABASEMAP.get(paramFile);
        }
        return database;
    }

    /**
     * This method forces the Database to close an existing instance.
     * 
     * @param paramFile
     *            where the database should be closed
     * @throws TreetankException
     *             if something weird happens while closing
     */
    public static synchronized void forceCloseDatabase(final File paramFile) throws TreetankException {
        final IDatabase database =
            DATABASEMAP.remove(paramFile);
        if (database != null) {
            database.close();
        }
    }

    /**
     * Closing a database. All {@link ISession} instances within this database
     * are closed.
     * 
     * @throws TreetankException
     *             if close is not successful.
     */
    public synchronized void close() throws TreetankException {
        if (mSession != null) {
            mSession.close();
        }
        if (mDatabaseConfiguration != null) {
            DATABASEMAP.remove(getFile(), this);
        }
        this.mDatabaseConfiguration =
            null;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TreetankException
     */
    @Override
    public synchronized ISession getSession() throws TreetankException {
        if (mSession == null || mSession.isClosed()) {
            mSession =
                new Session(this.mDatabaseConfiguration, this.mSessionConfiguration);
        }
        return mSession;
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
            new int[3];
        versions[0] =
            Integer.parseInt(mDatabaseConfiguration.getProps().getProperty(
                EDatabaseSetting.VERSION_MAJOR.name()));
        versions[1] =
            Integer.parseInt(mDatabaseConfiguration.getProps().getProperty(
                EDatabaseSetting.VERSION_MINOR.name()));
        versions[2] =
            Integer.parseInt(mDatabaseConfiguration.getProps().getProperty(
                EDatabaseSetting.VERSION_FIX.name()));
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
     * @throws TreetankUsageException
     *             if storage is not valid
     */
    private void checkStorage() throws TreetankUsageException {
        final int compareStructure =
            EStoragePaths.compareStructure(getFile());
        if (compareStructure != 0) {
            throw new TreetankUsageException("Storage has no valid storage structure."
                + " Compared to the specification, storage has", Integer.toString(compareStructure),
                "elements!");
        }
        final int[] versions =
            new int[3];
        versions[0] =
            Integer.parseInt(EDatabaseSetting.VERSION_MAJOR.getStandardProperty());
        versions[1] =
            Integer.parseInt(EDatabaseSetting.VERSION_MINOR.getStandardProperty());
        versions[2] =
            Integer.parseInt(EDatabaseSetting.VERSION_FIX.getStandardProperty());
        final int[] storedVersions =
            getVersion();
        if (storedVersions[0] < versions[0]) {
            throw new TreetankUsageException("Version Major expected:", Integer.toString(storedVersions[0]),
                "but was", Integer.toString(versions[0]));
        } else {
            if (storedVersions[1] < versions[1]) {
                throw new TreetankUsageException("Version Minor expected:", Integer
                    .toString(storedVersions[1]), "but was", Integer.toString(versions[1]));
            } else {
                if (storedVersions[2] < versions[2]) {
                    throw new TreetankUsageException("Version Fix expected:", Integer
                        .toString(storedVersions[2]), "but was", Integer.toString(versions[2]));
                }
            }
        }

    }
}
