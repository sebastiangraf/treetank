/**
 * 
 */
package org.treetank.io.jclouds;

import static com.google.common.base.Preconditions.checkState;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
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
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Accessing the Cloud storage for reading in a multithreaded manner.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsReader implements IBackendReader {

    private final static long POISONNUMBER = -15;

    private final static int BUCKETS_TO_PREFETCH = 3;

    /** Blob Store for Reading the data. */
    protected final BlobStore mBlobStore;

    /** Factory for building Pages. */
    private final PageFactory mFac;

    /** Inflater to decompress. */
    protected final IByteHandlerPipeline mByteHandler;

    /** Resource name of this container and the database. */
    protected final String mResourceName;

    /** Cache for reading data. */
    protected final Cache<Long, IPage> mCache;

    /** Executing read requests. */
    private final ExecutorService mReaderService;

    /** CompletionService for getting aware of concluded tasks. */
    private final CompletionService<Map.Entry<Long, IPage>> mReaderCompletion;

    /** Blocking already performing tasks. */
    private final ConcurrentHashMap<Long, Future<Map.Entry<Long, IPage>>> mTasks;

    public JCloudsReader(BlobStore pBlobStore, PageFactory pFac, IByteHandlerPipeline pByteHandler,
        String pResourceName) throws TTException {
        mBlobStore = pBlobStore;
        mByteHandler = pByteHandler;
        mFac = pFac;
        mResourceName = pResourceName;
        mCache = CacheBuilder.newBuilder().maximumSize(1000).build();

        mTasks = new ConcurrentHashMap<Long, Future<Map.Entry<Long, IPage>>>();
        mReaderService = Executors.newCachedThreadPool();

        mReaderCompletion = new ExecutorCompletionService<Map.Entry<Long, IPage>>(mReaderService);
        final FutureCleaner cleaner = new FutureCleaner();
        final ExecutorService cleanerService = Executors.newSingleThreadExecutor();
        cleanerService.submit(cleaner);
        cleanerService.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberPage readUber() throws TTIOException {
        try {
            final Blob blobRetrieved = mBlobStore.getBlob(mResourceName, Long.toString(-1l));
            final DataInputStream datain = new DataInputStream(blobRetrieved.getPayload().getInput());
            final long uberpagekey = datain.readLong();
            final UberPage page = (UberPage)read(uberpagekey);
            datain.close();
            return page;
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage read(long pKey) throws TTIOException {
        IPage returnval = mCache.getIfPresent(pKey);
        if (returnval == null) {
            try {
                returnval = getAndprefetchBuckets(pKey);
            } catch (InterruptedException | ExecutionException exc) {
                throw new TTIOException(exc);
            }
        }
        return returnval;

    }

    private final IPage getAndprefetchBuckets(final long pId) throws InterruptedException, ExecutionException {
        IPage returnVal = null;
        Future<Map.Entry<Long, IPage>> startTask = null;
        for (long i = pId; i < pId + BUCKETS_TO_PREFETCH; i++) {
            Future<Map.Entry<Long, IPage>> currentTask = mTasks.remove(i);
            if (currentTask == null) {
                currentTask = mReaderCompletion.submit(new ReadTask(i));
                mTasks.put(i, currentTask);
            }
            if (i == pId) {
                startTask = currentTask;
            }
        }
        returnVal = startTask.get().getValue();
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        mReaderCompletion.submit(new PoisonTask());
        mCache.invalidateAll();
        mReaderService.shutdown();
        try {
            mReaderService.awaitTermination(100, TimeUnit.SECONDS);
        } catch (final InterruptedException exc) {
            throw new TTIOException(exc);
        }
        checkState(mReaderService.isTerminated());
    }

    /**
     * Cleaning up the Running-Tasks Hashmap in the background.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class FutureCleaner implements Callable<Long> {

        public Long call() throws Exception {
            boolean run = true;
            while (run) {
                Future<Map.Entry<Long, IPage>> element = mReaderCompletion.take();
                long number = element.get().getKey();
                if (number == POISONNUMBER) {
                    run = false;
                } else {
                    mTasks.remove(element.get().getKey());
                }
            }
            return POISONNUMBER;
        }
    }

    /**
     * Single task to write data to the cloud.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class ReadTask implements Callable<Map.Entry<Long, IPage>> {

        /**
         * Bucket ID to be read.
         */
        final long mBucketId;

        ReadTask(final long pBucketId) {
            this.mBucketId = pBucketId;
        }

        @Override
        public Map.Entry<Long, IPage> call() throws Exception {

            IPage page = mCache.getIfPresent(mBucketId);
            if (page == null) {
                Blob blob = mBlobStore.getBlob(mResourceName, Long.toString(mBucketId));
                if (blob != null) {
                    DataInputStream datain =
                        new DataInputStream(mByteHandler.deserialize(blob.getPayload().getInput()));
                    page = mFac.deserializePage(datain);
                    datain.close();
                }
                mCache.put(mBucketId, page);
            }

            final IPage returnVal = page;

            return new Map.Entry<Long, IPage>() {
                @Override
                public Long getKey() {
                    return mBucketId;
                }

                @Override
                public IPage getValue() {
                    return returnVal;
                }

                @Override
                public IPage setValue(IPage value) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Tasks for ending the cleaner .
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class PoisonTask implements Callable<Map.Entry<Long, IPage>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Map.Entry<Long, IPage> call() throws Exception {
            return new Map.Entry<Long, IPage>() {

                @Override
                public Long getKey() {
                    return POISONNUMBER;
                }

                @Override
                public IPage getValue() {
                    return null;
                }

                @Override
                public IPage setValue(IPage value) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

}
