package com.treetank.io.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.treetank.exception.TreetankIOException;
import com.treetank.io.IReader;
import com.treetank.io.StorageProperties;
import com.treetank.page.AbstractPage;
import com.treetank.page.PagePersistenter;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.CryptoJavaImpl;
import com.treetank.utils.IConstants;
import com.treetank.utils.ICrypto;

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
     * @throws TreetankIOException
     *             if something bad happens
     */
    public FileReader(final SessionConfiguration paramConf,
            final File concreteStorage) throws TreetankIOException {

        try {
            if (!concreteStorage.exists()) {
                concreteStorage.createNewFile();
            }

            mFile = new RandomAccessFile(concreteStorage, IConstants.READ_ONLY);

            mDecompressor = new CryptoJavaImpl();
            mBuffer = new ByteBufferSinkAndSource();
        } catch (final IOException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * Read page from storage.
     * 
     * @param pageReference
     *            to read.
     * @return Byte array reader to read bytes from.o
     * @throws TreetankIOException
     *             if there was an error during reading.
     */
    public AbstractPage read(final PageReference pageReference)
            throws TreetankIOException {

        if (!pageReference.isCommitted()) {
            return null;
        }

        try {
            final FileKey fileKey = (FileKey) pageReference.getKey();

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
            final int outputLength = mDecompressor
                    .decrypt(inputLength, mBuffer);
            if (outputLength == 0) {
                throw new TreetankIOException("Page decrypt error.");
            }

        } catch (final IOException exc) {
            throw new TreetankIOException(exc);
        }

        // Return reader required to instantiate and deserialize page.
        mBuffer.position(24);
        return PagePersistenter.createPage(mBuffer);

    }

    public PageReference readFirstReference() throws TreetankIOException {
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
            if (mFile.getFilePointer() < IConstants.BEACON_START
                    + IConstants.BEACON_LENGTH) {
                mFile.setLength(IConstants.BEACON_START
                        + IConstants.BEACON_LENGTH);
            }

            final UberPage page = (UberPage) read(uberPageReference);
            uberPageReference.setPage(page);

            return uberPageReference;
        } catch (final IOException exc) {
            throw new TreetankIOException(exc);
        }
    }

    public StorageProperties getProps() throws TreetankIOException {
        try {

            mFile.seek(0L);
            final long localVersionMajor = mFile.readLong();
            final long localVersionMinor = mFile.readLong();

            return new StorageProperties(localVersionMajor, localVersionMinor);
        } catch (final IOException ioe) {
            throw new TreetankIOException(ioe);
        }

    }

    public void close() throws TreetankIOException {
        try {
            mFile.close();
        } catch (final IOException exc) {
            throw new TreetankIOException(exc);

        }
    }

}
