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

package org.treetank.io;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.SessionConfiguration;
import org.treetank.exception.TTIOException;
import org.treetank.io.berkeley.BerkeleyFactory;
import org.treetank.io.file.FileFactory;

/**
 * Abstract Factory to build up a concrete storage for the data. The Abstract
 * Instance must provide Reader and Writers as well as some additional methods.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbsIOFactory {

    /** Type for different storages. */
    public enum StorageType {
        /** File Storage. */
        File,
        /** Berkeley Storage. */
        Berkeley
    }

    /** Folder to store the data */
    public final File mFile;

    /**
     * Concurrent storage for all avaliable databases in runtime.
     */
    private static final Map<SessionConfiguration, AbsIOFactory> FACTORIES =
        new ConcurrentHashMap<SessionConfiguration, AbsIOFactory>();

    /**
     * Config for the session holding information about the settings of the
     * session.
     */
    protected final transient SessionConfiguration mSessionConfig;

    /**
     * Config for the database holding information about the location of the storage.
     */
    protected final transient DatabaseConfiguration mDatabaseConfig;

    /**
     * Protected constructor, just setting the sessionconfiguration.
     * 
     * @param paramSession
     *            to be set
     * @param paramDatabase
     *            to be set
     */
    protected AbsIOFactory(final File paramFile, final DatabaseConfiguration paramDatabase,
        final SessionConfiguration paramSession) {
        mSessionConfig = paramSession;
        mDatabaseConfig = paramDatabase;
        this.mFile = paramFile;
    }

    /**
     * Getting a writer.
     * 
     * @return an {@link IWriter} instance
     * @throws TTIOException
     *             if the initalisation fails
     */
    public abstract IWriter getWriter() throws TTIOException;

    /**
     * Getting a reader.
     * 
     * @return an {@link IReader} instance
     * @throws TTIOException
     *             if the initalisation fails
     */
    public abstract IReader getReader() throws TTIOException;

    /**
     * Getting a Closing this storage. Is equivalent to Session.close
     * 
     * @throws TTIOException
     *             exception to be throwns
     */
    public final void closeStorage() throws TTIOException {
        closeConcreteStorage();
        FACTORIES.remove(this.mSessionConfig);
    }

    /**
     * Closing concrete storage.
     * 
     * @throws TTIOException
     *             if anything weird happens
     */
    protected abstract void closeConcreteStorage() throws TTIOException;

    public static final void registerInstance(final File paramFile,
        final DatabaseConfiguration paramDatabaseConf, final SessionConfiguration paramSessionConf)
        throws TTIOException {
        AbsIOFactory fac = null;
        if (!FACTORIES.containsKey(paramSessionConf)) {
            final AbsIOFactory.StorageType storageType = paramDatabaseConf.mType;
            switch (storageType) {
            case File:
                fac = new FileFactory(paramFile, paramDatabaseConf, paramSessionConf);
                break;
            case Berkeley:
                fac = new BerkeleyFactory(paramFile, paramDatabaseConf, paramSessionConf);
                break;
            default:
                throw new TTIOException("Type", storageType.toString(), "not valid!");
            }
            FACTORIES.put(paramSessionConf, fac);
        }
    }

    /**
     * Getting an AbstractIOFactory instance.
     * !!!MUST CALL REGISTERINSTANCE BEFOREHAND!!!!
     * 
     * @param paramDatabaseConf
     *            with settings for the storage.
     * @param paramSessionConf
     *            with settings for the session
     * @throws TTIOException
     *             If error
     * @return an instance of this factory based on the kind in the conf
     */
    public static final AbsIOFactory getInstance(final SessionConfiguration paramSessionConf)
        throws TTIOException {
        return FACTORIES.get(paramSessionConf);
    }

    /**
     * Check if storage exists.
     * 
     * @return true if storage holds data, false otherwise
     * @throws TTIOException
     *             if storage is not accessable
     */
    public abstract boolean exists() throws TTIOException;

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("factory keys: ").append(FACTORIES.keySet()).append("\n");
        builder.append("DatabaseConfig: ").append(mDatabaseConfig.toString()).append("\n");
        builder.append("SessionConfig: ").append(mSessionConfig.toString()).append("\n");
        // builder.append("exists: ").append(exists()).append("\n");
        return builder.toString();
    }
}
