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

    /** Member for versionMajor */
    private transient final long versionMajor;

    /** Member for versionMinor */
    private transient final long versionMinor;

    /** Member for checksummed */
    private transient final boolean checksummed;

    /** Member for encrypted */
    private transient final boolean encrypted;

    /**
     * Constructor
     * 
     * @param paramVersionMajor
     *            parameter for the versionMajor
     * @param paramVersionMinor
     *            parameter for the versioninor
     * @param paramChecksummed
     *            parameter for the checksummed
     * @param paramEncrypted
     *            parameter for the parameter
     */
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
    public long getVersionMajor() {
        return versionMajor;
    }

    /**
     * @return the versionMinor
     */
    public long getVersionMinor() {
        return versionMinor;
    }

    /**
     * @return the checksummed
     */
    public boolean isChecksummed() {
        return checksummed;
    }

    /**
     * @return the encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * To String method for StorageProperties.
     */
    public String toString() {
        return new StringBuilder().append(versionMajor).append(versionMinor)
                .append(checksummed).append(encrypted).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (checksummed ? 1231 : 1237);
        result = prime * result + (encrypted ? 1231 : 1237);
        result = prime * result + (int) (versionMajor ^ (versionMajor >>> 32));
        result = prime * result + (int) (versionMinor ^ (versionMinor >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        boolean returnVal = true;
        if (obj == null) {
            returnVal = false;
        }
        if (getClass() != obj.getClass()) {
            returnVal = false;
        }
        final StorageProperties other = (StorageProperties) obj;
        if (checksummed != other.checksummed) {
            returnVal = false;
        }
        if (encrypted != other.encrypted) {
            returnVal = false;
        }
        if (versionMajor != other.versionMajor) {
            returnVal = false;
        }
        if (versionMinor != other.versionMinor) {
            returnVal = false;
        }
        return returnVal;
    }

}
