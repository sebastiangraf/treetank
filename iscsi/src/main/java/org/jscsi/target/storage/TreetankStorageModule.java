/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jscsi.target.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.Session;
import org.treetank.access.Storage;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.INode;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.page.UberPage;

/**
 * <h1>TreetankStorageModule</h1>
 * 
 * <p>
 * This implementation is used to store data into treetank via an iscsi target.
 * </p>
 * 
 * @author Andreas Rain
 *
 */
public class TreetankStorageModule implements IStorageModule{

    private static final Logger LOGGER = LoggerFactory.getLogger(TreetankStorageModule.class);

    /**
     * The size of the medium in blocks.
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    private final long sizeInBlocks;

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
     *            blocksize for this module
     * @param conf
     *            the fully initialized {@link StorageConfiguration}      
     * @throws TTException 
     */
    public TreetankStorageModule(final long sizeInBlocks, final StorageConfiguration conf, final File file) throws TTException{
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
        try {
            // Using the most recent revision
            pRtx = session.beginPageReadTransaction(session.getMostRecentVersion());
            
            INode node = pRtx.getNode(storageIndex);
            
            ByteArrayOutputStream nodeBytes = new ByteArrayOutputStream(node.getByteRepresentation().length);
            nodeBytes.write(node.getByteRepresentation());
            
            ByteArrayInputStream reader = new ByteArrayInputStream(node.getByteRepresentation());
            reader.read(bytes, bytesOffset, length);
        } catch (TTException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {
        try {
            IPageWriteTrx pWtx = session.beginPageWriteTransaction();
            
            INode node = pWtx.getNode(storageIndex);
            
            if(node != null){
                ByteArrayOutputStream nodeBytes = new ByteArrayOutputStream(node.getByteRepresentation().length);
                nodeBytes.write(node.getByteRepresentation());
                nodeBytes.write(bytes, bytesOffset, length);
            }
            else{
                
            }

            pWtx.close();
            
        } catch (TTException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        try {
            storage.close();
            
        // A small hack, so the {@link IStorageModule} doesn't have to be altered to
        // throw Exceptions in general.
        } catch (TTException e) {
            throw new IOException(e.getMessage());
        }
    }

}
