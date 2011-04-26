/**
 * Copyright (c) 2011, Distributed Systems Group, University of Konstanz
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
package org.treetank.node.io;

import java.io.ByteArrayInputStream;

/**
 * {@link NodeSource} implementation for reading node input.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public class NodeInputSource implements NodeSource {

    /**
     * Input stream for node source.
     */
    private final ByteArrayInputStream mNodeInput;

    /**
     * Offset for byte operations.
     */
    private int mOffset;

    /**
     * Constructor.
     */
    public NodeInputSource(final byte[] mByteStream) {
        this.mNodeInput = new ByteArrayInputStream(mByteStream);
        mOffset = 0;
    }

    /**
     * {@inheritDoc}
     */
    public long readLong() {
        final byte[] mLongBytes = new byte[8];

        final int mStatus = mNodeInput.read(mLongBytes, mOffset, 8);

        if (mStatus < 0) {
            throw new IndexOutOfBoundsException();
        }

        return byteArrayToLong(mLongBytes);
    }

    /**
     * {@inheritDoc}
     */
    public byte readByte() {

        final int mNextByte = mNodeInput.read();

        if (mNextByte < 0) {
            throw new IndexOutOfBoundsException();
        }

        return (byte)mNextByte;
    }

    /**
     * {@inheritDoc}
     */
    public int readInt() {
        final byte[] mIntBytes = new byte[4];

        final int mStatus = mNodeInput.read(mIntBytes, mOffset, 4);

        if (mStatus < 0) {
            throw new IndexOutOfBoundsException();
        }

        return byteArrayToInt(mIntBytes);
    }

    /**
     * Converting a byte array to integer.
     * 
     * @param mByteArray
     *            Byte array to convert.
     * @return converted integer value.
     */
    private int byteArrayToInt(final byte[] mByteArray) {
        final int mConvInt =
            ((mByteArray[0] & 0xff) << 24) | ((mByteArray[1] & 0xff) << 16) | ((mByteArray[2] & 0xff) << 8)
                | (mByteArray[3] & 0xff);

        return mConvInt;
    }

    /**
     * Converting a byte array to long.
     * 
     * @param mByteArray
     *            Byte array to convert.
     * @return converted long value.
     */
    private long byteArrayToLong(final byte[] mByteArray) {
        final long mConvLong =
            ((long)(mByteArray[0] & 0xff) << 56) | ((long)(mByteArray[1] & 0xff) << 48)
                | ((long)(mByteArray[2] & 0xff) << 40) | ((long)(mByteArray[3] & 0xff) << 32)
                | ((long)(mByteArray[4] & 0xff) << 24) | ((long)(mByteArray[5] & 0xff) << 16)
                | ((long)(mByteArray[6] & 0xff) << 8) | ((long)(mByteArray[7] & 0xff));

        return mConvLong;
    }

}
