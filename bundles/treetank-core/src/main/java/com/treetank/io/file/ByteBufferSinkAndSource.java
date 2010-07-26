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

    /** internal buffer */
    private transient ByteBuffer buffer;

    /**
     * Constructor
     */
    public ByteBufferSinkAndSource() {
        buffer = ByteBuffer.allocate(IConstants.BUFFER_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    public void writeByte(final byte byteVal) {
        checkAndIncrease(1);
        buffer.put(byteVal);
    }

    /**
     * {@inheritDoc}
     */
    public void writeLong(final long longVal) {
        checkAndIncrease(8);
        buffer.putLong(longVal);

    }

    /**
     * Setting position in buffer
     * 
     * @param val
     *            new position to set
     */
    public void position(final int val) {
        buffer.position(val);
    }

    /**
     * Getting position in buffer
     * 
     * @return position to get
     */
    public int position() {
        return buffer.position();
    }

    /**
     * Getting more bytes and fill it in the buffer
     * 
     * @param dst
     *            to fill
     * @param offset
     *            offset in buffer
     * @param length
     *            length of bytes
     */
    public void get(final byte[] dst, final int offset, final int length) {
        buffer.get(dst, offset, length);
    }

    /**
     * {@inheritDoc}
     */
    public byte readByte() {
        return buffer.get();
    }

    /**
     * {@inheritDoc}
     */
    public long readLong() {
        return buffer.getLong();
    }

    /**
     * {@inheritDoc}
     */
    public void writeInt(final int intVal) {
        checkAndIncrease(4);
        buffer.putInt(intVal);

    }

    /**
     * {@inheritDoc}
     */
    public int readInt() {
        return buffer.getInt();
    }

    /**
     * Checking of length is sufficient, if not, increase the bytebuffer
     * 
     * @param length
     *            for the bytes which have to be inserted
     */
    private void checkAndIncrease(final int length) {
        final int position = buffer.position();
        if (buffer.position() + length >= buffer.capacity()) {
            buffer.position(0);
            final ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + IConstants.BUFFER_SIZE);
            newBuffer.put(buffer);
            buffer = newBuffer;
            buffer.position(position);
        }
    }

}
