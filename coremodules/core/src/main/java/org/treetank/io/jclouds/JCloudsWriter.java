/**
 * 
 */
package org.treetank.io.jclouds;

import static com.google.common.base.Preconditions.checkState;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsWriter implements IBackendWriter {

    private final static long POISONNUMBER = -15;

    /** Delegate for reader. */
    private final JCloudsReader mReader;

    private final ConcurrentHashMap<Long, Future<Long>> mRunningWriteTasks;
    private final CompletionService<Long> mWriterCompletion;
    /** Executing read requests. */
    private final ExecutorService mWriterService;

    public JCloudsWriter(BlobStore pBlobStore, PageFactory pFac, IByteHandlerPipeline pByteHandler,
        String pResourceName) throws TTException {
        mReader = new JCloudsReader(pBlobStore, pFac, pByteHandler, pResourceName);

        mWriterService = Executors.newFixedThreadPool(20);
        mRunningWriteTasks = new ConcurrentHashMap<Long, Future<Long>>();
        mWriterCompletion = new ExecutorCompletionService<Long>(mWriterService);

        final WriteFutureCleaner cleaner = new WriteFutureCleaner();
        final ExecutorService cleanerService = Executors.newSingleThreadExecutor();
        cleanerService.submit(cleaner);
        cleanerService.shutdown();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage read(long pKey) throws TTIOException {
        Future<Long> task = mRunningWriteTasks.get(pKey);
        if (task != null) {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException exc) {
                throw new TTIOException(exc);
            }
        }
        return mReader.read(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final IPage pPage) throws TTIOException, TTByteHandleException {
        try {
            Future<Long> task = mWriterCompletion.submit(new WriteTask(pPage));
            mRunningWriteTasks.put(pPage.getPageKey(), task);
            mReader.mCache.put(pPage.getPageKey(), pPage);
        } catch (final Exception exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        mWriterCompletion.submit(new PoisonTask());
        mWriterService.shutdown();
        try {
            mWriterService.awaitTermination(100, TimeUnit.SECONDS);
        } catch (final InterruptedException exc) {
            throw new TTIOException(exc);
        }
        checkState(mWriterService.isTerminated());
        mReader.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberPage readUber() throws TTIOException {
        return mReader.readUber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeUberPage(UberPage page) throws TTException {
        try {
            long key = page.getPageKey();
            write(page);
            BlobBuilder blobbuilder = mReader.mBlobStore.blobBuilder(Long.toString(-1L));
            Blob blob = blobbuilder.build();
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(byteOut);
            dataOut.writeLong(key);
            dataOut.close();
            blob.setPayload(byteOut.toByteArray());
            mReader.mBlobStore.putBlob(mReader.mResourceName, blob);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }

    }

    /**
     * Single task to write data to the cloud.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class WriteTask implements Callable<Long> {
        /**
         * The bytes to buffer.
         */
        final IPage mPage;

        WriteTask(IPage pPage) {
            this.mPage = pPage;
        }

        @Override
        public Long call() throws Exception {
            boolean finished = false;

            while (!finished) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream dataOut = new DataOutputStream(mReader.mByteHandler.serialize(byteOut));
                mPage.serialize(dataOut);
                dataOut.close();

                BlobBuilder blobbuilder = mReader.mBlobStore.blobBuilder(Long.toString(mPage.getPageKey()));
                Blob blob = blobbuilder.build();
                blob.setPayload(byteOut.toByteArray());

                mReader.mBlobStore.putBlob(mReader.mResourceName, blob);
                finished = true;
            }

            return mPage.getPageKey();
        }
    }

    class WriteFutureCleaner implements Callable<Long> {

        public Long call() throws Exception {
            boolean run = true;
            while (run) {
                Future<Long> element = mWriterCompletion.take();
                if (!element.isCancelled()) {
                    long id = element.get();
                    if (id == POISONNUMBER) {
                        run = false;
                    } else {
                        mRunningWriteTasks.remove(element.get());
                    }
                }
            }
            return POISONNUMBER;
        }
    }

    /**
     * Tasks for ending the cleaner .
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class PoisonTask implements Callable<Long> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Long call() throws Exception {
            return POISONNUMBER;
        }
    }

}
