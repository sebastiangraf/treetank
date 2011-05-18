/**
 * Copyright (c) 2011, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package org.treetank.encryption;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Singleton class to provide operations for node encryption and decrpytion.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public class NodeEncryption {

    /**
     * Encryption algorithm.
     */
    protected final static String ENCRYPTION_TYPE = "AES";

    /**
     * Encryption algorithm padding type.
     */
    protected final static String ENCRYPTION_PADDING_TYPE = "AES/CBC/PKCS5Padding";

    /**
     * Number of bits for encryption.
     */
    protected final static int ENCRYPTION_BITS = 128;

    /**
     * Secret key for en- and decryption.
     */
    final SecretKey sKey;

    /**
     * Singleton instance.
     */
    private static NodeEncryption mInstance = new NodeEncryption();

    /**
     * Constructor generates an unique secret key.
     */
    private NodeEncryption() {
        sKey = generateSecretKey();
    }

    /**
     * Get singleton instance.
     * 
     * @return singleton instance.
     */
    public static NodeEncryption getInstance() {
        return mInstance;
    }

    /**
     * Encrypt a node.
     * 
     * @param bytesToEncrypt
     *            bytes to encrypt.
     * @return
     *         encrypted node as byte array.
     */
    public synchronized byte[] encrypt(final byte[] bytesToEncrypt) {

        // get secret key as byte array
        final byte[] rawSKey = sKey.getEncoded();

        // initialize secret key specifications and cipher
        final IvParameterSpec ivParams = new IvParameterSpec(rawSKey);

        final Cipher cipher;
        byte[] encrypted = null;
        try {
            cipher = Cipher.getInstance(ENCRYPTION_PADDING_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, sKey, ivParams);
            encrypted = cipher.doFinal(bytesToEncrypt);

        } catch (final GeneralSecurityException exc) {
            exc.printStackTrace();
        }
        return encrypted;
    }

    /**
     * Decrypt a node.
     * 
     * @param bytesToDecrypt
     *            Byte array to decrypt.
     * @return
     *         Original byte array of node.
     */
    public synchronized byte[] decrypt(final byte[] bytesToDecrypt) {

        final byte[] rawSKey = sKey.getEncoded();
        final Cipher cipher;
        byte[] decrypted = null;
        final IvParameterSpec ivParams = new IvParameterSpec(rawSKey);

        try {
            cipher = Cipher.getInstance(ENCRYPTION_PADDING_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, sKey, ivParams);
            decrypted = cipher.doFinal(bytesToDecrypt);
        } catch (final GeneralSecurityException exc) {
            exc.printStackTrace();
        }
        return decrypted;
    }

    /**
     * Generate a secret key for en- and decryption operations.
     * 
     * @return
     *         Generated secret key.
     */
    private synchronized SecretKey generateSecretKey() {
        final KeyGenerator kGen;
        SecretKey sKey = null;
        try {
            kGen = KeyGenerator.getInstance(ENCRYPTION_TYPE);
            kGen.init(ENCRYPTION_BITS);
            sKey = kGen.generateKey();

        } catch (final NoSuchAlgorithmException exc) {
            exc.printStackTrace();
        }
        return sKey;
    }

}
