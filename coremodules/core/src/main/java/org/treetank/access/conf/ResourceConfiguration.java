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
package org.treetank.access.conf;

import java.io.File;

import org.treetank.access.Session;
import org.treetank.api.INodeFactory;
import org.treetank.io.IStorageFactory;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.revisioning.IRevisioning;

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
         * @param pFile
         *            to be set
         * @param pIsFolder
         *            to be set.
         */
        private Paths(final File pFile, final boolean pIsFolder) {
            this.mFile = pFile;
            this.mIsFolder = pIsFolder;
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
         * @param pFile
         *            to be checked
         * @return -1 if less folders are there, 0 if the structure is equal to
         *         the one expected, 1 if the structure has more folders
         */
        public static int compareStructure(final File pFile) {
            int existing = 0;
            for (final Paths paths : values()) {
                final File currentFile = new File(pFile, paths.getFile().getName());
                if (currentFile.exists()) {
                    existing++;
                }
            }
            return existing - values().length;
        }
    }

    // MEMBERS FOR FIXED FIELDS
    /** Type of Storage (File, Berkeley). */
    public final IStorageFactory mStorage;

    /** Node Factory to generate nodes for this resource. */
    public final INodeFactory mNodeFac;

    /** Handler for bytes before serialization. */
    public final IByteHandler mByteHandler;

    /** Kind of revisioning (Incremental, Differential). */
    public final IRevisioning mRevision;

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
     * @param pBuilder
     *            {@link Builder} reference
     */
    private ResourceConfiguration(IStorageFactory pStorage, INodeFactory pNodeFactory, IByteHandler pByteHandler,
        IRevisioning pRevision) {
        mStorage = pStorage;
        mNodeFac = pNodeFactory;
        mByteHandler = pByteHandler;
        mRevision = pRevision;

        mDBConfig = pBuilder.mDBConfig;
        mByteHandler = pBuilder.mByteHandler;
        mPath =
            new File(new File(mDBConfig.mFile, DatabaseConfiguration.Paths.Data.getFile().getName()),
                pBuilder.mResource);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 90599;
        int result = 13;
        result = prime * result + mStorage.hashCode();
        result = prime * result + mRevision.hashCode();
        result = prime * result + mPath.hashCode();
        result = prime * result + mDBConfig.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object pObj) {
        return this.hashCode() == pObj.hashCode();
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
        builder.append(this.mStorage);
        builder.append("\nRevision: ");
        builder.append(this.mRevision);
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

        /** Resource for the this session. */
        private String mResource;

        /** Resource for the this session. */
        private DatabaseConfiguration mDBConfig;

        /**
         * Constructor, setting the mandatory fields.
         * 
         * @param paramResource
         *            the name of the resource, must to be set.
         * @param pConfig
         *            the related {@link DatabaseConfiguration}, must to be set.
         */
        public Builder(final String paramResource, final DatabaseConfiguration pConfig) {
            mResource = paramResource;
            mDBConfig = pConfig;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("\nResource: ");
            builder.append(this.mResource);
            builder.append("\nType: ");
            builder.append(this.mStorage);
            builder.append("\nRevision: ");
            builder.append(this.mRevision);
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
