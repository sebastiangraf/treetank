package com.treetank.exception;

/**
 * Static class for handling the Enum as an {@link TTException}
 */
public class TTXPathException extends TTException {

    /**
     * Constructor.
     * 
     * @param paramMessage
     *            message of the XPath Error.
     */
    public TTXPathException(final String paramMessage) {
        super(paramMessage);
    }

}
