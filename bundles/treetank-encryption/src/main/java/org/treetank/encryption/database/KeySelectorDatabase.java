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

import org.treetank.encryption.database.model.KeySelector;
import org.treetank.exception.TTIOException;

/**
 * Berkeley implementation of a persistent key selector database. That means
 * that all data is stored in this database and it is never removed.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class KeySelectorDatabase extends AbsKeyDatabase {

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
    private static final String NAME = "berkeleyKeySelector";

    /**
     * Constructor. Building up the berkeley db and setting necessary settings.
     * 
     * @param paramFile
     *            the place where the berkeley db is stored.
     */
    public KeySelectorDatabase(final File paramFile) {
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
                    throw new TTIOException("Couldn't delete!");
                }
            }
            if (!place.delete()) {
                throw new TTIOException("Couldn't delete!");
            }
            if (mStore != null) {
                mStore.close();
            }
            if (mEnv != null) {
                mEnv.close();
            }
        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        } catch (final TTIOException exc) {
            throw new IllegalStateException(exc);
        }

    }

    /**
     * Putting a {@link KeySelector} into the database with a corresponding
     * selector key.
     * 
     * @param paramEntity
     *            key selector instance to put into database.
     */
    public final void putEntry(final KeySelector paramEntity) {
        PrimaryIndex<Long, KeySelector> primaryIndex;
        try {
            primaryIndex =
                (PrimaryIndex<Long, KeySelector>)mStore.getPrimaryIndex(Long.class, KeySelector.class);

            primaryIndex.put(paramEntity);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Getting a {@link KeyingSelector} related to a given selector key.
     * 
     * @param paramKey
     *            selector key for related key selector instance.
     * @return
     *         key selector instance.
     */
    public final KeySelector getEntry(final long paramKey) {
        PrimaryIndex<Long, KeySelector> primaryIndex;
        KeySelector entity = null;
        try {
            primaryIndex =
                (PrimaryIndex<Long, KeySelector>)mStore.getPrimaryIndex(Long.class, KeySelector.class);
            entity = (KeySelector)primaryIndex.get(paramKey);

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
    public final boolean deleteEntry(final long paramKey) {
        PrimaryIndex<Long, KeySelector> primaryIndex;
        boolean status = false;
        try {
            primaryIndex =
                (PrimaryIndex<Long, KeySelector>)mStore.getPrimaryIndex(Long.class, KeySelector.class);
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
        PrimaryIndex<Long, KeySelector> primaryIndex;
        long counter = 0;
        try {
            primaryIndex =
                (PrimaryIndex<Long, KeySelector>)mStore.getPrimaryIndex(Long.class, KeySelector.class);
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
    public final SortedMap<Long, KeySelector> getEntries() {
        PrimaryIndex<Long, KeySelector> primaryIndex;
        SortedMap<Long, KeySelector> sMap = null;
        try {
            primaryIndex =
                (PrimaryIndex<Long, KeySelector>)mStore.getPrimaryIndex(Long.class, KeySelector.class);
            sMap = primaryIndex.sortedMap();

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return sMap;
    }

}
