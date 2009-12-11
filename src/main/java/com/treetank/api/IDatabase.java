package com.treetank.api;

import java.io.File;

import com.treetank.exception.TreetankException;
import com.treetank.settings.EDatabaseSetting;

/**
 * This interface describes database instances handled by treetank. A database
 * is a persistent place where all data is stored. The access to the data is
 * done with the help of {@link ISession}s.
 * 
 * Furthermore, databases can be created with the help of
 * {@link EDatabaseSetting}s. After creation, the settings of a database cannot
 * be changed.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IDatabase {

    /**
     * Get file name of TreeTank database.
     * 
     * @return File name of TreeTank database.
     */
    File getFile();

    /**
     * Get the version of the TreeTank. The layout is as follows int[0]: version
     * major int[1]: version minor int[2]: version fix
     * 
     * @return Minor revision of TreeTank version.
     */
    int[] getVersion();

    /**
     * Getting the session associated within this database.
     * 
     * @return the database
     */
    ISession getSession();

    void close() throws TreetankException;

}
