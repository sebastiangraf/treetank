package org.treetank.access.conf;

import java.io.File;
import java.io.Serializable;

/**
 * <h1>IConfigureSerializable</h1>
 * 
 * <p>
 * This interface offers a convinient way to serialize configurations. This includes mainly the concrete
 * location of the settings-file where the settings should be serialized to.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IConfigureSerializable extends Serializable {

    /**
     * Getting the location of the file where the configuration should be
     * serialized to.
     * 
     * @return the location of the file.
     */
    File getConfigFile();

}
