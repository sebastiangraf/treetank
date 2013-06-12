/**
 * 
 */
package org.treetank.commit;

import java.util.Iterator;

import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.NodeBucket;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.log.LRULog;
import org.treetank.log.LogKey;
import org.treetank.log.LogValue;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface ICommitStrategy {

    public void execute() throws TTException;
   
    public NodeBucket valueInProgress(final LogKey pKey)  throws TTIOException ;
    
    public boolean isInProgress();
    
    public static interface CommitStrategyFactory{
        
        /**
         * Generating a commit strategy
         */
        ICommitStrategy create();
        
    }

    public static class BlockingCommit implements ICommitStrategy {

        private final LRULog mLog;

        private final IBackendWriter mWriter;

        private final UberBucket mUber;

        private final MetaBucket mMeta;

        private final RevisionRootBucket mRev;
        
        private boolean mInProgress;

        @Inject
        public BlockingCommit(@Named("pLog") final LRULog pLog,@Named("pWriter") final IBackendWriter pWriter,@Named("pRoot") final RevisionRootBucket pRoot,
            @Named("pMeta") final MetaBucket pMeta,@Named("pUber") final UberBucket pUber) {
            mLog = pLog;
            mWriter = pWriter;
            mUber = pUber;
            mRev = pRoot;
            mMeta = pMeta;
            mInProgress = false;
        }

        /**
         * {@inheritDoc}
         * 
         * @throws TTException
         */
        @Override
        public void execute() throws TTException {
            mInProgress = true;
            Iterator<LogValue> entries = mLog.getIterator();
            while (entries.hasNext()) {
                LogValue next = entries.next();
                mWriter.write(next.getModified());
            }
            mWriter.write(mMeta);
            mWriter.write(mRev);
            mWriter.writeUberBucket(mUber);
            
            mLog.close();
            
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
