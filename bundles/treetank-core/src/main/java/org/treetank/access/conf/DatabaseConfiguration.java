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

/**
 * <h1>Database Configuration</h1>
 * 
 * <p>
 * Represents a configuration of a database. Includes all settings which have to be made when it comes to the
 * creation of the database.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public final class DatabaseConfiguration implements IConfigureSerializable {

    public enum Paths {

        /** File to store encryption db settings. */
        ConfigBinary(new File("dbsetting.obj"), false),
        /** File to store encryption db settings. */
        KEYSELECTOR(new File("keyselector"), true),
        /** File to store the data. */
        Data(new File("resources"), true);

        private final File mFile;

        private final boolean mIsFolder;

        private Paths(final File mFile, final boolean mIsFolder) {
            this.mFile = mFile;
            this.mIsFolder = mIsFolder;
        }

        /**
         * Getting the file for the kind.
         * 
         * @return the File to the kind
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
         * @param mFile
         *            to be checked
         * @return -1 if less folders are there, 0 if the structure is equal to
         *         the one expected, 1 if the structure has more folders
         */
        public static int compareStructure(final File mFile) {
            int existing = 0;
            for (final Paths paths : values()) {
                final File currentFile = new File(mFile, paths.getFile().getName());
                if (currentFile.exists()) {
                    existing++;
                }
            }
            return existing - values().length;
        }

    }

    // STATIC STANDARD FIELDS
    /** Identification for string. */
    public static final String BINARY = "5.3.7";
    // END STATIC STANDARD FIELDS

    /** Binary version of storage. */
    public final String mBinaryVersion;

    /** Path to file. */
    public final File mFile;

    /**
     * Constructor with all possible properties.
     * 
     * @param paramBuilder
     *            properties to be set for setting
     */
    public DatabaseConfiguration(final File paramFile) {
        mBinaryVersion = BINARY;
        mFile = paramFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DatabaseConfiguration, ");
        builder.append("File: ");
        builder.append(this.mFile);
        return builder.toString();
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
    public int hashCode() {
        final int prime = 72277;
        int result = 13;
        result = prime * result + mFile.hashCode();
        result = prime * result + mBinaryVersion.hashCode();
        return result;
    }

    public File getConfigFile() {
        return new File(mFile, Paths.ConfigBinary.getFile().getName());
    }

}
