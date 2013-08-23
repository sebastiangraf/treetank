/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All
 * rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer. *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. * Neither the name of
 * the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.jscsi;

import static com.google.common.base.Preconditions.checkState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jscsi.target.storage.IStorageModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.IscsiWriteTrx;
import org.treetank.access.conf.ConstructorProps;
import org.treetank.api.IData;
import org.treetank.api.IIscsiWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;

import com.google.common.io.Files;

/**
 * <h1>TreetankStorageModule</h1>
 * <p>
 * This implementation is used to store data into treetank via an iscsi target.
 * </p>
 * 
 * @author Andreas Rain
 */
public class TreetankStorageModule implements IStorageModule {

    /**
     * Number of Blocks in one Cluster.
     * 8 equals 4KB nodes
     * 16 equals 8KB nodes
     * ...
     * 
     * When using berkeley db as a backend consider that if you increase the size of the blocks in node
     * up to 12800 nodes can be held in ram.
     * If you have a 1gb storage maximally 1gb heap is needed. However if the storage is bigger and
     * the blocks per node is considerably high (e.g. 256kb per node) you might have to increase
     * the ram for the jvm.
     */
    public static final int BLOCKS_IN_NODE = 8;

    /** Threshold when commit should occur in number of bytes. */
    private static final int COMMIT_THRESHOLD = 268435456;

    /** Number of Bytes in Bucket. */
    public final static int BYTES_IN_NODE = BLOCKS_IN_NODE * VIRTUAL_BLOCK_SIZE;

    private static final Logger LOGGER = LoggerFactory.getLogger(TreetankStorageModule.class);

    /**
     * The number of nodes in the storage resulting in mNodeNumbers * BLOCKS_IN_NODE *
     * VIRTUAL_BLOCK_SIZE bytes
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    private final long mNodeNumbers;

    /**
     * The mSession this storage module uses to access the storage device.
     */
    private final ISession mSession;

    /**
     * {@link IIscsiWriteTrx} that is used to write/read from treetank.
     */
    private final IIscsiWriteTrx mRtx;

    /**
     * Bytewriter counter
     * - If a certain amount of bytes have been written, a commit is made to treetank.
     */
    private volatile int mByteCounter;

    /** JClouds mirror for faster response times (filesystem backend) */
    private BlobStore mMirrorStorage;
    private byte[] mCurrentBlobBytes;
    private long mCurrentBlobPos;
    private Map<Integer, byte[]> mBlobCache;

    /** ExecutorService to perform WriteTasks */
    private ExecutorService mWriteTaskExecutor;

    /**
     * Creates a storage module that is used by the target to handle I/O.
     * 
     * @param pNodeNumber
     *            Define how many nodes the storage holds.
     * @param pSession
     *            Pass the session associated to the location to this class.
     * @throws TTException
     *             will be thrown if there are problems creating this storage.
     */
    public TreetankStorageModule(final long pNodeNumber, final ISession pSession) throws TTException {

        mNodeNumbers = pNodeNumber;

        LOGGER.debug("Initializing storagemodule with: number of nodes=" + mNodeNumbers + ", blockSize="
            + IStorageModule.VIRTUAL_BLOCK_SIZE);

        mSession = pSession;
        mRtx = new IscsiWriteTrx(mSession.beginBucketWtx(), mSession);

        mWriteTaskExecutor = Executors.newSingleThreadExecutor();

        createStorage();
        System.out.println("Device ready");
    }

    /**
     * Bootstrap a new device as a treetank storage using
     * nodes to abstract the device.
     * 
     * @throws IOException
     *             is thrown if a node couldn't be created due to errors in the backend.
     */
    private void createStorage() throws TTException {

        LOGGER.debug("Creating storage with " + mNodeNumbers + " nodes containing " + BLOCKS_IN_NODE
            + " blocks with " + IStorageModule.VIRTUAL_BLOCK_SIZE + " bytes each.");

        long storageSize = mNodeNumbers * BLOCKS_IN_NODE * VIRTUAL_BLOCK_SIZE;

        // Creating random access file as mirror
        Properties properties = new Properties();
        properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, Files.createTempDir().getAbsolutePath());

        mMirrorStorage =
            ContextBuilder.newBuilder("filesystem").credentials("iscsi", "iscsi").overrides(properties)
                .buildView(BlobStoreContext.class).getBlobStore();
        mMirrorStorage.createContainerInLocation(null, "mirror");
        mBlobCache = new HashMap<Integer, byte[]>();

