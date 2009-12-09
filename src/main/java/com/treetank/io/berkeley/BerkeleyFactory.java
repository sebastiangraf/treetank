package com.treetank.io.berkeley;

import java.io.File;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.treetank.constants.EStorage;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.AbstractIOFactory;
import com.treetank.io.AbstractKey;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.io.StorageProperties;
import com.treetank.io.berkeley.binding.AbstractPageBinding;
import com.treetank.io.berkeley.binding.KeyBinding;
import com.treetank.io.berkeley.binding.PageReferenceUberPageBinding;
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

    /** Binding for {@link PageReference} */
    public static final TupleBinding<PageReference> FIRST_REV_VAL_B = new PageReferenceUberPageBinding();

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
     * Private constructor.
     * 
     * @param paramSession
     *            for the settings
     * @throws TreetankIOException
     *             of something odd happens while database-connection
     */
    public BerkeleyFactory(final SessionConfiguration paramSession)
            throws TreetankIOException {
        super(paramSession);

        final DatabaseConfig conf = new DatabaseConfig();
        conf.setTransactional(true);

        final EnvironmentConfig config = new EnvironmentConfig();
        config.setTransactional(true);
        config.setCacheSize(1024 * 1024);

        final File repoFile = new File(paramSession.getFile(),
                EStorage.TT.getFile().getName());
        if (repoFile.listFiles().length == 0
                || (repoFile.listFiles().length == 1 && "tt.tnk"
                        .equals(repoFile.listFiles()[0].getName()))) {
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
    public void closeConcreteStorage() throws TreetankIOException {
        try {
            mDatabase.close();
            env.close();
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TreetankIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        boolean returnVal = false;
        try {
            final IReader reader = new BerkeleyReader(env, mDatabase);
            BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(),
                    keyEntry);

            final OperationStatus status = mDatabase.get(null, keyEntry,
                    valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                returnVal = true;
            }
            reader.close();
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }
        return returnVal;

    }

}
