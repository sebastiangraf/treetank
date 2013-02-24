/**
 * 
 */
package org.treetank.io.bytepipe;

import static com.google.common.base.Objects.toStringHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import org.treetank.exception.TTByteHandleException;

import com.google.inject.Inject;

/**
 * Decorator for encrypting any content.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Encryptor implements IByteHandler {

    /** Kind of the algorithm. */
    private static final String ALGORITHM = "AES";

    /** Key for de-/encryption. */
    private final Key mKey;

    /** Cipher for encryption and decryption operations. */
    private final Cipher mCipher;

    /**
     * Constructor.
     * 
     * @param pKey
     *            to be injected
     */
    @Inject
    public Encryptor(final Key pKey) {
        try {
            mCipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        mKey = pKey;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTByteHandleException
     */
    public OutputStream serialize(final OutputStream pToSerialize) throws TTByteHandleException {
        try {
            mCipher.init(Cipher.ENCRYPT_MODE, mKey);
            return new CipherOutputStream(pToSerialize, mCipher);
        } catch (final InvalidKeyException exc) {
            throw new TTByteHandleException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream deserialize(InputStream pToDeserialize) throws TTByteHandleException {
        try {
            mCipher.init(Cipher.DECRYPT_MODE, mKey);
            return new CipherInputStream(pToDeserialize, mCipher);
        } catch (final InvalidKeyException exc) {
            throw new TTByteHandleException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Encryptor clone() {
        return new Encryptor(mKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}
