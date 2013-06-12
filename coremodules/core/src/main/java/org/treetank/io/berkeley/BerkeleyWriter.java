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
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;

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
     * @param pEnv
     *            env to create new reader
     * @param pDatabase
     *            {@link Storage} reference where the data should be written to
     * @param pBucketBinding
     *            {@link TupleBinding} for de/-serializing buckets
     * 
     * @throws TTIOException
     *             if something odd happens
     */
    public BerkeleyWriter(final Environment pEnv, final Database pDatabase,
        final TupleBinding<IBucket> pBucketBinding) throws TTIOException {
        mDatabase = pDatabase;
        mReader = new BerkeleyReader(pEnv, mDatabase, pBucketBinding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void write(final IBucket bucket) throws TTIOException {

        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        mReader.mBucketBinding.objectToEntry(bucket, valueEntry);
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(bucket.getBucketKey(), keyEntry);

        // final OperationStatus status = mBackend.put(mTxn, keyEntry, valueEntry);
        final OperationStatus status = mDatabase.put(null, keyEntry, valueEntry);

        if (status != OperationStatus.SUCCESS) {
            throw new TTIOException(new StringBuilder("Write of ").append(bucket.toString()).append(
                " failed!").toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IBucket read(final long pKey) throws TTIOException {
        return mReader.read(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        // containing inlaying transaction commit
        mReader.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberBucket readUber() throws TTIOException {
        return mReader.readUber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeUberBucket(UberBucket pBucket) throws TTException {
        long bucketKey = pBucket.getBucketKey();
        write(pBucket);

        final DatabaseEntry keyEntry = new DatabaseEntry();
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(-1l, keyEntry);

        final DatabaseEntry valueEntry = new DatabaseEntry();
        TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(bucketKey, valueEntry);

        try {
            mDatabase.put(null, keyEntry, valueEntry);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

}
