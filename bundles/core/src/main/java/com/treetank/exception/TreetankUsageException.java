package com.treetank.exception;

/**
 * Exception throw when an incorrect usage of Treetank occure
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class TreetankUsageException extends TreetankException {

    /**
     * Constructor
     * 
     * @param the
     *            message as string, they are concatenated with spaces in
     *            between
     */
    public TreetankUsageException(final String... message) {
        super(message);
    }

    private static final long serialVersionUID = 2029487202935915816L;

}
