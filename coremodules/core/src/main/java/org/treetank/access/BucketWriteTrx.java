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
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.treetank.access.BucketReadTrx.nodeBucketOffset;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.treetank.access.conf.ConstructorProps;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.IMetaEntry;
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
import org.treetank.io.BackendWriterProxy;
import org.treetank.io.IBackendWriter;
import org.treetank.io.LogKey;
import org.treetank.io.LogValue;

/**
 * <h1>BucketWriteTrx</h1>
 * 
 * <p>
 * See {@link BucketReadTrx}.
 * </p>
 */
public final class BucketWriteTrx implements IBucketWriteTrx {

    /** Bucket writer to serialize. */
    private final BackendWriterProxy mBucketWriter;

    /** Reference to the actual uberBucket. */
    private UberBucket mNewUber;

    /** Reference to the actual revRoot. */
    private RevisionRootBucket mNewRoot;

    /** Last reference to the actual nameBucket. */
    private MetaBucket mNewMeta;

    /** Delegate for read access. */
    private BucketReadTrx mDelegate;

    /** Executor for tracing commit in progress. */
    private final ExecutorService mCommitInProgress;

    /**
     * Standard constructor.
     * 
     * 
     * @param pSession
     *            {@link ISession} reference
     * @param pUberBucket
     *            root of resource
     * @param pWriter
     *            writer where this transaction should write to
     * @param pRepresentRev
     *            revision represent
     * @throws TTIOException
     *             if IO Error
     */
    protected BucketWriteTrx(final ISession pSession, final UberBucket pUberBucket,
        final IBackendWriter pWriter, final long pRepresentRev) throws TTException {

        mBucketWriter =
            new BackendWriterProxy(pWriter, new File(pSession.getConfig().mProperties
                .getProperty(org.treetank.access.conf.ConstructorProps.RESOURCEPATH)),
                pSession.getConfig().mNodeFac, pSession.getConfig().mMetaFac);

        final RevisionRootBucket revBucket =
            (RevisionRootBucket)pWriter.read(BucketReadTrx.dereferenceLeafOfTree(pWriter, pUberBucket
                .getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET], pRepresentRev));
        final MetaBucket metaBucket =
            (MetaBucket)pWriter.read(revBucket.getReferenceKeys()[RevisionRootBucket.META_REFERENCE_OFFSET]);

        mCommitInProgress = Executors.newSingleThreadExecutor();

