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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.jscsi.target.storage.IStorageModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.IscsiWriteTrx;
import org.treetank.access.Storage;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IIscsiWriteTrx;
import org.treetank.api.INode;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.jscsi.buffering.BufferedWriteTask;
import org.treetank.jscsi.buffering.Collision;
import org.treetank.node.ByteNode;
import org.treetank.node.ByteNodeFactory;
import org.treetank.node.ISCSIMetaPageFactory;
import org.treetank.revisioning.IRevisioning;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * <h1>TreetankStorageModule</h1>
 * <p>
 * This implementation is used to store data into treetank via an iscsi target.
 * </p>
 * 
 * @author Andreas Rain
 */
public class TreetankStorageModule implements IStorageModule {

    /** Number of Blocks in one Cluster. */
    public static final int BLOCK_IN_CLUSTER = 512;

    private static final Logger LOGGER = LoggerFactory.getLogger(TreetankStorageModule.class);

    /**
     * The number of clusters in the storage resulting in mNumberOfClusters * BLOCKSINCLUSTER *
     * VIRTUAL_BLOCK_SIZE bytes
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    private final long mNumberOfClusters;

    /**
     * Treetank storage the target uses as a storage device.
     */
    private final IStorage storage;

    /**
     * The session this storage module uses to access the storage device.
     */
    private final ISession session;

    /**
     * {@link IIscsiWriteTrx} that is used to write/read from treetank.
     */
    private final IIscsiWriteTrx mRtx;

    /**
     * The service that holds the BufferedTaskWorker
     */
    private final ExecutorService mWriterService;

    /**
     * Mirroring all tasks that are pending.
     */
    private LinkedBlockingQueue<BufferedWriteTask> mTasks;

    /**
     * Creates a storage module that is used by the target to handle I/O.
     * 
     * @param pSizeInClusters
     *            Define how many clusters the storage holds.
     * @param conf
     *            Pass the storage configuration to use for this storage module.
     * @throws TTException
     *             will be thrown if there are problems creating this storage.
     */
    public TreetankStorageModule(final long pSizeInClusters, final StorageConfiguration conf)
        throws TTException {

        mNumberOfClusters = pSizeInClusters;

        LOGGER.info("Initializing storagemodule with: sizeInBlocks=" + mNumberOfClusters + ", blockSize="
            + IStorageModule.VIRTUAL_BLOCK_SIZE);

        Injector injector =
            Guice.createInjector(new ModuleSetter().setNodeFacClass(ByteNodeFactory.class).setMetaFacClass(
                ISCSIMetaPageFactory.class).createModule());
        IBackendFactory backend = injector.getInstance(IBackendFactory.class);
        IRevisioning revision = injector.getInstance(IRevisioning.class);

        // Creating and opening the storage.
        // Making it ready for usage.
        if (Storage.existsStorage(conf.mFile)) {
            Storage.truncateStorage(conf);
            Storage.createStorage(conf);
        }

        storage = Storage.openStorage(conf.mFile);

        Properties props = StandardSettings.getProps(conf.mFile.getAbsolutePath(), "jscsi-target");
        ResourceConfiguration mResourceConfig =
            new ResourceConfiguration(props, backend, revision, new ByteNodeFactory(),
                new ISCSIMetaPageFactory());
        storage.createResource(mResourceConfig);

        session = storage.getSession(new SessionConfiguration("jscsi-target", null));
        mRtx = new IscsiWriteTrx(session.beginPageWriteTransaction(), session);

        try {
            createStorage();
        } catch (IOException exc) {
            throw new TTIOException(exc);
        }

        /*
         * Creating the writer service and adding the worker to the pool.
         */
        mWriterService = Executors.newSingleThreadExecutor();
        
        mTasks = new LinkedBlockingQueue<BufferedWriteTask>();
    }

