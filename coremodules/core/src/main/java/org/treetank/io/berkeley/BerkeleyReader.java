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

package org.treetank.io.berkeley;

import org.treetank.exception.TTIOException;
import org.treetank.io.IReader;
import org.treetank.page.IPage;
import org.treetank.page.PageReference;
import org.treetank.page.UberPage;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * This class represents an reading instance of the Treetank-Application
 * implementing the {@link IReader}-interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyReader implements IReader {

    /** Link to the {@link Database}. */
    private final Database mDatabase;

    /** Link to the {@link Transaction}. */
    private final Transaction mTxn;

    /**
     * Constructor.
     * 
     * @param paramDatabase
     *            {@link Database} reference to be connected to
     * @param paramTxn
     *            {@link Transaction} to be used
     */
    public BerkeleyReader(final Database paramDatabase, final Transaction paramTxn) {
        mTxn = paramTxn;
        mDatabase = paramDatabase;
    }

    /**
     * Constructor.
     * 
     * @param paramEnv
     *            {@link Envirenment} to be used
     * @param paramDatabase
     *            {@link Database} to be connected to
     * @throws DatabaseException
     *             if something weird happens
     */
    public BerkeleyReader(final Environment paramEnv, final Database paramDatabase) throws DatabaseException {
        this(paramDatabase, paramEnv.beginTransaction(null, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage read(final long pKey) throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        BerkeleyFactory.KEY.objectToEntry(pKey, keyEntry);

        IPage page = null;
        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                page = BerkeleyFactory.PAGE_VAL_B.entryToObject(valueEntry);
            }
            return page;
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageReference readFirstReference() throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        BerkeleyFactory.KEY.objectToEntry(-1l, keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            PageReference uberPageReference = null;
            if (status == OperationStatus.SUCCESS) {
                uberPageReference = BerkeleyFactory.FIRST_REV_VAL_B.entryToObject(valueEntry);
            }
            final UberPage page = (UberPage)read(uberPageReference.getKey());

            if (uberPageReference != null) {
                uberPageReference.setPage(page);
            }

            return uberPageReference;
        } catch (final DatabaseException e) {
            throw new TTIOException(e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        try {
            mTxn.abort();
        } catch (final DatabaseException e) {
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
    public boolean equals(final Object paramObj) {
        boolean returnVal = true;
        if (paramObj == null) {
            returnVal = false;
        } else if (getClass() != paramObj.getClass()) {
            returnVal = false;
        }
        final BerkeleyReader other = (BerkeleyReader)paramObj;
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
