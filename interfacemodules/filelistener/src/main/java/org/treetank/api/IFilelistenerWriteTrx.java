package org.treetank.api;

import java.io.File;

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * 
 * @author Andreas Rain
 * 
 */
public interface IFilelistenerWriteTrx extends IFilelistenerPageTrx {
	
	/**
	 * Add a new file to the system
	 * 
	 * @param file
	 * @param relativePath
	 */
	public void addFile(File file, String relativePath);
	
	/**
	 * Remove currently selected node.
	 * 
	 * @throws TTException
	 *             if node couldn't be removed
	 */
	public void removeFile(long pkey) throws TTException;

	/**
	 * Commit all modifications of the exclusive write transaction. Even commit
	 * if there are no modification at all.
	 * 
	 * @throws TTException
	 *             if this revision couldn't be commited
	 */
	public void commit() throws TTException;

	/**
	 * Abort all modifications of the exclusive write transaction.
	 * 
	 * @throws TTIOException
	 *             if this revision couldn't be aborted
	 */
	public void abort() throws TTException;

	/**
	 * 
	 * @return the maximum node key in the list
	 */
	public long getMaxNodeKey();
}
