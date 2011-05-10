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
 * <h1>Database Configuration</h1> class represents a configuration of a
 * database. includes all
 * settings which have to be made when it comes to the creation of the database.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public class DatabaseConfiguration {

    // STATIC STANDARD FIELDS
    /** Identification for string */
    public final static String BINARY = "5.4.0";
    /** Standard storage */
    public final static StorageType STORAGE = StorageType.File;
    /** Standard Versioning Approach */
    public final static ERevisioning VERSIONING = ERevisioning.INCREMENTAL;
    /** Type of hashing */
    public final static HashKind HASHKIND = HashKind.Rolling;
    /** Versions to restore. */
    public final static int VERSIONSTORESTORE = 4;
    // END STATIC STANDARD FIELDS

    /** Type of Storage (File, Berkeley). */
    public final StorageType mType;

    /** Kind of revisioning (Incremental, Differential). */
    public final ERevisioning mRevision;

    /** Kind of integrity hash (rolling, postorder). */
    public final HashKind mHashKind;

    /** Number of revisions to restore a complete set of data. */
    public final int mRevisionsToRestore;

    /** Binary version of storage. */
    public final String mBinaryVersion;

    /**
     * Constructor with all possible properties.
     * 
     * @param paramBuilder
     *            properties to be set for setting
     */
    private DatabaseConfiguration(final DatabaseConfiguration.Builder paramBuilder) {
        this.mType = paramBuilder.mType;
        this.mRevision = paramBuilder.mRevision;
        this.mHashKind = paramBuilder.mHashKind;
        this.mRevisionsToRestore = paramBuilder.mRevisionsToRestore;
        this.mBinaryVersion = paramBuilder.mBinaryVersion;
    }

    public static class Builder {

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
         * Setter for mStorageType
         * 
         * @param paramType
         *            to be set
         */
        public void setType(StorageType paramType) {
            this.mType = paramType;
        }

        /**
         * Setter for mRevision
         * 
         * @param paramRevision
         *            to be set
         */
        public void setRevision(ERevisioning paramRevision) {
            this.mRevision = paramRevision;
        }

        /**
         * Setter for mHashKind
         * 
         * @param paramHashKind
         *            to be set
         */
        public void setHashKind(HashKind paramHashKind) {
            this.mHashKind = paramHashKind;
        }

        /**
         * Setter for mRevisionsToRestore
         * 
         * @param paramRevisionsToRestore
         *            to be set
         */
        public void setRevisionsToRestore(int paramRevisionsToRestore) {
            this.mRevisionsToRestore = paramRevisionsToRestore;
        }

        /**
         * Setter for paramBinaryVersion
         * 
         * @param paramBinaryVersion
         *            to be set
         */
        public void setBinaryVersion(String paramBinaryVersion) {
            this.mBinaryVersion = paramBinaryVersion;
        }

        public DatabaseConfiguration build() {
            return new DatabaseConfiguration(this);
        }
    }

}
