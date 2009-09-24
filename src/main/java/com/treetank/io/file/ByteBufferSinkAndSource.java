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

	private final ByteBuffer buffer;

	/**
	 * Constructor
	 */
	public ByteBufferSinkAndSource() {
		buffer = ByteBuffer.allocate(IConstants.BUFFER_SIZE);
	}

	@Override
	public void writeByte(final byte byteVal) {
		buffer.put(byteVal);
	}

	@Override
	public void writeLong(final long longVal) {
		buffer.putLong(longVal);

	}

	public void position(final int val) {
		buffer.position(val);
	}

	public int position() {
		return buffer.position();
	}

	public byte get() {
		return buffer.get();
	}

	public void get(final byte[] dst, final int offset, final int length) {
		buffer.get(dst, offset, length);
	}

	@Override
	public byte readByte() {
		return buffer.get();
	}

	@Override
	public long readLong() {
		return buffer.getLong();
	}

	@Override
	public void writeInt(int intVal) {
		buffer.putInt(intVal);

	}

	@Override
	public int readInt() {
		return buffer.getInt();
	}

}
