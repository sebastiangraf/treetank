/**
 * 
 */
package org.treetank.io;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Deprecated
public interface ICommitStrategy {

    public void execute(final UberBucket pUber, final MetaBucket pMeta, final RevisionRootBucket pRev)
        throws TTException;

    public NodeBucket valueInProgress(final LogKey pKey) throws TTIOException;

    public boolean isInProgress();

    public void setLog(final LRULog pLog);

    public static interface CommitStrategyFactory {

        /**
         * Generating a commit strategy
         */
        ICommitStrategy create();

    }

    class NonBlockingCommit implements ICommitStrategy {

        private LRULog mLog;

        private final IBackendWriter mWriter;

        private boolean mInProgress;

        private ExecutorService mService;

        public NonBlockingCommit(final IBackendWriter pWriter) {
            mWriter = pWriter;
            mInProgress = false;
        }

        public void setLog(final LRULog pLog) {
            mLog = pLog;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute(final UberBucket pUber, final MetaBucket pMeta, final RevisionRootBucket pRev)
            throws TTException {
            mInProgress = true;
            final Iterator<LogValue> entries = mLog.getIterator();

            // exclude this progress into another thread, making he commit non blocking
            Callable<Void> workTask = new Callable<Void>() {

                @Override
                public Void call() throws Exception {

                    while (entries.hasNext()) {
                        LogValue next = entries.next();
                        mWriter.write(next.getModified());
                    }

                    mWriter.write(pMeta);
                    mWriter.write(pRev);
                    mWriter.writeUberBucket(pUber);

                    // Set in progress false, since the commit is finished
                    mInProgress = false;

                    

                    this.notifyAll();
                    return null;
                }

            };

            mService = Executors.newSingleThreadExecutor();
            mService.submit(workTask);

        }

        @Override
        public NodeBucket valueInProgress(LogKey pKey) throws TTIOException {
            return (NodeBucket)mLog.get(pKey).getComplete();
        }

        @Override
        public boolean isInProgress() {
            return mInProgress;
        }
    }

    class BlockingCommit implements ICommitStrategy {

        private LRULog mLog;

        private final IBackendWriter mWriter;

        private boolean mInProgress;

        public BlockingCommit(final IBackendWriter pWriter) {
            mWriter = pWriter;
            mInProgress = false;
        }

        public void setLog(final LRULog pLog) {
            this.mLog = pLog;
        }

        /**
         * {@inheritDoc}
         * 
         * @throws TTException
         */
        @Override
        public void execute(final UberBucket pUber, final MetaBucket pMeta, final RevisionRootBucket pRev)
            throws TTException {
            mInProgress = true;
            Iterator<LogValue> entries = mLog.getIterator();
            while (entries.hasNext()) {
                LogValue next = entries.next();
                mWriter.write(next.getModified());
            }
            mWriter.write(pMeta);
            mWriter.write(pRev);
            mWriter.writeUberBucket(pUber);

            // No need for notification
            // since this strategy blocks until finish.
            mInProgress = false;
        }

        @Override
        public NodeBucket valueInProgress(LogKey pKey) throws TTIOException {
            return (NodeBucket)mLog.get(pKey).getComplete();
        }

        @Override
        public boolean isInProgress() {
            return mInProgress;
        }

    }

}
