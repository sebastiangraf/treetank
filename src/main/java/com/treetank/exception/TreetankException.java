package com.treetank.exception;

/**
 * Exception to hold all relevant failures upcoming from Treetank.
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
     * Constructor
     * 
     * @param builder
     *            , convinience for super-constructor
     */
    private TreetankException(final StringBuilder message) {
        super(message.toString());
    }

    /**
     * Constructor
     * 
     * @param the
     *            message as string, they are concatenated with spaces in
     *            between
     */
    public TreetankException(final String... message) {
        this(concat(message));
    }

    /**
     * Util method to provide StringBuilder functionality;
     * 
     * @param message
     *            to be concatenated
     * @return the StringBuilder for the combined string
     */
    private final static StringBuilder concat(final String... message) {
        final StringBuilder builder = new StringBuilder();
        for (final String mess : message) {
            builder.append(mess);
            builder.append(" ");
        }
        return builder;
    }

}
