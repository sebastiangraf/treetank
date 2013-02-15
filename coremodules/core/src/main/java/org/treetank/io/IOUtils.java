package org.treetank.io;

import java.io.File;
import java.io.IOException;

import org.treetank.access.conf.IConfigurationPath;
import org.treetank.exception.TTIOException;

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
     * Creating a folder structure based on a set of paths given as parameter and returning a boolean
     * determining the success.
     * 
     * @param pFile
     *            the root folder where the configuration should be created to.
     * @param pPaths
     *            to be created
     * @return true if creations was successful, false otherwise.
     * @throws TTIOException
     */
    public static boolean createFolderStructure(final File pFile, IConfigurationPath[] pPaths)
        throws TTIOException {
        boolean returnVal = true;
        pFile.mkdir();
        // creation of folder structure
        for (IConfigurationPath paths : pPaths) {
            final File toCreate = new File(pFile, paths.getFile().getName());
            if (paths.isFolder()) {
                returnVal = toCreate.mkdir();
            } else {
                try {
                    returnVal = toCreate.createNewFile();
                } catch (final IOException exc) {
                    throw new TTIOException(exc);
                }
            }
            if (!returnVal) {
                break;
            }
        }
        return returnVal;
    }

    /**
     * Checking a structure in a folder to be equal with the data in this
     * enum.
     * 
     * @param pFile
     *            to be checked
     * @param pPaths
     *            containing the elements to be checked against
     * @return -1 if less folders are there, 0 if the structure is equal to
     *         the one expected, 1 if the structure has more folders
     */
    public static int compareStructure(final File pFile, IConfigurationPath[] pPaths) {
        int existing = 0;
        for (final IConfigurationPath path : pPaths) {
            final File currentFile = new File(pFile, path.getFile().getName());
            if (currentFile.exists()) {
                existing++;
            }
        }
        return existing - pPaths.length;
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
