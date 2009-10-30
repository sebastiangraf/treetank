package com.treetank.io.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.treetank.exception.TreetankIOException;
import com.treetank.io.IWriter;
import com.treetank.io.StorageProperties;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.CryptoJavaImpl;
import com.treetank.utils.IConstants;
import com.treetank.utils.ICrypto;

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

    /** Compressor to compress the page. */
    private transient final ICrypto mCompressor;

    /** Temporary data buffer. */
    private final transient ByteBufferSinkAndSource mBuffer;

    /** Reader instance for this writer */
    private transient final FileReader reader;

    /**
     * Constructor.
     * 
     * @param paramConf
     *            the path to the storage
     */
    public FileWriter(final SessionConfiguration paramConf)
            throws TreetankIOException {
        final File toRead = new File(paramConf.getAbsolutePath()
                + File.separatorChar + "tt.tnk");
        try {
            mFile = new RandomAccessFile(toRead, IConstants.READ_WRITE);
        } catch (final FileNotFoundException fileExc) {
            throw new TreetankIOException(fileExc);
        }

        mCompressor = new CryptoJavaImpl();
        mBuffer = new ByteBufferSinkAndSource();

        reader = new FileReader(paramConf);

    }

    /**
     * Write page contained in page reference to storage.
     * 
     * @param pageReference
     *            Page reference to write.
     * @throws RuntimeException
     *             due to errors during writing.
     */
    public void write(final PageReference pageReference)
            throws TreetankIOException {

        // Serialise page.
        mBuffer.position(24);
        final AbstractPage page = pageReference.getPage();
        page.serialize(mBuffer);
        final int inputLength = mBuffer.position();

        // Perform crypto operations.
        mBuffer.position(0);
        final int outputLength = mCompressor.crypt(inputLength, mBuffer);
        if (outputLength == 0) {
            throw new TreetankIOException("Page crypt error.");
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
            mFile.seek(fileSize);

            final byte[] tmp = new byte[outputLength - 24];
            mBuffer.get(tmp, 0, outputLength - 24);
            mFile.write(tmp);
            final FileKey key = new FileKey(fileSize, outputLength - 24);

            // Remember page coordinates.
            pageReference.setKey(key);
            pageReference.setChecksum(checksum);
        } catch (final IOException paramExc) {
            throw new TreetankIOException(paramExc);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void close() throws TreetankIOException {
        try {
            if (mFile != null) {
                mFile.close();
            }
        } catch (final IOException e) {
            throw new TreetankIOException(e);
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
    public void setProps(final StorageProperties props)
            throws TreetankIOException {
        try {
            mFile.setLength(IConstants.BEACON_START + IConstants.BEACON_LENGTH);
            mFile.writeLong(props.getVersionMajor());
            mFile.writeLong(props.getVersionMinor());
            mFile.writeBoolean(props.isChecksummed());
            mFile.writeBoolean(props.isEncrypted());

        } catch (final IOException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeFirstReference(final PageReference pageReference)
            throws TreetankIOException {
        final byte[] tmp = new byte[IConstants.CHECKSUM_SIZE];
        try {

            // Check to writer ensure writing after the Beacon_Start
            if (mFile.getFilePointer() < IConstants.BEACON_START
                    + IConstants.BEACON_LENGTH) {
                mFile.setLength(IConstants.BEACON_START
                        + IConstants.BEACON_LENGTH);
            }

            write(pageReference);

            mFile.seek(IConstants.BEACON_START);
            final FileKey key = (FileKey) pageReference.getKey();
            mFile.writeLong(key.getOffset());
            mFile.writeInt(key.getLength());
            pageReference.getChecksum(tmp);
            mFile.write(tmp);
        } catch (final IOException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public StorageProperties getProps() throws TreetankIOException {
        return reader.getProps();
    }

    /**
     * {@inheritDoc}
     */
    public AbstractPage read(final PageReference pageReference)
            throws TreetankIOException {
        return reader.read(pageReference);
    }

    /**
     * {@inheritDoc}
     */
    public PageReference readFirstReference() throws TreetankIOException {
        return reader.readFirstReference();
    }

}
