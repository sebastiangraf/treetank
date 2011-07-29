/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import org.treetank.access.WriteTransaction.HashKind;
import org.treetank.io.AbsIOFactory.StorageType;
import org.treetank.settings.ERevisioning;

/**
 * <h1>Database Configuration</h1>
 * 
 * <p>
 * Represents a configuration of a database. Includes all settings which have to be made when it comes to the
 * creation of the database.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public final class DatabaseConfiguration {

    // STATIC STANDARD FIELDS
    /** Identification for string. */
    public static final String BINARY = "5.4.0";
    /** Standard storage. */
    public static final StorageType STORAGE = StorageType.File;
    /** Standard Versioning Approach. */
    public static final ERevisioning VERSIONING = ERevisioning.INCREMENTAL;
    /** Type of hashing. */
    public static final HashKind HASHKIND = HashKind.Rolling;
    /** Versions to restore. */
    public static final int VERSIONSTORESTORE = 4;
    // END STATIC STANDARD FIELDS

    /** Type of Storage (File, Berkeley). */
    private final StorageType mType;

    /** Kind of revisioning (Incremental, Differential). */
    private final ERevisioning mRevision;

    /** Kind of integrity hash (rolling, postorder). */
    private final HashKind mHashKind;

    /** Number of revisions to restore a complete set of data. */
    private final int mRevisionsToRestore;

    /** Binary version of storage. */
    private final String mBinaryVersion;

    /**
     * Constructor with all possible properties.
     * 
     * @param paramBuilder
     *            properties to be set for setting
     */
    private DatabaseConfiguration(final DatabaseConfiguration.Builder paramBuilder) {
        mType = paramBuilder.mType;
        mRevision = paramBuilder.mRevision;
        mHashKind = paramBuilder.mHashKind;
        mRevisionsToRestore = paramBuilder.mRevisionsToRestore;
        mBinaryVersion = paramBuilder.mBinaryVersion;
    }

    /**
     * Get binaryVersion.
     * 
     * @return the binaryVersion
     */
    public String getBinaryVersion() {
        return mBinaryVersion;
    }

    /**
     * Get hashKind.
     * 
     * @return the hashKind
     */
    public HashKind getHashKind() {
        return mHashKind;
    }

    /**
     * Get revision.
     * 
     * @return the revision
     */
    public ERevisioning getRevision() {
        return mRevision;
    }

    /**
     * Get type.
     * 
     * @return the type
     */
    public StorageType getType() {
        return mType;
    }

    /**
     * Get revisionsToRestore.
     * 
     * @return the revisionsToRestore
     */
    public int getRevisionsToRestore() {
        return mRevisionsToRestore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Type: ");
        builder.append(this.mType);
        builder.append("\nRevision: ");
        builder.append(this.mRevision);
        builder.append("\nHashKind: ");
        builder.append(this.mHashKind);
        return builder.toString();
    }

    /**
     * Builder class for generating new {@link DatabaseConfiguration} instance.
     */
    public static final class Builder {

        /** Type of Storage (File, Berkeley). */
        private StorageType mType = STORAGE;

        /** Kind of revisioning (Incremental, Differential). */
        private ERevisioning mRevision = VERSIONING;

        /** Kind of integrity hash (rolling, postorder). */
        private HashKind mHashKind = HASHKIND;

        /** Number of revisions to restore a complete set of data. */
        private int mRevisionsToRestore = VERSIONSTORESTORE;

        /** Binary version of storage. */
        private String mBinaryVersion = BINARY;

        /**
         * Setter for mStorageType.
         * 
         * @param paramType
         *            to be set
         * @return reference to the builder object
         */
        public Builder setType(final StorageType paramType) {
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
         * Setter for mBinaryVersion.
         * 
         * @param paramBinaryVersion
         *            to be set
         * @return reference to the builder object
         */
        public Builder setBinaryVersion(final String paramBinaryVersion) {
            if (paramBinaryVersion == null) {
                throw new NullPointerException("paramBinaryVersion may not be null!");
            }
            mBinaryVersion = paramBinaryVersion;
            return this;
        }

        /**
         * Builder method to generate new configuration.
         * 
         * @return a new {@link DatabaseConfiguration} instance
         */
        public DatabaseConfiguration build() {
            return new DatabaseConfiguration(this);
        }
    }

}
