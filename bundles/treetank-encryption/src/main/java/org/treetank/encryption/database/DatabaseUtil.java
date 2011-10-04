package org.treetank.encryption.database;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.treetank.exception.TTEncryptionException;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

/**
 * This class acts as centralized Utility-Class for all berkeleyDB-database
 * operations.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class DatabaseUtil {

    /**
     * Berkeley Environment for the database.
     */
    protected final Environment mEnv;

    /**
     * Berkeley Entity store instance for the database.
     */
    protected final EntityStore mStore;

    /**
     * Place for storing the stuff.
     */
    protected final File mPlace;

    /**
     * Counter to give every instance a different place.
     */
    private static AtomicInteger counter = new AtomicInteger();

    /**
     * Constructor. Building up the berkeley db and setting necessary settings.
     * 
     * @param paramFile
     *            the place where the berkeley db is stored.
     */
    public DatabaseUtil(final File paramFile, final String paramName) {
        mPlace =
            new File(paramFile, new StringBuilder(new File("keyselector").getName()).append(File.separator)
                .append(counter.incrementAndGet()).toString());
        mPlace.mkdirs();

        final EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);

        final DatabaseConfig conf = new DatabaseConfig();
        conf.setTransactional(true);
        conf.setKeyPrefixing(true);

        mEnv = new Environment(paramFile, environmentConfig);

        final StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(true);
        mStore = new EntityStore(mEnv, paramName, storeConfig);

    }

    /**
     * Clearing the database. That is removing all elements
     * 
     * @throws TTEncryptionException
     */
    public final void clearPersistent() throws TTEncryptionException {
        for (final File file : mPlace.listFiles()) {
            if (!file.delete()) {
                throw new TTEncryptionException("Couldn't delete!");
            }
        }
        if (!mPlace.delete()) {
            throw new TTEncryptionException("Couldn't delete!");
        }
        if (mStore != null) {
            mStore.close();
        }
        if (mEnv != null) {
            mEnv.close();
        }

    }

}
