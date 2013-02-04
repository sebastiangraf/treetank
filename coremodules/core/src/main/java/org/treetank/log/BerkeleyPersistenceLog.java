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

package org.treetank.log;

import static com.google.common.base.Objects.toStringHelper;

import java.io.File;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Berkeley implementation of a persistent cache. That means that all data is
 * stored in this cache and it is never removed. This is useful e.g. when it
 * comes to transaction logging.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyPersistenceLog {

    /**
     * Name for the database.
     */
    private static final String NAME = "berkeleyCache";

    /**
     * Place to store the data.
     */
    private final File mPlace;

    /**
     * Berkeley database.
     */
    protected transient Database mDatabase;

    /**
     * Berkeley Environment for the database.
     */
    private transient Environment mEnv;

    /**
     * Binding for the key, which is the nodepage.
     */
    protected final transient LogKeyBinding mKeyBinding;

    /**
     * Binding for the value which is a page with related Nodes.
     */
    protected final transient LogValueBinding mValueBinding;

    /**
     * Counter to give every instance a different place.
     */
    private static int counter = 0;

    /**
     * Constructor. Building up the berkeley db and setting necessary settings.
     * 
     * @param pFile
     *            the place where the berkeley db is stored.
     * @param pRevision
     *            revision number, needed to reconstruct the sliding window in
     *            the correct way
     * @param pNodeFac
     *            for deserialization of nodes
     * @param pMetaFac
     *            for deserialization of meta entries
     * @throws TTIOException
     *             Exception if IO is not successful
     */
    public BerkeleyPersistenceLog(final File pFile, final INodeFactory pNodeFac,
        final IMetaEntryFactory pMetaFac) throws TTIOException {
        mPlace =
            new File(new File(pFile, ResourceConfiguration.Paths.TransactionLog.getFile().getName()), Integer
                .toString(counter));
        mPlace.mkdirs();
        counter++;
        try {
            /* Create a new, transactional database environment */
            final EnvironmentConfig config = new EnvironmentConfig();
            config.setAllowCreate(true);
            config.setLocking(false);
            config.setCacheSize(1024 * 1024);
            mEnv = new Environment(mPlace, config);
            setUp();
            mKeyBinding = new LogKeyBinding();
            mValueBinding = new LogValueBinding(pNodeFac, pMetaFac);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);

        }
    }

    private void setUp() {
        /* Make a database within that environment */
        final DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setExclusiveCreate(true);
        mDatabase = mEnv.openDatabase(null, NAME, dbConfig);
    }

    /**
     * {@inheritDoc}
     */
    public void put(final LogKey mKey, final LogValue mPage) throws TTIOException {
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
    public void clear() throws TTIOException {
        try {
            mDatabase.close();
            mEnv.removeDatabase(null, NAME);
            setUp();
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public LogValue get(final LogKey mKey) throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        mKeyBinding.objectToEntry(mKey, keyEntry);
        try {
            final OperationStatus status = mDatabase.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
            LogValue val = null;
            if (status == OperationStatus.SUCCESS) {
                val = mValueBinding.entryToObject(valueEntry);
            }

            return val;
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mPlace", mPlace).add("mBackend", mDatabase).add("mEnv", mEnv)
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    public LogIterator getIterator() {
        return new LogIterator(null, this);
    }

}
