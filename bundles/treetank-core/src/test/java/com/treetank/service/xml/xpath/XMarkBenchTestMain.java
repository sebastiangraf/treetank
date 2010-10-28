package com.treetank.service.xml.xpath;

import org.perfidix.Benchmark;
import org.perfidix.example.Config;
import org.perfidix.example.StackBenchmark;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class XMarkBenchTestMain {
	
	public static void main(String [] args){
		  final Benchmark bench = new Benchmark(new Config());
	        bench.add(XMarkBenchTestPerfidix.class);

	        final BenchmarkResult res = bench.run();
	        new TabularSummaryOutput().visitBenchmark(res);
	}

}
