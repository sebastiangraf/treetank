package org.treetank.access.conf;

import java.io.File;

/**
 * Interface for convinient accessing of paths within Storage-/ and ResourceConfigurations.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IConfigurationPath {
    /** Getting the File represention of a path. */
    File getFile();

    /** Check if the path is a folder. */
    boolean isFolder();
}
