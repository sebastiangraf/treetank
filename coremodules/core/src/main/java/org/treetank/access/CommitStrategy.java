/**
 * 
 */
package org.treetank.access;

import java.util.Iterator;

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.log.LRULog;
import org.treetank.log.LogKey;
import org.treetank.log.LogValue;
import org.treetank.page.MetaPage;
import org.treetank.page.NodePage;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface CommitStrategy {

    public void execute() throws TTException;
   
    public NodePage valueInProgress(final LogKey pKey)  throws TTIOException ;
    
    public boolean isInProgress();
    
    public static interface CommitStrategyFactory{
        
        /**
         * Generating a commit strategy
         */
        CommitStrategy create();
        
    }

    public static class BlockingCommit implements CommitStrategy {

        private final LRULog mLog;

        private final IBackendWriter mWriter;

        private final UberPage mUber;

        private final MetaPage mMeta;

        private final RevisionRootPage mRev;
        
        private boolean mInProgress;

        @Inject
        public BlockingCommit(@Named("pLog") final LRULog pLog,@Named("pWriter") final IBackendWriter pWriter,@Named("pRoot") final RevisionRootPage pRoot,
            @Named("pMeta") final MetaPage pMeta,@Named("pUber") final UberPage pUber) {
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
            mWriter.writeUberPage(mUber);
            
            mLog.close();
            
            // No need for notification
            // since this strategy blocks until finish.
            mInProgress = false;
        }

        @Override
        public NodePage valueInProgress(LogKey pKey) throws TTIOException {
            return (NodePage)mLog.get(pKey).getComplete();
        }

        @Override
        public boolean isInProgress() {
            return mInProgress;
        }
        
        
        
        

    }

}
