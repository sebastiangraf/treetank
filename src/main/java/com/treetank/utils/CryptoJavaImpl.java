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
 * $Id: JavaCompression.java 4358 2008-08-23 12:38:16Z kramis $
 */

package com.treetank.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CryptoJavaImpl implements ICrypto {

	private final Deflater mCompressor;

	private final Inflater mDecompressor;

	private final byte[] mTmp;

	private final ByteArrayOutputStream mOut;

	/**
	 * Initialize compressor.
	 */
	public CryptoJavaImpl() {
		mCompressor = new Deflater();
		mDecompressor = new Inflater();
		mTmp = new byte[IConstants.BUFFER_SIZE];
		mOut = new ByteArrayOutputStream();
	}

	/**
	 * Compress data.
	 * 
	 * @param data
	 *            data that should be compressed
	 * @return compressed data, null if failed
	 */
	public short crypt(final short length, final IByteBuffer buffer) {
		try {
			buffer.position(24);
			byte[] tmp = buffer.getArray(length - 24);
			mCompressor.reset();
			mOut.reset();
			mCompressor.setInput(tmp);
			mCompressor.finish();
			int count;
			while (!mCompressor.finished()) {
				count = mCompressor.deflate(mTmp);
				mOut.write(mTmp, 0, count);
			}
		} catch (Exception e) {
			return 0;
		}
		final byte[] result = mOut.toByteArray();
		final byte[] checksum = new byte[IConstants.CHECKSUM_SIZE];
		buffer.position(12);
		buffer.putArray(checksum);
		buffer.putArray(result);
		return (short) (buffer.position());
	}

	/**
	 * Decompress data.
	 * 
	 * @param data
	 *            data that should be decompressed
	 * @return Decompressed data, null if failed
	 */
	public short decrypt(final short length, final IByteBuffer buffer) {
		try {
			buffer.position(24);
			byte[] tmp = buffer.getArray(length - 24);
			mDecompressor.reset();
			mOut.reset();
			mDecompressor.setInput(tmp);
			int count;
			while (!mDecompressor.finished()) {
				count = mDecompressor.inflate(mTmp);
				mOut.write(mTmp, 0, count);
			}
		} catch (Exception e) {
			return 0;
		}
		final byte[] result = mOut.toByteArray();
		final byte[] checksum = new byte[IConstants.CHECKSUM_SIZE];
		buffer.position(12);
		buffer.putArray(checksum);
		buffer.putArray(result);
		return (short) (result.length + 24);
	}

}
