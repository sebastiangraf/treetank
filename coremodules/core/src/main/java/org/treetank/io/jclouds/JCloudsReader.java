/**
 * 
 */
package org.treetank.io.jclouds;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.treetank.bucket.BucketFactory;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Accessing the Cloud storage for reading in a multithreaded manner.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsReader implements IBackendReader {

//    private final static File readFile = new File("/Users/sebi/Desktop/runtimeResults/readaccess.txt");
//    private final static File downloadFile =
//        new File("/Users/sebi/Desktop/runtimeResults/downloadaccess.txt");
//
//    static final FileWriter reader;
//    static final FileWriter download;
//
//    static {
//        try {
//            reader = new FileWriter(readFile);
//            download = new FileWriter(downloadFile);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private final static long POISONNUMBER = -15;

//    private final static int BUCKETS_TO_PREFETCH = 3;

    /** Blob Store for Reading the data. */
    protected final BlobStore mBlobStore;

    /** Factory for building Buckets. */
    private final BucketFactory mFac;

    /** Inflater to decompress. */
    protected final IByteHandlerPipeline mByteHandler;

    /** Resource name of this container and the database. */
    protected final String mResourceName;

    /** Cache for reading data. */
    protected final Cache<Long, IBucket> mCache;

    //
    // /** Executing read requests. */
    // private final ExecutorService mReaderService;
    //
    // /** CompletionService for getting aware of concluded tasks. */
    // private final CompletionService<Map.Entry<Long, IBucket>> mReaderCompletion;
    //
    // /** Blocking already performing tasks. */
    // private final ConcurrentHashMap<Long, Future<Map.Entry<Long, IBucket>>> mTasks;

    public JCloudsReader(BlobStore pBlobStore, BucketFactory pFac, IByteHandlerPipeline pByteHandler,
        String pResourceName) throws TTException {
        mBlobStore = pBlobStore;
        mByteHandler = pByteHandler;
        mFac = pFac;
        mResourceName = pResourceName;
        mCache = CacheBuilder.newBuilder().maximumSize(100).build();

        // mTasks = new ConcurrentHashMap<Long, Future<Map.Entry<Long, IBucket>>>();
        // mReaderService = Executors.newFixedThreadPool(20);
        //
        // mReaderCompletion = new ExecutorCompletionService<Map.Entry<Long, IBucket>>(mReaderService);
        // final FutureCleaner cleaner = new FutureCleaner();
        // final ExecutorService cleanerService = Executors.newSingleThreadExecutor();
        // cleanerService.submit(cleaner);
        // cleanerService.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberBucket readUber() throws TTIOException {
        try {
            final Blob blobRetrieved = mBlobStore.getBlob(mResourceName, Long.toString(-1l));
            final DataInputStream datain = new DataInputStream(blobRetrieved.getPayload().getInput());
            final long uberkey = datain.readLong();
            final UberBucket bucket = (UberBucket)read(uberkey);
            datain.close();
            return bucket;
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBucket read(long pKey) throws TTIOException {
        IBucket returnval = mCache.getIfPresent(pKey);
        if (returnval == null) {
            try {
                returnval = getAndprefetchBuckets(pKey);
//                reader.write(returnval.getBucketKey() + "," + returnval.getClass().getName() + "\n");
//                reader.flush();
            } catch (Exception exc) {
                throw new TTIOException(exc);
            }
        }
        return returnval;

    }

    private final IBucket getAndprefetchBuckets(final long pId) throws InterruptedException, ExecutionException {
        IBucket returnVal = null;
        // // Future<Map.Entry<Long, IBucket>> startTask = null;
        // for (long i = pId; i < pId + BUCKETS_TO_PREFETCH; i++) {
        // Future<Map.Entry<Long, IBucket>> currentTask = mTasks.remove(i);
        // if (currentTask == null) {
        // currentTask = mReaderCompletion.submit(new ReadTask(i));
        // mTasks.put(i, currentTask);
        // }
        // if (i == pId) {
        // startTask = currentTask;
        // }
        // }
        // returnVal = startTask.get().getValue();
        try {
            returnVal = new ReadTask(pId).call().getValue();
            mCache.put(pId, returnVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        // mReaderCompletion.submit(new PoisonTask());
        // mCache.invalidateAll();
        // mReaderService.shutdown();
        // try {
        // mReaderService.awaitTermination(100, TimeUnit.SECONDS);
        // } catch (final InterruptedException exc) {
        // throw new TTIOException(exc);
        // }
        // checkState(mReaderService.isTerminated());
    }

    // /**
    // * Cleaning up the Running-Tasks Hashmap in the background.
    // *
    // * @author Sebastian Graf, University of Konstanz
    // *
    // */
    // class FutureCleaner implements Callable<Long> {
    //
    // public Long call() throws Exception {
    // boolean run = true;
    // while (run) {
    // Future<Map.Entry<Long, IBucket>> element = mReaderCompletion.take();
    // long number = element.get().getKey();
    // if (number == POISONNUMBER) {
    // run = false;
    // } else {
    // mTasks.remove(element.get().getKey());
    // }
    // }
    // return POISONNUMBER;
    // }
    // }

    /**
     * Single task to write data to the cloud.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class ReadTask implements Callable<Map.Entry<Long, IBucket>> {

        /**
         * Bucket ID to be read.
         */
        final long mBucketId;

        ReadTask(final long pBucketId) {
            this.mBucketId = pBucketId;
        }

        @Override
        public Map.Entry<Long, IBucket> call() throws Exception {

            IBucket bucket = null;
            // IBucket bucket = mCache.getIfPresent(mBucketId);
            // if (bucket == null) {
            Blob blob = mBlobStore.getBlob(mResourceName, Long.toString(mBucketId));
            checkNotNull(blob, "Blob %s not found", mBucketId);
            DataInputStream datain =
                new DataInputStream(mByteHandler.deserialize(blob.getPayload().getInput()));
            bucket = mFac.deserializeBucket(datain);
            datain.close();
            // mCache.put(mBucketId, bucket);
            // }

            // download.write(bucket.getBucketKey() + "," + bucket.getClass().getName() + "\n");
            // download.flush();

            final IBucket returnVal = bucket;

            return new Map.Entry<Long, IBucket>() {
                @Override
                public Long getKey() {
                    return mBucketId;
                }

                @Override
                public IBucket getValue() {
                    return returnVal;
                }

                @Override
                public IBucket setValue(IBucket value) {
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
    class PoisonTask implements Callable<Map.Entry<Long, IBucket>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Map.Entry<Long, IBucket> call() throws Exception {
            return new Map.Entry<Long, IBucket>() {

                @Override
                public Long getKey() {
                    return POISONNUMBER;
                }

                @Override
                public IBucket getValue() {
                    return null;
                }

                @Override
                public IBucket setValue(IBucket value) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

}
