/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
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
package org.treetank.cache;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import org.slf4j.LoggerFactory;
import org.treetank.access.DatabaseConfiguration;
import org.treetank.exception.TTIOException;
import org.treetank.utils.LogWrapper;

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
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(BerkeleyPersistenceCache.class));

    /**
     * Berkeley database.
     */
    private final transient Database mDatabase;

    /**
     * Berkeley Environment for the database.
     */
    private final transient Environment mEnv;

    /**
     * Name for the database.
     */
    private static final String NAME = "berkeleyCache";

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
     * @param paramSessionConfig
     *            the place where the berkeley db is stored.
     * @param paramRevision
     *            revision number, needed to reconstruct the sliding window in
     *            the correct way
     * @throws TTIOException
     *             Exception if IO is not successful
     */
    public BerkeleyPersistenceCache(final DatabaseConfiguration paramSessionConfig, final long paramRevision)
        throws TTIOException {
        super(paramSessionConfig);
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
            LOGWRAPPER.error(exc);
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
            LOGWRAPPER.error(exc);
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
            LOGWRAPPER.error(exc);
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
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }
    }

}
