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

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.treetank.access.conf.ConstructorProps;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.bucket.BucketFactory;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendReader;
import org.treetank.io.IBackendWriter;
import org.treetank.io.IOUtils;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

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
    private Environment mEnv;

    /** Storage instance per session. */
    private Database mDatabase;

    /** Binding for de/-serializing buckets. */
    private final TupleBinding<IBucket> mBucketBinding;

    /** Handling the byte-representation before serialization. */
    private final IByteHandlerPipeline mByteHandler;

    /** Factory for Buckets. */
    private final BucketFactory mFac;

    /** File for DB. */
    private final File mFile;

    /**
     * Simple constructor.
     * 
     * @param pProperties
     *            not only the file associated with the database
     * @param pNodeFac
     *            factory for the nodes
     * @param pMetaFac
     *            factory for meta bucket
     * @param pByteHandler
     *            handling any bytes
     * @throws TTIOException
     *             of something odd happens while database-connection
     */
    @Inject
    public BerkeleyStorage(@Assisted Properties pProperties, INodeFactory pNodeFac,
        IMetaEntryFactory pMetaFac, IByteHandlerPipeline pByteHandler) throws TTIOException {

        mFile =
            new File(pProperties.getProperty(ConstructorProps.RESOURCEPATH), ResourceConfiguration.Paths.Data
                .getFile().getName());

        mBucketBinding = new BucketBinding();
        mByteHandler = pByteHandler;
        mFac = new BucketFactory(pNodeFac, pMetaFac);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() throws TTIOException {
        try {
            EnvironmentConfig config = new EnvironmentConfig();
            config = config.setSharedCache(true);
            config.setAllowCreate(true);
            config.setTransactional(true);
            config.setCachePercent(20);
            mEnv = new Environment(mFile, config);

            final DatabaseConfig conf = new DatabaseConfig();
            conf.setAllowCreate(true);
            conf.setTransactional(true);

            mDatabase = mEnv.openDatabase(null, NAME, conf);

        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IBackendReader getReader() throws TTIOException {
        try {
            return new BerkeleyReader(mEnv, mDatabase, mBucketBinding);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IBackendWriter getWriter() throws TTIOException {
        return new BerkeleyWriter(mEnv, mDatabase, mBucketBinding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws TTIOException {
        try {
            mEnv.sync();
            mDatabase.close();
            mEnv.close();
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    @Override
    public IByteHandlerPipeline getByteHandler() {
        return mByteHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean truncate() throws TTException {
        mDatabase.close();
        if (mEnv.getDatabaseNames().contains(NAME)) {
            mEnv.removeDatabase(null, NAME);
        }
        mEnv.close();
        return IOUtils.recursiveDelete(mFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mByteHandler", mByteHandler).add("mFile", mFile).add("mFac", mFac)
            .toString();
    }

    /**
     * Binding for storing {@link IBucket} objects within the Berkeley DB.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class BucketBinding extends TupleBinding<IBucket>{

        /**
         * {@inheritDoc}
         */
        @Override
        public IBucket entryToObject(final TupleInput arg0) {
            try {
                final InputStream handledStream = mByteHandler.deserialize(arg0);
                final DataInput in = new DataInputStream(handledStream);
                final IBucket returnVal = mFac.deserializeBucket(in);
                handledStream.close();
                arg0.close();
                return returnVal;
            } catch (IOException | TTException exc) {
                throw new RuntimeException(exc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void objectToEntry(final IBucket arg0, final TupleOutput arg1) {
            try {
                final OutputStream handledStream = mByteHandler.serialize(arg1);
                final DataOutput output = new DataOutputStream(handledStream);
                arg0.serialize(output);
                handledStream.close();
                
                arg1.close();
            } catch (IOException | TTException exc) {
                throw new RuntimeException(exc);
            }
        }
    }

}
