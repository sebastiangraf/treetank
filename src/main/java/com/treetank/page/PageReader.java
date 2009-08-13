/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: PageReader.java 4467 2008-09-04 18:57:58Z kramis $
 */

package com.treetank.page;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.treetank.session.SessionConfiguration;
import com.treetank.utils.CryptoJavaImpl;
import com.treetank.utils.IConstants;
import com.treetank.utils.ICrypto;

/**
 * <h1>PageReader</h1>
 * 
 * <p>
 * Each thread working on a I[Read|Write]Transaction has one PageReader instance
 * to independently read from an TreeTank file.
 * </p>
 */
public final class PageReader {

	/** Random access mFile to work on. */
	private final RandomAccessFile mFile;

	/** Inflater to decompress. */
	private ICrypto mDecompressor;

	/** Temporary data buffer. */
	private ByteBuffer mBuffer;

	/**
	 * Constructor.
	 * 
	 * @param sessionConfiguration
	 *            Configuration of session we are bound to.
	 * @throws RuntimeException
	 *             if the class could not be instantiated.
	 */
	public PageReader(final SessionConfiguration sessionConfiguration) {

		try {

			mFile = new RandomAccessFile(sessionConfiguration.getFileName(),
					IConstants.READ_ONLY);

			mDecompressor = new CryptoJavaImpl();
			mBuffer = ByteBuffer.allocate(IConstants.BUFFER_SIZE);

		} catch (Exception e) {
			throw new RuntimeException("Could not create page reader: "
					+ e.getLocalizedMessage());
		}
	}

	/**
	 * Read page from storage.
	 * 
	 * @param pageReference
	 *            to read.
	 * @return Byte array reader to read bytes from.o
	 * @throws RuntimeException
	 *             if there was an error during reading.
	 */
	public final ByteBuffer read(
			final PageReference<? extends AbstractPage> pageReference) {

		if (!pageReference.isCommitted()) {
			throw new IllegalArgumentException("Page reference is invalid.");
		}

		try {

			// Prepare environment for read.
			final byte[] checksum = new byte[IConstants.CHECKSUM_SIZE];
			pageReference.getChecksum(checksum);
			final short inputLength = (short) (pageReference.getLength() + 24);
			mBuffer.position(12);
			for (final byte byteVal : checksum) {
				mBuffer.put(byteVal);
			}

			// Read page from file.
			final byte[] page = new byte[pageReference.getLength()];
			mFile.seek(pageReference.getStart());
			mFile.read(page);
			for (final byte byteVal : page) {
				mBuffer.put(byteVal);
			}

			// Perform crypto operations.
			final short outputLength = mDecompressor.decrypt(inputLength,
					mBuffer);
			if (outputLength == 0) {
				throw new Exception("Page decrypt error.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not read page " + pageReference
					+ " due to: " + e.getLocalizedMessage());
		}

		// Return reader required to instantiate and deserialize page.
		mBuffer.position(24);
		return mBuffer;

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

}
