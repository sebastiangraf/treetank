package com.treetank.io.berkeley;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.treetank.io.AbstractIOFactory;
import com.treetank.io.AbstractKey;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.io.StorageProperties;
import com.treetank.io.berkeley.binding.AbstractPageBinding;
import com.treetank.io.berkeley.binding.KeyBinding;
import com.treetank.io.berkeley.binding.PageReferenceBinding;
import com.treetank.io.berkeley.binding.StoragePropTupleBinding;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;
import com.treetank.session.SessionConfiguration;

public final class BerkeleyFactory extends AbstractIOFactory {

	static final TupleBinding<AbstractKey> KEY = new KeyBinding();

	static final TupleBinding<StorageProperties> PROPS_VAL_B = new StoragePropTupleBinding();
	static final TupleBinding<AbstractPage> PAGE_VAL_B = new AbstractPageBinding();
	static final TupleBinding<PageReference<?>> FIRST_REV_VAL_B = new PageReferenceBinding();
	static final TupleBinding<Long> DATAINFO_VAL_B = TupleBinding
			.getPrimitiveBinding(Long.class);

	/**
	 * Berkeley Environment for the database
	 */
	private final Environment env;

	/**
	 * Database instance per session
	 */
	private final Database mDatabase;

	/**
	 * Name for the database.
	 */
	protected final static String NAME = "berkeleyDatabase";

	/**
	 * Concurrent storage for all avaliable databases in runtime
	 */
	private static Map<SessionConfiguration, BerkeleyFactory> fac = new ConcurrentHashMap<SessionConfiguration, BerkeleyFactory>();

	private BerkeleyFactory(final SessionConfiguration paramSession) {
		super(paramSession);
		try {

			final DatabaseConfig conf = new DatabaseConfig();
			conf.setTransactional(true);

			final EnvironmentConfig config = new EnvironmentConfig();
			config.setTransactional(true);

			final File repoFile = new File(paramSession + File.separator + "tt");
			if (!repoFile.exists()) {
				repoFile.mkdirs();
				conf.setAllowCreate(true);
				config.setAllowCreate(true);
			}

			env = new Environment(repoFile, config);

			mDatabase = env.openDatabase(null, NAME, conf);

		} catch (final Exception e) {
			throw new RuntimeException(e);

		}
	}

	public static BerkeleyFactory getInstanceForBerkeley(
			final SessionConfiguration conf) {
		BerkeleyFactory fact = fac.get(conf);
		if (fact == null) {
			fact = new BerkeleyFactory(conf);
			fac.put(conf, fact);
		}
		return fact;
	}

	public IReader getReader() {
		try {
			return new BerkeleyReader(env, mDatabase);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IWriter getWriter() {
		return new BerkeleyWriter(env, mDatabase);
	}

	@Override
	public void closeStorage() {

		try {
			mDatabase.close();
			// env.removeDatabase(null, NAME);
			env.close();
			fac.remove(this.config);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean exists() {
		try {
			final boolean returnVal = mDatabase.count() > 0;
			return returnVal;
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

}
