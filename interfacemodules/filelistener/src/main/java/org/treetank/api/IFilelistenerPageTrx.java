package org.treetank.api;

import org.treetank.exception.TTIOException;

/**
 * 
 * @author Andreas Rain
 * 
 */
public interface IFilelistenerPageTrx {
	
	/**
	 * The returned array consists of all the header filenode keys
	 * currently in the database.
	 * 
	 * @return
	 */
	public long[] getFileKeys();
	
	/**
	 * This method allows you to get a full file
	 * using the node key of the header
	 * 
	 * @param pKey
	 * @return true if successful, false otherwise
	 */
	public boolean getFullFile(long pKey);

	/**
	 * Close this transaction
	 * 
	 * @throws TTIOException
	 */
	public void close() throws TTIOException;

	/**
	 * Check whether or not this transaction has been closed
	 * 
	 * @return true if closed, false otherwise
	 */
	public boolean isClosed();
}
