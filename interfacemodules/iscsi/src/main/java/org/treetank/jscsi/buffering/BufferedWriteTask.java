package org.treetank.jscsi.buffering;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.jscsi.target.storage.IStorageModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.api.IIscsiWriteTrx;
import org.treetank.api.INode;
import org.treetank.exception.TTException;
import org.treetank.jscsi.TreetankStorageModule;
import org.treetank.node.ByteNode;

/**
 * BufferedWriteTasks are used to
 * keep the information of a write request
 * for later storaging in TreeTank.
 * 
 * @author Andreas Rain
 * 
 */
public class BufferedWriteTask implements Callable<Void>{

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferedWriteTask.class);
    
    /**
     * The bytes to buffer.
     */
    private final byte[] mBytes;

    /**
     * Where to start writing in the storage.
     */
    private final long mStorageIndex;

    /**
     * The transaction to write into treetank.
     */
    private final IIscsiWriteTrx mRtx;
    
    /**
     * When the task is finished this variable is set to true.
     */
    private boolean finished;

    /**
     * All these parameters are passed to the {@link TreetankStorageModule} write method
     * and are being buffered using this class and its constructor.
     * 
     * @param pBytes
     * @param pStorageIndex
     */
    public BufferedWriteTask(byte[] pBytes, long pStorageIndex, IIscsiWriteTrx pWtx) {
        super();
        this.mBytes = pBytes;
        this.mStorageIndex = pStorageIndex;
        
        mRtx = pWtx;
        finished = false;
    }

    /**
     * 
     * @return byte[] - the bytes buffered in this task.
     */
    public byte[] getBytes() {
        return mBytes;
    }

    /**
     * 
     * @return long - the storageindex
     */
    public long getStorageIndex() {
        return mStorageIndex;
    }
    
    /**
     * Determine whether or not this task is finished.
     * @return true if task has finished, false otherwise.
     */
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Void call() throws Exception {
        LOGGER.info("Starting to write with param: \nbytes = " + Arrays.toString(mBytes).substring(0, 100)
            + "\nstorageIndex = " + mStorageIndex);
        try {
            
            int startIndex =
                (int)(mStorageIndex / (TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
            int startIndexOffset =
                (int)(mStorageIndex % (TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));

            int endIndex =
                (int)((mStorageIndex + mBytes.length) / (TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
            int endIndexMax =
                (int)((mStorageIndex + mBytes.length) % (TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));

            for (int i = startIndex; i <= endIndex; i++) {
                mRtx.moveTo(i);
                LOGGER.info("Writing to node " + i);

                INode node = mRtx.getCurrentNode();
                byte[] val = ((ByteNode)node).getVal();

                if (i == startIndex && i == endIndex) {
                    System.arraycopy(mBytes, 0, val, startIndexOffset, endIndexMax);
                } else if (i == startIndex) {
                    System.arraycopy(mBytes, 0, val, startIndexOffset,
                        (TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE)
                            - startIndexOffset);
                } else if (i == endIndex) {
                    System
                        .arraycopy(
                            mBytes,
                            0
                                + ((TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE) * (i - startIndex)),
                            val, 0, endIndexMax);
                } else {
                    System
                        .arraycopy(
                            mBytes,
                            0
                                + ((TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE) * (i - startIndex)),
                            val, 0,
                            (TreetankStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
                }

                mRtx.setValue(val);
            }

            this.mRtx.commit();
        } catch (TTException e) {
            throw new IOException(e.getMessage());
        }
        
        finished = true;
        return null;
    }

}
