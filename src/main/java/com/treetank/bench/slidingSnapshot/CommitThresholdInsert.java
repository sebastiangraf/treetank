package com.treetank.bench.slidingSnapshot;

import java.io.File;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.result.BenchmarkResult;

import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.constants.EStorage;
import com.treetank.exception.TreetankException;
import com.treetank.session.Session;

public class CommitThresholdInsert {

    private static int mProb = 1;

    private static int NODE_SET_SIZE = 3000;

    private IWriteTransaction wtx;
    private ISession session;

    // @BeforeBenchClass
    // public void setUp() {
    // 
    // }

    @AfterEachRun
    public void tearDown() {
        try {
            wtx.close();
            session.close();
            Session.removeSession(CommonStuff.PATH1);
        } catch (TreetankException exc) {

        }
    }

    @Bench
    public void benchSeq() {
        try {
            session = Session.beginSession(CommonStuff.PATH1);
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(getString(), "");
            for (int i = 0; i < NODE_SET_SIZE; i++) {
                if (CommonStuff.ran.nextBoolean()) {
                    wtx.insertElementAsFirstChild(getString(), "");

                } else {
                    wtx.insertElementAsRightSibling(getString(), "");
                }
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    @Bench
    public void benchRandom() {
        try {
            session = Session.beginSession(CommonStuff.PATH1);
            wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(getString(), "");
            for (int i = 0; i < NODE_SET_SIZE; i++) {
                if (CommonStuff.ran.nextBoolean()) {
                    wtx.insertElementAsFirstChild(getString(), "");
                } else {
                    wtx.insertElementAsRightSibling(getString(), "");
                }
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    wtx.commit();
                }
                long nextKey = 0;
                do {
                    nextKey = CommonStuff.ran.nextLong();
                    if (nextKey < 0) {
                        nextKey = nextKey * -1;
                    }
                    nextKey = nextKey % wtx.getNodeCount();
                } while (nextKey == 0);

                wtx.moveTo(nextKey);
            }
            wtx.commit();
        } catch (TreetankException exc) {
            exc.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        EStorage.recursiveDelete(CommonStuff.RESULTFOLDER);
        CommonStuff.RESULTFOLDER.mkdirs();
        for (int i = 1; i <= 100; i++) {
            mProb = i;
            final CommitThresholdInsert toBench = new CommitThresholdInsert();
            final Benchmark benchmark = new Benchmark(new BenchmarkConfig());
            benchmark.add(toBench);
            final BenchmarkResult res = benchmark.run();
            final StringBuilder pathBuilder = new StringBuilder(
                    CommonStuff.RESULTFOLDER.getAbsolutePath())
                    .append(File.separator);
            if (i < 100) {
                pathBuilder.append("0");
                if (i < 10) {
                    pathBuilder.append("0");
                }
            }
            pathBuilder.append(mProb);
            final File file = new File(pathBuilder.toString());
            file.mkdirs();
            final CSVOutput output = new CSVOutput(file);
            // final TabularSummaryOutput tab = new TabularSummaryOutput();
            output.visitBenchmark(res);
            // tab.visitBenchmark(res);
        }

    }

    private final static String chars = "abcdefghijklm";
    private final static int NUM_CHARS = 3;

    private static String getString() {
        char[] buf = new char[NUM_CHARS];

        for (int i = 0; i < buf.length; i++) {
            buf[i] = chars.charAt(CommonStuff.ran.nextInt(chars.length()));
        }

        return new String(buf);
    }

    private final static int RUNS = 5;
    private final static AbstractMeter[] METERS = {
            new TimeMeter(Time.MilliSeconds),
            new FileSizeMeter(CommonStuff.PATH1) };
    private final static AbstractOutput[] OUTPUT = {};
    private final static KindOfArrangement ARRAN = KindOfArrangement.NoArrangement;
    private final static double gcProb = 1.0d;

    static class BenchmarkConfig extends AbstractConfig {

        protected BenchmarkConfig() {
            super(RUNS, METERS, OUTPUT, ARRAN, gcProb);

        }
    }

}
