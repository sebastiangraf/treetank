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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.jscsi.buffering.BufferedTaskWorker;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(TreetankStorageModule.class);

    /**
     * The size of the medium in blocks.
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    private final long sizeInClusters;

    /**
     * The size of each block.
     */
    private final int blockSize;

    /**
     * This variable is used to determine,
     * how many sectors of the size 'blockSize'
     * one node holds.
     */
    private final int clusterSize;

    /**
     * Treetank storage the target uses as a storage device.
     */
    private final IStorage storage;

    /**
     * The session this storage module uses to access the storage device.
     */
    private final ISession session;

    /**
     * 
     */
    private final IIscsiWriteTrx mRtx;

    private final ExecutorService mWriterService;

    private final BufferedTaskWorker mWorker;

    /**
     * Creates a storage module that is used by the target to handle I/O.
     * 
     * @param pSizeInClusters
     *            Define how many clusters the storage holds.
     * @param pBlockSize
     *            Define the bytes in a sector.
     * @param pClusterSize
     *            Define the amount of sectors one cluster contains.
     * @param conf
     *            Pass the storage configuration to use for this storage module.
     * @param file
     *            The file path to the storage on the harddisk.
     * @throws TTException
     *             will be thrown if there are problems creating this storage.
     */
    public TreetankStorageModule(final long pSizeInClusters, final int pBlockSize, final int pClusterSize,
        final StorageConfiguration conf, final File file) throws TTException {

        clusterSize = pClusterSize;
        sizeInClusters = pSizeInClusters;
        blockSize = pBlockSize;

        LOGGER.info("Initializing storagemodule with: sizeInBlocks=" + sizeInClusters + ", blockSize="
            + blockSize);

        Injector injector =
            Guice.createInjector(new ModuleSetter().setNodeFacClass(ByteNodeFactory.class).setMetaFacClass(
                ISCSIMetaPageFactory.class).createModule());
        IBackendFactory backend = injector.getInstance(IBackendFactory.class);
        IRevisioning revision = injector.getInstance(IRevisioning.class);

        // Creating and opening the storage.
        // Making it ready for usage.
        if (!file.exists()) {
            Storage.truncateStorage(conf);
            Storage.createStorage(conf);
        }

        storage = Storage.openStorage(file);

        Properties props = StandardSettings.getProps(file.getAbsolutePath(), "jscsi-target");
        ResourceConfiguration mResourceConfig =
            new ResourceConfiguration(props, backend, revision, new ByteNodeFactory(),
                new ISCSIMetaPageFactory());
        storage.createResource(mResourceConfig);

        session = storage.getSession(new SessionConfiguration("jscsi-target", null));
        mRtx = new IscsiWriteTrx(session.beginPageWriteTransaction(), session);

        try {
            createStorage();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /*
         * Creating the writer service and adding the worker to the pool.
         */
        mWriterService = Executors.newCachedThreadPool();
        mWorker = new BufferedTaskWorker(mRtx, pClusterSize, pBlockSize);

        mWriterService.submit(mWorker);
    }

    private void createStorage() throws IOException {

        LOGGER.info("Creating storage with " + sizeInClusters + " clusters containing " + clusterSize
            + " sectors with " + blockSize + " bytes each.");

        try {

            INode node = this.mRtx.getCurrentNode();

            if (node != null)
                return;

            boolean hasNextNode = true;

            for (int i = 0; i < sizeInClusters; i++) {
                if (i == sizeInClusters - 1) {
                    hasNextNode = false;
                }

                try {
                    // Bootstrapping nodes containing clusterSize -many blocks/sectors.
                    LOGGER.info("Bootstraping node " + i + "\tof " + (sizeInClusters - 1));
                    this.mRtx.bootstrap(new byte[(int)(blockSize * clusterSize)], hasNextNode);
                } catch (TTException e) {
                    throw new IOException(e);
                }

                if (i % 50 == 0) {
                    this.mRtx.commit();
                }
            }

            this.mRtx.commit();

        } catch (TTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * {@inheritDoc}
     */
    public int checkBounds(long logicalBlockAddress, int transferLengthInBlocks) {
        // Checking if the logical block address is out of bounds
        if (logicalBlockAddress < 0 || logicalBlockAddress >= getSizeInBlocks())
            return 1;

        // if the logical block address is in bounds but the transferlength either exceeds
        // the device size or is faulty return 2
        if (transferLengthInBlocks < 0 || logicalBlockAddress + transferLengthInBlocks > getSizeInBlocks())
            return 2;
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getSizeInBlocks() {

        return sizeInClusters * clusterSize;
    }

    /**
     * {@inheritDoc}
     */
    public void read(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {

        LOGGER.info("Starting to read with param: \nbytesOffset = " + bytesOffset + "\nlength = " + length
            + "\nstorageIndex = " + storageIndex);
        // Using the most recent revision
        if (bytesOffset + length > bytes.length) {
            throw new IOException();
        }
        int startIndex = (int)(storageIndex / (clusterSize * blockSize));
        int startIndexOffset = (int)(storageIndex % (clusterSize * blockSize));

        int endIndex = (int)((storageIndex + length) / (clusterSize * blockSize));
        int endIndexMax = (int)((storageIndex + length) % (clusterSize * blockSize));

        LOGGER.info("Starting to read from node " + startIndex + " to node " + endIndex);

        ByteArrayDataOutput output = ByteStreams.newDataOutput(length);

        for (long i = startIndex; i <= endIndex; i++) {
            setCursorToIndex(i);

            INode node = this.mRtx.getCurrentNode();
            byte[] val = ((ByteNode)node).getVal();

            if (i == startIndex && i == endIndex) {
                output.write(val, startIndexOffset, length);
            } else if (i == startIndex) {
                output.write(val, startIndexOffset, (clusterSize * blockSize) - startIndexOffset);
            } else if (i == endIndex) {
                output.write(val, 0, endIndexMax);
            } else {
                output.write(val);
            }

        }

        System.arraycopy(output.toByteArray(), 0, bytes, bytesOffset, length);

        // Overwriting segments in the byte array using the writer tasks that are still in progress.
        readConcurrent(bytes, bytesOffset, length, storageIndex);
    }

    private void readConcurrent(byte[] bytes, int bytesOffset, int length, long storageIndex)
        throws IOException {
        List<Collision> collisions = mWorker.checkForCollisions(length, storageIndex);

        for (Collision collision : collisions) {
            overwriteCollision(bytes, bytesOffset, length, storageIndex, collision);
        }
    }

    private void overwriteCollision(byte[] bytes, int bytesOffset, int length, long storageIndex,
        Collision collision) {
        if (collision.getStart() != storageIndex) {
            System.arraycopy(collision.getBytes(), 0, bytes,
                (int)(bytesOffset + (collision.getStart() - storageIndex)), collision.getBytes().length);
        } else {
            System.arraycopy(collision.getBytes(), 0, bytes, bytesOffset, collision.getBytes().length);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {

        // The write method won't block and will be performed in the background
        // so the initiator gets a faster response.
        mWorker.newTask(bytes, bytesOffset, length, storageIndex);

    }

    private void setCursorToIndex(long pIndex) throws IOException {

        // Since the device has been bootstraped,
        // we can assume that the indexes of nodes don't get mixed
        // and are linear.
        this.mRtx.moveTo(pIndex);
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {

        try {
            mRtx.close();
            session.close();
            storage.close();

            // A small hack, so the {@link IStorageModule} doesn't have to be altered
            // to
            // throw Exceptions in general.
        } catch (TTException e) {
            throw new IOException(e.getMessage());
        }
    }

}
