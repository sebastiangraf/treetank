/**
 * 
 */
package com.treetank.constants;

import java.io.File;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 * 
 */
public enum EStorage {

    /** Folder for storage of data */
    TT( new File("tt")),
    /**Folder for transactionaction log*/
    TRANSACTIONLOG( new File("transactionLog"));
    ;

    private final File mFile;

    private EStorage(final File file) {
        mFile = file;
    }


    public File getFile() {
        return mFile;
    }

    public static boolean recursiveDelete(final File file) {
        if (file.isDirectory()) {
            for (final File child : file.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return file.delete();
    }
    
}
