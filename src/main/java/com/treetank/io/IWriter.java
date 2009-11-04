package com.treetank.io;

import com.treetank.exception.TreetankIOException;
import com.treetank.page.PageReference;

/**
 * Interface to provide the abstract layer related to write access of the
 * tt-backend.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IWriter extends IReader {

    /**
     * Initializing the storage if the <code>UberPage</code> is bootstraped.
     * 
     * @throws TreetankIOException
     *             if something bad happens
     */
    void setProps(final StorageProperties props) throws TreetankIOException;

    /**
     * Writing a page related to the reference
     * 
     * @param pageReference
     *            that points to a page
     * @throws TreetankIOException
     *             execption to be thrown if something bad happens
     */
    void write(final PageReference pageReference) throws TreetankIOException;

    /**
     * Write Beacon for the first reference
     * 
     * @param pageReference
     *            that points to the beacon
     * @throws TreetankIOException
     *             exception if something bad happens
     */
    void writeFirstReference(final PageReference pageReference)
            throws TreetankIOException;

    /**
     * Closing the write access.
     * 
     * @throws TreetankIOException
     *             if closing fails
     */
    void close() throws TreetankIOException;

}
