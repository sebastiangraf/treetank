package org.treetank.io.combinedCloud;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

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
    public IPage read(final long pKey) throws TTIOException {
        Future<IPage> secondReturn = mService.submit(new Callable<IPage>() {
            @Override
            public IPage call() throws Exception {
                return mSecondWriter.read(pKey);
            }
        });
        IPage returnVal = mFirstWriter.read(pKey);
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
    public UberPage readUber() throws TTIOException {
        Future<UberPage> secondReturn = mService.submit(new Callable<UberPage>() {
            @Override
            public UberPage call() throws Exception {
                return mSecondWriter.readUber();
            }
        });
        UberPage returnVal = mFirstWriter.readUber();
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
    public void write(final IPage page) throws TTException {
        mService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mSecondWriter.write(page);
                return null;
            }
        });
        mFirstWriter.write(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeUberPage(final UberPage page) throws TTException {
        mService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mSecondWriter.writeUberPage(page);
                return null;
            }
        });
        mFirstWriter.writeUberPage(page);
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
