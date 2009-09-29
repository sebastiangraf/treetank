package com.treetank.io;

import java.io.IOException;

/**
 * All Treetank IO Exception are wrapped in this class. It inherits from
 * IOException since it is a Treetank IO Exception.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TreetankIOException extends IOException {

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
    public TreetankIOException(final IOException paramExc) {
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
