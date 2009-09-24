package com.treetank.io;

/**
 * Interface for providing byteAccess to the write-process in the storage. That
 * means that every serialization process in TreeTank is using this interface
 * and that the related concrete storage implementation is implementing this
 * interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface ITTSink {

	/**
	 * Writing a long to the storage
	 * 
	 * @param longVal
	 *            to be written
	 */
	public void writeLong(final long longVal);

	/**
	 * Writing an int to the storage
	 * 
	 * @param intVal
	 *            to be written
	 */
	public void writeInt(final int intVal);

	/**
	 * Writing a byte to the storage
	 * 
	 * @param byteVal
	 *            to be written
	 */
	public void writeByte(final byte byteVal);

}
