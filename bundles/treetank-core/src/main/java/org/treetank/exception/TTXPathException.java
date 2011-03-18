package org.treetank.exception;

/**
 * Static class for handling the Enum as an {@link AbsTTException}.
 */
public class TTXPathException extends AbsTTException {

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
