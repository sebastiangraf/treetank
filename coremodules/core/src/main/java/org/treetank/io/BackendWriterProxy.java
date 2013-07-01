package org.treetank.io;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
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
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

import com.sleepycat.je.tree.ChildReference;

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

//            IReferenceBucket currentRefBuck;
//            final Stack<LogKey> childAndRightSib = new Stack<LogKey>();
//            childAndRightSib.push(new LogKey(false, 0, 0));
//
//            int level = 1;
//            while (!childAndRightSib.isEmpty()) {
//                final LogKey key = childAndRightSib.pop();
//                final IBucket val = mFormerLog.get(key).getModified();
//                if (val instanceof IReferenceBucket) {
//                    currentRefBuck = (IReferenceBucket)val;
//                    for (int i = currentRefBuck.getReferenceHashs().length - 1; i >= 0; i--) {
//                        final byte[] hash = currentRefBuck.getReferenceHashs()[i];
//                        if (Arrays.equals(hash, IConstants.NON_HASHED)) {
//                            final LogKey toPush = new LogKey(false, level, i);
//                            childAndRightSib.push(toPush);
//                        }
//                    }
//                    level++;
//                } // ended at nodepage, leaf level
//                else {
//
//                    System.out.println(key.toString());
//                    System.out.println(val == null ? "Val not found" : val.toString());
//                    // NodePageStuff goes here!
//                }
//            }

            // iterating over all data
            final Iterator<LogValue> entries = mFormerLog.getIterator();
            while (entries.hasNext()) {
                LogValue next = entries.next();
                IBucket bucket = next.getModified();
                //debug code for marking hashes as written
                if (bucket instanceof IReferenceBucket) {
                    IReferenceBucket refBucket = (IReferenceBucket)bucket;
                    for (int i = 0; i < refBucket.getReferenceHashs().length; i++) {
                        refBucket.setReferenceHash(i, new byte[] {
                            0
                        });
                    }
                }
                mWriter.write(bucket);
            }
            // writing the important pages
            mWriter.write(mMeta);
            mWriter.write(mRoot);
            mWriter.writeUberBucket(mUber);
            return null;
        }
    }

}
