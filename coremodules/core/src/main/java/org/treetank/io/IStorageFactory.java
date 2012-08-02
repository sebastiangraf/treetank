package org.treetank.io;

import java.io.File;

public interface IStorageFactory {

    IStorage create(File pFile);

}
