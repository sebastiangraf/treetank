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

package com.treetank.io.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TTIOException;
import com.treetank.io.IReader;
import com.treetank.page.AbstractPage;
import com.treetank.page.PagePersistenter;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;
import com.treetank.utils.CryptoJavaImpl;
import com.treetank.utils.IConstants;
import com.treetank.utils.ICrypto;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * File Reader. Used for ReadTransaction to provide read only access on a
 * RandomAccessFile.
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz.
 * 
 * 
 */
public final class FileReader implements IReader {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(FileReader.class));

    /** Random access mFile to work on. */
    private transient final RandomAccessFile mFile;

    /** Inflater to decompress. */
    private transient final ICrypto mDecompressor;

    /** Temporary data buffer. */
    private transient ByteBufferSinkAndSource mBuffer;

    /**
     * Constructor.
     * 
     * @param paramConf
     *            Configuration of session we are bound to.
     * @throws TTIOException
     *             if something bad happens
     */
    public FileReader(final SessionConfiguration paramConf, final File mConcreteStorage)
        throws TTIOException {

        try {
            if (!mConcreteStorage.exists()) {
                mConcreteStorage.createNewFile();
            }

            mFile = new RandomAccessFile(mConcreteStorage, IConstants.READ_ONLY);

            mDecompressor = new CryptoJavaImpl();
            mBuffer = new ByteBufferSinkAndSource();
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
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
    public AbstractPage read(final PageReference pageReference) throws TTIOException {

        if (!pageReference.isCommitted()) {
            return null;
        }

        try {
            final FileKey fileKey = (FileKey)pageReference.getKey();

            // Prepare environment for read.
            final byte[] checksum = new byte[IConstants.CHECKSUM_SIZE];
            pageReference.getChecksum(checksum);
            final int inputLength = fileKey.getLength() + 24;
            mBuffer.position(12);
            for (final byte byteVal : checksum) {
                mBuffer.writeByte(byteVal);
            }

            // Read page from file.
            final byte[] page = new byte[fileKey.getLength()];
            mFile.seek(fileKey.getOffset());
            mFile.read(page);
            for (final byte byteVal : page) {
                mBuffer.writeByte(byteVal);
            }

            // Perform crypto operations.
            final int outputLength = mDecompressor.decrypt(inputLength, mBuffer);
            if (outputLength == 0) {
                throw new TTIOException("Page decrypt error.");
            }

        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }

        // Return reader required to instantiate and deserialize page.
        mBuffer.position(24);
        return PagePersistenter.createPage(mBuffer);

    }

    public PageReference readFirstReference() throws TTIOException {
        final PageReference uberPageReference = new PageReference();
        try {
            final byte[] tmp = new byte[IConstants.CHECKSUM_SIZE];

            // Read primary beacon.
            mFile.seek(IConstants.BEACON_START);

            final FileKey key = new FileKey(mFile.readLong(), mFile.readInt());

            uberPageReference.setKey(key);
            mFile.read(tmp);
            uberPageReference.setChecksum(tmp);

            // Check to writer ensure writing after the Beacon_Start
            if (mFile.getFilePointer() < IConstants.BEACON_START + IConstants.BEACON_LENGTH) {
                mFile.setLength(IConstants.BEACON_START + IConstants.BEACON_LENGTH);
            }

            final UberPage page = (UberPage)read(uberPageReference);
            uberPageReference.setPage(page);

            return uberPageReference;
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }
    }

    public void close() throws TTIOException {
        try {
            mFile.close();
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);

        }
    }

}
