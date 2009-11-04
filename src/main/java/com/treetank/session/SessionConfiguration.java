/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: SessionConfiguration.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.session;

import java.io.File;
import java.util.Arrays;

import com.treetank.io.AbstractIOFactory;
import com.treetank.utils.IConstants;

/**
 * <h1>SessionConfiguration</h1>
 * 
 * <p>
 * Holds the session-wide settings that can not change.
 * 
 * The following logic applies:
 * <li>If the encryption key is null, no encryption is used.</li>
 * <li>If the checksum algorithm is null, no checksumming is used.</li>
 * </p>
 */
public final class SessionConfiguration {

    /** Absolute path to .tnk file. */
    private final String mAbsolutePath;

    /** Key of .tnk file or null. */
    private final byte[] mEncryptionKey;

    /** Checksum algorithm. */
    private final boolean mChecksummed;

    /** Used {@link StorageType} */
    private final AbstractIOFactory.StorageType mType;

    /**
     * Convenience constructor binding to .tnk file without encryption or
     * end-to-end integrity.
     * 
     * @param path
     *            Path to .tnk file.
     */
    public SessionConfiguration(final String path) {
        this(path, null, false, IConstants.STORAGE_TYPE);
    }

    /**
     * Standard constructor binding to .tnk file with encryption but no
     * end-to-end integrity.
     * 
     * @param path
     *            Path to .tnk file.
     * @param encryptionKey
     *            Key to encrypt .tnk file with.
     */
    public SessionConfiguration(final String path, final byte[] encryptionKey) {
        this(path, encryptionKey, false, IConstants.STORAGE_TYPE);
    }

    /**
     * Standard constructor binding to .tnk file with encryption.
     * 
     * @param path
     *            Path to .tnk file.
     * @param encryptionKey
     *            Key to encrypt .tnk file with.
     * @param checksummed
     *            Does the .tnk file uses end-to-end checksumming?
     * @param type
     *            which storage <code>StorageType</code> should be used?
     */
    public SessionConfiguration(final String path, final byte[] encryptionKey,
            final boolean checksummed, final AbstractIOFactory.StorageType type) {

        // Make sure the path is legal.
        if (path == null
                && (!new File(path).isDirectory() && new File(path).list().length > 0)) {
            throw new IllegalArgumentException(
                    "Path to TreeTank file must not be null and be an emtpy directory");
        }

        // Set path and name.
        final File file = new File(path);

        // Make sure parent path exists.
        file.mkdirs();

        mAbsolutePath = file.getAbsolutePath();

        mEncryptionKey = encryptionKey;
        mChecksummed = checksummed;
        mType = type;
    }

    /**
     * Get absolute path to .tnk file.
     * 
     * @return Path to .tnk file.
     */
    public String getAbsolutePath() {
        return mAbsolutePath;
    }

    /**
     * Is the .tnk file encrypted or not?
     * 
     * @return True if the .tnk file is encrypted. False else.
     */
    public boolean isEncrypted() {
        return mEncryptionKey != null;
    }

    /**
     * Get the encryption key of the .tnk file.
     * 
     * @return Encryption key to .tnk file.
     */
    protected byte[] getEncryptionKey() {
        return mEncryptionKey;
    }

    /**
     * Is the .tnk file checksummed or not?
     * 
     * @return True if the .tnk file is checksummed. False else.
     */
    public boolean isChecksummed() {
        return mChecksummed;
    }

    /**
     * To String method
     * 
     * @return String with a string representation.
     */
    public String toString() {
        return mAbsolutePath + File.separator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mAbsolutePath == null) ? 0 : mAbsolutePath.hashCode());
        result = prime * result + (mChecksummed ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(mEncryptionKey);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SessionConfiguration other = (SessionConfiguration) obj;
        if (mAbsolutePath == null) {
            if (other.mAbsolutePath != null)
                return false;
        } else if (!mAbsolutePath.equals(other.mAbsolutePath))
            return false;
        if (mChecksummed != other.mChecksummed)
            return false;
        if (!Arrays.equals(mEncryptionKey, other.mEncryptionKey))
            return false;
        return true;
    }

    /**
     * Getting the <code>StorageType</code> for this configuration
     * 
     * @return the storageType for this configuration
     */
    public AbstractIOFactory.StorageType getType() {
        return mType;
    }

}
