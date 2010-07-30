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

package com.treetank.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.berkeley.BerkeleyFactory;
import com.treetank.io.file.FileFactory;
import com.treetank.settings.EDatabaseSetting;

/**
 * Abstract Factory to build up a concrete storage for the data. The Abstract
 * Instance must provide Reader and Writers as well as some additional methods.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbstractIOFactory {

    /** Type for different storages. */
    public enum StorageType {
        File, Berkeley
    }

    /**
     * Concurrent storage for all avaliable databases in runtime.
     */
    private final static Map<SessionConfiguration, AbstractIOFactory> FACTORIES =
        new ConcurrentHashMap<SessionConfiguration, AbstractIOFactory>();

    /**
     * Config for the session holding information about the location of the
     * storage.
     */
    protected final transient SessionConfiguration mSessionConfig;

    protected final transient DatabaseConfiguration mDatabaseConfig;

    /**
     * Protected constructor, just setting the sessionconfiguration.
     * 
     * @param paramSession
     *            to be set
     * @param paramDatabase
     *            to be set
     */
    protected AbstractIOFactory(final DatabaseConfiguration paramDatabase,
        final SessionConfiguration paramSession) {
        mSessionConfig = paramSession;
        mDatabaseConfig = paramDatabase;
    }

    /**
     * Getting a writer.
     * 
     * @return an {@link IWriter} instance
     * @throws TreetankIOException
     *             if the initalisation fails
     */
    public abstract IWriter getWriter() throws TreetankIOException;

    /**
     * Getting a reader.
     * 
     * @return an {@link IReader} instance
     * @throws TreetankIOException
     *             if the initalisation fails
     */
    public abstract IReader getReader() throws TreetankIOException;

    /**
     * Getting a Closing this storage. Is equivalent to Session.close
     * 
     * @throws TreetankIOException
     *             exception to be throwns
     */
    public final void closeStorage() throws TreetankIOException {
        closeConcreteStorage();
        FACTORIES.remove(this.mSessionConfig);
    }

    protected abstract void closeConcreteStorage() throws TreetankIOException;

    /**
     * Getting an AbstractIOFactory instance.
     * 
     * @param mDatabaseConf
     *            with settings for the storage.
     * @param mSessionConf
     *            with settings for the session
     * @throws TreetankIOException
     *            If error
     * @return an instance of this factory based on the kind in the conf
     */
    public final static AbstractIOFactory getInstance(final DatabaseConfiguration mDatabaseConf,
        final SessionConfiguration mSessionConf) throws TreetankIOException {
        AbstractIOFactory fac = null;
        if (FACTORIES.containsKey(mSessionConf)) {
            fac = FACTORIES.get(mSessionConf);
        } else {
            final AbstractIOFactory.StorageType storageType =
                AbstractIOFactory.StorageType.valueOf(mDatabaseConf.getProps().getProperty(
                    EDatabaseSetting.STORAGE_TYPE.name()));
            switch (storageType) {
            case File:
                fac = new FileFactory(mDatabaseConf, mSessionConf);
                break;
            case Berkeley:
                fac = new BerkeleyFactory(mDatabaseConf, mSessionConf);
                break;
            default:
                throw new TreetankIOException("Type", storageType.toString(), "not valid!");
            }
            FACTORIES.put(mSessionConf, fac);
        }
        return fac;
    }

    /**
     * Getting of all active {@link AbstractIOFactory} and related {@link SessionConfiguration}s.
     * 
     * @return a {@link Map} with the {@link SessionConfiguration} and {@link AbstractIOFactory} pairs.
     */
    public final static Map<SessionConfiguration, AbstractIOFactory> getActiveFactories() {
        return FACTORIES;
    }

    /**
     * Check if storage exists.
     * 
     * @return true if storage holds data, false otherwise
     * @throws TreetankIOException
     *             if storage is not accessable
     */
    public abstract boolean exists() throws TreetankIOException;
}
