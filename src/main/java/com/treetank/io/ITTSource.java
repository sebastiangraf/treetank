package com.treetank.io;

/**
 * Interface for providing byteAccess to the read-process in the storage. That
 * means that every initialisation process in TreeTank from the concrete storage
 * is using this interface and that the related concrete storage implementation
 * is implementing this interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface ITTSource {

	/**
	 * Reading a long to the storage
	 * 
	 * @param longVal
	 *            to be read
	 */
	public long readLong();

	/**
	 * Reading an byte to the storage
	 * 
	 * @param byte to be read
	 */
	public byte readByte();

	/**
	 * Reading an int to the storage
	 * 
	 * @param int to be read
	 */
	public int readInt();

}
