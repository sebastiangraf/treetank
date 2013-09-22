package org.treetank.api;

import java.io.File;
import java.io.IOException;

import org.treetank.exception.TTIOException;

/**
 * 
 * @author Andreas Rain
 * 
 */
public interface IFilelistenerReadTrx {

    /**
     * The returned array consists of all the relative paths there are in the storage.
     * 
     * @return array containing relative file paths contained in this storage.
     */
    public String[] getFilePaths();

    /**
     * The returned array consists of all the relative paths there are in the storage.
     * 
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String pRelativePath);

    /**
     * 
     * @return int - amount of buckets
     */
    public int getCount();

    /**
     * This method allows you to get a full file using the node key of the
     * header
     * 
     * @param pRelativePath
     * @return true if successful, false otherwise
     */
    public File getFullFile(String pRelativePath) throws TTIOException, IOException;

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
