package com.treetank.io.berkeley;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.treetank.io.AbstractIOFactory;
import com.treetank.io.AbstractKey;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.io.StorageProperties;
import com.treetank.io.TreetankIOException;
import com.treetank.io.berkeley.binding.AbstractPageBinding;
import com.treetank.io.berkeley.binding.KeyBinding;
import com.treetank.io.berkeley.binding.PageReferenceBinding;
import com.treetank.io.berkeley.binding.StoragePropTupleBinding;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;
import com.treetank.session.SessionConfiguration;

/**
 * Factory class to build up {@link IReader} {@link IWriter} instances for the
 * Treetank Framework.
 * 
 * After all this class is implemented as a Singleton to hold one
 * {@link BerkeleyFactory} per {@link SessionConfiguration}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyFactory extends AbstractIOFactory {

    /** Binding for {@link AbstractKey} */
    public static final TupleBinding<AbstractKey> KEY = new KeyBinding();

    /** Binding for {@link StorageProperties} */
    public static final TupleBinding<StorageProperties> PROPS_VAL_B = new StoragePropTupleBinding();

    /** Binding for {@link AbstractPage} */
    public static final TupleBinding<AbstractPage> PAGE_VAL_B = new AbstractPageBinding();

    /** Binding for {@link PageReference<AbstractPage>} */
    public static final TupleBinding<PageReference<AbstractPage>> FIRST_REV_VAL_B = new PageReferenceBinding();

    /** Binding for {@link Long} */
    public static final TupleBinding<Long> DATAINFO_VAL_B = TupleBinding
            .getPrimitiveBinding(Long.class);

    /**
     * Berkeley Environment for the database
     */
    private transient final Environment env;

    /**
     * Database instance per session
     */
    private transient final Database mDatabase;

    /**
     * Name for the database.
     */
    private final static String NAME = "berkeleyDatabase";

    /**
     * Concurrent storage for all avaliable databases in runtime
     */
    private static Map<SessionConfiguration, BerkeleyFactory> fac = new ConcurrentHashMap<SessionConfiguration, BerkeleyFactory>();

    /**
     * Private constructor.
     * 
     * @param paramSession
     *            for the settings
     * @throws TreetankIOException
     *             of something odd happens while database-connection
     */
    private BerkeleyFactory(final SessionConfiguration paramSession)
            throws TreetankIOException {
        super(paramSession);

        final DatabaseConfig conf = new DatabaseConfig();
        conf.setTransactional(true);

        final EnvironmentConfig config = new EnvironmentConfig();
        config.setTransactional(true);

        final File repoFile = new File(paramSession + File.separator + "tt");
        if (!repoFile.exists()) {
            repoFile.mkdirs();
            conf.setAllowCreate(true);
            config.setAllowCreate(true);
        }

        try {
            env = new Environment(repoFile, config);

            mDatabase = env.openDatabase(null, NAME, conf);
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }

    }

    /**
     * Getting one instance for a setting
     * 
     * @param conf
     *            setting for the database
     * @return {@link BerkeleyFactory} normally
     * @throws TreetankIOException
     *             if something odd happens
     */
    public static BerkeleyFactory getInstanceForBerkeley(
            final SessionConfiguration conf) throws TreetankIOException {
        BerkeleyFactory fact = fac.get(conf);
        if (fact == null) {
            fact = new BerkeleyFactory(conf);
            fac.put(conf, fact);
        }
        return fact;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReader getReader() throws TreetankIOException {
        try {
            return new BerkeleyReader(env, mDatabase);
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWriter getWriter() throws TreetankIOException {
        return new BerkeleyWriter(env, mDatabase);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeStorage() throws TreetankIOException {

        try {
            mDatabase.close();
            env.close();
            fac.remove(this.config);
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TreetankIOException {
        try {
            return mDatabase.count() > 0;
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }
    }

}
