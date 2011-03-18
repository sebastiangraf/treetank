/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.settings;

import java.io.File;

/**
 * 
 * Enumeration for determining all storage relevant stuff like paths to
 * different databases, etc.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 * 
 */
public enum EStoragePaths {

    /** Folder for storage of data. */
    TT(new File("tt"), true),
    /** Folder for transaction log. */
    TRANSACTIONLOG(new File("transactionLog"), true),
    /** File to store the db settings. */
    DBSETTINGS(new File("dbsettings.properties"), false),
    /** File to store encryption db settings */
    ENCRYPTIONSTORE(new File("encryption"), true);

    private final File mFile;

    private final boolean mIsFolder;

    private EStoragePaths(final File mFile, final boolean mIsFolder) {
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
     * Checking a structure in a folder to be equal with the data in this enum.
     * 
     * @param mFile
     *            to be checked
     * @return -1 if less folders are there, 0 if the structure is equal to the
     *         one expected, 1 if the structure has more folders
     */
    public static int compareStructure(final File mFile) {
        int existing = 0;
        for (final EStoragePaths paths : values()) {
            final File currentFile = new File(mFile, paths.getFile().getName());
            if (currentFile.exists()) {
                existing++;
            }
        }
        return existing - values().length;
    }

}
