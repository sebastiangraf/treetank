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

    /** Type for different storages */
    public enum StorageType {
        File, Berkeley
    }

    /**
     * Concurrent storage for all avaliable databases in runtime
     */
    private final static Map<SessionConfiguration, AbstractIOFactory> FACTORIES = new ConcurrentHashMap<SessionConfiguration, AbstractIOFactory>();

    /**
     * Config for the session holding information about the location of the
     * storage
     */
    protected final transient SessionConfiguration sessionConfig;

    protected final transient DatabaseConfiguration databaseConfig;

    /**
     * Protected constructor, just setting the sessionconfiguration.
     * 
     * @param paramSession
     *            to be set
     */
    protected AbstractIOFactory(final DatabaseConfiguration paramDatabase,
            final SessionConfiguration paramSession) {
        sessionConfig = paramSession;
        databaseConfig = paramDatabase;
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
     * Getting a reader
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
        FACTORIES.remove(this.sessionConfig);
    }

    protected abstract void closeConcreteStorage() throws TreetankIOException;

    /**
     * Getting an AbstractIOFactory instance.
     * 
     * @param databaseConf
     *            with settings for the storage.
     * @param sessionConf
     *            with settings for the session
     * @return an instance of this factory based on the kind in the conf
     */
    public final static AbstractIOFactory getInstance(
            final DatabaseConfiguration databaseConf,
            final SessionConfiguration sessionConf) throws TreetankIOException {
        AbstractIOFactory fac = null;
        if (FACTORIES.containsKey(sessionConf)) {
            fac = FACTORIES.get(sessionConf);
        } else {
            final AbstractIOFactory.StorageType storageType = AbstractIOFactory.StorageType
                    .valueOf(databaseConf.getProps().getProperty(
                            EDatabaseSetting.STORAGE_TYPE.name()));
            switch (storageType) {
            case File:
                fac = new FileFactory(databaseConf, sessionConf);
                break;
            case Berkeley:
                fac = new BerkeleyFactory(databaseConf, sessionConf);
                break;
            default:
                throw new IllegalArgumentException(new StringBuilder("Type ")
                        .append(storageType.toString()).append(" not valid!")
                        .toString());
            }
            FACTORIES.put(sessionConf, fac);
        }
        return fac;
    }

    /**
     * Getting of all active {@link AbstractIOFactory} and related
     * {@link SessionConfiguration}s.
     * 
     * @return a {@link Map} with the {@link SessionConfiguration} and
     *         {@link AbstractIOFactory} pairs.
     */
    public final static Map<SessionConfiguration, AbstractIOFactory> getActiveFactories() {
        return FACTORIES;
    }

    /**
     * Check if storage exists
     * 
     * @return true if storage holds data, false otherwise
     * @throws TreetankIOException
     *             if storage is not accessable
     */
    public abstract boolean exists() throws TreetankIOException;
}
