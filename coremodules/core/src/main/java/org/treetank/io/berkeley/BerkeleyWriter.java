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
import org.treetank.io.IWriter;
import org.treetank.page.IPage;
import org.treetank.page.PageReference;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * This class represents an reading instance of the Treetank-Application
 * implementing the {@link IWriter}-interface. It inherits and overrides some
 * reader methods because of the transaction layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyWriter implements IWriter {

    /** Current {@link Database} to write to. */
    private final Database mDatabase;

    /** Current {@link Transaction} to write with. */
    private final Transaction mTxn;

    /** Current {@link BerkeleyReader} to read with. */
    private final BerkeleyReader mReader;

    /** Key of nodepage. */
    private long mNodepagekey;

    /**
     * Simple constructor starting with an {@link Environment} and a {@link Database}.
     * 
     * @param paramEnv
     *            {@link Environment} reference for the write
     * @param paramDatabase
     *            {@link Database} reference where the data should be written to
     * @throws TTIOException
     *             if something odd happens
     */
    public BerkeleyWriter(final Environment paramEnv, final Database paramDatabase) throws TTIOException {
        try {
            mTxn = paramEnv.beginTransaction(null, null);
            mDatabase = paramDatabase;
            mNodepagekey = getLastNodePage();
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }

        mReader = new BerkeleyReader(mDatabase, mTxn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        try {
            setLastNodePage(mNodepagekey);
            mTxn.commit();
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long write(final PageReference pageReference) throws TTIOException {
        final IPage page = pageReference.getPage();

        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        // TODO make this better
        mNodepagekey++;

        mReader.mPageBinding.objectToEntry(page, valueEntry);
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(mNodepagekey, keyEntry);

        final OperationStatus status = mDatabase.put(mTxn, keyEntry, valueEntry);
        if (status != OperationStatus.SUCCESS) {
            throw new TTIOException(new StringBuilder("Write of ").append(pageReference.toString()).append(
                " failed!").toString());
        }

        pageReference.setKey(mNodepagekey);
        return mNodepagekey;

    }

    /**
     * Setting the last nodePage to the persistent storage.
     * 
     * @param paramData
     *            key to be stored
     * @throws TTIOException
     *             If can't set last Node page
     */
    private void setLastNodePage(final Long paramData) throws TTIOException {
        final DatabaseEntry keyEntry = new DatabaseEntry();
        final DatabaseEntry valueEntry = new DatabaseEntry();

        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(-2l, keyEntry);
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(paramData, valueEntry);
        try {
            mDatabase.put(mTxn, keyEntry, valueEntry);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Getting the last nodePage from the persistent storage.
     * 
     * @throws TTIOException
     *             If can't get last Node page
     * @return the last nodepage-key
     */
    private long getLastNodePage() throws TTIOException {
        final DatabaseEntry keyEntry = new DatabaseEntry();
        final DatabaseEntry valueEntry = new DatabaseEntry();

        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(-2l, keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            Long val;
            if (status == OperationStatus.SUCCESS) {
                val = TupleBinding.getPrimitiveBinding(Long.class).entryToObject(valueEntry);
            } else {
                val = 0L;
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
    public void writeFirstReference(final PageReference paramPageReference) throws TTIOException {
        write(paramPageReference);

        final DatabaseEntry keyEntry = new DatabaseEntry();
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(-1l, keyEntry);

        final DatabaseEntry valueEntry = new DatabaseEntry();
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(paramPageReference.getKey(), valueEntry);

        try {
            mDatabase.put(mTxn, keyEntry, valueEntry);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage read(final long pKey) throws TTIOException {
        return mReader.read(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageReference readFirstReference() throws TTIOException {
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
