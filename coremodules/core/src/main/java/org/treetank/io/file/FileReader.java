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

package org.treetank.io.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import org.treetank.exception.TTIOException;
import org.treetank.io.IReader;
import org.treetank.node.NodeFactory;
import org.treetank.page.IPage;
import org.treetank.page.PageFactory;
import org.treetank.page.PageReference;
import org.treetank.page.UberPage;

/**
 * File Reader. Used for NodeReadTrx to provide read only access on a
 * RandomAccessFile.
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz.
 * 
 * 
 */
public final class FileReader implements IReader {

    // TODO Care about this one via injection
    private final PageFactory mFac = PageFactory.getInstance(NodeFactory
            .getInstance());

    /** Beacon of first references. */
    protected final static int FIRST_BEACON = 12;

    /** Beacon of the other references. */
    protected final static int OTHER_BEACON = 4;

    /** Random access mFile to work on. */
    private transient final RandomAccessFile mFile;

    /** Inflater to decompress. */
    private transient final CryptoJavaImpl mDecompressor;

    /**
     * Constructor.
     * 
     * @throws TTIOException
     *             if something bad happens
     */
    public FileReader(final File mConcreteStorage) throws TTIOException {

        try {
            if (!mConcreteStorage.exists()) {
                mConcreteStorage.getParentFile().mkdirs();
                mConcreteStorage.createNewFile();
            }

            mFile = new RandomAccessFile(mConcreteStorage, "r");

            mDecompressor = new CryptoJavaImpl();
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Read page from storage.
     * 
     * @param pageReference
     *            to read.
     * @return Byte array reader to read bytes from.o
     * @throws TTIOException
     *             if there was an error during reading.
     */
    public IPage read(final long pKey) throws TTIOException {

        final ByteBuffer mBuffer = ByteBuffer.allocate(FileFactory.BUFFERSIZE);

        try {

            // Prepare environment for read.
            mBuffer.position(OTHER_BEACON);

            // Read page from file.
            mFile.seek(pKey);
            final int dataLength = mFile.readInt();
            final byte[] page = new byte[dataLength - 4];

            mFile.read(page);
            mBuffer.put(page);

            // Perform crypto operations.
            int decryptedLength = mDecompressor.decrypt(dataLength, mBuffer);

            // Return reader required to instantiate and deserialize page.
            byte[] returnVal = new byte[decryptedLength - OTHER_BEACON];
            mBuffer.position(OTHER_BEACON);
            mBuffer.get(returnVal);

            return mFac.deserializePage(returnVal);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        } catch (final DataFormatException exc) {
            throw new TTIOException("Page decrypt error.");
        }

    }

    public PageReference readFirstReference() throws TTIOException {
        final PageReference uberPageReference = new PageReference();
        try {
            // Read primary beacon.
            mFile.seek(0);
            uberPageReference.setKey(mFile.readLong());
            final UberPage page = (UberPage) read(uberPageReference.getKey());
            uberPageReference.setPage(page);
            return uberPageReference;
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    public void close() throws TTIOException {
        try {
            mFile.close();
        } catch (final IOException exc) {
            throw new TTIOException(exc);

        }
    }

}
