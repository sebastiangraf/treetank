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

package com.treetank.io.file;

import java.io.File;

import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.AbstractIOFactory;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.settings.EStoragePaths;

/**
 * Factory to provide File access as a backend.
 * 
 * @author Sebastian Graf, University of Konstanz.
 * 
 */
public final class FileFactory extends AbstractIOFactory {

    /** private constant for fileName. */
    private final static String FILENAME = "tt.tnk";

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
    public IReader getReader() throws TreetankIOException {
        return new FileReader(super.mSessionConfig, getConcreteStorage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWriter getWriter() throws TreetankIOException {
        return new FileWriter(super.mSessionConfig, getConcreteStorage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConcreteStorage() {
        // not used over here
    }

    protected File getConcreteStorage() {
        return new File(super.mDatabaseConfig.getFile(), new StringBuilder(EStoragePaths.TT.getFile()
            .getName()).append(File.separator).append(FILENAME).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TreetankIOException {
        final File file = getConcreteStorage();
        final boolean returnVal = file.length() > 0;
        return returnVal;
    }
}
