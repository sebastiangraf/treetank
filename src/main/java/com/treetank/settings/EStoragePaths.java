/**
 * 
 */
package com.treetank.settings;

import java.io.File;

import com.treetank.exception.TreetankUsageException;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 * 
 */
public enum EStoragePaths {

    /** Folder for storage of data */
    TT( new File("tt")),
    /**Folder for transactionaction log*/
    TRANSACTIONLOG( new File("transactionLog"));
    ;

    private final File mFile;

    private EStoragePaths(final File file) {
        mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * @param file which should be deleted included descendants
     * @return true if delete is valid
     */
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
 
    
    
    /**
     * Checking if path is valid
     * 
     * @param file to be checked after the common structure
     * @throws TreetankUsageException if validation fails
     */
    public static void validateAndBuildPath(final File file)
            throws TreetankUsageException {
        boolean createTransactionLog = false;
        boolean createStorage = false;

        final File transactionLog = new File(file,
                EStoragePaths.TRANSACTIONLOG.getFile().getName());
        final File storage = new File(file, EStoragePaths.TT.getFile()
                .getName());
        if (file == null) {
            throw new TreetankUsageException(
                    "Path to TreeTank file must not be null");
        } else {
            if (!file.exists()) {
                file.mkdirs();
                createTransactionLog = true;
                createStorage = true;
            } else {
                if (file.isDirectory()) {
                    final File[] files = file.listFiles();
                    if (files != null) {
                        boolean foundTransactionLog = false;
                        boolean foundStorage = false;
                        for (File child : files) {
                            if (child.equals(transactionLog)) {
                                foundTransactionLog = true;
                            } else if (child.equals(storage)) {
                                foundStorage = true;
                            } else {
                                throw new TreetankUsageException(
                                        "Path to TreeTank file must be a directory with defined transactionlog/storage structure");
                            }
                            createTransactionLog = !foundTransactionLog;
                            createStorage = !foundStorage;
                        }

                    } else {
                        createTransactionLog = true;
                        createStorage = true;
                    }
                } else {
                    throw new TreetankUsageException(
                            "Path to TreeTank file must be n a directory");
                }
            }
        }
        if (createTransactionLog) {
            if (!transactionLog.mkdir()) {
                throw new TreetankUsageException(
                        "Path to TreeTank file must a directory");
            }
        } else {
            final File[] files = transactionLog.listFiles();
            if (files != null) {
                for (final File child : files) {
                    EStoragePaths.recursiveDelete(child);
                }
            }
        }
        if (createStorage) {
            if (!storage.mkdir()) {
                throw new TreetankUsageException(
                        "Path to TreeTank file must a directory");
            }
        }
    }
    
}
