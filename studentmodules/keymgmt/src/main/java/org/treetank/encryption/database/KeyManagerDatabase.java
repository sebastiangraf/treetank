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
package org.treetank.encryption.database;

import java.io.File;
import java.util.SortedMap;

import org.treetank.encryption.database.model.KeyManager;
import org.treetank.exception.TTEncryptionException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.PrimaryIndex;

/**
 * Berkeley implementation of a persistent key manager database. That means that
 * all data is stored in this database and it is never removed.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class KeyManagerDatabase {

    /**
     * Name for the database.
     */
    private static final String NAME = "berkeleyKeyManager";

    /**
     * DB-Util for summarizing common access.
     */
    private final DatabaseUtil mUtil;

    /**
     * Constructor. Building up the berkeley db and setting necessary settings.
     * 
     * @param paramFile
     *            the place where the berkeley db is stored.
     */
    public KeyManagerDatabase(final File paramFile) {
        mUtil = new DatabaseUtil(paramFile, NAME);
    }

    /**
     * Clearing the database. That is removing all elements
     * 
     * @throws TTEncryptionException
     */
    public final void clearPersistent() throws TTEncryptionException {
        mUtil.clearPersistent();
    }

    /**
     * Putting a {@link KeyManager} into the database with a corresponding user.
     * 
     * @param paramEntity
     *            key manager instance to get information for storage.
     */
    public final void putEntry(final KeyManager paramEntity) {
        PrimaryIndex<String, KeyManager> primaryIndex;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mUtil.mStore.getPrimaryIndex(String.class, KeyManager.class);

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
     * @return key manager instance.
     */
    public final KeyManager getEntry(final String paramKey) {
        PrimaryIndex<String, KeyManager> primaryIndex;
        KeyManager entity = null;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mUtil.mStore.getPrimaryIndex(String.class, KeyManager.class);

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
     * @return status whether deletion was successful or not.
     */
    public final boolean deleteEntry(final String paramKey) {
        PrimaryIndex<String, KeyManager> primaryIndex;
        boolean status = false;
        try {
            primaryIndex =
                (PrimaryIndex<String, KeyManager>)mUtil.mStore
                    .getPrimaryIndex(String.class, KeyManager.class);
            status = primaryIndex.delete(paramKey);

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }

        return status;
    }

    /**
     * Returns number of database entries.
     * 
     * @return number of entries in database.
     */
    public final int count() {
        PrimaryIndex<String, KeyManager> primaryIndex;
        long counter = 0;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mUtil.mStore.getPrimaryIndex(String.class, KeyManager.class);

            counter = primaryIndex.count();

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return (int)counter;
    }

    /**
     * Returns all database entries as {@link SortedMap}.
     * 
     * @return all database entries.
     */
    public final SortedMap<String, KeyManager> getEntries() {
        PrimaryIndex<String, KeyManager> primaryIndex;
        SortedMap<String, KeyManager> sMap = null;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mUtil.mStore.getPrimaryIndex(String.class, KeyManager.class);

            sMap = primaryIndex.sortedMap();

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return sMap;
    }

    /**
     * Checking whether user is contained into the database.
     * 
     * @param key
     *            user name.
     * @return containing user or not.
     * 
     */
    public final boolean containsEntry(final String key) {
        PrimaryIndex<String, KeyManager> primaryIndex;
        try {
            primaryIndex =

            (PrimaryIndex<String, KeyManager>)mUtil.mStore.getPrimaryIndex(String.class, KeyManager.class);

            return primaryIndex.contains(key);

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return false;

    }

}
