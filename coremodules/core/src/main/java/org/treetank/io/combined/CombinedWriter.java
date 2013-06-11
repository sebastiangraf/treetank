package org.treetank.io.combined;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;

/**
 * Combined Writer for writing to a first and a second writer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class CombinedWriter implements IBackendWriter {

    /** For non-blocking write to second storage. */
    private final ExecutorService mService;

    /** First writing instance. */
    private final IBackendWriter mFirstWriter;

    /** Second writing instance. */
    private final IBackendWriter mSecondWriter;

    /**
     * Constructor.
     * 
     * @param pFirstWriter
     *            first writer
     * @param pSecondWriter
     *            second writer
     */
    public CombinedWriter(final IBackendWriter pFirstWriter, final IBackendWriter pSecondWriter) {
        this.mFirstWriter = pFirstWriter;
        this.mSecondWriter = pSecondWriter;
        mService = Executors.newSingleThreadExecutor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBucket read(final long pKey) throws TTIOException {
        Future<IBucket> secondReturn = mService.submit(new Callable<IBucket>() {
            @Override
            public IBucket call() throws Exception {
                return mSecondWriter.read(pKey);
            }
        });
        IBucket returnVal = mFirstWriter.read(pKey);
        try {
            if (returnVal == null) {
                return secondReturn.get();
            } else {
                return returnVal;
            }
        } catch (ExecutionException | InterruptedException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberBucket readUber() throws TTIOException {
        Future<UberBucket> secondReturn = mService.submit(new Callable<UberBucket>() {
            @Override
            public UberBucket call() throws Exception {
                return mSecondWriter.readUber();
            }
        });
        UberBucket returnVal = mFirstWriter.readUber();
        try {
            if (returnVal == null) {
                return secondReturn.get();
            } else {
                return returnVal;
            }
        } catch (ExecutionException | InterruptedException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final IBucket bucket) throws TTException {
        mService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mSecondWriter.write(bucket);
                return null;
            }
        });
        mFirstWriter.write(bucket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeUberBucket(final UberBucket pBucket) throws TTException {
        mService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mSecondWriter.writeUberBucket(pBucket);
                return null;
            }
        });
        mFirstWriter.writeUberBucket(pBucket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        try {
            mService.shutdown();
            mService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException exc) {
            throw new TTIOException(exc);
        }
        mFirstWriter.close();
        mSecondWriter.close();

    }
}
