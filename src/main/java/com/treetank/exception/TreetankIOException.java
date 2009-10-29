package com.treetank.exception;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import com.sleepycat.je.DatabaseException;

/**
 * All Treetank IO Exception are wrapped in this class. It inherits from
 * IOException since it is a Treetank IO Exception.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TreetankIOException extends TreetankException {

    /**
     * serializable id.
     */
    private static final long serialVersionUID = 4099242625448155216L;

    /**
     * Constructor.
     * 
     * @param paramExc
     *            exception to be wrapped
     */
    public TreetankIOException(final XMLStreamException paramExc) {
        super(paramExc);
    }

    /**
     * Constructor.
     * 
     * @param paramExc
     *            exception to be wrapped
     */
    public TreetankIOException(final IOException paramExc) {
        super(paramExc);
    }

    /**
     * Constructor.
     * 
     * @param paramExc
     *            exception to be wrapped
     */
    public TreetankIOException(final DatabaseException paramExc) {
        super(paramExc);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            for the overlaying {@link IOException}
     */
    public TreetankIOException(final String message) {
        super(message);
    }

}
