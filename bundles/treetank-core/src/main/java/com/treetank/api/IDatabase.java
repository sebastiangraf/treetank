/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.api;

import java.io.File;

import com.treetank.exception.TreetankException;

/**
 * This interface describes database instances handled by treetank. A database
 * is a persistent place where all data is stored. The access to the data is
 * done with the help of {@link ISession}s.
 * 
 * Furthermore, databases can be created with the help of {@link DatabaseConfiguration}s. After creation, the
 * settings of a database
 * cannot be changed.
 * 
 * 
 * <h2>Usage Example</h2>
 * 
 * <p>
 * 
 * <pre>
 * // Simple session with standards as defined in <code>EDatabaseSetting</code> and 
 * <code>ESessionSetting</code>. Creation takes place in open-process
 * final IDatabase database = Database.openDatabase(&quot;examplek&quot;);
 * final ISession session = database.getSession()
 * 
 * // Database with berkeley db and incremental revisioning
 * final Properties dbProps = new Properties();
 * dbProps.setProperty(STORAGE_TYPE.name(),StorageType.Berkeley.name());
 * dbProps.setProperty(REVISION_TYPE.name(), ERevisioning.INCREMENTAL.name());
 * final DatabaseConfiguration dbConfig = new DatabaseConfiguration(&quot;example&quot;, dbProps);
 * Database.create(dbConfig);
 * final IDatabase database = Database.openDatabase(&quot;examplek&quot;);
 * final ISession session = database.getSession();
 * </pre>
 * 
 * </p>
 * 
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
     * @throws TreetankException
     *             if can't get session.
     * @return the database
     */
    ISession getSession() throws TreetankException;

    /**
     * Closing the database for further operations.
     * 
     * @throws TreetankException
     *             if close is not valid
     */
    void close() throws TreetankException;

}
