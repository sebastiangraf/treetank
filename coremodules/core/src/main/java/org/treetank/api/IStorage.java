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

import java.io.File;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * 
 * This interface describes storage-instances handled by Treetank. A {@link IStorage} is a persistent place
 * where all data is stored.
 * 
 * Resources must be created within this {@link IStorage} with the help of {@link ResourceConfiguration}s.
 * 
 * 
 * <code>
 *      //Creating the Storage
 *      Storage.createDatabase(new StorageConfiguration(FILE));
 *      final IStorage storage = Storage.openStorage(FILE);
 *      
 *      //Getting a ResourceConfiguration over Guice.
 *      storage.createResource(mResourceConfig);
 * 
 * </code> The access to the data is done with the help of {@link ISession}s:
 * 
 * <code>
 *      //Ensure, storage and resources are created
 *      final IStorage storage = Storage.openStorage(FILE);
 *      final ISession session =
 *           storage.getSession(new SessionConfiguration(RESOURCENAME, KEY));
 *      final IPageReadTrx pRtx = session.beginPageReadTransaction(REVISION);
 *      final IPageWriteTrx pWtx = session.beginPageWriteTransaction();
 * </code>
 * 
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IStorage {
    /**
     * Creation of a resource. Since databases can consist out of several
     * resources, those can be created within this method. This includes the
     * creation of a suitable folder structure as well as the serialization of
     * the configuration of this resource.
     * 
     * @param pResConf
     *            the config of the resource
     * @return boolean with true if successful, false otherwise
     * @throws TTIOException
     *             if anything happens while creating the resource
     */
    boolean createResource(final ResourceConfiguration pResConf) throws TTException;

    /**
     * Getting the session associated within this database.
     * 
     * @param pSessionConf
     *            {@link SessionConfiguration} reference
     * @throws TTException
     *             if can't get session
     * @return the session
     */
    ISession getSession(final SessionConfiguration pSessionConf) throws TTException;

    /**
     * Truncating a resource. This includes the removal of all data stored
     * within this resource.
     * 
     * @param pResConf
     *            storing the name of the resource
     * @throws TTException
     *             if anything weird happens
     */
    boolean truncateResource(final SessionConfiguration pResConf) throws TTException;

    /**
     * Is the resource within this database existing?
     * 
     * @param pResourceName
     *            ot be checked
     * @return true, if existing; false otherwise
     */
    boolean existsResource(final String pResourceName);

    /**
     * Listing all resources within this database.
     * 
     * @return all resources
     */
    String[] listResources();

    /**
     * Getting the file location of this database.
     * 
     * @return the File of this database
     */
    File getLocation();

    /**
     * Closing the database for further access.
     * 
     * @return true if successful, false otherwise
     * @throws TTException
     *             if anything happens within treetank.
     */
    boolean close() throws TTException;

}