        setUpTransaction(pUberBucket, revBucket, metaBucket, pSession, pRepresentRev, mBucketWriter);
    }

    /**
     * {@inheritDoc}
     */
    public long setNode(final INode pNode) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        // Allocate node key and increment node count.
        final long nodeKey = pNode.getNodeKey();
        final long seqBucketKey = nodeKey >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3];
        final int nodeBucketOffset = nodeBucketOffset(nodeKey);
        LogValue container = prepareNodeBucket(nodeKey);
        final NodeBucket bucket = ((NodeBucket)container.getModified());
        bucket.setNode(nodeBucketOffset, pNode);
        mBucketWriter.put(new LogKey(false, IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT.length, seqBucketKey),
            container);
        return nodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(final INode pNode) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        checkNotNull(pNode);
        final long nodeBucketKey = pNode.getNodeKey() >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3];
        LogValue container = prepareNodeBucket(pNode.getNodeKey());
        final INode delNode = new DeletedNode(pNode.getNodeKey());
        ((NodeBucket)container.getComplete()).setNode(nodeBucketOffset(pNode.getNodeKey()), delNode);
        ((NodeBucket)container.getModified()).setNode(nodeBucketOffset(pNode.getNodeKey()), delNode);

        mBucketWriter.put(
            new LogKey(false, IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT.length, nodeBucketKey), container);
    }

    /**
     * {@inheritDoc}
     */
    public INode getNode(final long pNodeKey) throws TTIOException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        // Calculate bucket and node part for given nodeKey.
        final long nodeBucketKey = pNodeKey >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3];
        final int nodeBucketOffset = nodeBucketOffset(pNodeKey);

        final LogKey key =
            new LogKey(false, IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT.length, nodeBucketKey);
        final LogValue container = mBucketWriter.get(key);
        // Bucket was not modified yet, delegate to read or..
        if (container.getModified() == null) {
            return mDelegate.getNode(pNodeKey);
        }// ...bucket was modified, but not this node, take the complete part, or...
        else if (((NodeBucket)container.getModified()).getNode(nodeBucketOffset) == null) {
            final INode item = ((NodeBucket)container.getComplete()).getNode(nodeBucketOffset);
            return mDelegate.checkItemIfDeleted(item);

        }// ...bucket was modified and the modification touched this node.
        else {
            final INode item = ((NodeBucket)container.getModified()).getNode(nodeBucketOffset);
            return mDelegate.checkItemIfDeleted(item);
        }

    }

    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void commit() throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");

        final Future<Void> commitInProgress = mBucketWriter.commit(mNewUber, mNewMeta, mNewRoot);

        Callable<Void> tracingCommit = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                commitInProgress.get();
                ((Session)mDelegate.mSession).setLastCommittedUberBucket(mNewUber);
                return null;
            }
        };
        try {
            mCommitInProgress.submit(tracingCommit).get();
        } catch (InterruptedException | ExecutionException exc) {
            throw new TTIOException(exc);
        }
        setUpTransaction(mNewUber, mNewRoot, mNewMeta, mDelegate.mSession, mNewUber.getRevisionNumber(),
            mBucketWriter);

    }

    public void clearLog() throws TTIOException {
        mBucketWriter.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean close() throws TTIOException {
        mCommitInProgress.shutdown();
        if (!mDelegate.isClosed()) {
            mDelegate.close();

            try {
                // Try to close the log.
                // It may already be closed if a commit
                // was the last operation.
                mBucketWriter.close();
            } catch (IllegalStateException e) {
                // Do nothing
            }
            mDelegate.mSession.deregisterBucketTrx(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long incrementNodeKey() {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        return mNewRoot.incrementMaxNodeKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRevision() throws TTIOException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        return mNewRoot.getRevision();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return mDelegate.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaBucket getMetaBucket() {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        return mNewMeta;
    }

    private LogValue prepareNodeBucket(final long pNodeKey) throws TTException {

        final long seqNodeBucketKey = pNodeKey >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3];

        LogKey key = new LogKey(false, IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT.length, seqNodeBucketKey);
        // See if on nodeBucketLevel, there are any buckets...
        LogValue container = mBucketWriter.get(key);
        // ... and start dereferencing of not.
        if (container.getModified() == null) {
            LogKey indirectKey = preparePathToLeaf(false, mNewRoot, pNodeKey);

            LogValue indirectContainer = mBucketWriter.get(indirectKey);
            int nodeOffset = nodeBucketOffset(seqNodeBucketKey);
            long bucketKey = ((IndirectBucket)indirectContainer.getModified()).getReferenceKeys()[nodeOffset];

            long newBucketKey = mNewUber.incrementBucketCounter();
            if (bucketKey != 0) {
                NodeBucket[] buckets = mDelegate.getSnapshotBuckets(seqNodeBucketKey);
                checkState(buckets.length > 0);
                if (mNewRoot.getRevision()
                    % Integer.parseInt(mDelegate.mSession.getConfig().mProperties
                        .getProperty(ConstructorProps.NUMBERTORESTORE)) == 0) {
                    container =
                        mDelegate.mSession.getConfig().mRevision.combineBucketsForModification(Integer
                            .parseInt(mDelegate.mSession.getConfig().mProperties
                                .getProperty(ConstructorProps.NUMBERTORESTORE)), newBucketKey, buckets, true);
                } else {
                    container =
                        mDelegate.mSession.getConfig().mRevision
                            .combineBucketsForModification(Integer
                                .parseInt(mDelegate.mSession.getConfig().mProperties
                                    .getProperty(ConstructorProps.NUMBERTORESTORE)), newBucketKey, buckets,
                                false);
                }
            } else {
                NodeBucket newBucket = new NodeBucket(newBucketKey, IConstants.NULL_NODE);
                container = new LogValue(newBucket, newBucket);
            }
            ((IndirectBucket)indirectContainer.getModified()).setReferenceKey(nodeOffset, newBucketKey);
            mBucketWriter.put(indirectKey, indirectContainer);
            mBucketWriter.put(key, container);
        }
        return container;
    }

    /**
     * Getting a {@link LogKey} containing the last IndirectBucket with the reference to any new/modified
     * bucket.
     * 
     * @param pIsRootLevel
     *            is this dereferencing walk based on the the search after a RevRoot or a NodeBucket. Needed
     *            because of the same keys in both subtrees.
     * @param pBucket
     *            where to start the tree-walk: either from an UberBucket (related to new
     *            RevisionRootBuckets) or from a RevisionRootBucket (related to new NodeBuckets).
     * @param pElementKey
     *            key to be dereferenced
     * @return the key the container representing the last level
     * @throws TTException
     */
    private LogKey preparePathToLeaf(final boolean pIsRootLevel, final IReferenceBucket pBucket,
        final long pElementKey) throws TTException {

        // computing the ordernumbers within all level. The ordernumbers are the position in the sequence of
        // all buckets within the same level.
        // ranges are for level 0: 0-127; level 1: 0-16383; level 2: 0-2097151; level 3: 0-268435455; ;level
        // 4: 0-34359738367
        long seqBucketKey = -1;
        // since the revision points to a bucket, the sequence-key bases on the last indirect-layer directly
        // within the search after a revision,...
        if (pIsRootLevel) {
            seqBucketKey = pElementKey >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3];
        } // ...whereas one layer above is used for the nodes based on the offsets pointing to nodes
          // instead of buckets.
        else {
            seqBucketKey = pElementKey >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[2];
        }

        long[] orderNumber = new long[IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT.length];
        for (int level = 0; level < orderNumber.length; level++) {
            orderNumber[level] = seqBucketKey >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[level];
        }

        IReferenceBucket bucket = null;
        IReferenceBucket parentBucket = pBucket;
        LogKey key = null;
        LogKey parentKey = new LogKey(pIsRootLevel, -1, 0);

        // Iterate through all levels...
        for (int level = 0; level < orderNumber.length; level++) {
            // ...see if the actual bucket requested is already in the log
            key = new LogKey(pIsRootLevel, level, orderNumber[level]);
            LogValue container = mBucketWriter.get(key);
            // if the bucket is not existing,..
            if (container.getModified() == null) {
                // ..create a new bucket
                final long newKey = mNewUber.incrementBucketCounter();
                bucket = new IndirectBucket(newKey);

                // compute the offset of the new bucket
                int offset = nodeBucketOffset(orderNumber[level]);

                // if there existed the same bucket in former versions (referencable over the offset within
                // the parent)...
                if (parentBucket.getReferenceKeys()[offset] != 0) {
                    IReferenceBucket oldBucket =
                        (IReferenceBucket)mBucketWriter.read(parentBucket.getReferenceKeys()[offset]);
                    for (int i = 0; i < oldBucket.getReferenceKeys().length; i++) {
                        bucket.setReferenceKey(i, oldBucket.getReferenceKeys()[i]);
                    }
                }
                // Set the newKey on the computed offset...
                parentBucket.setReferenceKey(offset, newKey);
                // .. and put the parent-reference to the log...
                container = new LogValue(parentBucket, parentBucket);
                // ..if the parent is not referenced as UberBucket or RevisionRootBucket within the Wtx
                // itself...
                if (level > 0) {
                    mBucketWriter.put(parentKey, container);
                }
                // ..but set the reference of the current bucket in every case.
                container = new LogValue(bucket, bucket);
                mBucketWriter.put(key, container);

            } // if the bucket is already in the log, get it simply from the log.
            else {
                bucket = (IReferenceBucket)container.getModified();
            }
            // finally, set the new bucketkey for the next level
            parentKey = key;
            parentBucket = bucket;
        }

        // Return reference to leaf of indirect tree.
        return key;
    }

    private void setUpTransaction(final UberBucket pUberBucket, final RevisionRootBucket pRevRoot,
        final MetaBucket pMetaOld, final ISession pSession, final long pRepresentRev,
        final BackendWriterProxy pWriter) throws TTException {

        mNewUber =
            new UberBucket(pUberBucket.incrementBucketCounter(), pUberBucket.getRevisionNumber() + 1,
                pUberBucket.getBucketCounter());
        mNewUber.setReferenceKey(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET,
            pUberBucket.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET]);

        mDelegate = new BucketReadTrx(pSession, pUberBucket, pRevRoot, pMetaOld, pWriter);

        // Get previous revision root bucket..
        final RevisionRootBucket previousRevRoot = mDelegate.mRootBucket;
        // ...and using this data to initialize a fresh revision root including the pointers.
        mNewRoot =
            new RevisionRootBucket(mNewUber.incrementBucketCounter(), pRepresentRev + 1, previousRevRoot
                .getMaxNodeKey());
        mNewRoot.setReferenceKey(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, previousRevRoot
            .getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET]);

        // Prepare indirect tree to hold reference to prepared revision root
        // nodeBucketReference.
        LogKey indirectKey = preparePathToLeaf(true, mNewUber, mNewUber.getRevisionNumber());
        LogValue indirectContainer = mBucketWriter.get(indirectKey);
        int offset = nodeBucketOffset(mNewUber.getRevisionNumber());
        ((IndirectBucket)indirectContainer.getModified()).setReferenceKey(offset, mNewRoot.getBucketKey());
        mBucketWriter.put(indirectKey, indirectContainer);

        // Setting up a new metabucket
        Map<IMetaEntry, IMetaEntry> oldMap = pMetaOld.getMetaMap();
        mNewMeta = new MetaBucket(mNewUber.incrementBucketCounter());

        for (IMetaEntry key : oldMap.keySet()) {
            mNewMeta.setEntry(key, oldMap.get(key));
        }

        mNewRoot.setReferenceKey(RevisionRootBucket.META_REFERENCE_OFFSET, mNewMeta.getBucketKey());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mDelegate", mDelegate).add("mBucketWriterProxy", mBucketWriter).add(
            "mRootBucket", mNewRoot).add("mDelegate", mDelegate).toString();
    }

}
