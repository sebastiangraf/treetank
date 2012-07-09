/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

package org.treetank.io.file;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CryptoJavaImpl {

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
        mTmp = new byte[FileFactory.BUFFERSIZE];
        mOut = new ByteArrayOutputStream();
    }

    /**
     * Compress data.
     * 
     * @param pLength
     *            of the data to be compressed
     * @param pData
     *            data that should be compressed
     * @return compressed data, null if failed
     */
    public byte[] crypt(final byte[] pData) {

        mCompressor.reset();
        mOut.reset();
        mCompressor.setInput(pData);
        mCompressor.finish();
        int count;
        while (!mCompressor.finished()) {
            count = mCompressor.deflate(mTmp);
            mOut.write(mTmp, 0, count);
        }
        final byte[] result = mOut.toByteArray();
        return result;
    }

    /**
     * Decompress data.
     * 
     * @param pBuffer
     *            data that should be decompressed
     * @param pLength
     *            of the data to be decompressed
     * @return Decompressed data, null if failed
     * @throws DataFormatException
     */
    public byte[] decrypt(byte[] pBuffer) throws DataFormatException {
        mDecompressor.reset();
        mOut.reset();
        mDecompressor.setInput(pBuffer);
        int count;
        while (!mDecompressor.finished()) {
            count = mDecompressor.inflate(mTmp);
            mOut.write(mTmp, 0, count);
        }
        final byte[] result = mOut.toByteArray();
        return result;
    }

    // /**
    // * Checking of length is sufficient, if not, increase the bytebuffer.
    // *
    // * @param mLength
    // * for the bytes which have to be inserted
    // */
    // private ByteBuffer checkAndIncrease(final int mLength, ByteBuffer mBuffer) {
    // if (mBuffer.position() + mLength > mBuffer.capacity()) {
    // final int position = mBuffer.position();
    // mBuffer.position(0);
    // final ByteBuffer newBuffer = ByteBuffer.allocate(position + mLength);
    // newBuffer.put(mBuffer);
    // newBuffer.position(position);
    // return newBuffer;
    // } else {
    // return mBuffer;
    // }
    // }

}
