package com.treetank.io.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.treetank.io.AbstractIOFactory;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.session.SessionConfiguration;

/**
 * Factory to provide File access as a backend.
 * @author Sebastian Graf, University of Konstanz.
 *
 */
public final class FileFactory extends AbstractIOFactory {

	/**
	 * Constructor
	 * @param paramSession the location
	 */
	public FileFactory(final SessionConfiguration paramSession) {
		super(paramSession);
	}

	@Override
	public IReader getReader() {
		return new FileReader(super.config);
	}

	@Override
	public IWriter getWriter() {
		return new FileWriter(super.config);
	}

	@Override
	public void closeStorage() {
	}

	@Override
	public boolean exists() {
		try {
			final File file = new File(super.config.getAbsolutePath()
					+ File.separatorChar + "tt.tnk");
			boolean returnVal = false;
			if (file.exists()) {
				returnVal = new RandomAccessFile(file, "r").length() > 0;
			}
			return returnVal;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
