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

import static com.google.common.base.Objects.toStringHelper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

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
import org.treetank.node.ByteNode;
import org.treetank.node.ByteNodeFactory;
import org.treetank.node.ISCSIMetaPageFactory;
import org.treetank.revisioning.IRevisioning;

import com.google.common.io.ByteArrayDataInput;
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
    private final long sizeInBlocks;

    /**
     * The size of each block.
     */
    private final int blockSize;

    /**
     * 
     */
    private final IStorage storage;

    /**
     * 
     */
    private final ISession session;

    /**
     * 
     */
    private final IIscsiWriteTrx mRtx;

    /**
     * Creates a new {@link TreetankStorageModule} backed by the specified {@link IStorage}.
     * 
     * @param sizeInBlocks
     *            blocksize for this module
     * @param conf
     *            the fully initialized {@link StorageConfiguration}
     * @throws TTException
     */
    public TreetankStorageModule(final long sizeInBlocks, final int blockSize,
        final StorageConfiguration conf, final File file) throws TTException {

        LOGGER.info("Initializing storagemodule with: sizeInBlocks=" + sizeInBlocks + ", blockSize="
            + blockSize);

        this.sizeInBlocks = sizeInBlocks;
        this.blockSize = blockSize;

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
    }

    private void createStorage() throws IOException {

        LOGGER.info("Creating storage with " + sizeInBlocks + " blocks of size 512.");

        try {

            INode node = this.mRtx.getCurrentNode();

            if (node != null)
                return;
            for (int i = 0; i < sizeInBlocks; i++) {
                try {
                    this.mRtx.bootstrap(new byte[(int)(blockSize)]);
                } catch (TTException e) {
                    throw new IOException("The creation of a new node was started and somehow didn't finish.");
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

        if (logicalBlockAddress < 0 || logicalBlockAddress >= sizeInBlocks)
            return 1;
        if (transferLengthInBlocks < 0 || logicalBlockAddress + transferLengthInBlocks > sizeInBlocks)
            return 2;
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getSizeInBlocks() {

        return sizeInBlocks;
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
        int realIndex = (int)(storageIndex / blockSize);
        LOGGER.info("Starting to read realIndex " + realIndex);

        ByteArrayDataOutput output = ByteStreams.newDataOutput(length);

        int inc = (length % blockSize == 0) ? 0 : 1;
        for (long i = realIndex; i < ((length + storageIndex) / blockSize) + inc; i++) {
            setCursorToIndex(i);

            INode node = this.mRtx.getCurrentNode();

            byte[] val = ((ByteNode)node).getVal();

            output.write(val);

        }

        System.arraycopy(output.toByteArray(), 0, bytes, bytesOffset, length);

        LOGGER.info("Giving back bytes: " + Arrays.toString(bytes).substring(0, 1000));
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {

        LOGGER.info("Starting to write with param: \nbytes = " + Arrays.toString(bytes).substring(0, 100)
            + "\nbytesOffset = " + bytesOffset + "\nlength = " + length + "\nstorageIndex = " + storageIndex);
        try {
            // Using the most recent revision
            if (bytesOffset + length > bytes.length) {
                throw new IOException();
            }

            int realIndex = (int)(storageIndex / blockSize);

            ByteArrayDataOutput output = ByteStreams.newDataOutput(length);
            System.out.println(bytes.length - (blockSize - (length % blockSize)));
            output.write(bytes, bytesOffset, length);

            ByteArrayDataInput input = ByteStreams.newDataInput(output.toByteArray());

            LOGGER.info("Writing from node " + realIndex + " to node "
                + (((length + storageIndex) / blockSize)));

            for (long i = realIndex; i < ((length + storageIndex) / blockSize); i++) {
                setCursorToIndex(i);
                byte[] val = new byte[blockSize];

                input.readFully(val);

                this.mRtx.setValue(val);
            }

            if (length % blockSize != 0) {
                setCursorToIndex((int)((length + storageIndex) / blockSize));
                INode node = this.mRtx.getCurrentNode();

                byte[] val = ((ByteNode)node).getVal();

                input.readFully(val, 0, (length % blockSize));

                this.mRtx.setValue(val);
            }

            this.mRtx.commit();
        } catch (TTException e) {
            throw new IOException(e.getMessage());
        }
    }

    // private void getNodeByIndex(IIscsiReadTrx iRtx, int realIndex) throws IOException {
    //
    // INode node = iRtx.getCurrentNode();
    //
    // if (node == null) {
    // throw new IOException(
    // "It seems like no data has been written. Please do so and then try to read again.");
    // }
    //
    // while (((ByteNode)node).getIndex() != realIndex && ((ByteNode)node).getNextNodeKey() != 0) {
    // iRtx.nextNode();
    // node = iRtx.getCurrentNode();
    // }
    //
    // if (((ByteNode)node).getIndex() == realIndex) {
    // return;
    // } else {
    // throw new IOException(
    // "The index point you were seeking is not filled with data yet. Please check your bounds and try again.");
    // }
    // }

    private void setCursorToIndex(long pIndex) throws IOException {

        ByteNode node = (ByteNode)this.mRtx.getCurrentNode();

        if (node == null) {
            /*
             * Only used for lazy creation. ByteNode newNode = new ByteNode(0, new
             * byte[(int) (blockSize)]); newNode.setIndex(0); try { ((IIscsiWriteTrx)
             * iWtx).insert(newNode); return; } catch (TTException e) {
             */
            throw new IOException("The creation of a new node was started and somehow didn't finish.");
            // }
        }

        if (node.getIndex() == pIndex) {
            return;
        } else if (node.getIndex() < pIndex) {
            do {
                this.mRtx.nextNode();
                node = (ByteNode)this.mRtx.getCurrentNode();
                if (node.getIndex() == pIndex) {
                    return;
                }
            } while (node.getNextNodeKey() != 0);
        } else {
            do {
                this.mRtx.previousNode();
                node = (ByteNode)this.mRtx.getCurrentNode();
                if (node.getIndex() == pIndex) {
                    return;
                }
            } while (node.getPreviousNodeKey() >= 0);
        }
        throw new IOException(toStringHelper("Node never found with index").add("index", pIndex).toString());

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
