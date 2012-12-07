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

package org.treetank.io.berkeley;

import java.io.File;
import java.util.Properties;

import org.treetank.access.conf.ContructorProps;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IOUtils;
import org.treetank.io.IBackendReader;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendWriter;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;
import org.treetank.page.interfaces.IPage;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Factory class to build up {@link IBackendReader} {@link IBackendWriter} instances for the
 * Treetank Framework.
 * 
 * After all this class is implemented as a Singleton to hold one {@link BerkeleyStorage} per
 * {@link SessionConfiguration}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyStorage implements IBackend {

    /** Name for the database. */
    private static final String NAME = "berkeleyDatabase";

    /** Berkeley Environment for the database. */
    private Environment mEnv = null;

    /** Storage instance per session. */
    private Database mDatabase = null;

    /** Binding for de/-serializing pages. */
    private final TupleBinding<IPage> mPageBinding;

    /** Handling the byte-representation before serialization. */
    private final IByteHandlerPipeline mByteHandler;

    /** Factory for Pages. */
    private final PageFactory mFac;

    /** File for DB. */
    private final File mFile;

    /**
     * Private constructor.
     * 
     * @param pProperties
     *            not only the file associated with the database
     * @param pNodeFac
     *            factory for the nodes
     * @param pByteHandler
     *            handling any bytes
     * @throws TTIOException
     *             of something odd happens while database-connection
     */
    @Inject
    public BerkeleyStorage(@Assisted Properties pProperties, INodeFactory pNodeFac,
        IByteHandlerPipeline pByteHandler) throws TTIOException {

        mFile =
            new File(new File(new File(pProperties.getProperty(ContructorProps.STORAGEPATH),
                StorageConfiguration.Paths.Data.getFile().getName()), pProperties
                .getProperty(ContructorProps.RESOURCE)), ResourceConfiguration.Paths.Data.getFile().getName());

        mPageBinding = new PageBinding();
        mByteHandler = pByteHandler;
        mFac = new PageFactory(pNodeFac);

    }

    /**
     * Setting up the database after having the storage object. Necessary because folder creation takes not
     * place within the storage but within the ResourceConfiguration.
     * 
     * @throws TTIOException
     */
    private synchronized final void setUpIfNecessary() throws TTIOException {
        final DatabaseConfig conf = generateDBConf();
        final EnvironmentConfig config = generateEnvConf();

        if (mEnv == null) {

            if (mFile.listFiles().length == 0) {
                conf.setAllowCreate(true);
                config.setAllowCreate(true);
            }

            try {
                mEnv = new Environment(mFile, config);
                mDatabase = mEnv.openDatabase(null, NAME, conf);
            } catch (final DatabaseException exc) {
                throw new TTIOException(exc);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBackendReader getReader() throws TTIOException {
        try {
            setUpIfNecessary();
            return new BerkeleyReader(mDatabase, mEnv.beginTransaction(null, null), mPageBinding);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBackendWriter getWriter() throws TTIOException {
        setUpIfNecessary();
        return new BerkeleyWriter(mEnv, mDatabase, mPageBinding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        try {
            setUpIfNecessary();
            mDatabase.close();
            mEnv.close();
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TTIOException {
        setUpIfNecessary();
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        boolean returnVal = false;
        try {
            final IBackendReader backendReader =
                new BerkeleyReader(mDatabase, mEnv.beginTransaction(null, null), mPageBinding);
            TupleBinding.getPrimitiveBinding(Long.class).objectToEntry(-1l, keyEntry);

            final OperationStatus status = mDatabase.get(null, keyEntry, valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                returnVal = true;
            }
            backendReader.close();
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
        return returnVal;

    }

    private static EnvironmentConfig generateEnvConf() {
        final EnvironmentConfig config = new EnvironmentConfig();
        config.setTransactional(true);
        config.setCacheSize(1024 * 1024);
        return config;
    }

    private static DatabaseConfig generateDBConf() {
        final DatabaseConfig conf = new DatabaseConfig();
        conf.setTransactional(true);
        conf.setKeyPrefixing(true);
        return conf;
    }

    @Override
    public IByteHandlerPipeline getByteHandler() {
        return mByteHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void truncate() throws TTException {
        IOUtils.recursiveDelete(mFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BerkeleyStorage [mByteHandler=");
        builder.append(mByteHandler);
        builder.append(", mFile=");
        builder.append(mFile);
        builder.append(", mFac=");
        builder.append(mFac);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Binding for storing {@link IPage} objects within the Berkeley DB.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class PageBinding extends TupleBinding<IPage> {

        /**
         * {@inheritDoc}
         */
        @Override
        public IPage entryToObject(final TupleInput arg0) {
            final ByteArrayDataOutput data = ByteStreams.newDataOutput();
            int result = arg0.read();
            while (result != -1) {
                byte b = (byte)result;
                data.write(b);
                result = arg0.read();
            }
            byte[] resultBytes;
            try {
                resultBytes = mByteHandler.deserialize(data.toByteArray());
                return mFac.deserializePage(resultBytes);
            } catch (TTByteHandleException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void objectToEntry(final IPage arg0, final TupleOutput arg1) {
            final byte[] pagebytes = arg0.getByteRepresentation();
            try {
                arg1.write(mByteHandler.serialize(pagebytes));
            } catch (TTByteHandleException e) {
                e.printStackTrace();
            }
        }

    }

}
