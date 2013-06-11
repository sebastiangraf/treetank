package org.treetank.access.commit;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.treetank.access.CommitStrategy;
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
 * 
 * @author Andreas Rain, University of Konstanz
 * 
 */
public class NonBlockingCommitStrategy implements CommitStrategy {
    /** Log used during commit, not final since a copy is created during the execution */
    private LRULog mLog;

    private final IBackendWriter mWriter;

    private final UberBucket mUber;

    private final MetaBucket mMeta;

    private final RevisionRootBucket mRev;

    private boolean mInProgress;
    
    private ExecutorService mService;

    @Inject
    public NonBlockingCommitStrategy(@Named("pLog") LRULog pLog,
        @Named("pWriter") final IBackendWriter pWriter, @Named("pRoot") final RevisionRootBucket pRoot,
        @Named("pMeta") final MetaBucket pMeta, @Named("pUber") final UberBucket pUber) {
        mWriter = pWriter;
        mUber = pUber;
        mRev = pRoot;
        mMeta = pMeta;
        mInProgress = false;
        mLog = pLog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws TTException {
        mInProgress = true;
        final Iterator<LogValue> entries = mLog.getIterator();
        
        // exclude this progress into another thread, making he commit non blocking
        Callable<Void> workTask = new Callable<Void>(){
            
            @Override
            public Void call() throws Exception {

                while (entries.hasNext()) {
                    LogValue next = entries.next();
                    mWriter.write(next.getModified());
                }
                
                mWriter.write(mMeta);
                mWriter.write(mRev);
                mWriter.writeUberBucket(mUber);
                
                // Set in progress false, since the commit is finished
                mInProgress = false;

                mLog.close();
                
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
