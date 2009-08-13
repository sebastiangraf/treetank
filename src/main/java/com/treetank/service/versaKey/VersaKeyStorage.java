package com.treetank.service.versaKey;

import java.io.File;
import java.io.IOException;

import com.treetank.api.ISession;
import com.treetank.session.Session;

/**
 * Implementing the VersayKey Storage
 *  
 * @author Sebastian Graf, University of Konstanz
 * 
 */
class VersaKeyStorage implements IKeyStorage {

	private final ISession session;

	public VersaKeyStorage(final File paramStorage) {
		try {
			session = Session.beginSession(paramStorage);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	
}
