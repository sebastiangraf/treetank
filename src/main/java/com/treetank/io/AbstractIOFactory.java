package com.treetank.io;

import com.treetank.io.berkeley.BerkeleyFactory;
import com.treetank.io.file.FileFactory;
import com.treetank.session.SessionConfiguration;

/**
 * Abstract Factory to build up a concrete storage for the data. The Abstract
 * Instance must provide Reader and Writers as well as some additional methods.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbstractIOFactory {

	/** Type for different storages */
	public enum StorageType {
		File, Berkeley
	}

	/**
	 * Config for the session holding information about the location of the
	 * storage
	 */
	protected final SessionConfiguration config;

	/**
	 * Protected constructor, just setting the sessionconfiguration.
	 * 
	 * @param paramSession
	 *            to be set
	 */
	protected AbstractIOFactory(final SessionConfiguration paramSession) {
		config = paramSession;
	}

	/**
	 * Getting a writer.
	 * 
	 * @return an {@link IWriter} instance
	 * @throws TreetankIOException
	 *             if the initalisation fails
	 */
	public abstract IWriter getWriter() throws TreetankIOException;

	/**
	 * Getting a reader
	 * 
	 * @return an {@link IReader} instance
	 * @throws TreetankIOException
	 *             if the initalisation fails
	 */
	public abstract IReader getReader() throws TreetankIOException;

	/**
	 * Getting a Closing this storage. Is equivalent to Session.close
	 * 
	 * @throws TreetankIOException
	 *             exception to be throwns
	 */
	public abstract void closeStorage() throws TreetankIOException;

	/**
	 * Getting an AbstractIOFactory instance.
	 * 
	 * @param conf
	 *            with settings for the storage.
	 * @return an instance of this factory based on the kind in the conf
	 */
	public final static AbstractIOFactory getInstance(
			final SessionConfiguration conf) {
		// TODO fix that to use the conf file
		final StorageType type = StorageType.Berkeley;
		AbstractIOFactory fac = null;
		switch (type) {
		case File:
			fac = new FileFactory(conf);
			break;
		case Berkeley:
			fac = BerkeleyFactory.getInstanceForBerkeley(conf);
			break;
		default:
			throw new IllegalArgumentException(new StringBuilder("Type ")
					.append(type.toString()).append(" not valid!").toString());
		}
		return fac;

	}

	/**
	 * Check if storage exists
	 * 
	 * @return true if storage holds data, false otherwise
	 * @throws TreetankIOException
	 *             if storage is not accessable
	 */
	public abstract boolean exists() throws TreetankIOException;
}
