package com.treetank;

import java.io.File;

import org.perfidix.Benchmark;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

import com.treetank.service.xml.XMLShredder;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;

public final class ShredBench {

	// public void remove10() {
	// Session.removeSession(new File("test10"));
	// }
	//
	// public void remove100() {
	// Session.removeSession(new File("test100"));
	// }
	//
	 public void remove1000() {
	 Session.removeSession(new File("wikipedia"));
	 }
	//
	// @Bench(runs = 10, beforeEachRun = "remove10")
	// public void shred10() {
	// XMLShredder.shred("test10.xml", new SessionConfiguration("test10"));
	// }
	//
	// @Bench(runs = 10, beforeEachRun = "remove100")
	// public void shred100() {
	// XMLShredder.shred("test100.xml", new SessionConfiguration("test100"));
	// }

	@Bench(runs = 1, beforeEachRun="remove1000")
	public void shred1000() {
		XMLShredder
				.shred("wikipedia.xml", new SessionConfiguration("wikipedia"));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final Benchmark bench = new Benchmark();
		bench.add(ShredBench.class);
		final BenchmarkResult res = bench.run();
		final TabularSummaryOutput output = new TabularSummaryOutput();
		output.visitBenchmark(res);

	}

}