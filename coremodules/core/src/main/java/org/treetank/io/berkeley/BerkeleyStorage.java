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
import java.util.Properties;

import org.treetank.access.conf.ContructorProps;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendReader;
import org.treetank.io.IBackendWriter;
import org.treetank.io.IOUtils;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;
import org.treetank.page.interfaces.IPage;

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
import com.sleepycat.je.Transaction;

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
    private final Environment mEnv;

    /** Transaction for DB-Operations. */
    private final Transaction mTxn;

    /** Storage instance per session. */
    private final Database mDatabase;

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
     * @param pMetaFac
     *            factory for meta page
     * @param pByteHandler
     *            handling any bytes
     * @throws TTIOException
     *             of something odd happens while database-connection
     */
    @Inject
    public BerkeleyStorage(@Assisted Properties pProperties, INodeFactory pNodeFac,
        IMetaEntryFactory pMetaFac, IByteHandlerPipeline pByteHandler) throws TTIOException {

        mFile =
            new File(new File(new File(pProperties.getProperty(ContructorProps.STORAGEPATH),
                StorageConfiguration.Paths.Data.getFile().getName()), pProperties
                .getProperty(ContructorProps.RESOURCE)), ResourceConfiguration.Paths.Data.getFile().getName());

        mPageBinding = new PageBinding();
        mByteHandler = pByteHandler;
        mFac = new PageFactory(pNodeFac, pMetaFac);
        try {
            final EnvironmentConfig config = new EnvironmentConfig();
            config.setTransactional(true);
            config.setCacheSize(1024 * 1024);
            config.setAllowCreate(true);
            mEnv = new Environment(mFile, config);

            final DatabaseConfig conf = new DatabaseConfig();
            conf.setTransactional(true);
            conf.setAllowCreate(true);

            mTxn = mEnv.beginTransaction(null, null);

            mDatabase = mEnv.openDatabase(mTxn, NAME, conf);

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
            return new BerkeleyReader(mEnv.beginTransaction(null, null), mDatabase, mPageBinding);
        } catch (final DatabaseException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IBackendWriter getWriter() throws TTIOException {
        return new BerkeleyWriter(mEnv.beginTransaction(null, null), mDatabase, mPageBinding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws TTIOException {
        try {
            mEnv.sync();
            mTxn.commit();
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
            try {
                DataInput in = new DataInputStream(mByteHandler.deserialize(arg0));
                return mFac.deserializePage(in);
            } catch (TTException exc) {
                throw new RuntimeException(exc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void objectToEntry(final IPage arg0, final TupleOutput arg1) {
            try {
                final DataOutput output = new DataOutputStream(mByteHandler.serialize(arg1));
                arg0.serialize(output);
            } catch (final TTException exc) {
                throw new RuntimeException(exc);
            }
        }
    }

}
