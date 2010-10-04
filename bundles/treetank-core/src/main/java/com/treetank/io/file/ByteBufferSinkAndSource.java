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

package com.treetank.io.file;

import java.nio.ByteBuffer;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.utils.IConstants;

/**
 * This class represents the byte input/output mechanism for File-access. After
 * all, it is just a simple wrapper for the ByteBuffer and exists only for
 * convenience reasons.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class ByteBufferSinkAndSource implements ITTSink, ITTSource {

    /** internal buffer. */
    private transient ByteBuffer mBuffer;

    /**
     * Constructor.
     */
    public ByteBufferSinkAndSource() {
        mBuffer = ByteBuffer.allocate(IConstants.BUFFER_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeByte(final byte mByteVal) {
        checkAndIncrease(1);
        mBuffer.put(mByteVal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLong(final long paramLongVal) {
        checkAndIncrease(8);
        mBuffer.putLong(paramLongVal);

    }

    /**
     * Setting position in buffer.
     * 
     * @param paramVal
     *            new position to set
     */
    public void position(final int paramVal) {
        mBuffer.position(paramVal);
    }

    /**
     * Getting position in buffer.
     * 
     * @return position to get
     */
    public int position() {
        return mBuffer.position();
    }

    /**
     * Getting more bytes and fill it in the buffer.
     * 
     * @param mDst
     *            to fill
     * @param mOffset
     *            offset in buffer
     * @param mLength
     *            length of bytes
     */
    public void get(final byte[] mDst, final int mOffset, final int mLength) {
        mBuffer.get(mDst, mOffset, mLength);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() {
        return mBuffer.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong() {
        return mBuffer.getLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeInt(final int paramIntVal) {
        checkAndIncrease(4);
        mBuffer.putInt(paramIntVal);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() {
        return mBuffer.getInt();
    }

    /**
     * Checking of length is sufficient, if not, increase the bytebuffer.
     * 
     * @param mLength
     *            for the bytes which have to be inserted
     */
    private void checkAndIncrease(final int mLength) {
        final int position = mBuffer.position();
        if (mBuffer.position() + mLength >= mBuffer.capacity()) {
            mBuffer.position(0);
            final ByteBuffer newBuffer = ByteBuffer.allocate(mBuffer.capacity() + IConstants.BUFFER_SIZE);
            newBuffer.put(mBuffer);
            mBuffer = newBuffer;
            mBuffer.position(position);
        }
    }

}
