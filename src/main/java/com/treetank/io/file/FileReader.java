package com.treetank.io.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.treetank.io.IReader;
import com.treetank.io.StorageProperties;
import com.treetank.io.TreetankIOException;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageFactory;
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
public class FileReader implements IReader {

	/** Random access mFile to work on. */
	private transient final RandomAccessFile mFile;

	/** Inflater to decompress. */
	private transient final ICrypto mDecompressor;

	/** Temporary data buffer. */
	private transient ByteBufferSinkAndSource mBuffer;

	/**
	 * Constructor.
	 * 
	 * @param sessionConfiguration
	 *            Configuration of session we are bound to.
	 * @throws TreetankIOException
	 *             if something bad happens
	 */
	public FileReader(final SessionConfiguration paramConf)
			throws TreetankIOException {

		try {
			final File toWrite = new File(paramConf.getAbsolutePath()
					+ File.separatorChar + "tt.tnk");
			toWrite.createNewFile();
			mFile = new RandomAccessFile(toWrite, IConstants.READ_ONLY);

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
	public final AbstractPage read(
			final PageReference<? extends AbstractPage> pageReference)
			throws TreetankIOException {

		if (!pageReference.isCommitted()) {
			throw new IllegalArgumentException("Page reference is invalid.");
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
		return PageFactory.createPage(mBuffer);

	}

	@Override
	public PageReference<?> readFirstReference() throws TreetankIOException {
		final PageReference<UberPage> uberPageReference = new PageReference<UberPage>();
		try {
			final byte[] tmp = new byte[IConstants.CHECKSUM_SIZE];

			// Read primary beacon.
			mFile.seek(IConstants.BEACON_START);

			final FileKey key = new FileKey(mFile.readLong(), mFile.readInt());

			uberPageReference.setKey(key);
			mFile.read(tmp);
			uberPageReference.setChecksum(tmp);

			return uberPageReference;
		} catch (final IOException exc) {
			throw new TreetankIOException(exc);
		}
	}

	@Override
	public StorageProperties getProps() throws TreetankIOException {
		try {

			mFile.seek(0L);
			final long localVersionMajor = mFile.readLong();
			final long localVersionMinor = mFile.readLong();
			final boolean localChecksummed = mFile.readBoolean();
			final boolean localEncrypted = mFile.readBoolean();

			return new StorageProperties(localVersionMajor, localVersionMinor,
					localChecksummed, localEncrypted);
		} catch (final IOException ioe) {
			throw new TreetankIOException(ioe);
		}

	}

	@Override
	public void close() throws TreetankIOException {
		try {
			mFile.close();
		} catch (final IOException exc) {
			throw new TreetankIOException(exc);

		}
	}
}
