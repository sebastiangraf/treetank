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
import java.util.Iterator;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;
import org.treetank.log.LogKey.LogKeyBinding;
import org.treetank.log.LogValue.LogValueBinding;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * An LRU cache, based on <code>LinkedHashMap</code>. This cache can hold an
 * possible second cache as a second layer for example for storing data in a
 * persistent way.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public final class LRULog {

    /**
     * Name for the database.
     */
    private static final String NAME = "berkeleyCache";

    /**
     * Binding for the key, which is the nodepage.
     */
    private final transient LogKeyBinding mKeyBinding;

    /**
     * Binding for the value which is a page with related Nodes.
     */
    private final transient LogValueBinding mValueBinding;

    /**
     * Berkeley Environment for the database.
     */
    private final transient Environment mEnv;

    /**
     * Berkeley database.
     */
    private final transient Database mDatabase;

    /**
     * Creates a new LRU cache.
     * 
     * @param pFile
     *            Location of the cache.
     * @param pNodeFac
     *            NodeFactory for generating nodes adhering to the used interface
     * @param pMetaFac
     *            MetaFactory for generating meta-entries adhering to the used interface
     * @throws TTIOException
     * 
     */
    public LRULog(final File pFile, final INodeFactory pNodeFac, final IMetaEntryFactory pMetaFac)
        throws TTIOException {
        mKeyBinding = new LogKeyBinding();
        mValueBinding = new LogValueBinding(pNodeFac, pMetaFac);

        final File realPlace =
            new File(pFile, ResourceConfiguration.Paths.TransactionLog.getFile().getName());
        try {
            final EnvironmentConfig config = new EnvironmentConfig();
            config.setAllowCreate(true);
            config.setLocking(false);
            config.setCacheSize(1024 * 1024);
            mEnv = new Environment(realPlace, config);
            final DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setExclusiveCreate(true);
            mDatabase = mEnv.openDatabase(null, NAME, dbConfig);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }

    }

    /**
     * Getting a {@link LogValue} for a given key
     * 
     * @param pKey
     *            the key
     * @return a suitable {@link LogValue} if present, false otherwise
     * @throws TTIOException
     */
    public LogValue get(final LogKey pKey) throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        mKeyBinding.objectToEntry(pKey, keyEntry);
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
     * Putting a new entry to the log, overriding already existing entries.
     * 
     * @param pKey
     *            to be set
     * @param pValue
     *            to be set
     * @throws TTIOException
     */
    public void put(final LogKey pKey, final LogValue pValue) throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        mKeyBinding.objectToEntry(pKey, keyEntry);
        mValueBinding.objectToEntry(pValue, valueEntry);
        try {
            mDatabase.put(null, keyEntry, valueEntry);

        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }

    }

    /**
     * Closing the log.
     * 
     * @throws TTIOException
     */
    public void close() throws TTIOException {
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
    public String toString() {
        return toStringHelper(this).add("mDatabase", mDatabase).toString();
    }

    /**
     * Returning all elements as Iterator.
     * 
     * @return new LogIterator-instance
     */
    public LogIterator getIterator() {
        return new LogIterator();
    }

    class LogIterator implements Iterator<LogValue>, Iterable<LogValue> {

        private Cursor mCursor;
        private DatabaseEntry valueEntry;
        private DatabaseEntry keyEntry;

        /**
         * 
         * Constructor.
         * 
         */
        public LogIterator() {
            mCursor = mDatabase.openCursor(null, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<LogValue> iterator() {
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            boolean returnVal = false;
            valueEntry = new DatabaseEntry();
            keyEntry = new DatabaseEntry();
            try {
                final OperationStatus status = mCursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT);
                if (status == OperationStatus.SUCCESS) {
                    returnVal = true;
                }
            } catch (final DatabaseException exc) {
                throw new RuntimeException(exc);
            }
            if (returnVal == false) {
                mCursor.close();
            }
            return returnVal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public LogValue next() {
            return mValueBinding.entryToObject(valueEntry);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
