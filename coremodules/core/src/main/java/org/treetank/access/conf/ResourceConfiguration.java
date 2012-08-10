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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.treetank.access.Session;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;
import org.treetank.io.IStorage;
import org.treetank.io.IStorage.IStorageFactory;
import org.treetank.revisioning.IRevisioning;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

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
public final class ResourceConfiguration {

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
    public final IStorage mStorage;

    /** Kind of revisioning (Incremental, Differential). */
    public final IRevisioning mRevision;

    /** Path for the resource to be associated. */
    public final File mFile;

    /** Node Factory for deserializing nodes. */
    public final INodeFactory mNodeFac;
    // END MEMBERS FOR FIXED FIELDS

    /** DatabaseConfiguration for this {@link ResourceConfiguration}. */
    public final DatabaseConfiguration mDBConfig;

    /**
     * Convenience constructor using the standard settings.
     * 
     * @param pStorage
     * @param pNodeFactory
     * @param pByteHandler
     * @param pRevision
     */
    @Inject
    public ResourceConfiguration(@Assisted DatabaseConfiguration pDBConf, @Assisted String pResourceName,
        IStorageFactory pStorage, IRevisioning pRevision, INodeFactory pNodeFac) {
        mRevision = pRevision;
        mDBConfig = pDBConf;
        mNodeFac = pNodeFac;
        mFile =
            new File(new File(mDBConfig.mFile, DatabaseConfiguration.Paths.Data.getFile().getName()),
                pResourceName);
        mStorage = pStorage.create(mFile);

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
        result = prime * result + mFile.hashCode();
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
        builder.append(this.mFile);
        builder.append("Type: ");
        builder.append(this.mStorage);
        builder.append("\nRevision: ");
        builder.append(this.mRevision);
        return builder.toString();
    }

    /**
     * 
     * Factory for generating an {@link ResourceConfiguration}-instance. Needed mainly
     * because of Guice-Assisted utilization.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static interface IResourceConfigurationFactory {

        /**
         * Generating a storage for a fixed file.
         * 
         * @param pDBConf
         *            DatabaseConfiguration to be set.
         * @param pResourceName
         *            Name of resource to be set.
         * @return an {@link ResourceConfiguration}-instance
         */
        ResourceConfiguration create(DatabaseConfiguration pDBConf, String pResourceName);
    }

    public static void serialize(final ResourceConfiguration pConfig) throws TTIOException {
        try {
            FileWriter fileWriter =
                new FileWriter(new File(pConfig.mFile, Paths.ConfigBinary.getFile().getName()));
            JsonWriter jsonWriter = new JsonWriter(fileWriter);
            jsonWriter.beginObject();
            jsonWriter.name("storage").value(pConfig.mStorage.getClass().getName());
            jsonWriter.name("revisioning").value(pConfig.mRevision.getClass().getName());
            jsonWriter.name("file").value(pConfig.mFile.getAbsolutePath());
            jsonWriter.name("nodeFac").value(pConfig.mNodeFac.getClass().getName());
            jsonWriter.endObject();
            jsonWriter.close();
            fileWriter.close();
        } catch (FileNotFoundException fileExec) {
            throw new TTIOException(fileExec);
        } catch (IOException ioexc) {
            throw new TTIOException(ioexc);
        }
    }

    public static ResourceConfiguration deserialize(final File pFile) throws TTIOException {
        try {
            FileReader fileReader = new FileReader(new File(pFile, Paths.ConfigBinary.getFile().getName()));
            JsonReader jsonReader = new JsonReader(fileReader);
            jsonReader.beginObject();

            jsonReader.endObject();
            return null;
        } catch (FileNotFoundException fileExec) {
            throw new TTIOException(fileExec);
        } catch (IOException ioexc) {
            throw new TTIOException(ioexc);
        }
    }

}
