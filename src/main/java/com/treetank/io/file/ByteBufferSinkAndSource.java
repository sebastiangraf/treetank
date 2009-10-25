package com.treetank.io.file;

import java.nio.ByteBuffer;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.utils.IConstants;

/**
 * This class represents the byte input/output mechanism for File-access. After
 * all, it is just a simple wrapper for the ByteBuffer and exists only for
 * convinience reasons.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class ByteBufferSinkAndSource implements ITTSink, ITTSource {

    /** internal buffer */
    private transient final ByteBuffer buffer;

    /**
     * Constructor
     */
    public ByteBufferSinkAndSource() {
        buffer = ByteBuffer.allocate(IConstants.BUFFER_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeByte(final byte byteVal) {
        buffer.put(byteVal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLong(final long longVal) {
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
    @Override
    public byte readByte() {
        return buffer.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong() {
        return buffer.getLong();
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public void writeInt(final int intVal) {
        buffer.putInt(intVal);

    }

    /**
     *{@inheritDoc}
     */
    @Override
    public int readInt() {
        return buffer.getInt();
    }

}
