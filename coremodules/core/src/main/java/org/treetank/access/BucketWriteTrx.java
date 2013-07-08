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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.treetank.access.conf.ConstructorProps;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.IMetaEntry;
import org.treetank.api.INode;
import org.treetank.api.ISession;
import org.treetank.bucket.BucketFactory;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.NodeBucket.DeletedNode;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.BackendWriterProxy;
import org.treetank.io.IBackendWriter;
import org.treetank.io.LogKey;
import org.treetank.io.LogValue;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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

    /** Bucket-Factory to clone buckets. */
    private final BucketFactory mBucketFac;

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

        final long revkey =
            BucketReadTrx.dereferenceLeafOfTree(pWriter,
                pUberBucket.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET], pRepresentRev);
        final RevisionRootBucket revBucket = (RevisionRootBucket)pWriter.read(revkey);
        final MetaBucket metaBucket =
            (MetaBucket)pWriter.read(revBucket.getReferenceKeys()[RevisionRootBucket.META_REFERENCE_OFFSET]);

        mCommitInProgress = Executors.newSingleThreadExecutor();
        mDelegate = new BucketReadTrx(pSession, pUberBucket, revBucket, metaBucket, pWriter);
        mBucketFac = new BucketFactory(pSession.getConfig().mNodeFac, pSession.getConfig().mMetaFac);

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
        final LogValue container = prepareNodeBucket(nodeKey);
        final NodeBucket modified = ((NodeBucket)container.getModified());
        final NodeBucket complete = ((NodeBucket)container.getComplete());
        modified.setNode(nodeBucketOffset, pNode);
        complete.setNode(nodeBucketOffset, pNode);
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
        LogValue container = mBucketWriter.get(key);
        INode item = null;
        // Bucket was modified...
        if (container.getModified() != null) {
            // ..check if the real node was touched and set it or..
            if (((NodeBucket)container.getModified()).getNode(nodeBucketOffset) == null) {
                item = ((NodeBucket)container.getComplete()).getNode(nodeBucketOffset);
            }// ..take the node from the complete status of the page.
            else {
                item = ((NodeBucket)container.getModified()).getNode(nodeBucketOffset);
            }
            checkNotNull(item, "Item must be set!");
            item = mDelegate.checkItemIfDeleted(item);
        }// ...bucket was modified within a former version,...
        else {
            // check the former version as..
            container = mBucketWriter.getFormer(key);
            // ..modified element within this version or...
            if (container.getModified() != null) {
                // ..check if the real node was touched and set it or..
                if (((NodeBucket)container.getModified()).getNode(nodeBucketOffset) == null) {
                    item = clone(((NodeBucket)container.getComplete())).getNode(nodeBucketOffset);
                }// ..take the node from the complete status of the page.
                else {
                    item = clone(((NodeBucket)container.getComplete())).getNode(nodeBucketOffset);
                }
                checkNotNull(item, "Item must be set!");
                item = mDelegate.checkItemIfDeleted(item);
            }// bucket was modified long long before, read it normally.
            else {
                item = mDelegate.getNode(pNodeKey);
            }
        }
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");

        mDelegate.mSession.waitForRunningCommit();

        final UberBucket uber = clone(mNewUber);
        final MetaBucket meta = clone(mNewMeta);
        final RevisionRootBucket rev = clone(mNewRoot);
        final Future<Void> commitInProgress = mBucketWriter.commit(uber, meta, rev);
        mDelegate.mSession.setRunningCommit(mCommitInProgress.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // serializing of new UberPage including its Subtree is concluded.
                commitInProgress.get();
                ((Session)mDelegate.mSession).setLastCommittedUberBucket(uber);
                mDelegate = new BucketReadTrx(mDelegate.mSession, uber, rev, meta, mBucketWriter);
                mBucketWriter.closeFormerLog();
                return null;
            }
        }));
        // Comment here to enabled blocked behaviour
