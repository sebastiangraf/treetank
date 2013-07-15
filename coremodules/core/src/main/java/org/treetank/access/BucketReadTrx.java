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
import java.util.List;

import org.treetank.access.conf.ConstructorProps;
import org.treetank.api.IBucketReadTrx;
import org.treetank.api.INode;
import org.treetank.api.ISession;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.NodeBucket.DeletedNode;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
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
    protected final Cache<Long, NodeBucket> mCache;

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
     * Getting the node related to the given node key.
     * 
     * @param pNodeKey
     *            searched for
     * @return the related Node
     * @throws TTIOException
     *             if the read to the persistent storage fails
     */
    public INode getNode(final long pNodeKey) throws TTIOException {
        checkArgument(pNodeKey >= 0);
        checkState(!mClose, "Transaction already closed");
        // Calculate bucket and node part for given nodeKey.
        final long seqBucketKey = pNodeKey >> IConstants.INDIRECT_BUCKET_COUNT[3];
        final int nodeBucketOffset = nodeBucketOffset(pNodeKey);
        NodeBucket bucket = mCache.getIfPresent(seqBucketKey);
        if (bucket == null) {
            final List<NodeBucket> listRevs = getSnapshotBuckets(seqBucketKey);
            final NodeBucket[] revs = listRevs.toArray(new NodeBucket[listRevs.size()]);
            checkState(revs.length > 0, "Number of Buckets to reconstruct must be larger than 0");
            // Build up the complete bucket.
            final IRevisioning revision = mSession.getConfig().mRevision;
            bucket = revision.combineBuckets(revs);
            mCache.put(seqBucketKey, bucket);
        }
        final INode returnVal = bucket.getNode(nodeBucketOffset);
        // root-node is excluded from the checkagainst deletion based on the necesssity of the node-layer to
        // reference against this node while creation of the transaction
        if (pNodeKey == 0) {
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
     * Method to check if an {@link INode} is a deleted one.
     * 
     * @param pToCheck
     *            of the IItem
     * @return the item if it is valid, null otherwise
     */
    protected final INode checkItemIfDeleted(final INode pToCheck) {
        if (pToCheck == null) {
            throw new IllegalStateException(new StringBuilder("Node not existing.").toString());
        } else if (pToCheck instanceof DeletedNode) {
            return null;
        } else {
            return pToCheck;
        }
    }

    /**
     * Dereference node bucket reference.
     * 
     * @param pSeqNodeBucketKey
     *            Key of node bucket.
     * @return Dereferenced bucket.
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected final List<NodeBucket> getSnapshotBuckets(final long pSeqNodeBucketKey) throws TTIOException {

        // Return Value, since the revision iterates a flexible number of version, this has to be a list
        // first.
        final List<NodeBucket> nodeBuckets = new ArrayList<NodeBucket>();

        // Getting the keys for the revRoots
        final long currentRevKey =
            BucketReadTrx.dereferenceLeafOfTree(mBucketReader,
                mUberBucket.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET], mRootBucket
                    .getRevision());
        final RevisionRootBucket rootBucket = (RevisionRootBucket)mBucketReader.read(currentRevKey);
        final int numbersToRestore =
            Integer.parseInt(mSession.getConfig().mProperties.getProperty(ConstructorProps.NUMBERTORESTORE));
        // starting from the current nodebucket
        long nodeBucketKey =
            dereferenceLeafOfTree(mBucketReader,
                rootBucket.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET], pSeqNodeBucketKey);
        NodeBucket bucket;
        // jumping through the nodebuckets based on the pointers
        while (nodeBuckets.size() < numbersToRestore && nodeBucketKey > -1) {
            bucket = (NodeBucket)mBucketReader.read(nodeBucketKey);
            nodeBuckets.add(bucket);
            nodeBucketKey = bucket.getLastBucketPointer();
        }

        return nodeBuckets;

    }

    /**
     * Calculate node bucket offset for a given node key.
     * 
     * @param pNodeKey
     *            Node key to find offset for.
     * @return Offset into node bucket.
     */
    protected static final int nodeBucketOffset(final long pNodeKey) {
        // INDIRECT_BUCKET_COUNT[3] is only taken to get the difference between 2^7 and the actual
        // nodekey as offset. It has nothing to do with the levels.
        final long nodeBucketOffset =
            (pNodeKey - ((pNodeKey >> IConstants.INDIRECT_BUCKET_COUNT[3]) << IConstants.INDIRECT_BUCKET_COUNT[3]));
        return (int)nodeBucketOffset;
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
    protected static final long dereferenceLeafOfTree(final IBackendReader pReader, final long pStartKey,
        final long pSeqBucketKey) throws TTIOException {
        // computing the ordernumbers within all level. The ordernumbers are the position in the sequence of
        // all buckets within the same level.
        // ranges are for level 0: 0-127; level 1: 0-16383; level 2: 0-2097151; level 3: 0-268435455; ;level
        // 4: 0-34359738367
        long[] orderNumber = new long[IConstants.INDIRECT_BUCKET_COUNT.length];
        for (int level = 0; level < orderNumber.length; level++) {
            orderNumber[level] = pSeqBucketKey >> IConstants.INDIRECT_BUCKET_COUNT[level];
        }

        // Initial state pointing to the indirect bucket of level 0.
        long bucketKey = pStartKey;
        IndirectBucket bucket = null;
        // Iterate through all levels...

        for (int level = 0; level < orderNumber.length; level++) {
            // ..read the buckets and..
            bucket = (IndirectBucket)pReader.read(bucketKey);
            // ..compute the offsets out of the order-numbers pre-computed before.
            bucketKey = bucket.getReferenceKeys()[nodeBucketOffset(orderNumber[level])];
            // if the bucketKey is 0, return -1 to distinguish mark non-written buckets explicitly.
            if (bucketKey == 0) {
                return -1;
            }
        }

        // Return reference to leaf of indirect tree.
        return bucketKey;
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
