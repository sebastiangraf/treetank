package com.treetank.exception;

/**
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class TreetankException extends Exception {

    /** general id */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor to encapsulate parsing
     * 
     * @param exc
     *            to encapsulate
     */
    public TreetankException(final Exception exc) {
        super(exc);
    }

    /**
     * Constructor to encapsulate everything which wants to blame
     * 
     * @param exc
     *            to encapsulate
     */
    public TreetankException(final String message) {
        super(message);
    }

}
