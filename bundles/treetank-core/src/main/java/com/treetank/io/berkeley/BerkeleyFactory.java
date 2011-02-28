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
import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TTIOException;
import com.treetank.io.AbsIOFactory;
import com.treetank.io.AbsKey;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.io.berkeley.binding.AbstractPageBinding;
import com.treetank.io.berkeley.binding.KeyBinding;
import com.treetank.io.berkeley.binding.PageReferenceUberPageBinding;
import com.treetank.page.AbsStractPage;
import com.treetank.page.PageReference;
import com.treetank.settings.EStoragePaths;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * Factory class to build up {@link IReader} {@link IWriter} instances for the
 * Treetank Framework.
 * 
 * After all this class is implemented as a Singleton to hold one {@link BerkeleyFactory} per
 * {@link SessionConfiguration}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyFactory extends AbsIOFactory {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(BerkeleyFactory.class));

    /** Binding for {@link AbsKey}. */
    public static final TupleBinding<AbsKey> KEY = new KeyBinding();

    /** Binding for {@link AbsStractPage}. */
    public static final TupleBinding<AbsStractPage> PAGE_VAL_B = new AbstractPageBinding();

    /** Binding for {@link PageReference}. */
    public static final TupleBinding<PageReference> FIRST_REV_VAL_B = new PageReferenceUberPageBinding();

    /** Binding for {@link Long}. */
    public static final TupleBinding<Long> DATAINFO_VAL_B = TupleBinding.getPrimitiveBinding(Long.class);

    /**
     * Berkeley Environment for the database.
     */
    private transient final Environment mEnv;

    /**
     * Database instance per session.
     */
    private transient final Database mDatabase;

    /**
     * Name for the database.
     */
    private final static String NAME = "berkeleyDatabase";

    /**
     * Private constructor.
     * 
     * @param mParamDatabase
     *            for the Database settings
     * @param paramSession
     *            for the settings
     * @throws TTIOException
     *             of something odd happens while database-connection
     */
    public BerkeleyFactory(final DatabaseConfiguration mParamDatabase, final SessionConfiguration paramSession)
        throws TTIOException {
        super(mParamDatabase, paramSession);

        final DatabaseConfig conf = new DatabaseConfig();
        conf.setTransactional(true);
        conf.setKeyPrefixing(true);

        final EnvironmentConfig config = new EnvironmentConfig();
        config.setTransactional(true);
        config.setCacheSize(1024 * 1024);

        final File repoFile = new File(mParamDatabase.getFile(), EStoragePaths.TT.getFile().getName());
        if (!repoFile.exists()) {
            repoFile.mkdirs();
        }
        if (repoFile.listFiles().length == 0
            || (repoFile.listFiles().length == 1 && "tt.tnk".equals(repoFile.listFiles()[0].getName()))) {
            conf.setAllowCreate(true);
            config.setAllowCreate(true);
        }

        try {
            mEnv = new Environment(repoFile, config);

            mDatabase = mEnv.openDatabase(null, NAME, conf);
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReader getReader() throws TTIOException {
        try {
            return new BerkeleyReader(mEnv, mDatabase);
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWriter getWriter() throws TTIOException {
        return new BerkeleyWriter(mEnv, mDatabase);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConcreteStorage() throws TTIOException {
        try {
            mDatabase.close();
            mEnv.close();
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TTIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        boolean returnVal = false;
        try {
            final IReader reader = new BerkeleyReader(mEnv, mDatabase);
            BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(), keyEntry);

            final OperationStatus status = mDatabase.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                returnVal = true;
            }
            reader.close();
        } catch (final DatabaseException exc) {
            LOGWRAPPER.error(exc);
            throw new TTIOException(exc);
        }
        return returnVal;

    }

}
