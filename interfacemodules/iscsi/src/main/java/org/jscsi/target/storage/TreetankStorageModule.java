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

package org.jscsi.target.storage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.IscsiReadTrx;
import org.treetank.access.IscsiWriteTrx;
import org.treetank.access.StandardByteNodeSettings;
import org.treetank.access.Storage;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IIscsiReadTrx;
import org.treetank.api.IIscsiWriteTrx;
import org.treetank.api.INode;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.iscsi.node.ByteNode;
import org.treetank.iscsi.node.ByteNodeFactory;
import org.treetank.revisioning.IRevisioning.IRevisioningFactory;

import com.google.common.io.ByteArrayDataInput;
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

  private static final Logger LOGGER = LoggerFactory
      .getLogger(TreetankStorageModule.class);

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
   * The {@link StorageConfiguration} used for accessing the storage medium.
   * 
   * @see #MODE
   */
  private final StorageConfiguration conf;

  /**
     * 
     */
  private IStorage storage = null;

  /**
     * 
     */
  private ISession session = null;

  /**
     * 
     */
  IPageReadTrx pRtx = null;

  /**
   * Creates a new {@link TreetankStorageModule} backed by the specified
   * {@link IStorage}.
   * 
   * @param sizeInBlocks
   *          blocksize for this module
   * @param conf
   *          the fully initialized {@link StorageConfiguration}
   * @throws TTException
   */
  public TreetankStorageModule(final long sizeInBlocks, final int blockSize,
      final StorageConfiguration conf, final File file) throws TTException {
    LOGGER.info("Initializing storagemodule with: sizeInBlocks=" + sizeInBlocks + ", blockSize=" + blockSize);
    
    this.sizeInBlocks = sizeInBlocks;
    this.blockSize = blockSize;
    this.conf = conf;

    Injector injector = Guice.createInjector(new StandardByteNodeSettings());
    IBackendFactory backend = injector.getInstance(IBackendFactory.class);
    IRevisioningFactory revision = injector
        .getInstance(IRevisioningFactory.class);

    // Creating and opening the storage.
    // Making it ready for usage.
    if (!file.exists()) {
      Storage.truncateStorage(conf);
      Storage.createStorage(conf);
    }

    storage = Storage.openStorage(file);

    Properties props = StandardSettings.getStandardProperties(
        file.getAbsolutePath(), "jscsi-target");
    ResourceConfiguration mResourceConfig = new ResourceConfiguration(props, 1,
        backend, revision, new ByteNodeFactory());
    storage.createResource(mResourceConfig);

    session = storage
        .getSession(new SessionConfiguration("jscsi-target", null));
  }

  /**
   * {@inheritDoc}
   */
  public int checkBounds(long logicalBlockAddress, int transferLengthInBlocks) {

    if (logicalBlockAddress < 0 || logicalBlockAddress >= sizeInBlocks)
      return 1;
    if (transferLengthInBlocks < 0
        || logicalBlockAddress + transferLengthInBlocks > sizeInBlocks)
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
  public void read(byte[] bytes, int bytesOffset, int length, long storageIndex)
      throws IOException {
    LOGGER.info("Starting to read with param: \nbytesOffset = " + bytesOffset + "\nlength = " + length + "\nstorageIndex = " + storageIndex);
    try {
      // Using the most recent revision
      if (bytesOffset + length > bytes.length)
        throw new IOException();

      int realIndex = (int) (storageIndex / blockSize);
      LOGGER.info("Starting to read realIndex " + realIndex);

      pRtx = session.beginPageReadTransaction(session.getMostRecentVersion());
      IscsiReadTrx iRtx = new IscsiReadTrx(pRtx);
      getNodeByIndex(iRtx, realIndex);

      INode node = iRtx.getCurrentNode();

      byte[] val = ((ByteNode) node).getVal();
      ByteArrayDataInput input = ByteStreams.newDataInput(val);

      if (length <= blockSize) {
        input.readFully(bytes, bytesOffset, length);
      } else {
        int i = 0;
        int bytesLeft = length;

        while (bytesLeft > 0) {
          if (bytesLeft >= blockSize) {
            input.readFully(bytes, (int) (bytesOffset + (blockSize * i)),
                (int) blockSize);
            bytesLeft -= blockSize;
            i++;

            getNodeByIndex(iRtx, ((ByteNode) node).getIndex() + 1);
            node = iRtx.getCurrentNode();
            input = ByteStreams.newDataInput(((ByteNode) iRtx.getCurrentNode())
                .getVal());
          } else {
            input.readFully(bytes, (int) (bytesOffset + (blockSize * i)),
                bytesLeft);
            bytesLeft = 0;
          }
        }
      }

      session.deregisterPageTrx(pRtx);
    } catch (TTException e) {
      throw new IOException(e.getMessage());
    }
    
    LOGGER.info("Giving back bytes: " + Arrays.toString(bytes).substring(0, 100));
  }

  /**
   * {@inheritDoc}
   */
  public void write(byte[] bytes, int bytesOffset, int length, long storageIndex)
      throws IOException {
    
    LOGGER.info("Starting to write with param: \nbytes = " + Arrays.toString(bytes).substring(0, 100) + "\nbytesOffset = " + bytesOffset + "\nlength = " + length + "\nstorageIndex = " + storageIndex);
    try {
      // Using the most recent revision
      if (bytesOffset + length > bytes.length)
        throw new IOException();

      int realIndex = (int) (storageIndex / blockSize);
      
      pRtx = session.beginPageWriteTransaction();
      IscsiWriteTrx iWtx = new IscsiWriteTrx((IPageWriteTrx) pRtx, session);
      getNodeByIndex(iWtx, realIndex);

      INode node = iWtx.getCurrentNode();

      byte[] val = ((ByteNode) node).getVal();
      ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

      // The result of the modulo operation is the offset of the node to write
      // to.
      int writeOffset = (int) (storageIndex % blockSize);

      // If the byte length to be written doesn't exceed the rest bytes in the
      // array
      // we can fully write the bytes with the given offset. Otherwise we have
      // to write
      // the remaining bytes to the following nodes!

      // Example (sizeInBlocks = 1, whereas one block has 64 bytes):
      // node1:[b1,b2,..,b64],node2:[b1,b2,..,b64],node3:[b1,b2,..,b64],node4:[b1,b2,..,b64]

      // We now want to write to storage index 100. First the node gets
      // calculated using
      // (storageIndex / blockSize) which is 100 / (64*1). 64
      // fits into 100
      // exactly once so the real index of the node is 1. (if we start to count
      // from zero!).

      // So starting with the second (index = 1) node, we have an offset of
      // (storageIndex % blockSize)
      // which in this case is 36. So the byte where we start to write is, 36 in
      // the second node.

      // There are 64-36 bytes = 28 bytes left. If our length would exceed this,
      // we'd have to jump to the next node. If there
      // is no next node we just create a new node until there are no bytes left
      // to be written.
      if (length <= blockSize - writeOffset) {
        input.readFully(val, bytesOffset, length);
        iWtx.setValue(val);

        return;
      } else {
        // In this case the size of the bytes to write exceeds the bytes
        // available in the first node.
        // As we know that our current node is going to be filled, we check how
        // many nodes have to be
        // filled aswell until we reach our last node that is probably not going
        // to be filled until the end.

        // We write blockSize - writeOffset in the first run which will then be
        // taken from the bytesLeft.

        int bytesLeft = length;

        // Writing exactly blockSize - writeOffset bytes into val.
        input.readFully(val, bytesOffset, (int) (blockSize - writeOffset));
        iWtx.setValue(val);
        bytesLeft -= blockSize - writeOffset;

        // To how many nodes a full write is going to do the job.
        int nodeCount = (int) (bytesLeft / blockSize);
        LOGGER.info("Writing to " + nodeCount + " nodes.");

        // After the nodes that have been written to, there is probably going to
        // be one node,
        // which we take a few bytes from.
        int lastNodeByteToWrite = (int) (bytesLeft % blockSize);

        // First we get the next node according to the index structure. Then we
        // fully write the bytes to it.
        // Afterwards the value is set into the backend.
        for (int i = 0; i < nodeCount; i++) {
          getNodeByIndex(iWtx, ((ByteNode) node).getIndex() + 1);
          node = iWtx.getCurrentNode();

          val = ((ByteNode) node).getVal();
          input.readFully(val, 0, (int) blockSize);
          iWtx.setValue(val);
        }

        // Repeating the process with different parameter for the reading.
        getNodeByIndex(iWtx, ((ByteNode) node).getIndex() + 1);
        node = iWtx.getCurrentNode();

        val = ((ByteNode) node).getVal();
        input.readFully(val, bytesOffset
            + (int) ((blockSize - writeOffset) + nodeCount
                * (sizeInBlocks * blockSize)), lastNodeByteToWrite);
        iWtx.setValue(val);

      }

      iWtx.commit();
      session.deregisterPageTrx(pRtx);
    } catch (TTException e) {
      throw new IOException(e.getMessage());
    }
  }

  private void getNodeByIndex(IIscsiReadTrx iRtx, int realIndex)
      throws IOException {

    INode node = iRtx.getCurrentNode();

    if (node == null) {
      throw new IOException(
          "It seems like no data has been written. Please do so and then try to read again.");
    }

    while (((ByteNode) node).getIndex() != realIndex
        && ((ByteNode) node).getNextNodeKey() != 0) {
      iRtx.nextNode();
      node = iRtx.getCurrentNode();
    }

    if (((ByteNode) node).getIndex() == realIndex) {
      return;
    } else {
      throw new IOException(
          "The index point you were seeking is not filled with data yet. Please check your bounds and try again.");
    }
  }

  private void getNodeByIndex(IIscsiWriteTrx iWtx, int realIndex)
      throws IOException {

    INode node = iWtx.getCurrentNode();

    if (node == null) {
      ByteNode newNode = new ByteNode(0,
          new byte[(int) (blockSize)]);
      newNode.setIndex(1);
      try {
        ((IIscsiWriteTrx) iWtx).insert(newNode);
        return;
      } catch (TTException e) {
        throw new IOException(
            "The creation of a new node was started and somehow didn't finish.");
      }
    }

    while (((ByteNode) node).getIndex() != realIndex
        && ((ByteNode) node).getNextNodeKey() != 0) {
      iWtx.nextNode();
      node = iWtx.getCurrentNode();
    }

    if (((ByteNode) node).getIndex() != realIndex
        && ((ByteNode) node).getNextNodeKey() != 0) {
      throw new IOException("The index structure seems to be damaged.");
    } else {
      ByteNode newNode = new ByteNode(
          ((IIscsiWriteTrx) iWtx).getMaxNodeKey() + 1,
          new byte[(int) (blockSize)]);
      newNode.setIndex(realIndex);
      try {
        ((IIscsiWriteTrx) iWtx).insert(newNode);
        iWtx.moveTo(iWtx.getMaxNodeKey());
      } catch (TTException e) {
        throw new IOException(
            "The creation of a new node was started and somehow didn't finish.");
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void close() throws IOException {

    try {
      storage.close();

      // A small hack, so the {@link IStorageModule} doesn't have to be altered
      // to
      // throw Exceptions in general.
    } catch (TTException e) {
      throw new IOException(e.getMessage());
    }
  }

}
