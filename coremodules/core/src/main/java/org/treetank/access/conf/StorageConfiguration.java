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

import static com.google.common.base.Objects.toStringHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import org.treetank.exception.TTIOException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * <h1>Storage Configuration</h1>
 * 
 * <p>
 * Represents a configuration of a database. Includes all settings which have to be made when it comes to the
 * creation of the database.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public final class StorageConfiguration {

    /**
     * Paths for a {@link org.treetank.access.Storage}. Each {@link org.treetank.access.Storage} has the
     * same folder.layout.
     */
    public enum Paths implements IConfigurationPath {

        /** File to store db settings. */
        ConfigBinary(new File("dbsetting.obj"), false),
        /** File to store the data. */
        Data(new File("resources"), true);

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

    }

    /** Path to file. */
    public final File mFile;

    /**
     * Constructor with the path to be set.
     * 
     * @param paramFile
     *            file to be set
     */
    public StorageConfiguration(final File paramFile) {
        mFile = paramFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mFile", mFile).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object pObj) {
        return this.hashCode() == pObj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(mFile);
    }

    /**
     * Serializing a {@link StorageConfiguration} to a json file.
     * 
     * @param pConfig
     *            to be serialized
     * @throws TTIOException
     */
    public static void serialize(final StorageConfiguration pConfig) throws TTIOException {
        try {
            FileWriter fileWriter =
                new FileWriter(new File(pConfig.mFile, Paths.ConfigBinary.getFile().getName()));
            JsonWriter jsonWriter = new JsonWriter(fileWriter);
            jsonWriter.beginObject();
            jsonWriter.name("file").value(pConfig.mFile.getAbsolutePath());
            jsonWriter.endObject();
            jsonWriter.close();
            fileWriter.close();
        } catch (IOException ioexc) {
            throw new TTIOException(ioexc);
        }
    }

    /**
     * Generate a StorageConfiguration out of a file.
     * 
     * @param pFile
     *            where the StorageConfiguration lies in as json
     * @return a new {@link StorageConfiguration} class
     * @throws TTIOException
     */
    public static StorageConfiguration deserialize(final File pFile) throws TTIOException {
        try {
            FileReader fileReader = new FileReader(new File(pFile, Paths.ConfigBinary.getFile().getName()));
            JsonReader jsonReader = new JsonReader(fileReader);
            jsonReader.beginObject();
            jsonReader.nextName();
            File file = new File(jsonReader.nextString());
            jsonReader.endObject();
            jsonReader.close();
            fileReader.close();
            return new StorageConfiguration(file);
        } catch (IOException ioexc) {
            throw new TTIOException(ioexc);
        }
    }
}
