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

package org.treetank.access;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.treetank.access.conf.ConstructorProps;
import org.treetank.api.IBucketReadTrx;
import org.treetank.api.IData;
import org.treetank.api.ISession;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.DataBucket;
import org.treetank.bucket.DataBucket.DeletedData;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.revisioning.IRevisioning;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * <h1>BucketReadTrx</h1>
 * 
 * <p>
 * State of a reading transaction. The only thing shared amongst transactions is the bucket cache. Everything
 * else is exclusive to this transaction. It is required that only a single thread has access to this
 * transaction.
 * </p>
 * 
 * <p>
 * A path-like cache boosts sequential operations.
 * </p>
 */
public class BucketReadTrx implements IBucketReadTrx {

    /** Bucket reader exclusively assigned to this transaction. */
    private final IBackendReader mBucketReader;

    /** Uber bucket this transaction is bound to. */
    private final UberBucket mUberBucket;

    /** Cached root bucket of this revision. */
    protected final RevisionRootBucket mRootBucket;

    /** Cached name bucket of this revision. */
    protected final MetaBucket mMetaBucket;

    /** Configuration of the session */
    protected final ISession mSession;

    /** Boolean for determinc close. */
    private boolean mClose;

    /** Cache for reading data. */
    protected final Cache<Long, DataBucket> mCache;

    /**
     * Standard constructor.
     * 
     * @param pSession
     *            State of state.
     * @param pUberBucket
     *            Uber bucket to start reading with.
     * @param pRevBucket
     *            RevisionBucket with reference from either log to commit or persistent memory.
     * @param pMetaBucket
     *            MetaBucket with reference from either log to commit or persistent memory.
     * @param pReader
     *            for this transaction
     * @throws TTIOException
     *             if the read of the persistent storage fails
     */
    protected BucketReadTrx(final ISession pSession, final UberBucket pUberBucket,
        final RevisionRootBucket pRevBucket, final MetaBucket pMetaBucket, final IBackendReader pReader)
        throws TTException {
        mSession = pSession;
        mBucketReader = pReader;
        mUberBucket = pUberBucket;
        mRootBucket = pRevBucket;
        mMetaBucket = pMetaBucket;
        mClose = false;
        mCache = CacheBuilder.newBuilder().maximumSize(100).build();
    }

    /**
     * Getting the data related to the given data key.
     * 
     * @param pDataKey
     *            searched for
     * @return the related data
     * @throws TTIOException
     *             if the read to the persistent storage fails
     */
    public IData getData(final long pDataKey) throws TTIOException {
        checkArgument(pDataKey >= 0);
        checkState(!mClose, "Transaction already closed");
        // Calculate bucket and data part for given datakey.
        final long seqBucketKey = pDataKey >> IConstants.INDIRECT_BUCKET_COUNT[3];
        final int dataBucketOffset = dataBucketOffset(pDataKey);
        DataBucket bucket = mCache.getIfPresent(seqBucketKey);
        if (bucket == null) {
            final List<DataBucket> listRevs = getSnapshotBuckets(seqBucketKey);
            final DataBucket[] revs = listRevs.toArray(new DataBucket[listRevs.size()]);
            checkState(revs.length > 0, "Number of Buckets to reconstruct must be larger than 0");
            // Build up the complete bucket.
            final IRevisioning revision = mSession.getConfig().mRevision;
            bucket = revision.combineBuckets(revs);
            mCache.put(seqBucketKey, bucket);
        }
        final IData returnVal = bucket.getData(dataBucketOffset);
        // root-fsys is excluded from the checkagainst deletion based on the necesssity of the data-layer to
        // reference against this data while creation of the transaction
        if (pDataKey == 0) {
            return returnVal;
        } else {
            return checkItemIfDeleted(returnVal);
        }

    }

    /**
     * Closing this Readtransaction.
     * 
     * @throws TTIOException
     *             if the closing to the persistent storage fails.
     */
    public boolean close() throws TTIOException {
        if (!mClose) {
            mSession.deregisterBucketTrx(this);
            mBucketReader.close();
            mClose = true;
            return true;
        } else {
            return false;
        }

    }

