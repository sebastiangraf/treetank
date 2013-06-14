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

package org.treetank.io;

import static com.google.common.base.Objects.toStringHelper;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;
import org.treetank.io.LogKey.LogKeyBinding;
import org.treetank.io.LogValue.LogValueBinding;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
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
final class LRULog {
    //
    // // START DEBUG CODE
    // private final static File insertFile = new File("/Users/sebi/Desktop/runtimeResults/insert.txt");
    // private final static File getFile = new File("/Users/sebi/Desktop/runtimeResults/get.txt");
    //
    // static final FileWriter insert;
    // static final FileWriter get;
    //
    // static {
    // try {
    // insert = new FileWriter(insertFile);
    // get = new FileWriter(getFile);
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }

    /**
     * Name for the database.
     */
    private static final String NAME = "berkeleyCache";

    /**
     * Binding for the key, which is the nodebucket.
     */
    private final transient LogKeyBinding mKeyBinding;

    /**
     * Binding for the value which is a bucket with related Nodes.
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

    /** Location to the BDB. */
    private final transient File mLocation;

    /** Transient cache for buffering buckets. */
    private final Cache<LogKey, LogValue> mCache;

    /** DB to select to support non-blocking writes. */
    private int mSelected_db;

    /** Flag if closed. */
    private boolean mClosed;

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
    LRULog(final File pFile, final INodeFactory pNodeFac, final IMetaEntryFactory pMetaFac)
        throws TTIOException {
        mClosed = false;
        mKeyBinding = new LogKeyBinding();
        mValueBinding = new LogValueBinding(pNodeFac, pMetaFac);
        mSelected_db = 1;

        if (new File(pFile, ResourceConfiguration.Paths.TransactionLog.getFile().getName()).list().length > 0) {
            mLocation = new File(pFile, "_" + ResourceConfiguration.Paths.TransactionLog.getFile().getName());
            mLocation.mkdirs();
            mSelected_db = 2;
        } else {
            mLocation = new File(pFile, ResourceConfiguration.Paths.TransactionLog.getFile().getName());
        }

        try {
            EnvironmentConfig config = new EnvironmentConfig();
            config.setAllowCreate(true);
            config = config.setSharedCache(false);
            config.setLocking(false);
            config.setCachePercent(20);
            mEnv = new Environment(mLocation, config);
            mEnv.cleanLog();
            final DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setExclusiveCreate(true);
            mDatabase = mEnv.openDatabase(null, NAME + mSelected_db, dbConfig);

        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }

        mCache =
            CacheBuilder.newBuilder().maximumSize(100).removalListener(
                new RemovalListener<LogKey, LogValue>() {
                    @Override
                    public void onRemoval(RemovalNotification<LogKey, LogValue> notification) {
                        if (notification.getCause() != RemovalCause.REPLACED) {
                            insertIntoBDB(notification.getKey(), notification.getValue());
                        }

                    }
                }).build();

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
        LogValue val = mCache.getIfPresent(pKey);
        if (val == null) {
            final DatabaseEntry valueEntry = new DatabaseEntry();
            final DatabaseEntry keyEntry = new DatabaseEntry();
            mKeyBinding.objectToEntry(pKey, keyEntry);
            try {
                final OperationStatus status = mDatabase.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
                if (status == OperationStatus.SUCCESS) {
                    val = mValueBinding.entryToObject(valueEntry);
                } else {
                    val = new LogValue(null, null);
                }
                mCache.put(pKey, val);
                // get.write(pKey.getLevel() + "," + pKey.getSeq() + "\n");
                // get.flush();

            } catch (DatabaseException exc) {
                throw new TTIOException(exc);
            }
        }
        return val;
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
        mCache.put(pKey, pValue);
    }

    /**
     * Closing the log.
     * 
     * @throws TTIOException
     */
    public void close() throws TTIOException {
        try {
            mDatabase.close();
            mEnv.removeDatabase(null, NAME + mSelected_db);
            mEnv.close();
            IOUtils.recursiveDelete(mLocation);
            mLocation.mkdir();
            mClosed = true;
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Check if log is closed or not.
     * 
     * @return if log is closed.
     */
    public boolean isClosed() {
        return mClosed;
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
        Set<Entry<LogKey, LogValue>> entries = mCache.asMap().entrySet();
        for (Entry<LogKey, LogValue> entry : entries) {
            insertIntoBDB(entry.getKey(), entry.getValue());
        }

        return new LogIterator();
    }

    private void insertIntoBDB(LogKey pKey, LogValue pVal) {
        if (pVal.getModified() != null) {
            final DatabaseEntry valueEntry = new DatabaseEntry();
            final DatabaseEntry keyEntry = new DatabaseEntry();
            mKeyBinding.objectToEntry(pKey, keyEntry);
            mValueBinding.objectToEntry(pVal, valueEntry);
            try {

                // ////TODO DEBUGCODE///////
                // final DatabaseEntry valueOld = new DatabaseEntry();
                // OperationStatus status2 = mDatabase.get(null, keyEntry, valueOld, LockMode.DEFAULT);
                // if (status2 == OperationStatus.SUCCESS) {
                // System.out.println(mDatabase.count());
                // status2 = mDatabase.delete(null, keyEntry);
                // System.out.println(mDatabase.count());
                // status2 = mDatabase.get(null, keyEntry, valueOld, LockMode.DEFAULT);
                // System.out.println(mDatabase.count());
                // mDatabase.sync();
                // System.out.println(mDatabase.count());
                // }

                // OperationStatus status = mDatabase.put(null, keyEntry, valueEntry);
                mDatabase.put(null, keyEntry, valueEntry);
                // insert.write(pKey.getLevel() + "," + pKey.getSeq() + "\n");
                // insert.flush();

            } catch (DatabaseException exc) {
                throw new RuntimeException(exc);
            }
        }
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
            LogValue val;
            if ((val = mCache.getIfPresent(keyEntry)) != null) {
                return val;
            }
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
