/**
 * 
 */
package org.treetank.exception;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;

import javax.crypto.NoSuchPaddingException;

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
    public TTByteHandleException(final IOException pExc) {
        super(pExc);
    }
    
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

    /**
     * 
     * Constructor.
     * 
     * @param pExc
     *            to be encapsulated
     */
    public TTByteHandleException(final NoSuchAlgorithmException pExc) {
        super(pExc);
    }

    /**
     * 
     * Constructor.
     * 
     * @param pExc
     *            to be encapsulated
     */
    public TTByteHandleException(final NoSuchPaddingException pExc) {
        super(pExc);
    }

    /**
     * 
     * Constructor.
     * 
     * @param pExc
     *            to be encapsulated
     */
    public TTByteHandleException(final InvalidKeyException pExc) {
        super(pExc);
    }

    /**
     * 
     * Constructor.
     * 
     * @param pExc
     *            to be encapsulated
     */
    public TTByteHandleException(final GeneralSecurityException pExc) {
        super(pExc);
    }

}
