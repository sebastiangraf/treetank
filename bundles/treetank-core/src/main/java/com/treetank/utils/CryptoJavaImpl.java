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


package com.treetank.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.treetank.io.file.ByteBufferSinkAndSource;

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
     * @param mLength
     *            of the data to be compressed
     * @param mBuffer
     *            data that should be compressed
     * @return compressed data, null if failed
     */
    public int crypt(final int mLength, final ByteBufferSinkAndSource mBuffer) {
        try {
            mBuffer.position(24);
            final byte[] tmp = new byte[mLength - 24];
            mBuffer.get(tmp, 0, tmp.length);
            mCompressor.reset();
            mOut.reset();
            mCompressor.setInput(tmp);
            mCompressor.finish();
            int count;
            while (!mCompressor.finished()) {
                count = mCompressor.deflate(mTmp);
                mOut.write(mTmp, 0, count);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return 0;
        }
        final byte[] result = mOut.toByteArray();
        final byte[] checksum = new byte[IConstants.CHECKSUM_SIZE];
        mBuffer.position(12);
        for (final byte byteVal : checksum) {
            mBuffer.writeByte(byteVal);
        }
        for (final byte byteVal : result) {
            mBuffer.writeByte(byteVal);
        }
        return mBuffer.position();
    }

    /**
     * Decompress data.
     * 
     * @param mBuffer
     *            data that should be decompressed
     * @param mLength
     *            of the data to be decompressed
     * @return Decompressed data, null if failed
     */
    public int decrypt(final int mLength, final ByteBufferSinkAndSource mBuffer) {
        try {
            mBuffer.position(24);
            final byte[] tmp = new byte[mLength - 24];
            mBuffer.get(tmp, 0, tmp.length);
            mDecompressor.reset();
            mOut.reset();
            mDecompressor.setInput(tmp);
            int count;
            while (!mDecompressor.finished()) {
                count = mDecompressor.inflate(mTmp);
                mOut.write(mTmp, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        final byte[] result = mOut.toByteArray();
        final byte[] checksum = new byte[IConstants.CHECKSUM_SIZE];
        mBuffer.position(12);
        for (final byte byteVal : checksum) {
            mBuffer.writeByte(byteVal);
        }
        for (final byte byteVal : result) {
            mBuffer.writeByte(byteVal);
        }
        return result.length + 24;
    }

}
