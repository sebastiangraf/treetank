/**
 * 
 */
package com.treetank.settings;

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

    /** Folder for storage of data */
    TT(new File("tt"), true),
    /** Folder for transaction log */
    TRANSACTIONLOG(new File("transactionLog"), true),
    /** File to store the db settings */
    DBSETTINGS(new File("dbsettings.properties"), false);

    private final File mFile;

    private final boolean mIsFolder;

    private EStoragePaths(final File file, final boolean isFolder) {
        mFile = file;
        mIsFolder = isFolder;
    }

    /**
     * Getting the file for the kind-
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
     * Checking a structure in a folder to be equal with the data in this enum
     * 
     * @param file
     *            to be checked
     * @return -1 if less folders are there, 0 if the structure is equal to the
     *         one expected, 1 if the structure has more folders
     */
    public static int compareStructure(final File file) {
        int existing = 0;
        for (final EStoragePaths paths : values()) {
            final File currentFile = new File(file, paths.getFile().getName());
            if (currentFile.exists()) {
                existing++;
            }
        }
        return existing - values().length;
    }

}
