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
 * $Id$
 */

package org.treetank.nodelayer;

/**
 * <h1>SessionConfiguration</h1>
 * 
 * <p>
 * Holds the session-wide settings that can not change.
 * 
 * The following logic applies:
 * <li>If the key is null, no encryption is used.</li>
 * 
 * </p>
 */
public final class SessionConfiguration {

  /** Path to .tnk file. */
  private final String mPath;

  /** Key of .tnk file or null. */
  private final byte[] mEncryptionKey;

  /**
   * Convenience constructor binding to .tnk file without encryption.
   * 
   * @param path Path to .tnk file.
   */
  public SessionConfiguration(final String path) {
    this(path, null);
  }

  /**
   * Standard constructor binding to .tnk file with encryption.
   * 
   * @param path Path to .tnk file.
   * @param encryptionKey Key to encrypt .tnk file with.
   */
  public SessionConfiguration(final String path, final byte[] encryptionKey) {
    mPath = path;
    mEncryptionKey = encryptionKey;
  }

  /**
   * Get path to .tnk file.
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

}
