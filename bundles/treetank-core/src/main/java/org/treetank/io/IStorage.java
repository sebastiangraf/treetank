package org.treetank.io;

import org.treetank.exception.TTIOException;

/**
 * Interface to generate access to the underlaying storage. The underlaying storage is flexible as long as
 * {@link IReader} and {@link IWriter}-implementations are provided. Utility methods for common interaction
 * with the storage are provided via the <code>EStorage</code>-enum.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IStorage {

    /**
     * Getting a writer.
     * 
     * @return an {@link IWriter} instance
     * @throws TTIOException
     *             if the initalisation fails
     */
    IWriter getWriter() throws TTIOException;

    /**
     * Getting a reader.
     * 
     * @return an {@link IReader} instance
     * @throws TTIOException
     *             if the initalisation fails
     */
    IReader getReader() throws TTIOException;

    /**
     * Closing this storage.
     * 
     * @throws TTIOException
     *             exception to be throwns
     */
    void close() throws TTIOException;

    /**
     * Check if storage exists.
     * 
     * @return true if storage holds data, false otherwise
     * @throws TTIOException
     *             if storage is not accessible
     */
    boolean exists() throws TTIOException;

    /**
     * Truncate database completely
     * 
     * @throws TTIOException
     *             if storage is not accessible
     */
    void truncate() throws TTIOException;

}
