/*
 * Copyright (c) 2009, Sebastian Graf (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.cache;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.exception.TreetankIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Berkeley implementation of a persistent cache. That means that all data is
 * stored in this cache and it is never removed. This is useful e.g. when it
 * comes to transaction logging.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class BerkeleyPersistenceCache extends AbstractPersistenceCache {

	/** Logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BerkeleyPersistenceCache.class);

	/**
	 * Berkeley database
	 */
	private transient final Database database;

	/**
	 * Berkeley Environment for the database
	 */
	private transient final Environment env;

	/**
	 * Name for the database.
	 */
	private transient final static String NAME = "berkeleyCache";

	/**
	 * Binding for the key, which is the nodepage.
	 */
	private transient final TupleBinding<Long> keyBinding;

	/**
	 * Binding for the value which is a page with related Nodes.
	 */
	private transient final NodePageContainerBinding valueBinding;

	/**
	 * Constructor. Building up the berkeley db and setting necessary settings.
	 * 
	 * @param sessionConfig
	 *            the place where the berkeley db is stored.
	 * @param revision
	 *            revision number, needed to reconstruct the sliding window in
	 *            the correct way
	 */
	public BerkeleyPersistenceCache(final DatabaseConfiguration sessionConfig,
			final long revision) throws TreetankIOException {
		super(sessionConfig);
		try {

			/* Create a new, transactional database environment */
			final EnvironmentConfig config = new EnvironmentConfig();
			config.setAllowCreate(true);
			config.setLocking(false);
			config.setCacheSize(1024 * 1024);
			env = new Environment(place, config);

			/* Make a database within that environment */
			final DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			dbConfig.setExclusiveCreate(true);
			database = env.openDatabase(null, NAME, dbConfig);

			keyBinding = TupleBinding.getPrimitiveBinding(Long.class);
			valueBinding = new NodePageContainerBinding();
			
			//debug
			LOGGER.debug(new StringBuilder(
					"Creating new BerkeleyPersistenceCache with revision ")
					.append(revision).append(" and DatabaseConfiguration ")
					.append(sessionConfig.toString()).toString());
			

		} catch (final DatabaseException exc) {
			LOGGER.error(exc.getMessage(), exc);
			throw new TreetankIOException(exc);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putPersistent(final long key, final NodePageContainer page)
			throws TreetankIOException {
		final DatabaseEntry valueEntry = new DatabaseEntry();
		final DatabaseEntry keyEntry = new DatabaseEntry();

		keyBinding.objectToEntry(key, keyEntry);
		valueBinding.objectToEntry(page, valueEntry);
		try {
			database.put(null, keyEntry, valueEntry);
			
			//debug
			LOGGER.debug(new StringBuilder(
			"Put new BerkeleyPersistenceCache with key ")
			.append(key).append(" and Node Page Continer ")
			.append(page.toString()).toString());
			
		} catch (final DatabaseException exc) {
			LOGGER.error(exc.getMessage(), exc);
			throw new TreetankIOException(exc);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearPersistent() throws TreetankIOException {
		try {
			database.close();
			env.removeDatabase(null, NAME);
			env.close();
			
			//debug
			LOGGER.debug(new StringBuilder(
			"Clear BerkeleyPersistenceCache").toString());
			
		} catch (final DatabaseException exc) {
			LOGGER.error(exc.getMessage(), exc);
			throw new TreetankIOException(exc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodePageContainer getPersistent(final long key)
			throws TreetankIOException {
		final DatabaseEntry valueEntry = new DatabaseEntry();
		final DatabaseEntry keyEntry = new DatabaseEntry();
		keyBinding.objectToEntry(key, keyEntry);
		try {
			final OperationStatus status = database.get(null, keyEntry,
					valueEntry, LockMode.DEFAULT);
			NodePageContainer val = null;
			if (status == OperationStatus.SUCCESS) {
				val = valueBinding.entryToObject(valueEntry);
			}
			
			//debug
			LOGGER.debug(new StringBuilder(
			"Get BerkeleyPersistenceCache with key ")
			.append(key).toString());
			
			return val;
		} catch (final DatabaseException exc) {
			LOGGER.error(exc.getMessage(), exc);
			throw new TreetankIOException(exc);
		}
	}

}
