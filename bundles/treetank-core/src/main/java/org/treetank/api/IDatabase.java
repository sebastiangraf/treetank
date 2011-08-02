/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.api;

import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.SessionConfiguration;
import org.treetank.exception.AbsTTException;

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
 * // Database with berkeley db and incremental revisioning.
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
     * Get the version of the TreeTank.
     * 
     * @return TreeTank version
     */
    String getVersion();

    /**
     * Getting the session associated within this database.
     * 
     * @param paramBuilder
     *            {@link SessionConfiguration.Builder} reference
     * @throws AbsTTException
     *             if can't get session
     * @return the database
     */
    ISession getSession(final SessionConfiguration.Builder paramBuilder) throws AbsTTException;

    /**
     * Get the database configuration.
     * 
     * @return the {@link DatabaseConfiguration} reference
     */
    DatabaseConfiguration getDatabaseConf();

}
