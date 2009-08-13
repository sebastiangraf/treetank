package com.treetank;

import java.io.File;
import java.util.Random;

import org.perfidix.annotation.Bench;

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
			final File file = new File("/Volumes/Felice/bla");
			Session.removeSession(file);
			long time = System.currentTimeMillis();
			final ISession session = Session.beginSession(file);
			final IWriteTransaction wtx = session.beginWriteTransaction();
			int lastKey = 1;
			long border = 100000000;
			wtx.insertElementAsFirstChild("bly", "");
			for (long i = 0; i < border; i++) {
				//if (ran.nextBoolean()) {
					wtx.insertElementAsFirstChild("bly", "");
				//} else {
					wtx.insertElementAsRightSibling("bly", "");
				//}
				lastKey++;
//				wtx.moveTo(ran.nextInt(lastKey - 1) + 1);
				if(i % 10000==0) {
					System.out.println("Inserted nodes " + i);
				}
				
			}

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
		new ExtremeInsertTest().bench();
	}

	public static String getString() {
		char[] buf = new char[NUM_CHARS];

		for (int i = 0; i < buf.length; i++) {
			buf[i] = chars.charAt(ran.nextInt(chars.length()));
		}

		return new String(buf);
	}
}
