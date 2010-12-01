package com.treetank.service.xml.xpath.xmark;

import org.perfidix.Benchmark;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class XMarkBenchTestMain {
	
	public static void main(String [] args){
		  final Benchmark bench = new Benchmark(new BenchConfigClass());
	        bench.add(XMarkBenchTestPerfidix.class);

	        final BenchmarkResult res = bench.run();
	        new TabularSummaryOutput().visitBenchmark(res);
	}

}
