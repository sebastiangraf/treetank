package com.treetank;

import java.io.File;
import java.util.Random;

import org.perfidix.annotation.Bench;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.session.Session;

public class ExtremeInsertTest {

	private static final int NUM_CHARS = 10;
	private static final Random ran = new Random(0l);
	public static String chars = "abcdefghijklmonpqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	@Bench(runs = 10)
	public void bench() {
		try {
			final File file = new File("bla");
			Session.removeSession(file);
			long time = System.currentTimeMillis();
			final ISession session = Session.beginSession(file);
			IWriteTransaction wtx = session.beginWriteTransaction();

			wtx.insertElementAsFirstChild("bla", "");
			wtx.commit();
			wtx.close();
			wtx = null;
			wtx = session.beginWriteTransaction();
			wtx.moveToFirstChild();
			wtx.setName("bla2");
			wtx.commit();

			wtx.close();
			session.close();
			System.out.println(" done [" + (System.currentTimeMillis() - time)
					+ "ms].");
			super.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// final Benchmark bench = new Benchmark();
		// bench.add(ExtremeInsert.class);
		// final BenchmarkResult res = bench.run();
		// final TabularSummaryOutput output = new TabularSummaryOutput();
		// output.visitBenchmark(res);
		// System.out.println(output);

		// new ExtremeInsertTest().bench();

		try {

			final File repoFile = new File("tt");
			repoFile.mkdirs();

			/* Create a new, transactional database environment */
			final EnvironmentConfig config = new EnvironmentConfig();
			config.setAllowCreate(true);
			config.setLocking(false);
			Environment env = new Environment(repoFile, config);

			/* Make a database within that environment */
			final DatabaseConfig dbConfig = new DatabaseConfig();
			dbConfig.setAllowCreate(true);
			dbConfig.setExclusiveCreate(true);
			Database database = env.openDatabase(null, "tt", dbConfig);
		} catch (final Exception e) {
			throw new RuntimeException(e);

		}
	}

	public static String getString() {
		char[] buf = new char[NUM_CHARS];

		for (int i = 0; i < buf.length; i++) {
			buf[i] = chars.charAt(ran.nextInt(chars.length()));
		}

		return new String(buf);
	}
}
