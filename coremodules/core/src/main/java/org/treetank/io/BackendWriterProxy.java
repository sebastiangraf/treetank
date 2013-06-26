package org.treetank.io;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

public class BackendWriterProxy implements IBackendReader {

    private final IBackendWriter mWriter;
    private final File mPathToLog;

    private LRULog mLog;

    private final INodeFactory mNodeFac;
    private final IMetaEntryFactory mMetaFac;

    private LRULog mFormerLog;
    private final ExecutorService mExec;

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

    public Future<Void> commit(final UberBucket pUber, final MetaBucket pMeta, final RevisionRootBucket pRev)
        throws TTException {

        mFormerLog = mLog;
        mLog = new LRULog(mPathToLog, mNodeFac, mMetaFac);

        final Future<Void> runningTask = mExec.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
//                 Thread.sleep(1000);

                final Iterator<LogValue> entries = mFormerLog.getIterator();
                while (entries.hasNext()) {
                    LogValue next = entries.next();
                    mWriter.write(next.getModified());
                }
                mWriter.write(pMeta);
                mWriter.write(pRev);
                mWriter.writeUberBucket(pUber);
                return null;
            }
        });
        return runningTask;
    }

    public void put(final LogKey pKey, final LogValue pValue) throws TTIOException {
        mLog.put(pKey, pValue);
    }

    public LogValue get(final LogKey pKey) throws TTIOException {
        final LogValue val = mLog.get(pKey);
        return val;
    }

    public LogValue getFormer(final LogKey pKey) throws TTIOException {
        LogValue val = mFormerLog.get(pKey);
        return val;
    }

    @Override
    public IBucket read(long pKey) throws TTIOException {
        return mWriter.read(pKey);
    }

    @Override
    public UberBucket readUber() throws TTIOException {
        return mWriter.readUber();
    }

    public void closeFormerLog() throws TTIOException {
        if (mFormerLog != null && !mFormerLog.isClosed()) {
            mFormerLog.close();
        }
    }

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

}
