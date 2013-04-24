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

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jscsi.target.storage.IStorageModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.IscsiWriteTrx;
import org.treetank.api.IIscsiWriteTrx;
import org.treetank.api.INode;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;

/**
 * <h1>TreetankStorageModule</h1>
 * <p>
 * This implementation is used to store data into treetank via an iscsi target.
 * </p>
 * 
 * @author Andreas Rain
 */
public class TreetankStorageModule implements IStorageModule {
    
    /** Bytewriter counter
     *  - If a certain amount of bytes have been written, a commit is made to treetank. */
    private static int BYTE_WRITER_COUNTER = 0;

    /** Number of Blocks in one Cluster. */
    public static final int BLOCKS_IN_NODE = 8;

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
     * Determine whether or not the device has been bootstraped and is readable.
     */
    private boolean mDeviceReady = true;

    /**
     * Creates a storage module that is used by the target to handle I/O.
     * 
     * @param pNodeNumber
     *            Define how many nodes the storage holds.
     * @param pSession
     *            Pass the sessiona associated to the location to this class.
     * @throws TTException
     *             will be thrown if there are problems creating this storage.
     */
    public TreetankStorageModule(final long pNodeNumber, final ISession pSession) throws TTException {
        
        mDeviceReady = false;
        
        mNodeNumbers = pNodeNumber;
        
        LOGGER.info("Initializing storagemodule with: number of nodes=" + mNodeNumbers + ", blockSize="
            + IStorageModule.VIRTUAL_BLOCK_SIZE);
        
        mSession = pSession;
        mRtx = new IscsiWriteTrx(mSession.beginPageWriteTransaction(), mSession);
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        
        service.submit(new CreateStorageCallable());

    }

    /**
     * A callable to bootstrap the storage device in the background
     * so the target is available for discovery immdiately.
     * 
     * @author Andreas Rain
     * 
     */
    class CreateStorageCallable implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            createStorage();
            LOGGER.info("Device is ready");
            mDeviceReady = true;
            return null;
        }

    }

    /**
     * Bootstrap a new device as a treetank storage using
     * nodes to abstract the device.
     * 
     * @throws IOException
     *             is thrown if a node couldn't be created due to errors in the backend.
     */
    private void createStorage() throws IOException {

        LOGGER.info("Creating storage with " + mNodeNumbers + " nodes containing " + BLOCKS_IN_NODE
            + " blocks with " + IStorageModule.VIRTUAL_BLOCK_SIZE + " bytes each.");

        try {

            INode node = this.mRtx.getCurrentNode();

            if (node != null) {
                return;
            }
            boolean hasNextNode = true;

            for (int i = 0; i < mNodeNumbers; i++) {
                if (i == mNodeNumbers - 1) {
                    hasNextNode = false;
                }

                try {
                    // Bootstrapping nodes containing clusterSize -many blocks/sectors.
                    LOGGER.debug("Bootstraping node " + i + "\tof " + (mNodeNumbers - 1));
                    this.mRtx.bootstrap(new byte[TreetankStorageModule.BYTES_IN_NODE], hasNextNode);
                } catch (TTException e) {
                    throw new IOException(e);
                }

//                if (i % 10000 == 0) {
//                    this.mRtx.commit();
//                }
            }

            this.mRtx.commit();

        } catch (TTException exc) {
            throw new IOException(exc);
        }

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

        LOGGER.info("Starting to read with param: " + "\nstorageIndex = " + storageIndex + "\nbytes.length = " + bytes.length);

        long startIndex = storageIndex / BYTES_IN_NODE;
        int startIndexOffset = (int)(storageIndex % BYTES_IN_NODE);

        long endIndex = (storageIndex + bytes.length) / BYTES_IN_NODE;

        int endIndexMax = (int)((storageIndex + bytes.length) % BYTES_IN_NODE);

        LOGGER.debug("startIndex: " + startIndex);
        LOGGER.debug("startIndexOffset: " + startIndexOffset);
        LOGGER.debug("endIndex: " + endIndex);
        LOGGER.debug("endIndexMax: " + endIndexMax);

        int bytesRead =
            bytes.length + startIndexOffset > BYTES_IN_NODE ? BYTES_IN_NODE - startIndexOffset : bytes.length;

        checkState(mRtx.moveTo(startIndex));
        byte[] data = mRtx.getValueOfCurrentNode();
        System.arraycopy(data, startIndexOffset, bytes, 0, bytesRead);

        for (long i = startIndex + 1; i < endIndex; i++) {
            checkState(mRtx.moveTo(i));
            data = mRtx.getValueOfCurrentNode();
            System.arraycopy(data, 0, bytes, bytesRead, data.length);
            bytesRead = bytesRead + data.length;

        }

        if (startIndex != endIndex && endIndex < mNodeNumbers) {
            checkState(mRtx.moveTo(endIndex));
            data = mRtx.getValueOfCurrentNode();
            System.arraycopy(data, 0, bytes, bytesRead, endIndexMax);

            bytesRead += endIndexMax;
        }

        // Bytes read is the actual number of bytes that have been read.
        // The two lengths have to match, otherwise not enough bytes have been read (or too much?).
        checkState(bytesRead == bytes.length);
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] bytes, long storageIndex) throws IOException {

        LOGGER.info("Starting to write with param: " + "\nstorageIndex = " + storageIndex + "\nbytes.length = " + bytes.length);
        
        long startIndex = storageIndex / BYTES_IN_NODE;
        int startIndexOffset = (int)(storageIndex % BYTES_IN_NODE);

        long endIndex = (storageIndex + bytes.length) / BYTES_IN_NODE;
        int endIndexMax = (int)((storageIndex + bytes.length) % BYTES_IN_NODE);

        int bytesWritten =
            bytes.length + startIndexOffset > BYTES_IN_NODE ? BYTES_IN_NODE - startIndexOffset : bytes.length;

        try {
            checkState(mRtx.moveTo(startIndex));
            byte[] data = mRtx.getValueOfCurrentNode();
            System.arraycopy(bytes, 0, data, startIndexOffset, bytesWritten);
            mRtx.setValue(data);

            for (long i = startIndex + 1; i < endIndex; i++) {
                checkState(mRtx.moveTo(i));
                data = mRtx.getValueOfCurrentNode();
                System.arraycopy(bytes, bytesWritten, data, 0, data.length);
                mRtx.setValue(data);
                bytesWritten = bytesWritten + data.length;

            }

            if (startIndex != endIndex && endIndex < mNodeNumbers) {
                checkState(mRtx.moveTo(endIndex));
                data = mRtx.getValueOfCurrentNode();
                System.arraycopy(bytes, bytesWritten, data, 0, endIndexMax);
                mRtx.setValue(data);

                bytesWritten += endIndexMax;
            }

            // Bytes written is the actual number of bytes that have been written.
            // The two lengths have to match, otherwise not enough bytes have been written (or too much?).
            checkState(bytesWritten == bytes.length);
            
            // Incrementing bytewriter counter
            BYTE_WRITER_COUNTER += bytesWritten;
            
            //If 256 MB written, a commit is made..
            if(BYTE_WRITER_COUNTER >= 268435456){
                this.mRtx.commit();

                LOGGER.info("Commited changes to treetank.");
                BYTE_WRITER_COUNTER = 0;
            }
        } catch (Exception exc) {
            throw new IOException(exc);
        }
    }
    
    /**
     * @return  true, if device is bootstraped
     *          false, if bootstraping is still in process
     */
    public boolean isReady(){
        return mDeviceReady;
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {

        try {
            mRtx.close();

        } catch (TTException exc) {
            throw new IOException(exc);
        }
    }

}