    /**
     * Bootstrap a new device as a treetank storage using
     * nodes to abstract the device.
     * 
     * @throws IOException
     *             is thrown if a node couldn't be created due to errors in the backend.
     */
    private void createStorage() throws IOException {

        LOGGER.info("Creating storage with " + mNumberOfClusters + " clusters containing " + BLOCK_IN_CLUSTER
            + " sectors with " + IStorageModule.VIRTUAL_BLOCK_SIZE + " bytes each.");

        try {

            INode node = this.mRtx.getCurrentNode();

            if (node != null) {
                return;
            }
            boolean hasNextNode = true;

            for (int i = 0; i < mNumberOfClusters; i++) {
                if (i == mNumberOfClusters - 1) {
                    hasNextNode = false;
                }

                try {
                    // Bootstrapping nodes containing clusterSize -many blocks/sectors.
                    LOGGER.info("Bootstraping node " + i + "\tof " + (mNumberOfClusters - 1));
                    this.mRtx.bootstrap(
                        new byte[(int)(IStorageModule.VIRTUAL_BLOCK_SIZE * BLOCK_IN_CLUSTER)], hasNextNode);
                } catch (TTException e) {
                    throw new IOException(e);
                }

                if (i % 10 == 0) {
                    this.mRtx.commit();
                }
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
        return mNumberOfClusters * BLOCK_IN_CLUSTER;
    }

    /**
     * {@inheritDoc}
     */
    public void read(byte[] bytes, long storageIndex) throws IOException {

        LOGGER.info("Starting to read with param: " + "\nstorageIndex = " + storageIndex);
        
        // CLean up tasks
        while(mTasks.peek().isFinished()){
            //This task is finished and already in the treetank backend
            //we can remove it from the list.
            mTasks.poll();
        }
        
        // Using the most recent revision
        
        int startIndex = (int)(storageIndex / (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
        int startIndexOffset = (int)(storageIndex % (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));

        int endIndex =
            (int)((storageIndex + bytes.length) / (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
        int endIndexMax =
            (int)((storageIndex + bytes.length) % (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));

        LOGGER.info("Starting to read from node " + startIndex + " to node " + endIndex);

        ByteArrayDataOutput output = ByteStreams.newDataOutput(bytes.length);

        for (long i = startIndex; i <= endIndex; i++) {
            this.mRtx.moveTo(i);

            INode node = this.mRtx.getCurrentNode();
            byte[] val = ((ByteNode)node).getVal();

            if (i == startIndex && i == endIndex) {
                output.write(val, startIndexOffset, bytes.length);
            } else if (i == startIndex) {
                output.write(val, startIndexOffset, (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE)
                    - startIndexOffset);
            } else if (i == endIndex) {
                output.write(val, 0, endIndexMax);
            } else {
                output.write(val);
            }

        }

        System.arraycopy(output.toByteArray(), 0, bytes, 0, bytes.length);
        
        // Overwriting segments in the byte array using the writer tasks that are still in progress.
        readConcurrent(bytes, storageIndex);
    }

    /**
     * Read the newest version w.r.t the pending
     * write tasks.
     * 
     * @param bytes
     *            bytes to read into
     * @param bytesOffset
     *            offset to start reading into
     * @param length
     *            how many bytes have to be read
     * @param storageIndex
     *            where to start reading in terms of storage device
     * @throws IOException
     */
    private void readConcurrent(byte[] bytes, long storageIndex)
        throws IOException {
        List<Collision> collisions = checkForCollisions(bytes.length, storageIndex);

        for (Collision collision : collisions) {
            if (collision.getStart() != storageIndex) {
                System.arraycopy(collision.getBytes(), 0, bytes,
                    (int)((collision.getStart() - storageIndex)), collision.getBytes().length);
            } else {
                System.arraycopy(collision.getBytes(), 0, bytes, 0, collision.getBytes().length);
            }
        }
    }

    /**
     * The returned collisions are ordered chronologically.
     * 
     * @param pLength
     * @param pStorageIndex
     * @return List<Collision> - returns a list of collisions
     */

    private List<Collision> checkForCollisions(int pLength, long pStorageIndex) {
        List<Collision> collisions = new ArrayList<Collision>();
        //TODO rewrite
        for (BufferedWriteTask task : mTasks) {
            if (overlappingIndizes(pLength, pStorageIndex, task.getBytes().length, task.getStorageIndex())) {
                // Determining where the two tasks collide
                int start = 0;
                int end = 0;
                byte[] bytes = null;

                // Determining the start point
                if (task.getStorageIndex() < pStorageIndex) {
                    start = (int)pStorageIndex;
                } else {
                    start = (int)task.getStorageIndex();
                }

                // Determining the end point
                if (task.getStorageIndex() + task.getBytes().length > pStorageIndex + pLength) {
                    end = (int)(pStorageIndex + pLength);
                } else {
                    end = (int)(task.getStorageIndex() + task.getBytes().length);
                }

                bytes = new byte[end - start];

                if (start == pStorageIndex) {
                    System.arraycopy(task.getBytes(), (int)((pStorageIndex - task
                        .getStorageIndex())), bytes, 0, end - start);
                } else {
                    System.arraycopy(task.getBytes(), 0, bytes, 0, end - start);
                }

                collisions.add(new Collision(start, end, bytes));

                LOGGER.info("Found collision from " + start + " to " + end);
            }
        }

        return collisions;
    }

    /**
     * Determine if indizes overlap.
     * 
     * @param srcLength
     * @param srcStorageIndex
     * @param destLength
     * @param destStorageIndex
     * @return true if indizes overlap, false otherwise
     */
    private boolean overlappingIndizes(int srcLength, long srcStorageIndex, int destLength,
        long destStorageIndex) {
        if (destLength + destStorageIndex < srcStorageIndex || destStorageIndex > srcStorageIndex + srcLength) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] bytes, long storageIndex) throws IOException {
        BufferedWriteTask task = new BufferedWriteTask(bytes, storageIndex, mRtx);
        mTasks.offer(task);
        mWriterService.submit(task);
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {

        try {
            mWriterService.shutdown();
            
            while(!mWriterService.isTerminated()){
                // Do nothing and wait
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getStackTrace().toString());
                }
            }
            
            mRtx.close();
            session.close();
            storage.close();

        } catch (TTException exc) {
            throw new IOException(exc);
        }
    }

}