        IData data = this.mRtx.getCurrentNode();

        if (data != null) {
            return;
        }
        boolean hasNextNode = true;

        for (int i = 0; i < mNodeNumbers; i++) {
            if (i == mNodeNumbers - 1) {
                hasNextNode = false;
            }

            // Bootstrapping nodes containing clusterSize -many blocks/sectors.
            LOGGER.debug("Bootstraping node " + i + "\tof " + (mNodeNumbers - 1));
            this.mRtx.bootstrap(new byte[TreetankStorageModule.BYTES_IN_NODE], hasNextNode);
        }

        this.mRtx.commit();

    }

    /**
     * {@inheritDoc}
     */
    public int checkBounds(long logicalBlockAddress, int transferLengthInBlocks) {
        // Checking if the logical block address is out of bounds
        if (logicalBlockAddress < 0 || logicalBlockAddress >= getSizeInBlocks()) {
            return 1;
        } else
        // if the logical block address is in bounds but the transferlength either exceeds
        // the device size or is faulty return 2
        if (transferLengthInBlocks < 0 || logicalBlockAddress + transferLengthInBlocks > getSizeInBlocks()) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSizeInBlocks() {
        return mNodeNumbers * BLOCKS_IN_NODE;
    }

    /**
     * {@inheritDoc}
     */
    public void read(byte[] bytes, long storageIndex) throws IOException {

        LOGGER.debug("Starting to read with param: " + "\nstorageIndex = " + storageIndex
            + "\nbytes.length = " + bytes.length);

        long blobPos = storageIndex / (4 * bytes.length);
        int size = (4 * bytes.length);
        int storageOffset = (int)(storageIndex % size);

        Blob blob = mMirrorStorage.getBlob("mirror", String.valueOf(blobPos));
        byte[] blobBytes = new byte[4 * bytes.length];

        if (mCurrentBlobPos != blobPos) {
            if (mBlobCache.containsKey(blobPos)) {
                mCurrentBlobPos = blobPos;
                mCurrentBlobBytes = mBlobCache.get(blobPos);
            } else {

                if (mBlobCache.entrySet().size() > 256) {
                    mBlobCache.remove(mCurrentBlobPos);
                }

                blob.getPayload().getInput().read(blobBytes);
                mCurrentBlobBytes = blobBytes;
                mCurrentBlobPos = blobPos;

                mBlobCache.put((int)mCurrentBlobPos, mCurrentBlobBytes);
            }
        }

        System.arraycopy(mCurrentBlobBytes, storageOffset, bytes, 0, bytes.length);

        blob.getPayload().release();
        blob.getPayload().close();

        // long startIndex = storageIndex / BYTES_IN_NODE;
        // int startIndexOffset = (int)(storageIndex % BYTES_IN_NODE);
        //
        // long endIndex = (storageIndex + bytes.length) / BYTES_IN_NODE;
        //
        // int endIndexMax = (int)((storageIndex + bytes.length) % BYTES_IN_NODE);
        //
        // LOGGER.debug("startIndex: " + startIndex);
        // LOGGER.debug("startIndexOffset: " + startIndexOffset);
        // LOGGER.debug("endIndex: " + endIndex);
        // LOGGER.debug("endIndexMax: " + endIndexMax);
        //
        // int bytesRead =
        // bytes.length + startIndexOffset > BYTES_IN_NODE ? BYTES_IN_NODE - startIndexOffset : bytes.length;
        //
        // checkState(mRtx.moveTo(startIndex));
        // byte[] data = mRtx.getValueOfCurrentNode();
        // System.arraycopy(data, startIndexOffset, bytes, 0, bytesRead);
        //
        // for (long i = startIndex + 1; i < endIndex; i++) {
        // checkState(mRtx.moveTo(i));
        // data = mRtx.getValueOfCurrentNode();
        // System.arraycopy(data, 0, bytes, bytesRead, data.length);
        // bytesRead = bytesRead + data.length;
        //
        // }
        //
        // if (startIndex != endIndex && endIndex < mNodeNumbers) {
        // checkState(mRtx.moveTo(endIndex));
        // data = mRtx.getValueOfCurrentNode();
        // System.arraycopy(data, 0, bytes, bytesRead, endIndexMax);
        //
        // bytesRead += endIndexMax;
        // }
        //
        // // Bytes read is the actual number of bytes that have been read.
        // // The two lengths have to match, otherwise not enough bytes have been read (or too much?).
        // checkState(bytesRead == bytes.length);
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] bytes, long storageIndex) throws IOException {

        LOGGER.debug("Starting to write with param: " + "\nstorageIndex = " + storageIndex
            + "\nbytes.length = " + bytes.length);

        long blobPos = storageIndex / (4 * bytes.length);
        int size = (4 * bytes.length);
        int storageOffset = (int)(storageIndex % size);

        Blob blob = mMirrorStorage.getBlob("mirror", String.valueOf(blobPos));
        byte[] blobBytes = new byte[4 * bytes.length];

        if (blob != null) {
            if (mCurrentBlobPos == blobPos && mCurrentBlobBytes != null) {
                blobBytes = mCurrentBlobBytes;
            } else {
                if (mBlobCache.containsKey(blobPos)) {
                    mCurrentBlobBytes = mBlobCache.get(blobPos);
                    mCurrentBlobPos = blobPos;
                } else {

                    if (mBlobCache.entrySet().size() > 256) {
                        mBlobCache.remove(mCurrentBlobPos);
                    }

                    blob.getPayload().getInput().read(blobBytes);
                    mCurrentBlobBytes = blobBytes;
                    mCurrentBlobPos = blobPos;

                    mBlobCache.put((int)mCurrentBlobPos, mCurrentBlobBytes);
                }
                mCurrentBlobPos = blobPos;
            }
        }
        else{
            mCurrentBlobBytes = blobBytes;
            mCurrentBlobPos = blobPos;
        }

        System.arraycopy(bytes, 0, mCurrentBlobBytes, storageOffset, bytes.length);
        blob = mMirrorStorage.blobBuilder(String.valueOf(blobPos)).payload(mCurrentBlobBytes).build();
        mMirrorStorage.putBlob("mirror", blob);

        blob.getPayload().release();
        blob.getPayload().close();

        // Submitting into treetank
         mWriteTaskExecutor.submit(new WriteTask(bytes, storageIndex));
    }

    /**
     * WriteTask for writing data to treetank.
     * 
     * @author Andreas Rain
     * 
     */
    private class WriteTask implements Callable<Void> {

        private final byte[] mBytes;
        private final long mStorageIndex;

        public WriteTask(byte[] pBytes, long pStorageIndex) {
            mBytes = pBytes;
            mStorageIndex = pStorageIndex;
        }

        @Override
        public Void call() throws Exception {
            long startIndex = mStorageIndex / BYTES_IN_NODE;
            int startIndexOffset = (int)(mStorageIndex % BYTES_IN_NODE);

            long endIndex = (mStorageIndex + mBytes.length) / BYTES_IN_NODE;
            int endIndexMax = (int)((mStorageIndex + mBytes.length) % BYTES_IN_NODE);

            int bytesWritten =
                mBytes.length + startIndexOffset > BYTES_IN_NODE ? BYTES_IN_NODE - startIndexOffset
                    : mBytes.length;

            try {
                checkState(mRtx.moveTo(startIndex));
                byte[] data = mRtx.getValueOfCurrentNode();
                System.arraycopy(mBytes, 0, data, startIndexOffset, bytesWritten);
                mRtx.setValue(data);

                for (long i = startIndex + 1; i < endIndex; i++) {
                    checkState(mRtx.nextNode());
                    data = mRtx.getValueOfCurrentNode();
                    System.arraycopy(mBytes, bytesWritten, data, 0, data.length);
                    mRtx.setValue(data);
                    bytesWritten = bytesWritten + data.length;

                }

                if (startIndex != endIndex && endIndex < mNodeNumbers) {
                    checkState(mRtx.nextNode());
                    data = mRtx.getValueOfCurrentNode();
                    System.arraycopy(mBytes, bytesWritten, data, 0, endIndexMax);
                    mRtx.setValue(data);

                    bytesWritten += endIndexMax;
                }

                // Bytes written is the actual number of bytes that have been written.
                // The two lengths have to match, otherwise not enough bytes have been written (or too much?).
                checkState(bytesWritten == mBytes.length);

                // Incrementing bytewriter counter
                mByteCounter += bytesWritten;

                // If 1024 nodes have been fully written.
                if (mByteCounter >= COMMIT_THRESHOLD) {
                    mRtx.commit();

                    mByteCounter = 0;
                }

            } catch (TTException exc) {
                throw new IOException(exc);
            }
            return null;
        }

    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {

        try {
            mMirrorStorage.getContext().close();
            mRtx.close();

        } catch (TTException exc) {
            throw new IOException(exc);
        }
    }

}
