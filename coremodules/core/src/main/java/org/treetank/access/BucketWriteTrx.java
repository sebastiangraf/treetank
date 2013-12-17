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
import static org.treetank.access.BucketReadTrx.dataBucketOffset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jclouds.javax.annotation.Nullable;
import org.treetank.access.conf.ConstructorProps;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.IData;
import org.treetank.api.IMetaEntry;
import org.treetank.api.ISession;
import org.treetank.bucket.BucketFactory;
import org.treetank.bucket.DataBucket;
import org.treetank.bucket.DataBucket.DeletedData;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.IndirectBucket;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.io.ILog;
import org.treetank.io.LRULog;
import org.treetank.io.LogKey;
import org.treetank.io.LogValue;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

    /** BackendWriter to serialize. */
    private final IBackendWriter mBackendWriter;

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

    /** Current LRULog instance to write currently to. */
    private ILog mLog;

    /** Former log instance utilizing while commit is in process. */
    @Nullable
    private ILog mFormerLog;

    /** Transient cache for buffering former data-bucket hashes */
    private final Cache<Long, byte[]> mFormerDataBucketHashes;

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
        mBackendWriter = pWriter;

        final long revkey =
            BucketReadTrx.dereferenceLeafOfTree(pWriter,
                pUberBucket.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET], pRepresentRev)[IConstants.INDIRECT_BUCKET_COUNT.length];
        final RevisionRootBucket revBucket = (RevisionRootBucket)pWriter.read(revkey);
        final MetaBucket metaBucket =
            (MetaBucket)pWriter.read(revBucket.getReferenceKeys()[RevisionRootBucket.META_REFERENCE_OFFSET]);

        mCommitInProgress = Executors.newSingleThreadExecutor();
        mDelegate = new BucketReadTrx(pSession, pUberBucket, revBucket, metaBucket, pWriter);
        mBucketFac = new BucketFactory(pSession.getConfig().mDataFac, pSession.getConfig().mMetaFac);

        // mLog = new MemoryLog();
        mLog =
            new LRULog(new File(pSession.getConfig().mProperties
                .getProperty(org.treetank.access.conf.ConstructorProps.RESOURCEPATH)),
                pSession.getConfig().mDataFac, pSession.getConfig().mMetaFac);

        mFormerLog = mLog;
        mFormerDataBucketHashes = CacheBuilder.newBuilder().maximumSize(16384).build();
        setUpTransaction(pUberBucket, revBucket, metaBucket, pSession, pRepresentRev);
    }

    /**
     * {@inheritDoc}
     */
    public long setData(final IData pData) throws TTException {

        checkState(!mDelegate.isClosed(), "Transaction already closed");
        // Allocate data key and increment data count.
        final long dataKey = pData.getDataKey();
        final long seqBucketKey = dataKey >> IConstants.INDIRECT_BUCKET_COUNT[3];
        final int dataBucketOffset = dataBucketOffset(dataKey);
        final LogValue container = prepareDataBucket(dataKey);
        final DataBucket modified = ((DataBucket)container.getModified());
        final DataBucket complete = ((DataBucket)container.getComplete());
        modified.setData(dataBucketOffset, pData);
        complete.setData(dataBucketOffset, pData);
        mLog.put(new LogKey(false, IConstants.INDIRECT_BUCKET_COUNT.length, seqBucketKey), container);
        return dataKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeData(final IData pData) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        checkNotNull(pData);
        final long dataBucketKey = pData.getDataKey() >> IConstants.INDIRECT_BUCKET_COUNT[3];
        LogValue container = prepareDataBucket(pData.getDataKey());
        final IData delData = new DeletedData(pData.getDataKey());
        ((DataBucket)container.getComplete()).setData(dataBucketOffset(pData.getDataKey()), delData);
        ((DataBucket)container.getModified()).setData(dataBucketOffset(pData.getDataKey()), delData);

        mLog.put(new LogKey(false, IConstants.INDIRECT_BUCKET_COUNT.length, dataBucketKey), container);
    }

    /**
     * {@inheritDoc}
     */
    public IData getData(final long pDataKey) throws TTIOException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        // Calculate bucket and data part for given dataKey.
        final long dataBucketKey = pDataKey >> IConstants.INDIRECT_BUCKET_COUNT[3];
        final int dataBucketOffset = dataBucketOffset(pDataKey);

        final LogKey key = new LogKey(false, IConstants.INDIRECT_BUCKET_COUNT.length, dataBucketKey);
        LogValue container = mLog.get(key);
        IData item = null;
        // Bucket was modified...
        if (container.getModified() != null) {
            // ..check if the real data was touched and set it or..
            if (((DataBucket)container.getModified()).getData(dataBucketOffset) == null) {
                item = ((DataBucket)container.getComplete()).getData(dataBucketOffset);
            }// ..take the data from the complete status of the page.
            else {
                item = ((DataBucket)container.getModified()).getData(dataBucketOffset);
            }
            checkNotNull(item, "Item must be set!");
            item = mDelegate.checkItemIfDeleted(item);
        }// ...bucket was modified within a former version,...
        else {
            // check the former version as..
            container = mFormerLog.get(key);
            // ..modified element within this version or...
            if (container.getModified() != null) {
                // ..check if the real data was touched and set it or..
                if (((DataBucket)container.getModified()).getData(dataBucketOffset) == null) {
                    item = clone(((DataBucket)container.getComplete())).getData(dataBucketOffset);
                }// ..take the data from the complete status of the page.
                else {
                    item = clone(((DataBucket)container.getComplete())).getData(dataBucketOffset);
                }
                checkNotNull(item, "Item must be set!");
                item = mDelegate.checkItemIfDeleted(item);
            }// bucket was modified long long before, read it normally.
            else {
                item = mDelegate.getData(pDataKey);
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
        // storing the reference to the former log.
        mFormerLog = mLog;
        // new log
        // mLog = new MemoryLog();
        mLog =
            new LRULog(new File(mDelegate.mSession.getConfig().mProperties
                .getProperty(org.treetank.access.conf.ConstructorProps.RESOURCEPATH)), mDelegate.mSession
                .getConfig().mDataFac, mDelegate.mSession.getConfig().mMetaFac);

        mDelegate.mSession.setRunningCommit(mCommitInProgress.submit(new CommitCallable(uber, rev, meta)));
        // Comment here to enabled blocked behaviour
        // mDelegate.mSession.waitForRunningCommit();

        setUpTransaction(uber, rev, meta, mDelegate.mSession, uber.getRevisionNumber());

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
        mLog.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean close() throws TTIOException {
        mCommitInProgress.shutdown();
        mDelegate.mSession.waitForRunningCommit();
        if (!mDelegate.isClosed()) {
            mDelegate.close();

            try {
                // Try to close the log.
                // It may already be closed if a commit
                // was the last operation.
                mLog.close();
                mBackendWriter.close();
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
    public long incrementDataKey() {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        return mNewRoot.incrementMaxDataKey();
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

    private LogValue prepareDataBucket(final long pDataKey) throws TTException {

        final long seqDataBucketKey = pDataKey >> IConstants.INDIRECT_BUCKET_COUNT[3];

        final LogKey key = new LogKey(false, IConstants.INDIRECT_BUCKET_COUNT.length, seqDataBucketKey);
        // See if on dataBucketLevel, there are any buckets.
        LogValue container = mLog.get(key);
        // if not,...
        if (container.getModified() == null) {
            // ..start preparing a new container to be logged
            final LogKey indirectKey = preparePathToLeaf(false, mNewRoot, pDataKey);
            final LogValue indirectContainer = mLog.get(indirectKey);
            final int dataOffset = dataBucketOffset(seqDataBucketKey);
            final long bucketKey =
                ((IndirectBucket)indirectContainer.getModified()).getReferenceKeys()[dataOffset];
            final long newBucketKey = mNewUber.incrementBucketCounter();
            // if there is not any bucket already existing...
            if (bucketKey != 0) {
                // ...just denote the number of elements necessary to restore (only for visibility reasons).
                final int revToRestore =
                    Integer.parseInt(mDelegate.mSession.getConfig().mProperties
                        .getProperty(ConstructorProps.NUMBERTORESTORE));

                // Gather all data, from the former log..
                final LogValue formerModified = mFormerLog.get(key);
                // ..and from the former revision.
                final List<DataBucket> formerBuckets = mDelegate.getSnapshotBuckets(seqDataBucketKey);
                // declare summarized buckets.
                final List<DataBucket> bucketList = new ArrayList<DataBucket>();

                // Look, if a former log is currently in process to be written...
                if (formerModified.getModified() != null) {
                    // ..if so, check if the modified one...
                    final DataBucket currentlyInProgress = (DataBucket)formerModified.getModified();
                    // ... is the same one than recently written (to avoid race conditions).
                    if (formerBuckets.isEmpty()
                        || formerBuckets.get(0).getBucketKey() < currentlyInProgress.getBucketKey()) {
                        bucketList.add((DataBucket)clone(currentlyInProgress));
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
                final DataBucket[] buckets = bucketList.toArray(new DataBucket[bucketList.size()]);
                // ..and check that the number of buckets are valid and return the entire bucket.
                checkState(buckets.length > 0);
                container =
                    mDelegate.mSession.getConfig().mRevision.combineBucketsForModification(revToRestore,
                        newBucketKey, buckets, mNewRoot.getRevision() % revToRestore == 0);

                // // // DEBUG CODE!!!!!
                // IData[] toCheck = ((DataBucket)container.getComplete()).getDatas();
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
                final DataBucket newBucket = new DataBucket(newBucketKey, IConstants.NULLDATA);
                container = new LogValue(newBucket, newBucket);
            }
            ((IndirectBucket)indirectContainer.getModified()).setReferenceKey(dataOffset, newBucketKey);
            ((IndirectBucket)indirectContainer.getModified()).setReferenceHash(dataOffset,
                IConstants.NON_HASHED);
            mLog.put(indirectKey, indirectContainer);
            mLog.put(key, container);
        }
        return container;
    }

    /**
     * Getting a {@link LogKey} containing the last IndirectBucket with the reference to any new/modified
     * bucket.
     * 
     * @param pIsRootLevel
     *            is this dereferencing walk based on the the search after a RevRoot or a DataBucket. Needed
     *            because of the same keys in both subtrees.
     * @param pBucket
     *            where to start the tree-walk: either from an UberBucket (related to new
     *            RevisionRootBuckets) or from a RevisionRootBucket (related to new DataBuckets).
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
            seqBucketKey = pElementKey >> IConstants.INDIRECT_BUCKET_COUNT[3];
        } // ...whereas one layer above is used for the datas based on the offsets pointing to datas
          // instead of buckets.
        else {
            seqBucketKey = pElementKey >> IConstants.INDIRECT_BUCKET_COUNT[2];
        }

        long[] orderNumber = new long[IConstants.INDIRECT_BUCKET_COUNT.length];
        for (int level = 0; level < orderNumber.length; level++) {
            orderNumber[level] = seqBucketKey >> IConstants.INDIRECT_BUCKET_COUNT[level];
        }

        IReferenceBucket bucket = null;
        IReferenceBucket parentBucket = pBucket;
        LogKey key = null;
        LogKey parentKey = new LogKey(pIsRootLevel, -1, 0);

        // Iterate through all levels...
        for (int level = 0; level < orderNumber.length; level++) {
            // ...see if the actual bucket requested is already in the log
            key = new LogKey(pIsRootLevel, level, orderNumber[level]);
            LogValue container = mLog.get(key);
            // if the bucket is not existing,..
            if (container.getModified() == null) {
                // ..create a new bucket
                final long newKey = mNewUber.incrementBucketCounter();
                bucket = new IndirectBucket(newKey);

                // compute the offset of the new bucket
                int offset = dataBucketOffset(orderNumber[level]);

                // if there existed the same bucket in former versions in a former log or
                container = mFormerLog.get(key);
                IReferenceBucket oldBucket = null;
                if (container.getModified() != null) {
                    oldBucket = (IReferenceBucket)container.getModified();
                }
                // over the former log or the offset within
                // the parent...
                else if (parentBucket.getReferenceKeys()[offset] != 0) {
                    oldBucket =
                        (IReferenceBucket)mBackendWriter.read(parentBucket.getReferenceKeys()[offset]);
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
                    mLog.put(parentKey, container);
                }
                // ..but set the reference of the current bucket in every case.
                container = new LogValue(bucket, bucket);
                mLog.put(key, container);

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
        final MetaBucket pMetaOld, final ISession pSession, final long pRepresentRev) throws TTException {

        mNewUber =
            new UberBucket(pUberOld.getBucketCounter() + 1, pUberOld.getRevisionNumber() + 1, pUberOld
                .getBucketCounter() + 1);
        mNewUber.setReferenceKey(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET,
            pUberOld.getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET]);
        mNewUber.setReferenceHash(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, IConstants.NON_HASHED);

        // Prepare indirect tree to hold reference to prepared revision root
        // dataBucketReference.
        final LogKey indirectKey = preparePathToLeaf(true, mNewUber, mNewUber.getRevisionNumber());
        final LogValue indirectContainer = mLog.get(indirectKey);
        final int offset = dataBucketOffset(mNewUber.getRevisionNumber());

        // Get previous revision root bucket and using this data to initialize a fresh revision root including
        // the pointers.
        mNewRoot =
            new RevisionRootBucket(mNewUber.incrementBucketCounter(), pRepresentRev + 1, pRootToRepresent
                .getMaxDataKey());
        mNewRoot.setReferenceKey(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, pRootToRepresent
            .getReferenceKeys()[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET]);
        mNewRoot.setReferenceHash(IReferenceBucket.GUARANTEED_INDIRECT_OFFSET, IConstants.NON_HASHED);

        // setting the new revRoot to the correct offset
        ((IndirectBucket)indirectContainer.getModified()).setReferenceKey(offset, mNewRoot.getBucketKey());
        ((IndirectBucket)indirectContainer.getModified()).setReferenceHash(offset, IConstants.NON_HASHED);

        mLog.put(indirectKey, indirectContainer);

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
        return toStringHelper(this).add("mDelegate", mDelegate).add("mBackendWriter", mBackendWriter).add(
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

    /**
     * Closing the former log if not needed any more
     * 
     * @throws TTIOException
     *             if any weird happens
     */
    private void closeFormerLog() throws TTIOException {
        if (mFormerLog != null && !mFormerLog.isClosed()) {
            mFormerLog.close();
        }
    }

    /**
     * Persistence-task to be performed within a commit.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class CommitCallable implements Callable<Void> {

        final MetaBucket mMeta;
        final RevisionRootBucket mRoot;
        final UberBucket mUber;

        /**
         * Constructor.
         * 
         * @param pUber
         *            to persist
         * @param pRoot
         *            to persist
         * @param pMeta
         *            to persist
         */
        CommitCallable(final UberBucket pUber, final RevisionRootBucket pRoot, final MetaBucket pMeta) {
            mUber = pUber;
            mRoot = pRoot;
            mMeta = pMeta;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Void call() throws Exception {
            // final long time = System.currentTimeMillis();
            // iterate data tree
            iterateSubtree(false);
            // get last IndirectBucket referenced from the RevRoot.
            final LogKey dataKey = new LogKey(false, 0, 0);
            final IReferenceBucket dataBuck = (IReferenceBucket)mFormerLog.get(dataKey).getModified();
            // Check if there are any modifications and if so, set the hash for the indirect buckets.
            if (dataBuck != null) {
                final byte[] dataHash = dataBuck.secureHash().asBytes();
                mBackendWriter.write(dataBuck);
                mRoot.setReferenceHash(RevisionRootBucket.GUARANTEED_INDIRECT_OFFSET, dataHash);
            }
            // Make the same for the meta bucket which is always written.
            final byte[] metaHash = mMeta.secureHash().asBytes();
            mBackendWriter.write(mMeta);
            mRoot.setReferenceHash(RevisionRootBucket.META_REFERENCE_OFFSET, metaHash);

            // iterate revision tree
            iterateSubtree(true);

            final LogKey revKey = new LogKey(true, 0, 0);
            final IReferenceBucket revBuck = (IReferenceBucket)mFormerLog.get(revKey).getModified();
            final byte[] revHash = revBuck.secureHash().asBytes();
            mBackendWriter.write(revBuck);
            mUber.setReferenceHash(UberBucket.GUARANTEED_INDIRECT_OFFSET, revHash);
            mBackendWriter.writeUberBucket(mUber);

            // // iterating over all data
            // final Iterator<LogValue> entries = mFormerLog.getIterator();
            // while (entries.hasNext()) {
            // LogValue next = entries.next();
            // IBucket bucket = next.getModified();
            // // debug code for marking hashes as written
            // if (bucket instanceof IReferenceBucket) {
            // IReferenceBucket refBucket = (IReferenceBucket)bucket;
            // for (int i = 0; i < refBucket.getReferenceHashs().length; i++) {
            // refBucket.setReferenceHash(i, new byte[] {
            // 0
            // });
            // }
            // }
            // mWriter.write(bucket);
            // }
            // // writing the important pages
            // mWriter.write(mMeta);
            // mWriter.write(mRoot);
            // mWriter.writeUberBucket(mUber);
            ((Session)mDelegate.mSession).setLastCommittedUberBucket(mUber);
            mDelegate = new BucketReadTrx(mDelegate.mSession, mUber, mRoot, mMeta, mBackendWriter);
            closeFormerLog();

            // System.out.println("Commit finished: " + (System.currentTimeMillis() - time));
            return null;
        }

        /**
         * Iterating through the subtree preorder-wise and adapting hashes recursively
         * 
         * @param pRootLevel
         *            if level is rootlevel or not
         * @throws TTException
         */
        private void iterateSubtree(final boolean pRootLevel) throws TTException {
            IReferenceBucket currentRefBuck;
            // Stack for caching the next elements (namely the right siblings and die childs)
            final Stack<LogKey> childAndRightSib = new Stack<LogKey>();
            // Stack to cache the path to the root
            final Stack<LogKey> pathToRoot = new Stack<LogKey>();
            // Push the first Indirect under the RevRoot to the stack
            childAndRightSib.push(new LogKey(pRootLevel, 0, 0));

            // if in the version is no data written, an intermediate return can occur.
            if (mFormerLog.get(childAndRightSib.peek()).getModified() == null) {
                return;
            }

            // if there are children or right sibling left...
            while (!childAndRightSib.isEmpty()) {
                // ...get the next element including the modified bucket.
                final LogKey key = childAndRightSib.pop();
                final IBucket val = mFormerLog.get(key).getModified();

                // if the bucket is an instance of a ReferenceBucket, it is not a leaf and..
                if (val instanceof IReferenceBucket) {
                    currentRefBuck = (IReferenceBucket)val;

                    // ..represents either a new child in the tree (if the level of the new data is bigger
                    // than the last one on the pat the root
                    if (pathToRoot.isEmpty() || key.getLevel() > pathToRoot.peek().getLevel()) {
                        // in this case, push the new child to the path to the root.
                        pathToRoot.push(key);
                    }// else, it is any right sibling whereas the entire subtree left of the current data must
                     // be handled
                    else {
                        LogKey childKey;
                        // for all elements on the stack
                        do {
                            // ..compute the checksum recursively until..
                            final LogKey parentKey = pathToRoot.peek();
                            childKey = pathToRoot.pop();
                            adaptHash(parentKey, childKey);
                        }// the left part of the subtree of the parent is done.
                        while (childKey.getLevel() > key.getLevel());
                        // Push the own key to the path since we are going one step down now/
                        pathToRoot.push(key);
                    }

                    // Iterate through all reference hashes from behind,..
                    for (int i = currentRefBuck.getReferenceHashs().length - 1; i >= 0; i--) {
                        // ..read the hashes and check..
                        final byte[] hash = currentRefBuck.getReferenceHashs()[i];
                        // ..if one offset is marked as fresh.
                        if (Arrays.equals(hash, IConstants.NON_HASHED)) {
                            // This offset marks the childs to go down.
                            final LogKey toPush =
                                new LogKey(pRootLevel, key.getLevel() + 1,
                                    (key.getSeq() << IConstants.INDIRECT_BUCKET_COUNT[3]) + i);
                            childAndRightSib.push(toPush);
                        }
                    }

                } // if we are on the leaf level...
                else {
                    // if we are over the revroot, take the revroot directly..
                    if (pRootLevel) {
                        final byte[] hash = mRoot.secureHash().asBytes();
                        mBackendWriter.write(mRoot);
                        final LogKey parentKey = pathToRoot.peek();
                        final IReferenceBucket parentVal =
                            (IReferenceBucket)mFormerLog.get(parentKey).getModified();
                        final int parentOffset =
                            (int)(key.getSeq() - ((key.getSeq() >> IConstants.INDIRECT_BUCKET_COUNT[3]) << IConstants.INDIRECT_BUCKET_COUNT[3]));
                        parentVal.setReferenceHash(parentOffset, hash);

                    } // otherwise, retrieve the bucket from the log.
                    else {
                        // if there is a hash marked as "toset" but no suitable bucket available, a former
                        // commit is currently in progess whereas the bucket must be retrieved from the
                        // backend.
                        if (val == null) {
                            final IReferenceBucket parent =
                                (IReferenceBucket)mFormerLog.get(pathToRoot.peek()).getModified();
                            final int parentOffset =
                                (int)(key.getSeq() - ((key.getSeq() >> IConstants.INDIRECT_BUCKET_COUNT[3]) << IConstants.INDIRECT_BUCKET_COUNT[3]));
                            byte[] persistedHash = mFormerDataBucketHashes.getIfPresent(key.getSeq());
                            if (persistedHash == null) {
                                final IBucket persistedBucket =
                                    mBackendWriter.read(parent.getReferenceKeys()[parentOffset]);
                                persistedHash = persistedBucket.secureHash().asBytes();
                                mFormerDataBucketHashes.put(key.getSeq(), persistedHash);
                            }
                            parent.setReferenceHash(parentOffset, persistedHash);
                        }// otherwise construct it over the log.
                        else {
                            // ..we need to have a DataBucket and...
                            checkState(val instanceof DataBucket);
                            // ...we adapt the parent with the own hash.
                            final LogKey parentKey = pathToRoot.peek();
                            adaptHash(parentKey, key);
                        }
                    }

                }
            }

            // After the preorder-traversal, we need to adapt the path to the root with the missing checksums,
            // but only if there are any modifications.
            if (!pathToRoot.isEmpty()) {
                do {
                    final LogKey child = pathToRoot.pop();
                    final LogKey parent = pathToRoot.peek();
                    adaptHash(parent, child);
                } while (pathToRoot.size() > 1);
            }

        }

        /**
         * Adapting hash and storing it in a parent-bucket
         * 
         * @param pParentKey
         *            the {@link LogKey} for the parent bucket
         * @param pChildKey
         *            the {@link LogKey} for the own bucket
         * @throws TTException
         */
        private void adaptHash(final LogKey pParentKey, final LogKey pChildKey) throws TTException {
            final IBucket val = mFormerLog.get(pChildKey).getModified();
            final byte[] hash = val.secureHash().asBytes();
            mBackendWriter.write(val);
            if (val instanceof DataBucket) {
                mFormerDataBucketHashes.put(pChildKey.getSeq(), hash);
            }

            final IReferenceBucket parent = (IReferenceBucket)mFormerLog.get(pParentKey).getModified();
            final int parentOffset =
                (int)(pChildKey.getSeq() - ((pChildKey.getSeq() >> IConstants.INDIRECT_BUCKET_COUNT[3]) << IConstants.INDIRECT_BUCKET_COUNT[3]));
            parent.setReferenceHash(parentOffset, hash);
        }
    }
}
