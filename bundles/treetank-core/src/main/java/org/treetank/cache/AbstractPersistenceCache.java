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

package org.treetank.cache;

import java.io.File;

import org.slf4j.LoggerFactory;
import org.treetank.access.FileDatabase;
import org.treetank.access.DatabaseConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.exception.TTIOException;
import org.treetank.settings.EStoragePaths;
import org.treetank.utils.LogWrapper;

/**
 * Abstract class for holding all persistence caches. Each instance of this
 * class stores the data in a place related to the {@link DatabaseConfiguration} at a different subfolder.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbstractPersistenceCache implements ICache {

    /**
     * Place to store the data.
     */
    protected final File place;

    /**
     * Counter to give every instance a different place.
     */
    private static int counter;

    /**
     * Constructor with the place to store the data.
     * 
     * @param paramFile
     *            {@link File} which holds the place to store
     *            the data.
     */
    protected AbstractPersistenceCache(final File paramFile) {
        place =
            new File(paramFile, new StringBuilder(EStoragePaths.TRANSACTIONLOG.getFile().getName()).append(
                File.separator).append(counter).toString());
        place.mkdirs();
        counter++;
    }

    /**
     * {@inheritDoc}
     */
    public final void put(final long mKey, final NodePageContainer mPage) {
        try {
            putPersistent(mKey, mPage);
        } catch (final TTIOException exc) {
            throw new IllegalStateException(exc);
        }
    }

    /**
     * {@inheritDoc}
     * */
    public final void clear() {
        try {
            clearPersistent();
            for (final File file : place.listFiles()) {
                if (!file.delete()) {
                    throw new TTIOException("Couldn't delete!");
                }
            }
            if (!place.delete()) {
                throw new TTIOException("Couldn't delete!");
            }
        } catch (final TTIOException exc) {
            throw new IllegalStateException(exc);
        }
    }

    /**
     * {@inheritDoc}
     * */
    public final NodePageContainer get(final long mKey) {
        try {
            return getPersistent(mKey);
        } catch (final TTIOException exc) {
            throw new IllegalStateException(exc);
        }
    }

    /**
     * Clearing a persistent cache.
     * 
     * @throws TTIOException
     *             if something odd happens
     */
    public abstract void clearPersistent() throws TTIOException;

    /**
     * Putting a page into a persistent log.
     * 
     * @param mKey
     *            to be put
     * @param mPage
     *            to be put
     * @throws TTIOException
     *             if something odd happens
     */
    public abstract void putPersistent(final long mKey, final NodePageContainer mPage) throws TTIOException;

    /**
     * Getting a NodePage from the persistent cache.
     * 
     * @param mKey
     *            to get the page
     * @return the Nodepage to be fetched
     * @throws TTIOException
     *             if something odd happens.
     */
    public abstract NodePageContainer getPersistent(final long mKey) throws TTIOException;

}
