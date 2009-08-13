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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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

	/** File name only. */
	private final String mFileName;

	/** Absolute path to .tnk file. */
	private final String mAbsolutePath;

	/** Key of .tnk file or null. */
	private final byte[] mEncryptionKey;

	/** Checksum algorithm. */
	private final boolean mChecksummed;

	/** Major of TreeTank version of TreeTank file. */
	private final int mVersionMajor;

	/** Minor of TreeTank version of TreeTank file. */
	private final int mVersionMinor;

	/**
	 * Convenience constructor binding to .tnk file without encryption or
	 * end-to-end integrity.
	 * 
	 * @param path
	 *            Path to .tnk file.
	 */
	public SessionConfiguration(final String path) {
		this(path, null, false);
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
		this(path, encryptionKey, false);
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
	 */
	public SessionConfiguration(final String path, final byte[] encryptionKey,
			final boolean checksummed) {

		// Make sure the path is legal.
		if (path == null) {
			if (!new File(path).isDirectory()
					&& new File(path).list().length > 0) {
				throw new IllegalArgumentException(
						"Path to TreeTank file must not be null and be an emtpy directory");
			}
		}

		// Set path and name.
		File file = new File(path + File.separator + "tt.tnk");

		// Make sure parent path exists.
		File parentFile = file.getParentFile();
		parentFile.mkdirs();

		mFileName = file.getAbsolutePath();
		mAbsolutePath = parentFile.getAbsolutePath();

		// Read version info from file if it contains a TreeTank.
		RandomAccessFile tnk = null;
		try {

			tnk = new RandomAccessFile(mFileName, IConstants.READ_WRITE);

			if (tnk.length() > 0L) {
				tnk.seek(0L);
				mVersionMajor = tnk.readInt();
				mVersionMinor = tnk.readInt();
				final boolean isChecksummed = tnk.readBoolean();
				final boolean isEncrypted = tnk.readBoolean();

				// Fail if an old TreeTank file is encountered.
				if (mVersionMajor < IConstants.LAST_VERSION_MAJOR
						|| mVersionMinor < IConstants.LAST_VERSION_MINOR) {
					throw new IllegalStateException("'" + mFileName
							+ "' was created with TreeTank release "
							+ mVersionMajor + "." + mVersionMinor
							+ " and is incompatible with release "
							+ IConstants.VERSION_MAJOR + "."
							+ IConstants.VERSION_MINOR + ".");
				}

				// Fail if the encryption info does not match.
				if (isEncrypted != (encryptionKey != null)) {
					throw new IllegalStateException("'" + mFileName
							+ "' encryption mode does not match "
							+ "this session configuration.");
				}

				// Fail if the checksum info does not match.
				if (isChecksummed != checksummed) {
					throw new IllegalStateException("'" + mFileName
							+ "' checksum mode does not match "
							+ "this session configuration.");
				}
			} else {
				mVersionMajor = IConstants.VERSION_MAJOR;
				mVersionMinor = IConstants.VERSION_MINOR;
			}
		} catch (FileNotFoundException fnfe) {
			throw new IllegalStateException("Could not find '" + mAbsolutePath
					+ "'.");
		} catch (IOException ioe) {
			throw new IllegalStateException("Could not read from '" + mFileName
					+ "'.");
		} finally {
			if (tnk != null) {
				try {
					tnk.close();
					tnk = null;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		// Make sure the encryption key is properly set.
		if ((encryptionKey != null)
				&& (encryptionKey.length != IConstants.ENCRYPTION_KEY_LENGTH)) {
			throw new IllegalArgumentException(
					"Encryption key must either be null (encryption disabled) or "
							+ IConstants.ENCRYPTION_KEY_LENGTH
							+ " bytes long (encryption enabled).");
		}

		mEncryptionKey = encryptionKey;
		mChecksummed = checksummed;
	}

	/**
	 * Get name of file.
	 * 
	 * @return Name of TreeTank file.
	 */
	public final String getFileName() {
		return mFileName;
	}

	/**
	 * Get absolute path to .tnk file.
	 * 
	 * @return Path to .tnk file.
	 */
	public final String getAbsolutePath() {
		return mAbsolutePath;
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
		return mChecksummed;
	}

	/**
	 * TreeTank version major of TreeTank file.
	 * 
	 * @return Major of TreeTank version.
	 */
	public final int getVersionMajor() {
		return mVersionMajor;
	}

	/**
	 * TreeTank version minor of TreeTank file.
	 * 
	 * @return Minor of TreeTank version.
	 */
	public final int getVersionMinor() {
		return mVersionMinor;
	}

	/**
	 * To String method
	 * 
	 * @return String with a string representation.
	 */
	public final String toString() {
		return mAbsolutePath + File.separator + " with version "
				+ mVersionMajor + "." + mVersionMinor;
	}

}