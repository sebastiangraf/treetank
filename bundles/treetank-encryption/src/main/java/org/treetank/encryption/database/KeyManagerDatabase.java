/**
 * Copyright (c) 2011, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package org.treetank.encryption.database;

import java.io.File;
import java.util.SortedMap;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import org.treetank.encrpytion.exception.TTEncryptionException;
import org.treetank.encryption.database.model.KeyManager;

/**
 * Berkeley implementation of a persistent key manager database. That
 * means that all data is stored in this database and it is never removed.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class KeyManagerDatabase extends AbsKeyDatabase {

    /**
     * Berkeley Environment for the database.
     */
    private Environment mEnv;

    /**
     * Berkeley Entity store instance for the database.
     */
    private EntityStore mStore;

    /**
     * Name for the database.
     */
    private static final String NAME = "berkeleyKeyManager";

    /**
     * Constructor. Building up the berkeley db and setting necessary settings.
     * 
     * @param paramFile
     *            the place where the berkeley db is stored.
     */
    public KeyManagerDatabase(final File paramFile) {
        super(paramFile);
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);

        final DatabaseConfig conf = new DatabaseConfig();
        conf.setTransactional(true);
        conf.setKeyPrefixing(true);

        try {
            mEnv = new Environment(place, environmentConfig);

            StoreConfig storeConfig = new StoreConfig();
            storeConfig.setAllowCreate(true);
            storeConfig.setTransactional(true);
            mStore = new EntityStore(mEnv, NAME, storeConfig);

        } catch (final EnvironmentLockedException mELExp) {
            mELExp.printStackTrace();
        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
    }

    /**
     * Clearing the database. That is removing all elements
     */
    public final void clearPersistent() {
        try {
            for (final File file : place.listFiles()) {
                if (!file.delete()) {
                    throw new TTEncryptionException("Couldn't delete!");
                }
            }
            if (!place.delete()) {
                throw new TTEncryptionException("Couldn't delete!");
            }

            if (mStore != null) {
                mStore.close();
            }
            if (mEnv != null) {
                mEnv.close();
            }
        } catch (final TTEncryptionException mDbExp) {
            mDbExp.printStackTrace();
        }

    }

    /**
     * Putting a {@link KeyManager} into the database with a corresponding
     * user.
     * 
     * @param paramEntity
     *            key manager instance to get information for storage.
     */
    public final void putEntry(final KeyManager paramEntity) {
        PrimaryIndex<String, KeyManager> primaryIndex;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mStore.getPrimaryIndex(String.class, KeyManager.class);

            primaryIndex.put(paramEntity);

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }

    }

    /**
     * Getting a {@link KeyManager} related to a given user.
     * 
     * @param paramUser
     *            user for getting related key manager.
     * @return
     *         key manager instance.
     */
    public final KeyManager getEntry(final String paramKey) {
        PrimaryIndex<String, KeyManager> primaryIndex;
        KeyManager entity = null;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mStore.getPrimaryIndex(String.class, KeyManager.class);

            entity = (KeyManager)primaryIndex.get(paramKey);

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return entity;
    }

    /**
     * Deletes an entry from storage.
     * 
     * @param paramKey
     *            primary key of entry to delete.
     * @return
     *         status whether deletion was successful or not.
     */
    public final boolean deleteEntry(final String paramKey) {
        PrimaryIndex<String, KeyManager> primaryIndex;
        boolean status = false;
        try {
            primaryIndex =
                (PrimaryIndex<String, KeyManager>)mStore.getPrimaryIndex(String.class, KeyManager.class);
            status = primaryIndex.delete(paramKey);

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }

        return status;
    }

    /**
     * Returns number of database entries.
     * 
     * @return
     *         number of entries in database.
     */
    public final int count() {
        PrimaryIndex<String, KeyManager> primaryIndex;
        long counter = 0;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mStore.getPrimaryIndex(String.class, KeyManager.class);

            counter = primaryIndex.count();

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return (int)counter;
    }

    /**
     * Returns all database entries as {@link SortedMap}.
     * 
     * @return
     *         all database entries.
     */
    public final SortedMap<String, KeyManager> getEntries() {
        PrimaryIndex<String, KeyManager> primaryIndex;
        SortedMap<String, KeyManager> sMap = null;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mStore.getPrimaryIndex(String.class, KeyManager.class);

            sMap = primaryIndex.sortedMap();

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return sMap;
    }

}
