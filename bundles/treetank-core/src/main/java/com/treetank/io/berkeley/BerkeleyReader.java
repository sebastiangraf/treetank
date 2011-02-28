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
import com.treetank.exception.TTIOException;
import com.treetank.io.IReader;
import com.treetank.page.AbsStractPage;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * This class represents an reading instance of the Treetank-Application
 * implementing the {@link IReader}-interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BerkeleyReader implements IReader {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(BerkeleyReader.class));

    /** Link to the {@link Database}. */
    private transient final Database mDatabase;

    /** Link to the {@link Transaction}. */
    private transient final Transaction mTxn;

    /**
     * Constructor.
     * 
     * @param mDatabase
     *            to be connected to
     * @param mTxn
     *            transaction to be used
     */
    public BerkeleyReader(final Database mDatabase, final Transaction mTxn) {
        this.mTxn = mTxn;
        this.mDatabase = mDatabase;

    }

    /**
     * Constructor.
     * 
     * @param mEnv
     *            to be used
     * @param mDatabase
     *            to be connected to
     * @throws DatabaseException
     *             if something weird happens
     */
    public BerkeleyReader(final Environment mEnv, final Database mDatabase) throws DatabaseException {
        this(mDatabase, mEnv.beginTransaction(null, null));
    }

    /**
     * {@inheritDoc}
     */
    public AbsStractPage read(final PageReference pageReference) throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        BerkeleyFactory.KEY.objectToEntry(pageReference.getKey(), keyEntry);

        AbsStractPage page = null;
        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                page = BerkeleyFactory.PAGE_VAL_B.entryToObject(valueEntry);

            }
            return page;
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    public PageReference readFirstReference() throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(), keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            PageReference uberPageReference = null;
            if (status == OperationStatus.SUCCESS) {

                uberPageReference = BerkeleyFactory.FIRST_REV_VAL_B.entryToObject(valueEntry);
            }
            final UberPage page = (UberPage)read(uberPageReference);

            if (uberPageReference != null) {
                uberPageReference.setPage(page);
            }

            return uberPageReference;
        } catch (final DatabaseException e) {
            LOGWRAPPER.error(e);
            throw new TTIOException(e);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void close() throws TTIOException {
        try {
            mTxn.abort();
        } catch (final DatabaseException e) {
            LOGWRAPPER.error(e);
            throw new TTIOException(e);

        }
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
        final BerkeleyReader other = (BerkeleyReader)mObj;
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
        return returnVal;
    }

}
