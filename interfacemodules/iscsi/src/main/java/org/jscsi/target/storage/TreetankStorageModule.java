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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.IscsiReadTrx;
import org.treetank.access.IscsiWriteTrx;
import org.treetank.access.PageReadTrx;
import org.treetank.access.Storage;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IIscsiReadTrx;
import org.treetank.api.INode;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.iscsi.node.ByteNode;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
   * The size of each block. If the block count is set to 512 each node has
   * 64*512 bytes in it.
   */
  private final int blockSize = 64;

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
  public TreetankStorageModule(final long sizeInBlocks,
      final StorageConfiguration conf, final File file) throws TTException {

    this.sizeInBlocks = sizeInBlocks;
    this.conf = conf;

    // Creating and opening the storage.
    // Making it ready for usage.
    Storage.createStorage(conf);
    storage = Storage.openStorage(file);
    session = storage.getSession(new SessionConfiguration("TMP", null));
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

    try {
      // Using the most recent revision
      if (bytesOffset + length > bytes.length)
        throw new IOException();

      int realIndex = (int) (storageIndex / (sizeInBlocks * blockSize));

      pRtx = session.beginPageReadTransaction(session.getMostRecentVersion());
      IscsiReadTrx iRtx = new IscsiReadTrx(pRtx);
      getNodeByIndex(iRtx, realIndex);

      INode node = iRtx.getCurrentNode();

      byte[] val = ((ByteNode) node).getVal();
      ByteArrayDataInput input = ByteStreams.newDataInput(val);

      if (length <= (sizeInBlocks * blockSize)) {
        input.readFully(bytes, bytesOffset, length);
      } else {
        int i = 0;
        int bytesLeft = length;

        while (bytesLeft > 0) {
          if (bytesLeft >= (sizeInBlocks * blockSize)) {
            input.readFully(bytes,
                (int) (bytesOffset + ((sizeInBlocks * blockSize) * i)),
                (int) (sizeInBlocks * blockSize));
            bytesLeft -= (sizeInBlocks * blockSize);
            i++;

            getNodeByIndex(iRtx, realIndex + 1);
            input = ByteStreams.newDataInput(((ByteNode) iRtx.getCurrentNode())
                .getVal());
          } else {
            input.readFully(bytes,
                (int) (bytesOffset + ((sizeInBlocks * blockSize) * i)),
                bytesLeft);
            bytesLeft = 0;
          }
        }
      }
    } catch (TTException e) {
      throw new IOException(e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void write(byte[] bytes, int bytesOffset, int length, long storageIndex)
      throws IOException {

    try {
      // Using the most recent revision
      if (bytesOffset + length > bytes.length)
        throw new IOException();

      int realIndex = (int) (storageIndex / (sizeInBlocks * blockSize));

      pRtx = session.beginPageWriteTransaction();
      IscsiWriteTrx iWtx = new IscsiWriteTrx((IPageWriteTrx) pRtx, session);
      getNodeByIndex((IIscsiReadTrx) iWtx, realIndex);

      INode node = iWtx.getCurrentNode();

      byte[] val = ((ByteNode) node).getVal();
      ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

      // The result of the modulo operation is the offset of the node to write
      // to.
      int writeOffset = (int) (storageIndex % (sizeInBlocks * blockSize));

      // If the byte length to be written doesn't exceed the rest bytes in the
      // array
      // we can fully write the bytes with the given offset. Otherwise we have
      // to write
      // the remaining bytes to the following nodes!

      // Example (sizeInBlocks = 1, whereas one block has 64 bytes):
      // node1:[b1,b2,..,b64],node2:[b1,b2,..,b64],node3:[b1,b2,..,b64],node4:[b1,b2,..,b64]

      // We now want to write to storage index 100. First the node gets
      // calculated using
      // (storageIndex / (sizeInBlocks * blockSize)) which is 100 / (64*1). 64
      // fits into 100
      // exactly once so the real index of the node is 1. (if we start to count
      // from zero!).

      // So starting with the second (index = 1) node, we have an offset of
      // (storageIndex % (sizeInBlocks * blockSize))
      // which in this case is 36. So the byte where we start to write is, 36 in
      // the second node.

      // There are 64-36 bytes = 28 bytes left. If our length would exceed this,
      // we'd have to jump to the next node. If there
      // is no next node we just create a new node until there are no bytes left
      // to be written.
      if (length <= (sizeInBlocks * blockSize) - writeOffset) {
        input.readFully(val, writeOffset, length);
        iWtx.setValue(val);

        return;
      } else {
        int i = 0;
        int bytesLeft = length;

        while (bytesLeft > 0) {
          if (bytesLeft >= (sizeInBlocks * blockSize)) {
            input.readFully(bytes,
                (int) (bytesOffset + ((sizeInBlocks * blockSize) * i)),
                (int) (sizeInBlocks * blockSize));
            bytesLeft -= (sizeInBlocks * blockSize);
            i++;

            getNodeByIndex(iWtx, realIndex + 1);
            input = ByteStreams.newDataInput(((ByteNode) iWtx.getCurrentNode())
                .getVal());
          } else {
            input.readFully(bytes,
                (int) (bytesOffset + ((sizeInBlocks * blockSize) * i)),
                bytesLeft);
            bytesLeft = 0;
          }
        }
      }
    } catch (TTException e) {
      throw new IOException(e.getMessage());
    }
  }

  private void getNodeByIndex(IIscsiReadTrx iRtx, int realIndex)
      throws IOException {

    INode node = iRtx.getCurrentNode();

    while (((ByteNode) node).getIndex() != realIndex
        && ((ByteNode) node).getNextNodeKey() != 0) {
      iRtx.nextNode();
      node = iRtx.getCurrentNode();
    }

    if (((ByteNode) node).getIndex() != realIndex && ((ByteNode) node).getNextNodeKey() != 0) {
      throw new IOException();
    }
    else{
      // TODO tell the calling method that a new node has to be created!.
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
