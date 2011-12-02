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
import java.util.concurrent.atomic.AtomicInteger;

import org.treetank.exception.TTEncryptionException;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

/**
 * This class acts as centralized Utility-Class for all berkeleyDB-database
 * operations.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class DatabaseUtil {

    /**
     * Berkeley Environment for the database.
     */
    protected final Environment mEnv;

    /**
     * Berkeley Entity store instance for the database.
     */
    protected final EntityStore mStore;

    /**
     * Place for storing the stuff.
     */
    protected final File mPlace;

    /**
     * Counter to give every instance a different place.
     */
    private static AtomicInteger counter = new AtomicInteger();

    /**
     * Constructor. Building up the berkeley db and setting necessary settings.
     * 
     * @param paramFile
     *            the place where the berkeley db is stored.
     */
    public DatabaseUtil(final File paramFile, final String paramName) {
        mPlace =
            new File(paramFile, new StringBuilder(new File("keyselector").getName()).append(File.separator)
                .append(counter.incrementAndGet()).toString());
        mPlace.mkdirs();

        final EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);

        final DatabaseConfig conf = new DatabaseConfig();
        conf.setTransactional(true);
        conf.setKeyPrefixing(true);

        mEnv = new Environment(paramFile, environmentConfig);

        final StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(true);
        mStore = new EntityStore(mEnv, paramName, storeConfig);

    }

    /**
     * Clearing the database. That is removing all elements
     * 
     * @throws TTEncryptionException
     */
    public final void clearPersistent() throws TTEncryptionException {
        for (final File file : mPlace.listFiles()) {
            if (!file.delete()) {
                throw new TTEncryptionException("Couldn't delete!");
            }
        }
        if (!mPlace.delete()) {
            throw new TTEncryptionException("Couldn't delete!");
        }
        if (mStore != null) {
            mStore.close();
        }
        if (mEnv != null) {
            mEnv.close();
        }

    }

}
