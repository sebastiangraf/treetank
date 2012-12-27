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
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * This class represents an reading instance of the Treetank-Application
 * implementing the {@link IBackendReader}-interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyReader implements IBackendReader {

    /** Binding for {@link IPage}. */
    protected final TupleBinding<IPage> mPageBinding;

    /** Link to the {@link Storage}. */
    private final Database mDatabase;

    /**
     * Constructor.
     * 
     * @param pDatabase
     *            {@link Storage} reference to be connected to
     * @param pPageBinding
     *            {@link TupleBinding} for de/-serializing pages
     */
    public BerkeleyReader(Database pDatabase, TupleBinding<IPage> pPageBinding) {
        mDatabase = pDatabase;
        mPageBinding = pPageBinding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage read(final long pKey) throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(pKey, keyEntry);

        IPage page = null;
        try {
            final OperationStatus status = mDatabase.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                page = mPageBinding.entryToObject(valueEntry);
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
    public void close() throws TTIOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberPage readUber() throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(-1l, keyEntry);

        try {
            // final OperationStatus status = mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            final OperationStatus status = mDatabase.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
            long key = 0;
            if (status == OperationStatus.SUCCESS) {
                key = TupleBinding.getPrimitiveBinding(Long.class).entryToObject(valueEntry);
            }
            final UberPage page = (UberPage)read(key);

            return page;
        } catch (final DatabaseException e) {
            throw new TTIOException(e);
        }

    }

}
