package com.treetank.io.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.treetank.io.IWriter;
import com.treetank.io.StorageProperties;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;
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
public class FileWriter implements IWriter {

	/** Random access mFile to work on. */
	private final RandomAccessFile mFile;

	/** Compressor to compress the page. */
	private final ICrypto mCompressor;

	/** Temporary data buffer. */
	private ByteBufferSinkAndSource mBuffer;

	private final FileReader reader;

	public FileWriter(final SessionConfiguration paramConf) {
		try {
			final File toRead = new File(paramConf.getAbsolutePath()
					+ File.separatorChar + "tt.tnk");
			mFile = new RandomAccessFile(toRead, IConstants.READ_WRITE);

			mCompressor = new CryptoJavaImpl();
			mBuffer = new ByteBufferSinkAndSource();

			reader = new FileReader(paramConf);

		} catch (Exception e) {
			throw new RuntimeException("Could not create page writer: "
					+ e.getLocalizedMessage());
		}
	}

	/**
	 * Write page contained in page reference to storage.
	 * 
	 * @param pageReference
	 *            Page reference to write.
	 * @throws RuntimeException
	 *             due to errors during writing.
	 */
	public final void write(
			final PageReference<? extends AbstractPage> pageReference) {

		try {

			// Serialise page.
			mBuffer.position(24);
			final AbstractPage page = pageReference.getPage();
			page.serialize(mBuffer);
			final short inputLength = (short) mBuffer.position();

			// Perform crypto operations.
			mBuffer.position(0);
			final short outputLength = mCompressor.crypt(inputLength, mBuffer);
			if (outputLength == 0) {
				throw new Exception("Page crypt error.");
			}

			// Write page to file.
			mBuffer.position(12);

			final byte[] checksum = new byte[IConstants.CHECKSUM_SIZE];
			for (int i = 0; i < checksum.length; i++) {
				checksum[i] = mBuffer.get();
			}

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

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not write page " + pageReference
					+ " due to: " + e.getLocalizedMessage());
		}

	}

	/**
	 * Properly close file handle.
	 */
	public final void close() {
		try {
			if (mFile != null) {
				mFile.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
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

	@Override
	public void initializingStorage(final StorageProperties props) {
		try {
			mFile.setLength(IConstants.BEACON_START + IConstants.BEACON_LENGTH);
			mFile.writeLong(props.getVersionMajor());
			mFile.writeLong(props.getVersionMinor());
			mFile.writeBoolean(props.getChecksummed());
			mFile.writeBoolean(props.getEncrypted());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeBeacon(PageReference<UberPage> pageReference) {
		byte[] tmp = new byte[IConstants.CHECKSUM_SIZE];
		try {
			mFile.seek(IConstants.BEACON_START);
			final FileKey key = (FileKey) pageReference.getKey();
			mFile.writeLong(key.getOffset());
			mFile.writeInt(key.getLength());
			pageReference.getChecksum(tmp);
			mFile.write(tmp);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public StorageProperties getProps() {
		return reader.getProps();
	}

	@Override
	public AbstractPage read(PageReference<? extends AbstractPage> pageReference) {
		return reader.read(pageReference);
	}

	@Override
	public PageReference<?> readFirstReference() {

		return reader.readFirstReference();
	}

}
