/**
 * 
 */
package org.treetank.io.bytepipe;

import static com.google.common.base.Objects.toStringHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import org.treetank.access.conf.SessionConfiguration;
import org.treetank.exception.TTByteHandleException;

/**
 * Decorator for encrypting any content.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Encryptor implements IByteHandler {

    private static final String ALGORITHM = "AES";

    /** Cipher to perform encryption and decryption operations. */
    private static final Cipher CIPHER;
    static {
        try {
            CIPHER = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTByteHandleException
     */
    public OutputStream serialize(final OutputStream pToSerialize) throws TTByteHandleException {
        try {
            CIPHER.init(Cipher.ENCRYPT_MODE, SessionConfiguration.getInstance().getKey());
            return new CipherOutputStream(pToSerialize, CIPHER);
        } catch (final GeneralSecurityException exc) {
            throw new TTByteHandleException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream deserialize(InputStream pToDeserialize) throws TTByteHandleException {
        try {
            CIPHER.init(Cipher.DECRYPT_MODE, SessionConfiguration.getInstance().getKey());
            return new CipherInputStream(pToDeserialize, CIPHER);
        } catch (final GeneralSecurityException exc) {
            throw new TTByteHandleException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}