//         mDelegate.mSession.waitForRunningCommit();

        setUpTransaction(uber, rev, meta, mDelegate.mSession, uber.getRevisionNumber(), mBucketWriter);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitBlocked() throws TTException {
        commit();
        mDelegate.mSession.waitForRunningCommit();
    }

    public void clearLog() throws TTIOException {
        mBucketWriter.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean close() throws TTIOException {
        mDelegate.mSession.waitForRunningCommit();
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

        final LogKey key =
            new LogKey(false, IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT.length, seqNodeBucketKey);
        // See if on nodeBucketLevel, there are any buckets.
        LogValue container = mBucketWriter.get(key);
        // if not,...
        if (container.getModified() == null) {
            // ..start preparing a new container to be logged
            final LogKey indirectKey = preparePathToLeaf(false, mNewRoot, pNodeKey);
            final LogValue indirectContainer = mBucketWriter.get(indirectKey);
            final int nodeOffset = nodeBucketOffset(seqNodeBucketKey);
            final long bucketKey =
                ((IndirectBucket)indirectContainer.getModified()).getReferenceKeys()[nodeOffset];
            final long newBucketKey = mNewUber.incrementBucketCounter();
            // if there is not any bucket already existing...
            if (bucketKey != 0) {
                // ...just denote the number of elements necessary to restore (only for visibility reasons).
                final int revToRestore =
                    Integer.parseInt(mDelegate.mSession.getConfig().mProperties
                        .getProperty(ConstructorProps.NUMBERTORESTORE));

                // Gather all data, from the former log..
                final LogValue formerModified = mBucketWriter.getFormer(key);
                // ..and from the former revision.
                final List<NodeBucket> formerBuckets = mDelegate.getSnapshotBuckets(seqNodeBucketKey);
                // declare summarized buckets.
                final List<NodeBucket> bucketList = new ArrayList<NodeBucket>();

                // Look, if a former log is currently in process to be written...
                if (formerModified.getModified() != null) {
                    // ..if so, check if the modified one...
                    final NodeBucket currentlyInProgress = (NodeBucket)formerModified.getModified();
                    // ... is the same one than recently written (to avoid race conditions).
                    if (formerBuckets.isEmpty()
                        || formerBuckets.get(0).getBucketKey() < currentlyInProgress.getBucketKey()) {
                        bucketList.add((NodeBucket)clone(currentlyInProgress));
                    }
                }

                // All currently written elements are inserted so if no elements are in the bucketlist...
                if (bucketList.isEmpty() || formerBuckets.size() < revToRestore) {
                    // ...add all former ones...
                    bucketList.addAll(formerBuckets);
                }// ..otherwise, take all elements starting index 1 into account
                else {
                    if (formerBuckets.size() > 1) {
                        bucketList.addAll(formerBuckets.subList(0, formerBuckets.size() - 1));
                    }
                }

                // Transform into array..
                final NodeBucket[] buckets = bucketList.toArray(new NodeBucket[bucketList.size()]);
                // ..and check that the number of buckets are valid and return the entire bucket.
                checkState(buckets.length > 0);
                container =
                    mDelegate.mSession.getConfig().mRevision.combineBucketsForModification(revToRestore,
                        newBucketKey, buckets, mNewRoot.getRevision() % revToRestore == 0);

                // // // DEBUG CODE!!!!!
                // INode[] toCheck = ((NodeBucket)container.getComplete()).getNodes();
                // boolean nullFound = false;
                // for (int i = 0; i < toCheck.length && !nullFound; i++) {
                // if ((i < toCheck.length - 1 && i > 0)
                // && (toCheck[i + 1] != null && toCheck[i] == null && toCheck[i - 1] != null)) {
                // nullFound = true;
                // }
                // }
                // if (nullFound) {
                // System.out.println("-----FAILURE------");
                // for (int i = 0; i < buckets.length; i++) {
                // System.out.println("+++++++++++++++");
                // System.out.println(buckets[i].toString());
                // System.out.println("+++++++++++++++");
                // }
                // System.out.println("-----------");
                // System.exit(-1);
                // }

            }// ...if no bucket is existing, create an entirely new one.
            else {
                final NodeBucket newBucket = new NodeBucket(newBucketKey, IConstants.NULL_NODE);
                container = new LogValue(newBucket, newBucket);
            }
            ((IndirectBucket)indirectContainer.getModified()).setReferenceKey(nodeOffset, newBucketKey);
            ((IndirectBucket)indirectContainer.getModified()).setReferenceHash(nodeOffset,
                IConstants.NON_HASHED);
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

                // if there existed the same bucket in former versions in a former log or
                container = mBucketWriter.getFormer(key);
                IReferenceBucket oldBucket = null;
                if (container.getModified() != null) {
                    oldBucket = (IReferenceBucket)container.getModified();
                }
                // over the former log or the offset within
                // the parent...
                else if (parentBucket.getReferenceKeys()[offset] != 0) {
                    oldBucket = (IReferenceBucket)mBucketWriter.read(parentBucket.getReferenceKeys()[offset]);
                }
                // ..copy all references to the new log.
                if (oldBucket != null) {
                    for (int i = 0; i < oldBucket.getReferenceKeys().length; i++) {
                        bucket.setReferenceKey(i, oldBucket.getReferenceKeys()[i]);
                        bucket.setReferenceHash(i, oldBucket.getReferenceHashs()[i]);
                    }
                }

                // Set the newKey on the computed offset...
                parentBucket.setReferenceKey(offset, newKey);
                // ...and mark the hashoffset as not written
                parentBucket.setReferenceHash(offset, IConstants.NON_HASHED);
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

    private void setUpTransaction(final UberBucket pUberOld, final RevisionRootBucket pRootToRepresent,
        final MetaBucket pMetaOld, final ISession pSession, final long pRepresentRev,
        final BackendWriterProxy pWriter) throws TTException {

        mNewUber =
            new UberBucket(pUberOld.getBucketCounter() + 1, pUberOld.getRevisionNumber() + 1, pUberOld
                .getBucketCounter() + 1);
        mNewUber.setReferenceKey(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET,
            pUberOld.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET]);
        mNewUber.setReferenceHash(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, IConstants.NON_HASHED);

        // Prepare indirect tree to hold reference to prepared revision root
        // nodeBucketReference.
        final LogKey indirectKey = preparePathToLeaf(true, mNewUber, mNewUber.getRevisionNumber());
        final LogValue indirectContainer = mBucketWriter.get(indirectKey);
        final int offset = nodeBucketOffset(mNewUber.getRevisionNumber());

        // Get previous revision root bucket and using this data to initialize a fresh revision root including
        // the pointers.
        mNewRoot =
            new RevisionRootBucket(mNewUber.incrementBucketCounter(), pRepresentRev + 1, pRootToRepresent
                .getMaxNodeKey());
        mNewRoot.setReferenceKey(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, pRootToRepresent
            .getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET]);
        mNewRoot.setReferenceHash(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, IConstants.NON_HASHED);

        // setting the new revRoot to the correct offset
        ((IndirectBucket)indirectContainer.getModified()).setReferenceKey(offset, mNewRoot.getBucketKey());
        ((IndirectBucket)indirectContainer.getModified()).setReferenceHash(
            IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, IConstants.NON_HASHED);

        mBucketWriter.put(indirectKey, indirectContainer);

        // Setting up a new metabucket and link it to the new root
        final Set<Map.Entry<IMetaEntry, IMetaEntry>> keySet = pMetaOld.entrySet();
        mNewMeta = new MetaBucket(mNewUber.incrementBucketCounter());
        for (final Map.Entry<IMetaEntry, IMetaEntry> key : keySet) {
            mNewMeta.put(clone(key.getKey()), clone(key.getValue()));
        }
        mNewRoot.setReferenceKey(RevisionRootBucket.META_REFERENCE_OFFSET, mNewMeta.getBucketKey());
        mNewRoot.setReferenceHash(RevisionRootBucket.META_REFERENCE_OFFSET, IConstants.NON_HASHED);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mDelegate", mDelegate).add("mBucketWriterProxy", mBucketWriter).add(
            "mRootBucket", mNewRoot).add("mDelegate", mDelegate).toString();
    }

    private IMetaEntry clone(final IMetaEntry pToClone) throws TTIOException {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        pToClone.serialize(output);
        final ByteArrayDataInput input = ByteStreams.newDataInput(output.toByteArray());
        return mDelegate.mSession.getConfig().mMetaFac.deserializeEntry(input);
    }

    @SuppressWarnings("unchecked")
    private <E extends IBucket> E clone(final E pToClone) throws TTIOException {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        pToClone.serialize(output);
        final ByteArrayDataInput input = ByteStreams.newDataInput(output.toByteArray());
        return (E)mBucketFac.deserializeBucket(input);
    }

}
