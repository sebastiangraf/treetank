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

package com.treetank.io.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.IWriter;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * This class represents an reading instance of the Treetank-Application
 * implementing the {@link IWriter}-interface. It inherits and overrides some
 * reader methods because of the transaction layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BerkeleyWriter implements IWriter {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(BerkeleyWriter.class));
    
    /** Current {@link Database} to write to. */
    private transient final Database mDatabase;

    /** Current {@link Transaction} to write with. */
    private transient Transaction mTxn;

    /** Current {@link BerkeleyReader} to read with. */
    private transient final BerkeleyReader mReader;

    private long mNodepagekey;

    /**
     * Simple constructor starting with an {@link Environment} and a {@link Database}.
     * 
     * @param mEnv
     *            for the write
     * @param mDatabase
     *            where the data should be written to
     * @throws TreetankIOException
     *             if something off happens
     */
    public BerkeleyWriter(final Environment mEnv, final Database mDatabase) throws TreetankIOException {

        try {
            this.mTxn = mEnv.beginTransaction(null, null);
            this.mDatabase = mDatabase;
            this.mNodepagekey = getLastNodePage();
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TreetankIOException(exc);
        }

        mReader = new BerkeleyReader(mDatabase, mTxn);
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws TreetankIOException {
        try {
            setLastNodePage(mNodepagekey);
            mTxn.commit();
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TreetankIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void write(final PageReference pageReference) throws TreetankIOException {
        final AbstractPage page = pageReference.getPage();

        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        // TODO make this better
        mNodepagekey++;
        final BerkeleyKey key = new BerkeleyKey(mNodepagekey);

        BerkeleyFactory.PAGE_VAL_B.objectToEntry(page, valueEntry);
        BerkeleyFactory.KEY.objectToEntry(key, keyEntry);

        try {
            final OperationStatus status = mDatabase.put(mTxn, keyEntry, valueEntry);
            if (status != OperationStatus.SUCCESS) {
                throw new DatabaseException(new StringBuilder("Write of ").append(pageReference.toString())
                    .append(" failed!").toString());
            }
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TreetankIOException(exc);
        }
        pageReference.setKey(key);

    }

    /**
     * Setting the last nodePage to the persistent storage.
     * 
     * @throws TreetankIOException
     *             If can't set last Node page
     * @param mData
     *            key to be stored
     */
    private void setLastNodePage(final Long mData) throws TreetankIOException {
        final DatabaseEntry keyEntry = new DatabaseEntry();
        final DatabaseEntry valueEntry = new DatabaseEntry();

        final BerkeleyKey key = BerkeleyKey.getDataInfoKey();
        BerkeleyFactory.KEY.objectToEntry(key, keyEntry);
        BerkeleyFactory.DATAINFO_VAL_B.objectToEntry(mData, valueEntry);
        try {
            mDatabase.put(mTxn, keyEntry, valueEntry);

        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TreetankIOException(exc);
        }
    }

    /**
     * Getting the last nodePage from the persistent storage.
     * 
     * @throws TreetankIOException
     *             If can't get last Node page
     * @return the last nodepage-key
     */
    private long getLastNodePage() throws TreetankIOException {
        final DatabaseEntry keyEntry = new DatabaseEntry();
        final DatabaseEntry valueEntry = new DatabaseEntry();

        final BerkeleyKey key = BerkeleyKey.getDataInfoKey();
        BerkeleyFactory.KEY.objectToEntry(key, keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            Long val;
            if (status == OperationStatus.SUCCESS) {
                val = BerkeleyFactory.DATAINFO_VAL_B.entryToObject(valueEntry);
            } else {
                val = 0L;
            }
            return val;
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TreetankIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void writeFirstReference(final PageReference pageReference) throws TreetankIOException {
        write(pageReference);

        final DatabaseEntry keyEntry = new DatabaseEntry();
        BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(), keyEntry);

        final DatabaseEntry valueEntry = new DatabaseEntry();
        BerkeleyFactory.FIRST_REV_VAL_B.objectToEntry(pageReference, valueEntry);

        try {
            mDatabase.put(mTxn, keyEntry, valueEntry);
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TreetankIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    public AbstractPage read(final PageReference pageReference) throws TreetankIOException {
        return mReader.read(pageReference);
    }

    /**
     * {@inheritDoc}
     */
    public PageReference readFirstReference() throws TreetankIOException {
        return mReader.readFirstReference();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDatabase == null) ? 0 : mDatabase.hashCode());
        result = prime * result + ((mTxn == null) ? 0 : mTxn.hashCode());
        result = prime * result + ((mReader == null) ? 0 : mReader.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object mObj) {
        boolean returnVal = true;
        if (mObj == null) {
            returnVal = false;
        } else if (getClass() != mObj.getClass()) {
            returnVal = false;
        }
        final BerkeleyWriter other = (BerkeleyWriter)mObj;
        if (mDatabase == null) {
            if (other.mDatabase != null) {
                returnVal = false;
            }
        } else if (!mDatabase.equals(other.mDatabase)) {
            returnVal = false;
        }
        if (mTxn == null) {
            if (other.mTxn != null) {
                returnVal = false;
            }
        } else if (!mTxn.equals(other.mTxn)) {
            returnVal = false;
        }
        if (mReader == null) {
            if (other.mReader != null) {
                returnVal = false;
            }
        } else if (!mReader.equals(other.mReader)) {
            returnVal = false;
        }
        return returnVal;
    }

}
