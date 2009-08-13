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
 * $Id: PageWriter.java 4467 2008-09-04 18:57:58Z kramis $
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
 * <h1>PageWriter</h1>
 * 
 * <p>
 * Each commit of the ISession creates one PageWriter instance to write to the
 * TreeTank file.
 * </p>
 */
public final class PageWriter {

	/** Random access mFile to work on. */
	private final RandomAccessFile mFile;

	/** Compressor to compress the page. */
	private ICrypto mCompressor;

	/** Temporary data buffer. */
	private ByteBuffer mBuffer;

	/**
	 * Constructor.
	 * 
	 * @param sessionConfiguration
	 *            Configuration of session we are bound to.
	 * @throws RuntimeException
	 *             if the PageWriter could not be instantiated.
	 */
	public PageWriter(final SessionConfiguration sessionConfiguration) {

		try {

			mFile = new RandomAccessFile(
					sessionConfiguration.getFileName(),
					IConstants.READ_WRITE);

			mCompressor = new CryptoJavaImpl();
			mBuffer = ByteBuffer.allocate(IConstants.BUFFER_SIZE);

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
			pageReference.getPage().serialize(mBuffer);
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

			final long fileSize = mFile.length();
			mFile.seek(fileSize);
			
			final byte[] tmp = new byte[outputLength - 24];
			mBuffer.get(tmp, 0, outputLength - 24);
			mFile.write(tmp);

			// Remember page coordinates.
			pageReference.setStart(fileSize);
			pageReference.setLength(outputLength - 24);
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

}
