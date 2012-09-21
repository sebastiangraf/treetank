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

package org.treetank.io.file;

import java.io.File;
import java.util.Properties;

import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.IConstants;
import org.treetank.io.IOUtils;
import org.treetank.io.IBackendReader;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendWriter;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.PageFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Factory to provide File access as a backend.
 * 
 * @author Sebastian Graf, University of Konstanz.
 * 
 */
public final class FileStorage implements IBackend {

    /** private constant for fileName. */
    private static final String FILENAME = "tt.tnk";

    /** private constant for fileName. */
    protected static final int BUFFERSIZE = 32767;

    /** Instance to storage. */
    private final File mFile;

    /** Factory for Pages. */
    private final PageFactory mFac;

    /** Handling the byte-representation before serialization. */
    private final IByteHandlerPipeline mByteHandler;

    /** Flag if storage is closed. */
    private boolean mClosed;

    /**
     * Constructor.
     * 
     * @param pProperties
     *            not only the location of the database
     * @param pNodeFac
     *            factory for the nodes
     * @param pByteHandler
     *            handling any bytes
     * 
     */
    @Inject
    public FileStorage(@Assisted Properties pProperties, INodeFactory pNodeFac,
        IByteHandlerPipeline pByteHandler) {

        mFile =
            new File(new File(new File(pProperties.getProperty(IConstants.DBFILE),
                DatabaseConfiguration.Paths.Data.getFile().getName()), pProperties
                .getProperty(IConstants.RESOURCE)), new StringBuilder(ResourceConfiguration.Paths.Data
                .getFile().getName()).append(File.separator).append(FILENAME).toString());
        mFac = new PageFactory(pNodeFac);
        mByteHandler = (ByteHandlerPipeline)pByteHandler;
        mClosed = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBackendReader getReader() throws TTException {
        return new FileReader(mFile, mFac, mByteHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBackendWriter getWriter() throws TTException {
        return new FileWriter(mFile, mFac, mByteHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        mClosed = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TTIOException {
        final boolean returnVal = mFile.length() > 0;
        return returnVal;
    }

    @Override
    public IByteHandlerPipeline getByteHandler() {
        return mByteHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FileStorage [mFile=");
        builder.append(mFile);
        builder.append(", mFac=");
        builder.append(mFac);
        builder.append(", mByteHandler=");
        builder.append(mByteHandler);
        builder.append("]");
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void truncate() throws TTException {
        if (!mClosed) {
            throw new TTUsageException("Storage opened, close first");
        }
        IOUtils.recursiveDelete(mFile);
    }

}
