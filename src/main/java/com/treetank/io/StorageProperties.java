package com.treetank.io;

/**
 * The Storage Propeties for each storage, holding metadata about the used
 * tt-environement. This class is used for checking if the underlaying storage
 * is valid with the used environment.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class StorageProperties {

	
	private final long versionMajor;

	private final long versionMinor;

	private final boolean checksummed;

	private final boolean encrypted;

	public StorageProperties(final long paramVersionMajor,
			final long paramVersionMinor, final boolean paramChecksummed,
			final boolean paramEncrypted) {
		this.versionMajor = paramVersionMajor;
		this.versionMinor = paramVersionMinor;
		this.checksummed = paramChecksummed;
		this.encrypted = paramEncrypted;

	}

	/**
	 * @return the versionMajor
	 */
	public final long getVersionMajor() {
		return versionMajor;
	}

	/**
	 * @return the versionMinor
	 */
	public final long getVersionMinor() {
		return versionMinor;
	}

	/**
	 * @return the checksummed
	 */
	public final boolean getChecksummed() {
		return checksummed;
	}

	/**
	 * @return the encrypted
	 */
	public final boolean getEncrypted() {
		return encrypted;
	}

	public final String toString() {
		return new StringBuilder().append(versionMajor).append(versionMinor)
				.append(checksummed).append(encrypted).toString();
	}

}
