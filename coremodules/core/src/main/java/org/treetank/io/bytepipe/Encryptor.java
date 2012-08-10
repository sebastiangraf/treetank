/**
 * 
 */
package org.treetank.io.bytepipe;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
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
    private static final int ITERATIONS = 2;

    /** Cipher to perform encryption and decryption operations. */
    private final Cipher mCipher;

    /**
     * Constructor.
     * 
     * @param pComponent
     * @throws TTByteHandleException
     */
    public Encryptor() throws TTByteHandleException {
        try {
            mCipher = Cipher.getInstance(ALGORITHM);
        } catch (final NoSuchAlgorithmException exc) {
            throw new TTByteHandleException(exc);
        } catch (final NoSuchPaddingException exc) {
            throw new TTByteHandleException(exc);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTByteHandleException
     */
    public byte[] serialize(final byte[] pToSerialize) throws TTByteHandleException {
        try {
            mCipher.init(Cipher.ENCRYPT_MODE, SessionConfiguration.getInstance().getKey());

            byte[] toEncrypt = pToSerialize;
            for (int i = 0; i < ITERATIONS; i++) {
                byte[] encValue = mCipher.doFinal(toEncrypt);
                toEncrypt = encValue;
            }
            return toEncrypt;
        } catch (final GeneralSecurityException exc) {
            throw new TTByteHandleException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte[] deserialize(byte[] pToDeserialize) throws TTByteHandleException {
        try {
            mCipher.init(Cipher.DECRYPT_MODE, SessionConfiguration.getInstance().getKey());

            byte[] toDecrypt = pToDeserialize;
            for (int i = 0; i < ITERATIONS; i++) {
                byte[] decValue = mCipher.doFinal(toDecrypt);
                toDecrypt = decValue;
            }
            return toDecrypt;

        } catch (final InvalidKeyException exc) {
            throw new TTByteHandleException(exc);
        } catch (final GeneralSecurityException exc) {
            throw new TTByteHandleException(exc);
        }

    }
}
