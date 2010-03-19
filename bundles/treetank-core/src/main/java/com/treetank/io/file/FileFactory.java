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

    /** private constant for fileName */
    private final static String FILENAME = "tt.tnk";

    /**
     * Constructor
     * 
     * @param paramSession
     *            the location of the storage
     */
    public FileFactory(final DatabaseConfiguration paramDatabase,
            final SessionConfiguration paramSession) {
        super(paramDatabase, paramSession);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReader getReader() throws TreetankIOException {
        return new FileReader(super.sessionConfig, getConcreteStorage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWriter getWriter() throws TreetankIOException {
        return new FileWriter(super.sessionConfig, getConcreteStorage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConcreteStorage() {
        // not used over here
    }

    protected final File getConcreteStorage() {
        return new File(super.databaseConfig.getFile(), new StringBuilder(
                EStoragePaths.TT.getFile().getName()).append(File.separator)
                .append(FILENAME).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists() throws TreetankIOException {
        final File file = getConcreteStorage();
        boolean returnVal = file.length() > 0;
        return returnVal;
    }
}
