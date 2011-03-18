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
package org.treetank.cache;

import java.io.File;


import org.slf4j.LoggerFactory;
import org.treetank.access.Database;
import org.treetank.access.DatabaseConfiguration;
import org.treetank.exception.TTIOException;
import org.treetank.settings.EStoragePaths;
import org.treetank.utils.LogWrapper;

/**
 * Abstract class for holding all persistence caches. Each instance of this
 * class stores the data in a place related to the {@link SessionConfiguration} at a different subfolder.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbstractPersistenceCache implements ICache {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(Database.class));

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
     * @param paramConfig
     *            {@link SessionConfiguration} which holds the place to store
     *            the data.
     */
    protected AbstractPersistenceCache(final DatabaseConfiguration paramConfig) {
        place =
            new File(paramConfig.getFile(), new StringBuilder(EStoragePaths.TRANSACTIONLOG.getFile()
                .getName()).append(File.separator).append(counter).toString());
        if (!place.mkdirs()) {
            LOGWRAPPER.error("Couldn't create directory for " + place);
        }
        counter++;
    }

    /**
     * {@inheritDoc}
     */
    public final void put(final long mKey, final NodePageContainer mPage) {
        try {
            putPersistent(mKey, mPage);
        } catch (final TTIOException exc) {
            LOGWRAPPER.error(exc);
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
            LOGWRAPPER.error(exc);
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
            LOGWRAPPER.error(exc);
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
    public abstract void putPersistent(final long mKey, final NodePageContainer mPage)
        throws TTIOException;

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
