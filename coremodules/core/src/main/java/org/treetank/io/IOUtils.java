package org.treetank.io;

import java.io.File;

/**
 * Public class for having utility methods related to the storage in one class.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IOUtils {

    /** Should not be instantiable. */
    private IOUtils() {
    }

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param pFile
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    public static boolean recursiveDelete(final File pFile) {
        if (pFile.isDirectory()) {
            for (final File child : pFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return pFile.delete();
    }

}
