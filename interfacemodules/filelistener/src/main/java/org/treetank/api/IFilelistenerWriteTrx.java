package org.treetank.api;

import java.io.File;
import java.io.IOException;

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * 
 * @author Andreas Rain
 * 
 */
public interface IFilelistenerWriteTrx extends IFilelistenerReadTrx {

    /**
     * Add a new file to the system
     * 
     * @param pFile
     * @param pRelativePath
     */
    public void addFile(File pFile, String pRelativePath) throws TTException, IOException;

    /**
     * Remove file from desired path
     * 
     * @throws TTException
     *             if node couldn't be removed
     */
    public void removeFile(String pRelativePath) throws TTException;

    /**
     * 
     * @return int - amount of buckets
     */
    public int getCount();

    /**
     * Non-blocking commit in TT
     * 
     * @throws TTException
     *             if this revision couldn't be commited
     */
    public void commit() throws TTException;

    /**
     * Blocking commit in TT
     * 
     * @throws TTException
     *             if this revision couldn't be commited
     */
    public void commitBlocked() throws TTException;

    /**
     * Abort all modifications of the exclusive write transaction.
     * 
     * @throws TTIOException
     *             if this revision couldn't be aborted
     */
    public void abort() throws TTException;

    /**
     * Add an empty file in case of ENTRY_CREATE event.
     * 
     * @param pRelativePath
     * @throws TTException
     * @throws IOException
     */
    void addEmptyFile(String pRelativePath) throws TTException, IOException;
}
