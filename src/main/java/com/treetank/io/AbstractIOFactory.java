package com.treetank.io;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.treetank.exception.TreetankIOException;
import com.treetank.io.berkeley.BerkeleyFactory;
import com.treetank.io.file.FileFactory;
import com.treetank.session.SessionConfiguration;
import com.treetank.settings.ESettable;

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
    protected final transient SessionConfiguration config;

    /**
     * Protected constructor, just setting the sessionconfiguration.
     * 
     * @param paramSession
     *            to be set
     */
    protected AbstractIOFactory(final SessionConfiguration paramSession) {
        config = paramSession;
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
        FACTORIES.remove(this.config);
    }

    protected abstract void closeConcreteStorage() throws TreetankIOException;

    /**
     * Getting an AbstractIOFactory instance.
     * 
     * @param conf
     *            with settings for the storage.
     * @return an instance of this factory based on the kind in the conf
     */
    public final static AbstractIOFactory getInstance(
            final SessionConfiguration conf) throws TreetankIOException {
        AbstractIOFactory fac = null;
        if (FACTORIES.containsKey(conf)) {
            fac = FACTORIES.get(conf);
        } else {
            final AbstractIOFactory.StorageType storageType = (AbstractIOFactory.StorageType) conf
                    .getProps().get(ESettable.STORAGE_TYPE.getName());
            switch (storageType) {
            case File:
                fac = new FileFactory(conf);
                break;
            case Berkeley:
                fac = new BerkeleyFactory(conf);
                break;
            default:
                throw new IllegalArgumentException(new StringBuilder("Type ")
                        .append(storageType.toString()).append(" not valid!")
                        .toString());
            }
            FACTORIES.put(conf, fac);
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
