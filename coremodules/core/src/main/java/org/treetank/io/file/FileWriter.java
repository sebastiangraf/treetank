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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IWriter;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.IPage;
import org.treetank.page.PageFactory;
import org.treetank.page.PageReference;

/**
 * File Writer for providing read/write access for file as a treetank backend.
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class FileWriter implements IWriter {

    /** Random access mFile to work on. */
    private transient final RandomAccessFile mFile;

    /** Reader instance for this writer. */
    private transient final FileReader mReader;

    /**
     * Constructor.
     * 
     * 
     * @param pFile
     *            the Concrete Storage
     * @param pFac
     *            the factory to build nodes.
     * @param pByteHandler
     *            handling the bytes.
     * @throws TTIOException
     *             if FileWriter IO error
     */
    public FileWriter(File pFile, PageFactory pFac, IByteHandlerPipeline pByteHandler) throws TTException {
        try {
            mFile = new RandomAccessFile(pFile, "rw");
        } catch (final FileNotFoundException fileExc) {
            throw new TTIOException(fileExc);
        }

        mReader = new FileReader(pFile, pFac, pByteHandler);

    }

    /**
     * Write page contained in page reference to storage.
     * 
     * @param pageReference
     *            Page reference to write.
     * @throws TTIOException
     *             due to errors during writing.
     * @throws TTByteHandleException
     */
    public long write(final PageReference pageReference) throws TTIOException, TTByteHandleException {

        final IPage page = pageReference.getPage();
        final byte[] rawPage = page.getByteRepresentation();

        // Perform crypto operations.
        final byte[] decryptedPage = mReader.mByteHandler.serialize(rawPage);

        final byte[] writtenPage = new byte[decryptedPage.length + FileReader.OTHER_BEACON];
        ByteBuffer buffer = ByteBuffer.allocate(writtenPage.length);
        buffer.putInt(decryptedPage.length);
        buffer.put(decryptedPage);
        buffer.position(0);
        buffer.get(writtenPage, 0, writtenPage.length);

        try {
            // Getting actual offset and appending to the end of the current
            // file
            final long fileSize = mFile.length();
            final long offset = fileSize == 0 ? FileReader.FIRST_BEACON : fileSize;
            mFile.seek(offset);
            mFile.write(writtenPage);
            // Remember page coordinates.
            pageReference.setKey(offset);
            return offset;
        } catch (final IOException paramExc) {
            throw new TTIOException(paramExc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws TTIOException {
        try {
            if (mFile != null) {
                mReader.close();
                mFile.close();
            }
        } catch (final IOException e) {
            throw new TTIOException(e);
        }
    }

    /**
     * Close file handle in case it is not properly closed by the application.
     * 
     * @throws Throwable
     *             if the finalization of the superclass does not work.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTByteHandleException
     */
    public void writeFirstReference(final PageReference pageReference) throws TTIOException,
        TTByteHandleException {
        try {
            write(pageReference);
            mFile.seek(0);
            mFile.writeLong(pageReference.getKey());
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IPage read(final long pKey) throws TTIOException {
        return mReader.read(pKey);
    }

    /**
     * {@inheritDoc}
     */
    public PageReference readFirstReference() throws TTIOException {
        return mReader.readFirstReference();
    }

}
