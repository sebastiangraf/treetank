/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.utils;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


import org.slf4j.LoggerFactory;
import org.treetank.io.file.ByteBufferSinkAndSource;

public class CryptoJavaImpl implements ICrypto {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(CryptoJavaImpl.class));

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
            LOGWRAPPER.error(e);
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
            LOGWRAPPER.error(e);
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
