/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.io.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


import org.slf4j.LoggerFactory;
import org.treetank.access.SessionConfiguration;
import org.treetank.exception.TTIOException;
import org.treetank.io.IWriter;
import org.treetank.page.AbsPage;
import org.treetank.page.PagePersistenter;
import org.treetank.page.PageReference;
import org.treetank.utils.CryptoJavaImpl;
import org.treetank.utils.IConstants;
import org.treetank.utils.ICrypto;
import org.treetank.utils.LogWrapper;

/**
 * File Writer for providing read/write access for file as a treetank backend.
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class FileWriter implements IWriter {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(FileWriter.class));

    /** Random access mFile to work on. */
    private transient final RandomAccessFile mFile;

    /** Compressor to compress the page. */
    private transient final ICrypto mCompressor;

    /** Temporary data buffer. */
    private final transient ByteBufferSinkAndSource mBuffer;

    /** Reader instance for this writer. */
    private transient final FileReader reader;

    /**
     * Constructor.
     * 
     * @param paramConf
     *            the path to the storage
     * @param mConcreteStorage
     *            the Concrete Storage
     * @throws TTIOException
     *             if FileWriter IO error
     */
    public FileWriter(final SessionConfiguration paramConf, final File mConcreteStorage)
        throws TTIOException {
        try {
            mFile = new RandomAccessFile(mConcreteStorage, IConstants.READ_WRITE);
        } catch (final FileNotFoundException fileExc) {
            LOGWRAPPER.error(fileExc);
            throw new TTIOException(fileExc);
        }

        mCompressor = new CryptoJavaImpl();
        mBuffer = new ByteBufferSinkAndSource();

        reader = new FileReader(paramConf, mConcreteStorage);

    }

    /**
     * Write page contained in page reference to storage.
     * 
     * @param pageReference
     *            Page reference to write.
     * @throws TTIOException
     *             due to errors during writing.
     */
    public void write(final PageReference pageReference) throws TTIOException {

        // Serialise page.
        mBuffer.position(24);
        final AbsPage page = pageReference.getPage();
        PagePersistenter.serializePage(mBuffer, page);
        final int inputLength = mBuffer.position();

        // Perform crypto operations.
        mBuffer.position(0);
        final int outputLength = mCompressor.crypt(inputLength, mBuffer);
        if (outputLength == 0) {
            throw new TTIOException("Page crypt error.");
        }

        // Write page to file.
        mBuffer.position(12);

        final byte[] checksum = new byte[IConstants.CHECKSUM_SIZE];
        for (int i = 0; i < checksum.length; i++) {
            checksum[i] = mBuffer.readByte();
        }
        try {
            // Getting actual offset and appending to the end of the current
            // file
            final long fileSize = mFile.length();
            final long offset = fileSize == 0 ? IConstants.BEACON_START + IConstants.BEACON_LENGTH : fileSize;
            mFile.seek(offset);
            final byte[] tmp = new byte[outputLength - 24];
            mBuffer.get(tmp, 0, outputLength - 24);
            mFile.write(tmp);
            final FileKey key = new FileKey(offset, outputLength - 24);

            // Remember page coordinates.
            pageReference.setKey(key);
            pageReference.setChecksum(checksum);
        } catch (final IOException paramExc) {
            LOGWRAPPER.error(paramExc);
            throw new TTIOException(paramExc);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void close() throws TTIOException {
        try {
            if (mFile != null) {
                reader.close();
                mFile.close();
            }
        } catch (final IOException e) {
            LOGWRAPPER.error(e);
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
     */
    public void writeFirstReference(final PageReference pageReference) throws TTIOException {
        final byte[] tmp = new byte[IConstants.CHECKSUM_SIZE];
        try {
            // Check to writer ensure writing after the Beacon_Start
            if (mFile.getFilePointer() < IConstants.BEACON_START + IConstants.BEACON_LENGTH) {
                mFile.setLength(IConstants.BEACON_START + IConstants.BEACON_LENGTH);
            }

            write(pageReference);

            mFile.seek(IConstants.BEACON_START);
            final FileKey key = (FileKey)pageReference.getKey();
            mFile.writeLong(key.getOffset());
            mFile.writeInt(key.getLength());
            pageReference.getChecksum(tmp);
            mFile.write(tmp);
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AbsPage read(final PageReference pageReference) throws TTIOException {
        return reader.read(pageReference);
    }

    /**
     * {@inheritDoc}
     */
    public PageReference readFirstReference() throws TTIOException {
        return reader.readFirstReference();
    }

}