    /**
     * {@inheritDoc}
     */
    public long getRevision() throws TTIOException {
        checkState(!mClose, "Transaction already closed");
        return mRootBucket.getRevision();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return mClose;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaBucket getMetaBucket() {
        checkState(!mClose, "Transaction already closed");
        return mMetaBucket;
    }

    /**
     * Method to check if an {@link IData} is a deleted one.
     * 
     * @param pToCheck
     *            of the IItem
     * @return the item if it is valid, null otherwise
     */
    protected final IData checkItemIfDeleted(final IData pToCheck) {
        if (pToCheck == null) {
            throw new IllegalStateException(new StringBuilder("Data not existing.").toString());
        } else if (pToCheck instanceof DeletedData) {
            return null;
        } else {
            return pToCheck;
        }
    }

    /**
     * Dereference data bucket reference.
     * 
     * @param pSeqDataBucketKey
     *            Key of data bucket.
     * @return Dereferenced bucket.
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected final List<DataBucket> getSnapshotBuckets(final long pSeqDataBucketKey) throws TTIOException {

        // Return Value, since the revision iterates a flexible number of version, this has to be a list
        // first.
        final List<DataBucket> dataBuckets = new ArrayList<DataBucket>();

        // Getting the keys for the revRoots
        final long[] pathToRoot =
            BucketReadTrx.dereferenceLeafOfTree(mBucketReader,
                mUberBucket.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET], mRootBucket
                    .getRevision());
        final RevisionRootBucket rootBucket =
            (RevisionRootBucket)mBucketReader.read(pathToRoot[IConstants.INDIRECT_BUCKET_COUNT.length]);

        final int numbersToRestore =
            Integer.parseInt(mSession.getConfig().mProperties.getProperty(ConstructorProps.NUMBERTORESTORE));
        // starting from the current databucket
        final long[] pathToRecentBucket =
            dereferenceLeafOfTree(mBucketReader,
                rootBucket.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET], pSeqDataBucketKey);

        DataBucket bucket;
        long bucketKey = pathToRecentBucket[IConstants.INDIRECT_BUCKET_COUNT.length];
        // jumping through the databuckets based on the pointers
        while (dataBuckets.size() < numbersToRestore && bucketKey > -1) {
            bucket = (DataBucket)mBucketReader.read(bucketKey);
            dataBuckets.add(bucket);
            bucketKey = bucket.getLastBucketPointer();
        }

        // check if bucket was ever written before to perform check
        if (bucketKey > -1) {
            checkStructure(mBucketReader, pathToRecentBucket, rootBucket, pSeqDataBucketKey);
            checkStructure(mBucketReader, pathToRoot, mUberBucket, mRootBucket.getRevision());
        }

        return dataBuckets;

    }

    /**
     * Calculate data bucket offset for a given data key.
     * 
     * @param pDataKey
     *            data key to find offset for.
     * @return Offset into data bucket.
     */
    protected static final int dataBucketOffset(final long pDataKey) {
        // INDIRECT_BUCKET_COUNT[3] is only taken to get the difference between 2^7 and the actual
        // datakey as offset. It has nothing to do with the levels.
        final long dataBucketOffset =
            (pDataKey - ((pDataKey >> IConstants.INDIRECT_BUCKET_COUNT[3]) << IConstants.INDIRECT_BUCKET_COUNT[3]));
        return (int)dataBucketOffset;
    }

    /**
     * Find reference pointing to leaf bucket of an indirect tree.
     * 
     * @param pStartKey
     *            Start reference pointing to the indirect tree.
     * @param pSeqBucketKey
     *            Key to look up in the indirect tree.
     * @return Reference denoted by key pointing to the leaf bucket.
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected static final long[] dereferenceLeafOfTree(final IBackendReader pReader, final long pStartKey,
        final long pSeqBucketKey) throws TTIOException {

        final long[] orderNumber = getOrderNumbers(pSeqBucketKey);

        // Initial state pointing to the indirect bucket of level 0.
        final long[] keys = new long[IConstants.INDIRECT_BUCKET_COUNT.length + 1];
        IndirectBucket bucket = null;
        keys[0] = pStartKey;
        // Iterate through all levels...
        for (int level = 0; level < orderNumber.length; level++) {
            // ..read the buckets and..
            bucket = (IndirectBucket)pReader.read(keys[level]);
            // ..compute the offsets out of the order-numbers pre-computed before and store it in the
            // key-array.
            keys[level + 1] = bucket.getReferenceKeys()[dataBucketOffset(orderNumber[level])];
            // if the bucketKey is 0, return -1 to distinguish mark non-written buckets explicitly.
            if (keys[level + 1] == 0) {
                Arrays.fill(keys, -1);
                return keys;
            }
        }

        // Return reference to leaf of indirect tree.
        return keys;
    }

    /**
     * Checking the structure based on a long array denoting the path to a leaf (either data or revrootbucket)
     * and their super-bucket.
     * 
     * The check is performed but no feedback is given because of the side-effects because of caching. This
     * can easily be covered by flag, etc. but not in the scope of this prototype.
     * 
     * @param pReader
     *            reader for getting the data from the backend
     * @param pKeys
     *            long array denoting the path to the leaf starting from top to bottom
     * @param mRootOfSubtree
     *            referencebucket representing the root
     * @param pSeqBucketKey
     *            order key for getting the offsets on each level
     * @throws TTIOException
     */
    private static final void checkStructure(final IBackendReader pReader, final long[] pKeys,
        final IReferenceBucket mRootOfSubtree, final long pSeqBucketKey) throws TTIOException {

        // getting the offsets on each level, globally
        final long[] orderNumbers = getOrderNumbers(pSeqBucketKey);
        // starting from the bottong...
        IBucket currentBucket = pReader.read(pKeys[pKeys.length - 1]);
        // ...all data is reconstructed bottom up (meaning from behind to the begin of the path...
        for (int i = orderNumbers.length - 1; i >= 0; i--) {
            // ..for each element, compute the hash and..
            // final byte[] currentHash = currentBucket.secureHash().asBytes();
            // just for benchmarking, since the hash is atm not checked to to side-effekts in caching
            // resolvable
            // over flags within this retrieval
            currentBucket.secureHash().asBytes();
            // ..retrieve the parent and.
            currentBucket = pReader.read(pKeys[i]);
            // ..retrieve the hash form the storage.
            final byte[] storedHash =
                ((IReferenceBucket)currentBucket).getReferenceHashs()[dataBucketOffset(orderNumbers[i])];
            // if the hash was either bootstrapped or the bucket is currently in progress,
            if (Arrays.equals(storedHash, IConstants.NON_HASHED)
                || Arrays.equals(storedHash, IConstants.BOOTSTRAP_HASHED)) {
                // ..just return.
                return;
            }// ...otherwise compare and print the error.
             // else {
             // if (!Arrays.equals(currentHash, storedHash)) {
             // System.err.println("Hashes differ!");
             // }
             // }
        }
        // for the last level, the top (either revrootbucket or uberbucket, do the same.
        // final byte[] currentHash = currentBucket.secureHash().asBytes();
        // just for benchmarking, since the hash is atm not checked to to side-effekts in caching resolvable
        // over flags within this retrieval
        currentBucket.secureHash().asBytes();
        final byte[] storedHash =
            mRootOfSubtree.getReferenceHashs()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET];
        // since the revroot currently in progress is linked to former substructure but has no actual hash,
        // ignore it in that case.
        if (Arrays.equals(storedHash, IConstants.NON_HASHED)
            || Arrays.equals(storedHash, IConstants.BOOTSTRAP_HASHED)) {
            return;
            // } else {
            // if (!Arrays.equals(currentHash, storedHash)) {
            // System.err.println("Hashes differ!");
            // }
        }
    }

    private static final long[] getOrderNumbers(final long pSeqBucketKey) {
        // computing the ordernumbers within all level. The ordernumbers are the position in the sequence of
        // all buckets within the same level.
        // ranges are for level 0: 0-127; level 1: 0-16383; level 2: 0-2097151; level 3: 0-268435455; ;level
        // 4: 0-34359738367
        final long[] orderNumber = new long[IConstants.INDIRECT_BUCKET_COUNT.length];
        for (int level = 0; level < orderNumber.length; level++) {
            orderNumber[level] = pSeqBucketKey >> IConstants.INDIRECT_BUCKET_COUNT[level];
        }
        return orderNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mBucketReader", mBucketReader).add("mBucketReader", mUberBucket)
            .add("mRootBucket", mRootBucket).add("mClose", mClose).toString();
    }

}
