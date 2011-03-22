/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.io.berkeley;

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

import org.slf4j.LoggerFactory;
import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.SessionConfiguration;
import org.treetank.exception.TTIOException;
import org.treetank.io.AbsIOFactory;
import org.treetank.io.AbsKey;
import org.treetank.io.IReader;
import org.treetank.io.IWriter;
import org.treetank.io.berkeley.binding.AbstractPageBinding;
import org.treetank.io.berkeley.binding.KeyBinding;
import org.treetank.io.berkeley.binding.PageReferenceUberPageBinding;
import org.treetank.page.AbsPage;
import org.treetank.page.PageReference;
import org.treetank.settings.EStoragePaths;
import org.treetank.utils.LogWrapper;

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

    /** Binding for {@link AbsPage}. */
    public static final TupleBinding<AbsPage> PAGE_VAL_B = new AbstractPageBinding();

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