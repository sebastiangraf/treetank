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
     * @return
     */
    public String[] getFilePaths();

    /**
     * The returned array consists of all the relative paths there are in the storage.
     * 
     * @return
     */
    public boolean fileExists(String pRelativePath);

    /**
     * This method allows you to get a full file using the node key of the
     * header
     * 
     * @param pKey
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
