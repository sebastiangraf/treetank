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

import org.treetank.access.Storage;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * This class represents an reading instance of the Treetank-Application
 * implementing the {@link IBackendWriter}-interface. It inherits and overrides some
 * reader methods because of the transaction layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyWriter implements IBackendWriter {

    /** Current {@link Storage} to write to. */
    private final Database mDatabase;

    /** Current {@link BerkeleyReader} to read with. */
    private final BerkeleyReader mReader;

    /**
     * Simple constructor starting with an {@link Environment} and a {@link Storage}.
     * 
     * @param pTxn
     *            {@link Transaction} transaction for the write and read
     * @param pDatabase
     *            {@link Storage} reference where the data should be written to
     * @param pPageBinding
     *            {@link TupleBinding} for de/-serializing pages
     * 
     * @throws TTIOException
     *             if something odd happens
     */
    public BerkeleyWriter(Database pDatabase, TupleBinding<IPage> pPageBinding) throws TTIOException {
        try {
            mDatabase = pDatabase;
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }

        mReader = new BerkeleyReader(mDatabase, pPageBinding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void write(final IPage page) throws TTIOException {

        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        mReader.mPageBinding.objectToEntry(page, valueEntry);
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(page.getPageKey(), keyEntry);

        // final OperationStatus status = mBackend.put(mTxn, keyEntry, valueEntry);
        final OperationStatus status = mDatabase.put(null, keyEntry, valueEntry);
        if (status != OperationStatus.SUCCESS) {
            throw new TTIOException(new StringBuilder("Write of ").append(page.toString()).append(" failed!")
                .toString());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IPage read(final long pKey) throws TTIOException {
        return mReader.read(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberPage readUber() throws TTIOException {
        return mReader.readUber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeUberPage(UberPage page) throws TTException {
        long pageKey = page.getPageKey();
        write(page);

        final DatabaseEntry keyEntry = new DatabaseEntry();
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(-1l, keyEntry);

        final DatabaseEntry valueEntry = new DatabaseEntry();
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(pageKey, valueEntry);

        try {
            // mBackend.put(mTxn, keyEntry, valueEntry);
            mDatabase.put(null, keyEntry, valueEntry);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

}
