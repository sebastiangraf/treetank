package com.treetank.io.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.treetank.io.AbstractIOFactory;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.io.TreetankIOException;
import com.treetank.session.SessionConfiguration;

/**
 * Factory to provide File access as a backend.
 * 
 * @author Sebastian Graf, University of Konstanz.
 * 
 */
public final class FileFactory extends AbstractIOFactory {

	/**
	 * Constructor
	 * 
	 * @param paramSession
	 *            the location of the storage
	 */
	public FileFactory(final SessionConfiguration paramSession) {
		super(paramSession);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IReader getReader() throws TreetankIOException {
		return new FileReader(super.config);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWriter getWriter() throws TreetankIOException {
		return new FileWriter(super.config);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeStorage() {
		// not used over here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists() throws TreetankIOException {
		try {
			final File file = new File(super.config.getAbsolutePath()
					+ File.separatorChar + "tt.tnk");
			boolean returnVal = false;
			if (file.exists()) {
				returnVal = new RandomAccessFile(file, "r").length() > 0;
			}
			return returnVal;
		} catch (final IOException exc) {
			throw new TreetankIOException(exc);
		}

	}

}
