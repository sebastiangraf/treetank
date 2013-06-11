/**
 * 
 */
package org.treetank.io.jclouds;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.treetank.bucket.BucketFactory;
import org.treetank.bucket.UberBucket;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsWriter implements IBackendWriter {

//    // START DEBUG CODE
//    private final static File writeFile = new File("/Users/sebi/Desktop/runtimeResults/writeaccess.txt");
//    private final static File uploadFile = new File("/Users/sebi/Desktop/runtimeResults/uploadaccess.txt");
//
//    static final FileWriter writer;
//    static final FileWriter upload;
//
//    static {
//        try {
//            writer = new FileWriter(writeFile);
//            upload = new FileWriter(uploadFile);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private final static long POISONNUMBER = -15;

    /** Delegate for reader. */
    private final JCloudsReader mReader;

    // private final ConcurrentHashMap<Long, Future<Long>> mRunningWriteTasks;
    // private final CompletionService<Long> mWriterCompletion;
    // /** Executing read requests. */
    // private final ExecutorService mWriterService;

    public JCloudsWriter(BlobStore pBlobStore, BucketFactory pFac, IByteHandlerPipeline pByteHandler,
        String pResourceName) throws TTException {
        mReader = new JCloudsReader(pBlobStore, pFac, pByteHandler, pResourceName);

        // mWriterService = Executors.newFixedThreadPool(20);
        // mRunningWriteTasks = new ConcurrentHashMap<Long, Future<Long>>();
        // mWriterCompletion = new ExecutorCompletionService<Long>(mWriterService);

        // final WriteFutureCleaner cleaner = new WriteFutureCleaner();
        // final ExecutorService cleanerService = Executors.newSingleThreadExecutor();
        // cleanerService.submit(cleaner);
        // cleanerService.shutdown();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBucket read(long pKey) throws TTIOException {
        // Future<Long> task = mRunningWriteTasks.get(pKey);
        // if (task != null) {
        // try {
        // task.get();
        // } catch (InterruptedException | ExecutionException exc) {
        // throw new TTIOException(exc);
        // }
        // }
        return mReader.read(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final IBucket pBucket) throws TTIOException, TTByteHandleException {
        try {
//            writer.write(pBucket.getBucketKey() + "," + pBucket.getClass().getName() + "\n");
//            writer.flush();

            new WriteTask(pBucket).call();
            // Future<Long> task = mWriterCompletion.submit(new WriteTask(pBucket));
            // mRunningWriteTasks.put(pBucket.getBucketKey(), task);
            // mReader.mCache.put(pBucket.getBucketKey(), pBucket);
        } catch (final Exception exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        // mWriterCompletion.submit(new PoisonTask());
        // mWriterService.shutdown();
        // try {
        // mWriterService.awaitTermination(100, TimeUnit.SECONDS);
        // } catch (final InterruptedException exc) {
        // throw new TTIOException(exc);
        // }
        // checkState(mWriterService.isTerminated());
        mReader.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UberBucket readUber() throws TTIOException {
        return mReader.readUber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeUberBucket(UberBucket pBucket) throws TTException {
        try {
            long key = pBucket.getBucketKey();
            write(pBucket);
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
        final IBucket mBucket;

        WriteTask(IBucket pBucket) {
            this.mBucket = pBucket;
        }

        @Override
        public Long call() throws Exception {
            boolean finished = false;

            while (!finished) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream dataOut = new DataOutputStream(mReader.mByteHandler.serialize(byteOut));
                mBucket.serialize(dataOut);
                dataOut.close();

                BlobBuilder blobbuilder = mReader.mBlobStore.blobBuilder(Long.toString(mBucket.getBucketKey()));
                Blob blob = blobbuilder.build();
                blob.setPayload(byteOut.toByteArray());

                mReader.mBlobStore.putBlob(mReader.mResourceName, blob);
                finished = true;

                // upload.write(mBucket.getBucketKey() + "," + mBucket.getClass().getName() + "\n");
                // upload.flush();
            }

            return mBucket.getBucketKey();
        }
    }
    //
    // class WriteFutureCleaner implements Callable<Long> {
    //
    // public Long call() throws Exception {
    // boolean run = true;
    // while (run) {
    // Future<Long> element = mWriterCompletion.take();
    // if (!element.isCancelled()) {
    // long id = element.get();
    // if (id == POISONNUMBER) {
    // run = false;
    // } else {
    // mRunningWriteTasks.remove(element.get());
    // }
    // }
    // }
    // return POISONNUMBER;
    // }
    // }
    //
    // /**
    // * Tasks for ending the cleaner .
    // *
    // * @author Sebastian Graf, University of Konstanz
    // *
    // */
    // class PoisonTask implements Callable<Long> {
    //
    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public Long call() throws Exception {
    // return POISONNUMBER;
    // }
    // }

}
