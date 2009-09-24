package com.treetank.io;

import com.treetank.io.berkeley.BerkeleyFactory;
import com.treetank.session.SessionConfiguration;

/**
 * Abstract Factory to build up a concrete storage for the data. The Abstract
 * Instance must provide Reader and Writers as well as some additional methods.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbstractIOFactory {

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

	/** Getting a Writer */
	public abstract IWriter getWriter();

	/** Getting a Reader */
	public abstract IReader getReader();
	/** Getting a Closing this storage. Is equivalent to Session.close */
	public abstract void closeStorage();

	/**
	 * Getting an AbstractIOFactory instance.
	 * 
	 * @param conf
	 * @return
	 */
	public final static AbstractIOFactory getInstance(
			final SessionConfiguration conf) {


		 return BerkeleyFactory.getInstanceForBerkeley(conf);
		//return new FileFactory(conf);


	}

	/**
	 * Check if storage exists
	 * 
	 * @return true if storage holds data, false otherwise
	 */
	public abstract boolean exists();
}
