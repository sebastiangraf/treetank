package org.treetank.io;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jclouds.javax.annotation.Nullable;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.bucket.IConstants;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * This class encapsulates the access to the persistent backend for writing purposes and combines it with a
 * transaction log.
 * Upon call of {@link #commit(UberBucket, MetaBucket, RevisionRootBucket)}, all data is written to the
 * transactionlog whereas the commit occurs in an extra thread. For nonblocking-puposes, this class offers a
 * Future for the commit as well as access to the former=log to access data currently in process.
 * 
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BackendWriterProxy implements IBackendReader {
    /** Instance to the write. */
    private final IBackendWriter mWriter;
    /** Path to the log. */
    private final File mPathToLog;

    /** Current LRULog instance to write currently to. */
    private LRULog mLog;

    /** Actual NodeFactory for de-/serializing any entries to the log. */
    private final INodeFactory mNodeFac;
    /** Actual MetaFactory for de-/serializing any entries to the log. */
    private final IMetaEntryFactory mMetaFac;

    /** Former log instance utilizing while commit is in process. */
    @Nullable
    private LRULog mFormerLog;
    /** Executor for performing non-blocking commits. */
    private final ExecutorService mExec;

    /**
     * 
     * Constructor.
     * 
     * @param pWriter
     *            to encapsulate
     * @param pPathToLog
     *            where the logs should be stored to
     * @param pNodeFac
     *            for adhering different node-types
     * @param pMetaFac
     *            for adhering different meta-types
     * @throws TTIOException
     *             if anything weird happens
     */
    public BackendWriterProxy(final IBackendWriter pWriter, final File pPathToLog,
        final INodeFactory pNodeFac, final IMetaEntryFactory pMetaFac) throws TTIOException {
        mWriter = pWriter;
        mPathToLog = pPathToLog;
        mNodeFac = pNodeFac;
        mMetaFac = pMetaFac;
        mLog = new LRULog(mPathToLog, mNodeFac, mMetaFac);
        mExec = Executors.newSingleThreadExecutor();
        mFormerLog = mLog;
    }

    /**
     * Performing a commit to transfer the data to a persistent log. The data in the log are stored in the
     * {@link #mLog}-instance.
     * 
     * @param pUber
     *            to be persisted.
     * @param pMeta
     *            to be persisted.
     * @param pRev
     *            to be persisted.
     * @return the Future representing the result of the commit.
     * @throws TTException
     *             if anything weird happens
     */
    public Future<Void> commit(final UberBucket pUber, final MetaBucket pMeta, final RevisionRootBucket pRev)
        throws TTException {
        // storing the reference to the former log.
        mFormerLog = mLog;
        // new log
        mLog = new LRULog(mPathToLog, mNodeFac, mMetaFac);
        // starting the persistence-process.
        return mExec.submit(new CommitCallable(pUber, pRev, pMeta));
    }

    /**
     * Putting data to the actual log.
     * 
     * @param pKey
     *            to be stored in
     * @param pValue
     *            to be stored in
     * @throws TTIOException
     *             if any weird happens
     */
    public void put(final LogKey pKey, final LogValue pValue) throws TTIOException {
        mLog.put(pKey, pValue);
    }

    /**
     * Getting data from the current log
     * 
     * @param pKey
     *            mapped to the value
     * @return a suitable {@link LogValue}-instance to be returned
     * @throws TTIOException
     *             if any weird happens
     */
    public LogValue get(final LogKey pKey) throws TTIOException {
        final LogValue val = mLog.get(pKey);
        return val;
    }

    /**
     * Getting data from the former log, currently in process.
     * 
     * @param pKey
     *            mapped to the value
     * 
     * @return a suitable {@link LogValue}-instance to be returned
     * @throws TTIOException
     *             if any weird happens
     */
    public LogValue getFormer(final LogKey pKey) throws TTIOException {
        LogValue val = mFormerLog.get(pKey);
        return val;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBucket read(long pKey) throws TTIOException {
        return mWriter.read(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberBucket readUber() throws TTIOException {
        return mWriter.readUber();
    }

    /**
     * Closing the former log if not needed any more
     * 
     * @throws TTIOException
     *             if any weird happens
     */
    public void closeFormerLog() throws TTIOException {
        if (mFormerLog != null && !mFormerLog.isClosed()) {
            mFormerLog.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        if (!mLog.isClosed()) {
            mLog.close();
        }
        mExec.shutdown();
        try {
            mExec.awaitTermination(300, TimeUnit.SECONDS);
        } catch (InterruptedException exc) {
            throw new TTIOException(exc);
        }
        mWriter.close();
    }

    static List<Long> keys = new ArrayList<Long>();

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
            keys.clear();
            // Thread.sleep(1000);

            // // iterate data tree
            // iterateSubtree(false);
            // // get last IndirectBucket referenced from the RevRoot.
            // final LogKey dataKey = new LogKey(false, 0, 0);
            // final IReferenceBucket dataBuck = (IReferenceBucket)mFormerLog.get(dataKey).getModified();
            // // Check if there are any modifications and if so, set the hash for the indirect buckets.
            // if (dataBuck != null) {
            // final byte[] dataHash = dataBuck.secureHash().asBytes();
            // mWriter.write(dataBuck);
            // keys.add(dataBuck.getBucketKey());
            // mRoot.setReferenceHash(RevisionRootBucket.GUARANTEED_INDIRECT_OFFSET, dataHash);
            // }
            // // Make the same for the meta bucket which is always written.
            // final byte[] metaHash = mMeta.secureHash().asBytes();
            // mWriter.write(mMeta);
            // keys.add(mMeta.getBucketKey());
            // mRoot.setReferenceHash(RevisionRootBucket.META_REFERENCE_OFFSET, metaHash);
            //
            // // iterate revision tree
            // iterateSubtree(true);
            //
            // final LogKey revKey = new LogKey(true, 0, 0);
            // final IReferenceBucket revBuck = (IReferenceBucket)mFormerLog.get(revKey).getModified();
            // final byte[] revHash = revBuck.secureHash().asBytes();
            // mWriter.write(revBuck);
            // keys.add(revBuck.getBucketKey());
            // mUber.setReferenceHash(UberBucket.GUARANTEED_INDIRECT_OFFSET, revHash);
            // mWriter.writeUberBucket(mUber);
            // keys.add(mUber.getBucketKey());

            // iterating over all data
            final Iterator<LogValue> entries = mFormerLog.getIterator();
            while (entries.hasNext()) {
                LogValue next = entries.next();
                IBucket bucket = next.getModified();
                // debug code for marking hashes as written
                if (bucket instanceof IReferenceBucket) {
                    IReferenceBucket refBucket = (IReferenceBucket)bucket;
                    for (int i = 0; i < refBucket.getReferenceHashs().length; i++) {
                        refBucket.setReferenceHash(i, new byte[] {
                            0
                        });
                    }
                }
                mWriter.write(bucket);
                keys.add(bucket.getBucketKey());
            }
            // writing the important pages
            mWriter.write(mMeta);
            keys.add(mMeta.getBucketKey());
            mWriter.write(mRoot);
            keys.add(mRoot.getBucketKey());
            mWriter.writeUberBucket(mUber);
            keys.add(mUber.getBucketKey());
//            System.out.println(keys.toString());
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

            // if there are children or right sibling left...
            while (!childAndRightSib.isEmpty()) {
                // ...get the next element including the modified bucket.
                final LogKey key = childAndRightSib.pop();
                final IBucket val = mFormerLog.get(key).getModified();

                // if there is no modification occurring, just return.
                if (!pRootLevel && val == null) {
                    break;
                } else
                // if the bucket is an instance of a ReferenceBucket, it is not a leaf and..
                if (val instanceof IReferenceBucket) {
                    currentRefBuck = (IReferenceBucket)val;

                    // ..represents either a new child in the tree (if the level of the new node is bigger
                    // than the last one on the pat the root
                    if (pathToRoot.isEmpty() || key.getLevel() > pathToRoot.peek().getLevel()) {
                        // in this case, push the new child to the path to the root.
                        pathToRoot.push(key);
                    }// else, it is any right sibling whereas the entire subtree left of the current node must
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
                                    (key.getSeq() << IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3]) + i);
                            childAndRightSib.push(toPush);
                        }
                    }

                } // if we are on the leaf level...
                else {
                    // if we are over the revroot, take the revroot directly..
                    if (pRootLevel) {
                        final byte[] hash = mRoot.secureHash().asBytes();
                        mWriter.write(mRoot);
                        keys.add(mRoot.getBucketKey());
                        final LogKey parentKey = pathToRoot.peek();
                        final IReferenceBucket parentVal =
                            (IReferenceBucket)mFormerLog.get(parentKey).getModified();
                        final int parentOffset =
                            (int)(key.getSeq() - ((key.getSeq() >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3]) << IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3]));
                        parentVal.setReferenceHash(parentOffset, hash);

                    } // otherwise, retrieve the bucket from the log.
                    else {
                        // ..we need to have a NodeBucket and...
                        checkState(val instanceof NodeBucket);
                        // ...we adapt the parent with the own hash.
                        final LogKey parentKey = pathToRoot.peek();
                        adaptHash(parentKey, key);
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
            mWriter.write(val);
            keys.add(val.getBucketKey());
            final IReferenceBucket parentVal = (IReferenceBucket)mFormerLog.get(pParentKey).getModified();
            final int parentOffset =
                (int)(pChildKey.getSeq() - ((pChildKey.getSeq() >> IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3]) << IConstants.INP_LEVEL_BUCKET_COUNT_EXPONENT[3]));
            parentVal.setReferenceHash(parentOffset, hash);
        }

    }

}
