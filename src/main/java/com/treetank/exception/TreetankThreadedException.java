package com.treetank.exception;

import java.util.concurrent.ExecutionException;

/**
 * This class holds all exceptions which can occure with the usage of
 * multithreaded exceptions.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TreetankThreadedException extends TreetankException {

    private static final long serialVersionUID = -2891221683798924769L;

    /**
     * Constructor for threaded exceptions
     * 
     * @param exc
     *            tp be stored
     */
    public TreetankThreadedException(final InterruptedException exc) {
        super(exc);
    }

    /**
     * Exception for weird thread behaviour.
     * 
     * @param message
     *            to be stored
     */
    public TreetankThreadedException(final String message) {
        super(message);
    }

    /**
     * Constructor for execution exception exceptions
     * 
     * @param exc
     *            tp be stored
     */
    public TreetankThreadedException(final ExecutionException exc) {
        super(exc);
    }

}
