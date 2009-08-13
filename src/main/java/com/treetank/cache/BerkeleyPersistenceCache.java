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
import com.treetank.cache.berkeleyBinding.Value;
import com.treetank.cache.berkeleyBinding.binding.ValueBinding;
import com.treetank.page.AbstractPage;
import com.treetank.session.SessionConfiguration;

/**
 * Berkeley implementation of a persistent cache. That means that all data is
 * stored in this cache and it is never removed. This is useful e.g. when it
 * comes to transaction logging.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BerkeleyPersistenceCache extends AbstractPersistenceCache {

	/**
	 * Berkeley database
	 */
	private final Database database;

	/**
	 * Berkeley Environment for the database
	 */
	private final Environment env;

	/**
	 * Name for the database.
	 */
	private final static String name = "berkeleyCache";

	/**
	 * Binding for the key, which is the nodepage.
	 */
	private final TupleBinding<Long> keyBinding;

	/**
	 * Binding for the value which is a page with related Nodes.
	 */
	private final ValueBinding valueBinding;

	/**
	 * Constructor. Building up the berkeley db and setting necessary settings.
	 * 
	 * @param sessionConfig
	 *            the place where the berkeley db is stored.
	 */
	public BerkeleyPersistenceCache(final SessionConfiguration sessionConfig) {
		super(sessionConfig);
		try {

			/* Create a new, transactional database environment */
			final EnvironmentConfig config = new EnvironmentConfig();
			config.setAllowCreate(true);
			config.setLocking(false);
			env = new Environment(persistentCachePlace, config);

			/* Make a database within that environment */
			final DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			dbConfig.setExclusiveCreate(true);
			database = env.openDatabase(null, name, dbConfig);

			keyBinding = TupleBinding.getPrimitiveBinding(Long.class);
			valueBinding = new ValueBinding();

		} catch (final Exception e) {
			throw new RuntimeException(e);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void put(final long key, final AbstractPage page) {
		final DatabaseEntry valueEntry = new DatabaseEntry();
		final DatabaseEntry keyEntry = new DatabaseEntry();

		keyBinding.objectToEntry(key, keyEntry);
		valueBinding.objectToEntry(new Value(page), valueEntry);
		try {
			database.put(null, keyEntry, valueEntry);
		} catch (DatabaseException e) {
			new RuntimeException(e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void clear() {
		try {
			database.close();
			env.removeDatabase(null, name);
			env.close();
		} catch (final DatabaseException e) {
			new RuntimeException(e);
		}
		super.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final AbstractPage get(final long key) {
		final DatabaseEntry valueEntry = new DatabaseEntry();
		final DatabaseEntry keyEntry = new DatabaseEntry();
		keyBinding.objectToEntry(key, keyEntry);
		try {
			final OperationStatus status = database.get(null, keyEntry,
					valueEntry, LockMode.DEFAULT);
			if (status == OperationStatus.SUCCESS) {
				final Value val = valueBinding.entryToObject(valueEntry);
				final AbstractPage page = val.getPage();
				return page;
			} else {
				return null;
			}
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

}
