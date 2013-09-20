package org.treetank.io;

import org.treetank.exception.TTIOException;

/**
 * Interface for Log to capsulate persistent against non-persistent logs.
 * 
 * @author Sebastian Graf, University of Kosntanz
 * 
 */
public interface ILog {

    /**
     * Getting a {@link LogValue} for a given key
     * 
     * @param pKey
     *            the key
     * @return a suitable {@link LogValue} if present, false otherwise
     * @throws TTIOException
     */
    LogValue get(LogKey pKey) throws TTIOException;

    /**
     * Putting a new entry to the log, overriding already existing entries.
     * 
     * @param pKey
     *            to be set
     * @param pValue
     *            to be set
     * @throws TTIOException
     */
    void put(LogKey pKey, LogValue pValue) throws TTIOException;

    /**
     * Closing the log.
     * 
     * @throws TTIOException
     */
    void close() throws TTIOException;

    /**
     * Check if log is closed or not.
     * 
     * @return if log is closed.
     */
    boolean isClosed();

}
