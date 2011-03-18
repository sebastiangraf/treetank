package org.treetank.service.xml.xpath.concurrent;


import org.perfidix.Benchmark;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;
import org.treetank.service.xml.xpath.xmark.XMarkBenchTestPerfidix;

/**
 * Main class for Perfidix test.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class ConcurrentBenchTestMain {

    public static void main(String[] args) {
        /*
         * Get config settings from config class and add a class to test.
         */
        final Benchmark bench = new Benchmark(new ConcurrentBenchConfig());
        bench.add(ConcurrentAxisTest.class);

        /*
         * Start test.
         */
        final BenchmarkResult res = bench.run();
        /*
         * Output test results.
         */
        new TabularSummaryOutput().visitBenchmark(res);
    }

}
