package org.treetank.access.conf;

import java.io.File;

import org.treetank.access.Session;
import org.treetank.access.WriteTransaction.HashKind;
import org.treetank.io.EStorage;
import org.treetank.settings.ERevisioning;

/**
 * <h1>ResourceConfiguration</h1>
 * 
 * <p>
 * Holds the settings for a resource which acts as a base for session that can not change. This includes all
 * settings which are persistent. Each {@link ResourceConfiguration} is furthermore bound to one fixed
 * database denoted by a related {@link DatabaseConfiguration}.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public final class ResourceConfiguration implements IConfigureSerializable {

    /** For serialization. */
    private static final long serialVersionUID = 1790483717305421672L;

    /**
     * Paths for a {@link Session}. Each resource has the same folder.layout.
     */
    public enum Paths {

        /** Folder for storage of data. */
        Data(new File("data"), true),
        /** Folder for transaction log. */
        TransactionLog(new File("log"), true),
        /** File to store the resource settings. */
        ConfigBinary(new File("ressetting.obj"), false);

        /** Location of the file. */
        private final File mFile;

        /** Is the location a folder or no? */
        private final boolean mIsFolder;

        /**
         * Constructor.
         * 
         * @param mFile
         *            to be set
         * @param mIsFolder
         *            to be set.
         */
        private Paths(final File mFile, final boolean mIsFolder) {
            this.mFile = mFile;
            this.mIsFolder = mIsFolder;
        }

        /**
         * Getting the file for the kind.
         * 
         * @return the file to the kind
         */
        public File getFile() {
            return mFile;
        }

        /**
         * Check if file is denoted as folder or not.
         * 
         * @return boolean if file is folder
         */
        public boolean isFolder() {
            return mIsFolder;
        }

        /**
         * Checking a structure in a folder to be equal with the data in this
         * enum.
         * 
         * @param paramFile
         *            to be checked
         * @return -1 if less folders are there, 0 if the structure is equal to
         *         the one expected, 1 if the structure has more folders
         */
        public static int compareStructure(final File paramFile) {
            int existing = 0;
            for (final Paths paths : values()) {
                final File currentFile = new File(paramFile, paths.getFile().getName());
                if (currentFile.exists()) {
                    existing++;
                }
            }
            return existing - values().length;
        }
    }

    // FIXED STANDARD FIELDS
    /** Standard storage. */
    public static final EStorage STORAGE = EStorage.File;
    /** Standard Versioning Approach. */
    public static final ERevisioning VERSIONING = ERevisioning.INCREMENTAL;
    /** Type of hashing. */
    public static final HashKind HASHKIND = HashKind.Rolling;
    /** Versions to restore. */
    public static final int VERSIONSTORESTORE = 4;
    /** Folder for tmp-database. */
    public static final String INTRINSICTEMP = "tmp";
    // END FIXED STANDARD FIELDS

    // MEMBERS FOR FIXED FIELDS
    /** Type of Storage (File, Berkeley). */
    public final EStorage mType;

    /** Kind of revisioning (Incremental, Differential). */
    public final ERevisioning mRevision;

    /** Kind of integrity hash (rolling, postorder). */
    public final HashKind mHashKind;

    /** Number of revisions to restore a complete set of data. */
    public final int mRevisionsToRestore;

    /** Path for the resource to be associated. */
    public final File mPath;
    // END MEMBERS FOR FIXED FIELDS

    /** DatabaseConfiguration for this {@link ResourceConfiguration}. */
    public final DatabaseConfiguration mDBConfig;

    /**
     * Convenience constructor using the standard settings.
     * 
     * @param paramBuilder
     *            {@link Builder} reference
     */
    private ResourceConfiguration(final ResourceConfiguration.Builder paramBuilder) {
        mType = paramBuilder.mType;
        mRevision = paramBuilder.mRevision;
        mHashKind = paramBuilder.mHashKind;
        mRevisionsToRestore = paramBuilder.mRevisionsToRestore;
        mDBConfig = paramBuilder.mDBConfig;
        mPath =
            new File(new File(mDBConfig.mFile, DatabaseConfiguration.Paths.Data.getFile().getName()),
                paramBuilder.mResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 90599;
        int result = 13;
        result = prime * result + mType.hashCode();
        result = prime * result + mRevision.hashCode();
        result = prime * result + mHashKind.hashCode();
        result = prime * result + mPath.hashCode();
        result = prime * result + mDBConfig.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object mObj) {
        return this.hashCode() == mObj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("\nResource: ");
        builder.append(this.mPath);
        builder.append("Type: ");
        builder.append(this.mType);
        builder.append("\nRevision: ");
        builder.append(this.mRevision);
        builder.append("\nHashKind: ");
        builder.append(this.mHashKind);
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getConfigFile() {
        final File file = new File(mPath, Paths.ConfigBinary.getFile().getName());
        return file;
    }

    /**
     * Builder class for generating new {@link ResourceConfiguration} instance.
     */
    public static final class Builder {

        /** Type of Storage (File, Berkeley). */
        private EStorage mType = STORAGE;

        /** Kind of revisioning (Incremental, Differential). */
        private ERevisioning mRevision = VERSIONING;

        /** Kind of integrity hash (rolling, postorder). */
        private HashKind mHashKind = HASHKIND;

        /** Number of revisions to restore a complete set of data. */
        private int mRevisionsToRestore = VERSIONSTORESTORE;

        /** Resource for the this session. */
        private String mResource;

        /** Resource for the this session. */
        private DatabaseConfiguration mDBConfig;

        /**
         * Constructor, setting the mandatory fields.
         * 
         * @param paramResource
         *            the name of the resource, must to be set.
         * @param paramConfig
         *            the related {@link DatabaseConfiguration}, must to be set.
         */
        public Builder(final String paramResource, final DatabaseConfiguration paramConfig) {
            if (paramResource == null || paramConfig == null) {
                throw new IllegalArgumentException("Parameter must not be null!");
            }
            mResource = paramResource;
            mDBConfig = paramConfig;
        }

        /**
         * Setter for mType.
         * 
         * @param paramType
         *            to be set
         * @return reference to the builder object
         */
        public Builder setType(final EStorage paramType) {
            if (paramType == null) {
                throw new NullPointerException("paramType may not be null!");
            }
            mType = paramType;
            return this;
        }

        /**
         * Setter for mRevision.
         * 
         * @param paramRevision
         *            to be set
         * @return reference to the builder object
         */
        public Builder setRevision(final ERevisioning paramRevision) {
            if (paramRevision == null) {
                throw new NullPointerException("paramType may not be null!");
            }
            mRevision = paramRevision;
            return this;
        }

        /**
         * Setter for mHashKind.
         * 
         * @param paramHashKind
         *            to be set
         * @return reference to the builder object
         */
        public Builder setHashKind(final HashKind paramHashKind) {
            if (paramHashKind == null) {
                throw new NullPointerException("paramType may not be null!");
            }
            mHashKind = paramHashKind;
            return this;
        }

        /**
         * Setter for mRevisionsToRestore.
         * 
         * @param paramRevisionsToRestore
         *            to be set
         * @return reference to the builder object
         */
        public Builder setRevisionsToRestore(final int paramRevisionsToRestore) {
            if (paramRevisionsToRestore <= 0) {
                throw new IllegalArgumentException("paramRevisionsToRestore must be > 0!");
            }
            mRevisionsToRestore = paramRevisionsToRestore;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("\nResource: ");
            builder.append(this.mResource);
            builder.append("Type: ");
            builder.append(this.mType);
            builder.append("\nRevision: ");
            builder.append(this.mRevision);
            builder.append("\nHashKind: ");
            builder.append(this.mHashKind);
            return builder.toString();
        }

        /**
         * Building a new {@link ResourceConfiguration} with immutable fields.
         * 
         * @return a new {@link ResourceConfiguration}.
         */
        public ResourceConfiguration build() {
            return new ResourceConfiguration(this);
        }

    }
}
