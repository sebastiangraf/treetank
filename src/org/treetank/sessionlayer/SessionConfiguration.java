/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id:SessionConfiguration.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import java.io.File;

import org.treetank.api.IConstants;

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
  private final String mPath;

  /** Key of .tnk file or null. */
  private final byte[] mEncryptionKey;

  /** Checksum algorithm. */
  private final boolean mIsChecksummed;

  /**
   * Convenience constructor binding to .tnk file without encryption or
   * end-to-end integrity.
   * 
   * @param path Path to .tnk file.
   */
  public SessionConfiguration(final String path) {
    this(path, null, false);
  }

  /**
   * Standard constructor binding to .tnk file with encryption but no
   * end-to-end integrity.
   * 
   * @param path Path to .tnk file.
   * @param encryptionKey Key to encrypt .tnk file with.
   */
  public SessionConfiguration(final String path, final byte[] encryptionKey) {
    this(path, encryptionKey, false);
  }

  /**
   * Standard constructor binding to .tnk file with encryption.
   * 
   * @param path Path to .tnk file.
   * @param encryptionKey Key to encrypt .tnk file with.
   * @param isChecksummed Does the .tnk file uses end-to-end checksumming?
   */
  public SessionConfiguration(
      final String path,
      final byte[] encryptionKey,
      final boolean isChecksummed) {

    // Make sure the path is legal.
    if ((path == null) || (!path.endsWith(".tnk"))) {
      throw new IllegalArgumentException(
          "Path to TreeTank file must not be null and end with '.tnk'.");
    }

    // Make sure the encryption key is properly set.
    if ((encryptionKey != null)
        && (encryptionKey.length != IConstants.ENCRYPTION_KEY_LENGTH)) {
      throw new IllegalArgumentException(
          "Encryption key must either be null (encryption disabled) or "
              + IConstants.ENCRYPTION_KEY_LENGTH
              + " bytes long (encryption enabled).");
    }

    mPath = new File(path).getAbsolutePath();
    mEncryptionKey = encryptionKey;
    mIsChecksummed = isChecksummed;
  }

  /**
   * Get absolute path to .tnk file.
   * 
   * @return Path to .tnk file.
   */
  public final String getPath() {
    return mPath;
  }

  /**
   * Is the .tnk file encrypted or not?
   * 
   * @return True if the .tnk file is encrypted. False else.
   */
  public final boolean isEncrypted() {
    return mEncryptionKey != null;
  }

  /**
   * Get the encryption key of the .tnk file.
   * 
   * @return Encryption key to .tnk file.
   */
  public final byte[] getEncryptionKey() {
    return mEncryptionKey;
  }

  /**
   * Is the .tnk file checksummed or not?
   * 
   * @return True if the .tnk file is checksummed. False else.
   */
  public final boolean isChecksummed() {
    return mIsChecksummed;
  }

}
