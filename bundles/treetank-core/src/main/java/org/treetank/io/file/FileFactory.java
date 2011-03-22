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
 *     * Neither the name of the <organization> nor the
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

package org.treetank.io.file;

import java.io.File;

import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.SessionConfiguration;
import org.treetank.exception.TTIOException;
import org.treetank.io.AbsIOFactory;
import org.treetank.io.IReader;
import org.treetank.io.IWriter;
import org.treetank.settings.EStoragePaths;

/**
 * Factory to provide File access as a backend.
 * 
 * @author Sebastian Graf, University of Konstanz.
 * 
 */
public final class FileFactory extends AbsIOFactory {

    /** private constant for fileName. */
    private static final String FILENAME = "tt.tnk";

    /**
     * Constructor.
     * 
     * @param mParamDatabase
     *            the location of the database
     * @param mParamSession
     *            the location of the storage
     */
    public FileFactory(final DatabaseConfiguration mParamDatabase, final SessionConfiguration mParamSession) {
        super(mParamDatabase, mParamSession);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReader getReader() throws TTIOException {
        return new FileReader(super.mSessionConfig, getConcreteStorage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWriter getWriter() throws TTIOException {
        return new FileWriter(super.mSessionConfig, getConcreteStorage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConcreteStorage() {
        // not used over here
    }

    /**
     * Getting concrete storage for this file.
     * 
     * @return the concrete storage for this database
     */
    protected File getConcreteStorage() {
        return new File(super.mDatabaseConfig.getFile(), new StringBuilder(EStoragePaths.TT.getFile()
            .getName()).append(File.separator).append(FILENAME).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TTIOException {
        final File file = getConcreteStorage();
        final boolean returnVal = file.length() > 0;
        return returnVal;
    }
    
}
