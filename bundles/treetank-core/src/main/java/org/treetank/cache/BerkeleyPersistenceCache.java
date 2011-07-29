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

package org.treetank.cache;

import java.io.File;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import org.treetank.exception.TTIOException;

/**
 * Berkeley implementation of a persistent cache. That means that all data is
 * stored in this cache and it is never removed. This is useful e.g. when it
 * comes to transaction logging.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyPersistenceCache extends AbstractPersistenceCache {

    /**
     * Name for the database.
     */
    private static final String NAME = "berkeleyCache";

    /**
     * Berkeley database.
     */
    private final transient Database mDatabase;

    /**
     * Berkeley Environment for the database.
     */
    private final transient Environment mEnv;

    /**
     * Binding for the key, which is the nodepage.
     */
    private final transient TupleBinding<Long> mKeyBinding;

    /**
     * Binding for the value which is a page with related Nodes.
     */
    private final transient NodePageContainerBinding mValueBinding;

    /**
     * Constructor. Building up the berkeley db and setting necessary settings.
     * 
     * @param paramFile
     *            the place where the berkeley db is stored.
     * @param paramRevision
     *            revision number, needed to reconstruct the sliding window in
     *            the correct way
     * @throws TTIOException
     *             Exception if IO is not successful
     */
    public BerkeleyPersistenceCache(final File paramFile, final long paramRevision) throws TTIOException {
        super(paramFile);
        try {
            /* Create a new, transactional database environment */
            final EnvironmentConfig config = new EnvironmentConfig();
            config.setAllowCreate(true);
            config.setLocking(false);
            config.setCacheSize(1024 * 1024);
            mEnv = new Environment(place, config);

            /* Make a database within that environment */
            final DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setExclusiveCreate(true);
            mDatabase = mEnv.openDatabase(null, NAME, dbConfig);

            mKeyBinding = TupleBinding.getPrimitiveBinding(Long.class);
            mValueBinding = new NodePageContainerBinding();

        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putPersistent(final long mKey, final NodePageContainer mPage) throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        mKeyBinding.objectToEntry(mKey, keyEntry);
        mValueBinding.objectToEntry(mPage, valueEntry);
        try {
            mDatabase.put(null, keyEntry, valueEntry);

        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearPersistent() throws TTIOException {
        try {
            mDatabase.close();
            mEnv.removeDatabase(null, NAME);
            mEnv.close();

        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer getPersistent(final long mKey) throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        mKeyBinding.objectToEntry(mKey, keyEntry);
        try {
            final OperationStatus status = mDatabase.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
            NodePageContainer val = null;
            if (status == OperationStatus.SUCCESS) {
                val = mValueBinding.entryToObject(valueEntry);
            }

            return val;
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

}
