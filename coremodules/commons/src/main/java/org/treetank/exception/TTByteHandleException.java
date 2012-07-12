/**
 * 
 */
package org.treetank.exception;

import java.util.zip.DataFormatException;

/**
 * Exception type for handle everything related to the handling of bytes especially within the io-layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class TTByteHandleException extends TTException {

    /** Default serialization id due to inheritance. */
    private static final long serialVersionUID = -7648446801135591946L;

    /**
     * 
     * Constructor.
     * 
     * @param pExc
     *            to be encapsulated
     */
    public TTByteHandleException(final DataFormatException pExc) {
        super(pExc);
    }

}
