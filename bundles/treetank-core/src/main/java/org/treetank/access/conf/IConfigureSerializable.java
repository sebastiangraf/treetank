package org.treetank.access.conf;

import java.io.File;
import java.io.Serializable;

public interface IConfigureSerializable extends Serializable {

    File getConfigFile();

}
