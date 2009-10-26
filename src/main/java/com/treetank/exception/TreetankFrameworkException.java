package com.treetank.exception;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sleepycat.je.DatabaseException;

/**
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class TreetankFrameworkException extends Exception {

    /** general id */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor to encapsulate parsing
     * 
     * @param exc
     *            to encapsulate
     */
    public TreetankFrameworkException(final XMLStreamException exc) {
        super(exc);
    }

    /**
     * Constructor to encapsulate the file access of the source.
     * 
     * @param exc
     *            to encapsulate
     */
    public TreetankFrameworkException(final FileNotFoundException exc) {
        super(exc);
    }

    /**
     * Constructor to encapsulate the writing to the Treetank storage
     * 
     * @param exc
     *            to encapsulate
     */
    public TreetankFrameworkException(final TreetankIOException exc) {
        super(exc);
    }

    /**
     * Constructor to encapsulate the writing to the Treetank storage
     * 
     * @param exc
     *            to encapsulate
     */
    public TreetankFrameworkException(final IOException exc) {
        super(exc);
    }

    /**
     * Constructor to encapsulate the writing to the Treetank storage
     * 
     * @param exc
     *            to encapsulate
     */
    public TreetankFrameworkException(final DatabaseException exc) {
        super(exc);
    }

    /**
     * Constructor to encapsulate everything which wants to blame
     * 
     * @param exc
     *            to encapsulate
     */
    public TreetankFrameworkException(final String message) {
        super(message);
    }

}
